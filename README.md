SWIFT Code REST API
This project is a REST API application designed to parse, store, and expose SWIFT (BIC) code data. SWIFT codes uniquely identify bank headquarters and branches for international wire transfers. The application reads SWIFT code data (originally maintained in a spreadsheet), processes it according to specific business rules, stores the data in a PostgreSQL database, and provides a set of RESTful endpoints to access and manage the data.

Table of Contents
Features
Technologies Used
Project Structure
Setup & Installation
API Endpoints
Testing
Troubleshooting
Contributing
License
Contact
Features
Data Parsing:

Determines if a SWIFT code represents a headquarters (codes ending with XXX) or a branch.
Associates branch codes with their corresponding headquarters based on the first 8 characters.
Formats country codes and names as uppercase strings.
Ignores redundant columns from the input data file.
Data Storage:

Uses PostgreSQL for fast, low-latency querying.
Supports efficient retrieval by individual SWIFT code and by country (using ISO-2 codes).
REST API:

Retrieve details for a single SWIFT code (headquarters include branch details).
Retrieve all SWIFT codes for a specific country.
Add new SWIFT code entries.
Delete SWIFT code entries by code.
Containerization:

The application and PostgreSQL database are containerized using Docker and Docker Compose.
Endpoints are accessible at http://localhost:8080.
Testing:

Comprehensive unit and integration tests ensure the solution’s correctness and reliability.
Technologies Used
Backend: Java with Spring Boot (or your chosen framework)
Database: PostgreSQL
Containerization: Docker & Docker Compose
Testing: JUnit (or your preferred testing framework)
Project Structure
plaintext
Kopiuj
├── src/
│ ├── main/
│ │ ├── java/ # Application source code
│ │ └── resources/ # Application configuration files
│ └── test/ # Unit and integration tests
├── Dockerfile # Containerizes the application
├── docker-compose.yml # Defines application and database services
├── README.md # Project documentation (this file)
└── ... # Other configuration files (e.g., pom.xml or build.gradle)
Setup & Installation
Prerequisites
Docker
Docker Compose
Clone the Repository
bash
Kopiuj
git clone <YOUR_REPOSITORY_URL>
cd <YOUR_REPOSITORY_DIRECTORY>
Docker Compose Configuration
The provided docker-compose.yml file defines two services:

postgres:
Runs PostgreSQL version 17 with:

Database: exercisedb
User: postgres
Password: postgres
Port mapping: 5432:5432
app:
Builds the application image from the Dockerfile in the exercise folder and sets the following environment variables:

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/exercisedb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
Port mapping for the application is 8080:8080.

Running the Application
Build and Start Containers

In the project’s root directory, run:

bash
Kopiuj
docker-compose up --build
Access the API

Once the containers are running, the REST API will be accessible at http://localhost:8080.

API Endpoints

1. Retrieve a Single SWIFT Code
   Endpoint:
   GET /v1/swift-codes/{swift-code}

For Headquarters:
If the SWIFT code represents a headquarters, the response includes branch details.

Example Response:

json
Kopiuj
{
"address": "123 Main St.",
"bankName": "BANK OF EXAMPLE",
"countryISO2": "US",
"countryName": "UNITED STATES",
"isHeadquarter": true,
"swiftCode": "EXAMPLXX",
"branches": [
{
"address": "456 Branch Ave.",
"bankName": "BANK OF EXAMPLE",
"countryISO2": "US",
"isHeadquarter": false,
"swiftCode": "EXAMPL12"
}
// ... additional branches
]
}
For Branches:
If the SWIFT code represents a branch, the response does not include branch details.

Example Response:

json
Kopiuj
{
"address": "456 Branch Ave.",
"bankName": "BANK OF EXAMPLE",
"countryISO2": "US",
"countryName": "UNITED STATES",
"isHeadquarter": false,
"swiftCode": "EXAMPL12"
} 2. Retrieve SWIFT Codes for a Specific Country
Endpoint:
GET /v1/swift-codes/country/{countryISO2code}

Example Response:

json
Kopiuj
{
"countryISO2": "US",
"countryName": "UNITED STATES",
"swiftCodes": [
{
"address": "123 Main St.",
"bankName": "BANK OF EXAMPLE",
"countryISO2": "US",
"isHeadquarter": true,
"swiftCode": "EXAMPLXX"
},
{
"address": "456 Branch Ave.",
"bankName": "BANK OF EXAMPLE",
"countryISO2": "US",
"isHeadquarter": false,
"swiftCode": "EXAMPL12"
}
// ... additional entries
]
} 3. Add a New SWIFT Code Entry
Endpoint:
POST /v1/swift-codes

Request Body:

json
Kopiuj
{
"address": "789 New Rd.",
"bankName": "NEW BANK",
"countryISO2": "GB",
"countryName": "UNITED KINGDOM",
"isHeadquarter": true,
"swiftCode": "NEWBKXXX"
}
Example Response:

json
Kopiuj
{
"message": "SWIFT code added successfully."
} 4. Delete a SWIFT Code Entry
Endpoint:
DELETE /v1/swift-codes/{swift-code}

Example Response:

json
Kopiuj
{
"message": "SWIFT code deleted successfully."
}
Testing
Running Unit & Integration Tests
Assuming you are using Maven, run:

bash
Kopiuj
./mvnw test
For Gradle:

bash
Kopiuj
./gradlew test
These tests ensure that all endpoints function correctly and edge cases are handled with clear error messages.
