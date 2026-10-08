# 1. Use the official lightweight Node.js image as the starting base
FROM node:20-alpine

# 2. Set the working directory inside the container
WORKDIR /usr/src/app

# 3. Copy the package.json file first to install dependencies
COPY package*.json ./

# 4. Install production dependencies (wont install test tools)
RUN npm install --only=production

# 5. Copy the rest of your application code (like node.js) into the container
COPY . .

# 6. Expose the port your application listens on (e.g., 3000)
EXPOSE 3000

# 7. Define the command to start your application
CMD [ "node", "node.js" ]
