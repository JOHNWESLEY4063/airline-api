# Use lightweight Node.js LTS image
FROM node:16-alpine

# Set working directory inside container
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy all source code
COPY . .

# Expose the port your API uses
EXPOSE 3001

# Start the server
CMD ["node", "index.js"]