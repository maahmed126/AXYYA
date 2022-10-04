def call(Map config = [:]) {
    pipeline {
        agent any

        tools {
            nodejs "NodeJs"
        }

        environment {
             registry = "211223789150.dkr.ecr.us-east-2.amazonaws.com/hello-world"
        }

        stages {
            stage('Start') {
                steps {

                    script {
                        GIT_COMMIT_MESSAGE = sh (
                            script: 'git log --format="medium" -1 ${GIT_COMMIT}',
                            returnStdout: true
                        )
                    } 
                   
                }
            }

            stage('Build') {
                steps {
                    sh 'npm ci'
                }
            }

            stage('Test') {
                steps {
                    sh 'npm run test'
                }
            }

            stage('Code Quality') {

                environment {
                    scannerHome = tool 'SonarQube'
                }

                steps {

                    script {
                        STAGE_NAME = "Code Quality Check"
                        PROJECT_NAME = sh (
                            script: 'jq -r .name package.json',
                            returnStdout: true
                        ).trim()
                    }

                    writeFile file: 'sonar-project.properties', text: "sonar.projectKey=${PROJECT_NAME}\nsonar.projectName=${PROJECT_NAME}\nsonar.projectVersion=${GIT_COMMIT}\nsonar.sources=./\nsonar.language=js\nsonar.sourceEncoding=UTF-8"

                    sh 'cat sonar-project.properties'

                    withSonarQubeEnv('SonarQube') {
                        sh "${scannerHome}/bin/sonar-scanner" 
                    }

                    timeout(time: 10, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }

                }

            }

            stage ("docker push") {
                 steps {
                   script {
                       sh "docker login -u AWS https://${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com -p $(aws ecr get-login-password --region ${REGION})"
                       sh "docker build --no-cache --build-arg BUILD_ID=${BUILD_NUMBER}-${BUILD_TIMESTAMP}-${GIT_COMMIT} -t ${ECR_APP_NAME}:v_${BUILD_TAG} ."
                       sh "docker tag ${ECR_APP_NAME}:v_${BUILD_TAG} ${ECR_REPO}:v_${BUILD_TAG}"
                       sh "docker push ${ECR_REPO}:v_${BUILD_TAG}"
                 
             }
            }   
            stage ("Kube Deploy") {
              steps {
                withKubeConfig(caCertificate: '', clusterName: '', contextName: '', credentialsId: 'K8S', namespace: '', serverUrl: '') {
                 sh "kubectl apply -f eks-deploy-from-ecr.yaml"
                }
               }
            }
            }
           

        post {

            always {

                sh 'npm run generate-report'

                publishHTML target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: 'tests-report',
                    reportFiles: 'index.html',
                    reportName: 'unit-test-report',
                    includes: 'index.html'
                ]

                sh "npm run sonar -- --allbugs true --noSecurityHotspot true --sonarurl ${SONARQUBE_URL} --sonarusername ${SONAR_USERNAME} --sonarpassword ${SONAR_PASSWORD} --sonarcomponent ${PROJECT_NAME} --project ${PROJECT_NAME} --application ${PROJECT_NAME} --release ${GIT_COMMIT}"
                
                publishHTML target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: './',
                    reportFiles: 'code_quality_report.html',
                    reportName: 'code-quality-report',
                    includes: 'code_quality_report.html'
                ]

            }


         }
        }
    }
}