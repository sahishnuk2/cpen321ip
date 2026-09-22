# syntax=docker/dockerfile:1

FROM node:22-alpine
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# sets process.env.PORT
ENV PORT=3000 
ENV NODE_ENV=development

# The API uses 3000 and the frontend WebSocket relay uses 3001.
EXPOSE 3000 3001

CMD ["npm", "run", "dev"]
