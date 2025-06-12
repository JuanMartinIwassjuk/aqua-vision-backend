## 🛠️ How to Run the Application

To run the entire application (backend, frontend, and database setup) with a single command, use the provided script:

```bash
./start.sh

Before running the script, make sure to configure the environment variables at the top of the run.sh (or start.sh) file:

# Database configuration
DB_NAME="notesdb"
DB_USER="root"
DB_PASS="123456"
DB_HOST="localhost"
DB_PORT="3306"

BACKEND_DIR="./backend"
FRONTEND_DIR="./frontend"

# Path to MySQL executable (adjust this to your OS and MySQL installation)
MYSQL_CMD="/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"

Make sure the MYSQL_CMD path matches your system's MySQL installation.
On macOS/Linux it might look like /usr/bin/mysql.
The script will:

    Verify or create the database

    Export environment variables used by the backend

    Start the Spring Boot backend

    Install frontend dependencies and serve the Angular app

Default Login Credentials
    If you want to test the login functionality, use the following default user:
        Username: testuser
        Password: test123


