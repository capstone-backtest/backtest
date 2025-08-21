#!/bin/bash
echo "Starting up all services using Docker Compose..."
docker-compose up --build -d

echo ""
echo "Services are running in the background."
echo "You can access the application at http://localhost:8080"
echo ""
