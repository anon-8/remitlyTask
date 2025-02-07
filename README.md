# Remitly Exercise

This project is a REST API application designed to parse, store, and expose SWIFT (BIC) code data. SWIFT codes uniquely identify bank headquarters and branches for international wire transfers. The application reads SWIFT code data (originally maintained in a spreadsheet), processes it according to specific business rules, stores the data in a PostgreSQL database, and provides a set of RESTful endpoints to access and manage the data.

## Table of Contents

- [Technologies Used](#technologies-used)
- [Setup & Installation](#setup--installation)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)

## Features

- **Data Parsing:**

  - Determines if a SWIFT code represents a headquarters (codes ending with `XXX`) or a branch.
  - Associates branch codes with their corresponding headquarters based on the first 8 characters.
  - Formats country codes and names as uppercase strings.
  - Ignores redundant columns from the input data file.

- **Data Storage:**

  - Uses PostgreSQL for fast, low-latency querying.
  - Supports efficient retrieval by individual SWIFT code and by country (using ISO-2 codes).

- **REST API Endpoints:**

  - Retrieve details for a single SWIFT code (headquarters include branch details).
  - Retrieve all SWIFT codes for a specific country.
  - Add new SWIFT code entries.
  - Delete SWIFT code entries by code.

- **Containerization:**

  - The application and PostgreSQL database are containerized using Docker and Docker Compose.
  - Endpoints are accessible at [http://localhost:8080].

- **Testing:**
  - Comprehensive unit and integration tests ensure the solution’s correctness and reliability.

## Technologies Used

- **Backend:** Java with Spring Boot
- **Database:** PostgreSQL
- **Containerization:** Docker & Docker Compose
- **Testing:** Junit, Mockito, TestContainers

# How to Run the Services

Below is a `docker-compose.yml` snippet that defines two services:

1. **Postgres** – A PostgreSQL 17 database.
2. **app** – The application container, which depends on the PostgreSQL service.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) installed.
- [Docker Compose](https://docs.docker.com/compose/install/) installed.  
  (If you're using Docker Desktop on Windows or macOS, Docker Compose comes bundled.)

## Steps to Run

1. **Clone/Download the Repository**  
   Make sure you have the project folder containing the `docker-compose.yml` file.

2. **Build the Application (If Not Already Built)**

   - If you have a Dockerfile for the application, ensure that it’s referenced properly in your `docker-compose.yml` under the `app` service (`build: exercise`).

3. **Start the Containers**  
   From the project root folder (where your `docker-compose.yml` lives), run:
   ```bash
   docker-compose up -d
   ```

## REST Endpoints

From the instructions, you have these required endpoints:

- **GET** `/v1/swift-codes/{swift-code}`
- **GET** `/v1/swift-codes/country/{countryISO2code}`
- **POST** `/v1/swift-codes`
- **DELETE** `/v1/swift-codes/{swift-code}`

---

### Currently Covered

#### Endpoint 1: `GET /v1/swift-codes/{swift-code}`

- **`shouldReturnNotFoundWhenSwiftCodeDoesNotExist()`**  
  Tests the `404 Not Found` scenario.
- **`shouldCreateAndRetrieveSwiftCode()`**  
  Tests a valid retrieval scenario for a newly created code.
- **`shouldRetrieveHeadquarterWithBranches()`**  
  Tests retrieval of a headquarter with its branches.

#### Endpoint 3: `POST /v1/swift-codes`

- **`shouldCreateAndRetrieveSwiftCode()`**  
  Verifies successful creation and retrieval.
- **`shouldThrowExceptionWhenCreatingDuplicateSwiftCode()`**  
  Verifies duplicate SWIFT code scenario.
- **`shouldReturnBadRequestWhenCreatingSwiftCodeWithEmptyBody()`**  
  Covers empty JSON requests.
- **`shouldReturnBadRequestWhenCreatingSwiftCodeWithInvalidData()`**  
  Partially covers validation failures.

#### Endpoint 4: `DELETE /v1/swift-codes/{swift-code}`

- **`shouldDeleteSwiftCode()`**  
  Tests successful deletion.
- **`shouldReturnNotFoundWhenDeletingNonExistingSwiftCode()`**  
  Tests `404 Not Found` scenario.

## How to Run Tests

This guide explains how to run both **unit** and **integration** tests in the project.

---

### Prerequisites

1. **Docker Installed**: Testcontainers requires Docker to be running on your machine.
2. **Build Tool**: Depending on the project setup, ensure you have the appropriate build tool installed (e.g., Maven or Gradle).

---

### Steps to Run Tests

1. **Clone/Download the Project**

   - Make sure you have the complete project source code on your local machine.

2. **Navigate to the Project Directory**

   - In your terminal or command prompt, go to the project's root folder (where the main build file—`pom.xml` or `build.gradle`—is located).

3. **Run Tests**

   #### Maven

   If the project uses Maven:

   ```bash
   mvn clean test
   ```
