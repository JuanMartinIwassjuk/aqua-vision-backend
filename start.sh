#!/bin/bash

# Cargar las variables de entorno
if [ -f .env.local ]; then
    source .env.local
else
    echo "❌ Archivo .env.local no encontrado. Abortando."
    exit 1
fi

BACKEND_DIR="./aqua-vision-backend"
FRONTEND_DIR="./aqua-vision-frontend"

echo "🔧 Checking if database '$DB_NAME' exists..."
"$MYSQL_CMD" -u"$DB_USER" -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\`;"




if [ $? -eq 0 ]; then
    echo "Database '$DB_NAME' verified/created successfully."
else
    echo "Error creating/verifying the database. Check your credentials."
    exit 1
fi


export DB_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
export DB_USERNAME="$DB_USER"
export DB_PASSWORD="$DB_PASS"


echo "🚀 Starting backend..."
cd "$BACKEND_DIR"
./mvnw spring-boot:run &
BACK_PID=$!


echo "🌐 Starting frontend..."
cd "../$FRONTEND_DIR"
npm install
ng serve &


wait $BACK_PID
