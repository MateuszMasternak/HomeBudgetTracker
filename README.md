# HomeBudgetTracker

## Table of Contents
1.  [Overview](#overview)
2.  [Features](#features)
3.  [Technologies Used](#technologies-used)
4.  [Prerequisites](#prerequisites)
5.  [Getting Started](#getting-started)
    * [Installation](#installation)
    * [`.env` File Configuration](#env-file-configuration)
    * [Docker Setup](#docker-setup)
6.  [API Endpoints](#api-endpoints)

## Overview
Home Budget Tracker is a Java-based API designed to help users manage their personal finances. It allows users to track their income and expenses, categorize transactions, and generate reports in CSV format.

## Features
* Authentication using JWT tokens provided by Cognito.
* CRUD operations for accounts, categories, and transactions.
* Export transactions to a CSV file.
* Calculate the sum of positive and/or negative transactions.
* Filter transactions by date and/or category within a specific account.
* Automatically retrieve exchange rates via an external API when creating transactions in a different currency. Alternatively, a custom exchange rate can be provided.
* Exception handling.
* Logging.
* Unit testing.
* Deployment on Heroku.
* Postgres database.
* Docker configuration.
* Uploading images for transactions to AWS S3.
* Signed AWS CloudFront URL with database caching for image-related responses (pre-signed S3 URL and default CloudFront URL are also available via the `aws.transaction-response-url-type` property - `cloudfront`, `s3`, or `cloudfront-signed` values are allowed, with the third one set by default).
* Importing transactions from a CSV file.

## Technologies Used
* Java
* Spring Boot
* Spring Security
* JPA/Hibernate
* Maven
* Lombok
* Apache Commons CSV
* AWS SDK
* AWS S3
* AWS CloudFront
* AWS Cognito
* Postgres
* Docker
* Heroku
* Swagger
* JUnit
* Mockito
* JWT

## Prerequisites
Before you begin, ensure you have the following tools installed:
* Git
* Docker
* Docker Compose
* AWS CLI (if you plan to use AWS features)
* Java JDK (e.g., 17 or newer)

## Getting Started

### Installation
1.  Clone the repository:
    ```bash
    git clone [https://github.com/MateuszMasternak/HomeBudgetTracker.git](https://github.com/MateuszMasternak/HomeBudgetTracker.git)
    cd HomeBudgetTracker
    ```
2.  Create a `.env` file in the project's root directory. You should use the provided `.env.example` as a template:
    ```bash
    cp .env.example .env
    ```

### `.env` File Configuration
The `.env` file contains environment variables that are used to configure the application.
Example file has default values for docker setup, but if you want to use endpoints related with AWS services or with the currency exchange rates, you need to set up your own values following the instructions in the example file.

### Docker Setup
1.  Build and run the Docker containers:
    ```bash
    docker-compose up --build -d
    ```
2.  Access the application at `http://localhost:8080`.

## API Endpoints
The API documentation is available at `http://localhost:8080/swagger-ui/index.html`.  
You can use tools like Postman or curl to test the API endpoints using default static "JWT token": 123456789.