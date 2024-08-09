# HomeBudgetTracker

## Overview
Home Budget Tracker is a Java-based API designed to help users manage their personal finances. It allows users to track their income and expenses, categorize transactions, and generate reports in CSV format.  

## Features
* User authentication and authorization
* CRUD operations for categories and transactions
* Export transactions to CSV files
* Sum of positive and negative transactions
* Filter transactions by date and category

## Technologies Used
* Java
* Spring Boot
* Spring Security
* JPA/Hibernate
* Maven
* Lombok
* Apache Commons CSV

## Getting Started
***Installation***
1. Clone the repository:  
> * git clone https://github.com/MateuszMasternak/HomeBudgetTracker.git  
> * cd HomeBudgetTracker
2. Create the .env file and fill it up with the following environment variables:
>JWT_SECRET_KEY_DEV=yoursecretkey  
DB_NAME_DEV=yourdatabasename 
DB_USERNAME_DEV=yourusername  
DB_PASSWORD_DEV=yourpassword  
DB_HOST_DEV=db // don't change  
DB_PORT_DEV=5432 // don't change  
MAILDEV_HOST_DEV=mail-dev-hbt // don't change  
EMAIL_USERNAME_DEV=username@test.com  
EMAIL_PASSWORD_DEV=password  

***Docker Setup***
1. Build and run the Docker containers:  
> docker-compose up --build
2. The API will be available at http://localhost:8080.  
3. Access the MailDev interface at http://localhost:1080 to view sent emails.

## API Endpoints
***Authentication Endpoints***  
* POST **/api/v1/auth/register**: Register a new user.
* POST **/api/v1/auth/authenticate**: Authenticate a user.
* GET **/api/v1/auth/activate-account**: Activate a user account with a token.
***Category Endpoints***
* GET **/api/v1/category**: Get all categories with pagination.
* GET **/api/v1/category/without-pagination**: Get all categories without pagination.
* POST **/api/v1/category**: Create a new category.
* DELETE **/api/v1/category/{id}**: Delete a category by ID.
***Transaction Endpoints***
* GET **/api/v1/transaction**: Get all transactions with pagination.
* GET **/api/v1/transaction/category**: Get transactions by category with pagination.
* GET **/api/v1/transaction/date**: Get transactions by date range with pagination.
* GET **/api/v1/transaction/category-date**: Get transactions by category and date range with pagination.
* POST **/api/v1/transaction**: Create a new transaction.
* DELETE **/api/v1/transaction/{id}**: Delete a transaction by ID.
* GET **/api/v1/transaction/sum-positiv**e: Get the sum of positive transactions.
* GET **/api/v1/transaction/sum-negative**: Get the sum of negative transactions.
* GET **/api/v1/transaction/sum**: Get the sum of all transactions.
* GET **/api/v1/transaction/sum-date**: Get the sum of transactions within a date range.
* GET **/api/v1/transaction/export**: Export transactions to a CSV file.