# Multi-stage Dockerfile for Java Log Analyzer & Monitoring System

# Stage 1: Build React Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build & Run Java Backend
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy Java backend dependencies & source
COPY backend/lib ./backend/lib
COPY backend/src ./backend/src
COPY .env ./

# Copy compiled frontend dist bundle into web root
COPY --from=frontend-builder /app/frontend/dist ./frontend/dist

# Compile Java classes
RUN mkdir -p out && javac -d out -cp "backend/lib/*" backend/src/*.java

# Expose HTTP port
EXPOSE 8080

# Start Java Web Server
CMD ["java", "-cp", "out:backend/lib/*", "Main"]
