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
EXCHANGE_RATE_URL=https://v6.exchangerate-api.com/v6
EXCHANGE_RATE_API_KEY=API_KEY // create an account at https://www.exchangerate-api.com/ for free and get your own KEY

***Docker Setup***
1. Build and run the Docker containers:  
> docker-compose up --build
2. The API will be available at http://localhost:8080.  
3. Access the MailDev interface at http://localhost:1080 to view sent emails.

## API Endpoints
http://localhost:8080/swagger-ui/index.html
