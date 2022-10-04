FROM node:14 

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
COPY package*.json app.js ./

RUN npm install

# Bundle app source
EXPOSE 3000

CMD [ "node", "app.js" ]
