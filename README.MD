We need add plugins and update the configurations for Global Pipeline Libraries under System Configuration

We have deployed a docker container, that listens on port 80 and responds on the route / with a simple json. We created the image of it and pushed it to DockerHub.In the pods we are pulling the image from DockerHub.

We create pods in Kubernetes, you assign selector labels to them. The Service acts as a layer of abstraction that hides the ephemeral nature of the pods, which can be scaled up and down or replaced at any time. The following Service definition collects all pods that have a selector label and routes traffic evenly among them.

Ingress resources have consolidated how services inside a Kubernetes cluster can be accessed by external clients, by allowing an API Gateway style of traffic routing. Proxied services are relayed through a common entrypoint and you control when and how to publish a service by using an intent-driven, YAML declaration.

How to access the generically application running inside the minikube cluster? We want to reach an application through domain-name, not through IP addresses, as reaching applications by memorizing IP addresses in the real world is impractical. So we need a mechanism such that the domain name we enter should somehow invoke the application

Ingress is a solution to the problem. As Ingress exposes HTTP and HTTPS routes from outside the cluster to services within the cluster. Basically, Ingress takes care of routing the traffic from outside the cluster to inside the cluster.
