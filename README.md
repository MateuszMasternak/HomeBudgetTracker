# HomeBudgetTracker

## Overview
Home Budget Tracker is a Java-based API designed to help users manage their personal finances. It allows users to track their income and expenses, categorize transactions, and generate reports in CSV format.  

## Features
* Simple JWT based user authentication and authorization
* Email verification for new users and password reset
* CRUD operations for accounts, categories and transactions
* Export transactions to CSV file
* Sum of positive or/and negative transactions
* Filter transactions by date or/and category within an account
* Get exchange rates via an external API when creating a new transaction if the currency is different from the account currency (you can also set the exchange rate manually)
* Exception handling
* Logging
* Some unit tests
* Deployment on Heroku
* Postgres database
* Docker setup
* Image upload for transaction to AWS S3 (done, but not merged yet - feature/images)
* Presigned URL in response which containing an image (done, but not merged yet - feature/images)

## In Progress or Planned
* Transaction details, including saving data related to the exchange
* Import transactions from a file (probably CSV, maybe PDF)
* Cloudfront with caching for images
* More secure authentication
* Frontend (probably Vue + PrimeVue for UI - already have one in the other repository, but it has implemented only a few feature and is not up to date)  
![hbt_fe_demo.gif](hbt_fe_demo.gif)

## Technologies Used
* Java
* Spring Boot
* Spring Security
* JPA/Hibernate
* Maven
* Lombok
* Apache Commons CSV
* AWS SDK
* Postgres
* Docker
* Heroku
* MailDev
* Swagger
* JUnit
* Mockito
* JWT

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
DB_HOST_DEV=postgres-hbt  
DB_PORT_DEV=5432  
MAILDEV_HOST_DEV=mail-dev-hbt  
EMAIL_USERNAME_DEV=username@test.com    
EMAIL_PASSWORD_DEV=password  
EXCHANGE_RATE_URL=https://v6.exchangerate-api.com/v6  
EXCHANGE_RATE_API_KEY=API_KEY // create an account at https://www.exchangerate-api.com/ for free and get your own KEY  
> * The .env file is used by the Docker containers.
> * If you want to run the application using features related to AWS S3, you need to clone the feature/images branch instead of the main branch, create S3 bucket, add the following environment variables to the .env file:  
> AWS_PROFILE_DEV=your-profile-name  
> AWS_S3_REGION_DEV=your-region  
> AWS_S3_BUCKET_DEV=your-bucket-name  
> * The profile should be configured in the ~/.aws/config file for example by aws configure sso through AWS CLI. Then you can login by aws sso login --profile your-profile-name. More info here: https://docs.aws.amazon.com/sdkref/latest/guide/access-sso.html. 

***Docker Setup***
1. Build and run the Docker containers:  
> docker-compose up --build
2. The API will be available at http://localhost:8080.  
3. Access the MailDev interface at http://localhost:1080 to view sent emails.

## API Endpoints
http://localhost:8080/swagger-ui/index.html
