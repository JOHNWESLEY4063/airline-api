# Stage 1: Define the base image (Node 16 LTS)
FROM node:16-alpine

# Set the working directory inside the container
WORKDIR /usr/src/app

# Copy package.json and install dependencies (for caching efficiency)
COPY package*.json ./
RUN npm install

# Copy the rest of the application code
COPY .. .

# Expose the port used by the Node.js API (3001, as per your Postman environment)
EXPOSE 3001

# Command to run the application when the container starts
CMD [ "node", "index.js" ]