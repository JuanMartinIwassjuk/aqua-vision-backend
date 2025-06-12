DB_NAME="notesdb"
DB_USER="root"
DB_PASS="123456"
DB_HOST="localhost"
DB_PORT="3306"


BACKEND_DIR="./backend"
FRONTEND_DIR="./frontend"

MYSQL_CMD="/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"

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
