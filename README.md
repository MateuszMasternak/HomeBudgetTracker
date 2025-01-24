# HomeBudgetTracker

## Overview
Home Budget Tracker is a Java-based API designed to help users manage their personal finances. It allows users to track their income and expenses, categorize transactions, and generate reports in CSV format.  

## Features
* Authentication using the JWT token provided by Cognito.
* CRUD operations for accounts, categories and transactions.  
* Export of transactions to CSV file.  
* Calculating the sum of positive and/or negative transactions.
* Filtering of transactions by date and/or category within an account.  
* Retrieve exchange rates via external API when creating a new transaction if the currency differs from the account currency (you can also enter a custom exchange rate).  
* Exception handling  
* Logging.  
* Unit testing.  
* Deployment on Heroku  
* Postgres database.  
* Docker configuration.  
* Uploading images for transactions to AWS S3.  
* Signed AWS Cloudfront URL with database caching for image-related responses (pre-signed S3 URL and default Cloudfront URL are also available via aws.transaction-response-url-type property - cloudfront, s3 or cloudfront-signed values are allowed, the third one is set by default).  

## In Progress or Planned
* Import transactions from a file (probably CSV, maybe PDF).
* Frontend: [Deployed on Vercel](https://home-budget-tracker.vercel.app/) - work in progress.

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
* AWS Cloudfront
* AWS Cognito
* Postgres
* Docker
* Heroku
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
> DB_NAME_DEV=yourdatabasename  
> DB_USERNAME_DEV=yourusername  
> DB_PASSWORD_DEV=yourpassword  
> DB_HOST_DEV=postgres-hbt  
> DB_PORT_DEV=5432
> EXCHANGE_RATE_URL=https://v6.exchangerate-api.com/v6  
> EXCHANGE_RATE_API_KEY=API_KEY // create an account at https://www.exchangerate-api.com/ for free and get your own KEY if you want to use the exchange rate feature otherwise leave default and enter custom exchange rate manually when creating a new transaction (?exchange-rate=your-rate)

> * If you want to use AWS-related features, you need to create a user pool for Cognito, an S3 bucket instance and optionally a Cloudfront instance, and add the following environment variables to the .env file:  
> AWS_COGNITO_PUBLIC_KEY_DEV=your-token-signing-key-url // you can find it in the Cognito user pool overiew
> AWS_PROFILE_DEV=your-profile-name  
> AWS_S3_REGION_DEV=your-region  
> AWS_S3_BUCKET_DEV=your-bucket-name
> AWS_S3_PRESIGNED_URL_EXPIRATION_TIME_DEV=1234 // in seconds if you want to use pre-signed S3 URL otherwise leave default
> AWS_CLOUDFRONT_URL_DEV=your-cloudfront-url // leave default if you want to share images directly via S3  
> AWS_CLOUDFRONT_PUBLIC_KEY_PAIR_ID_DEV=cloud-front-public-key-id // leave default if you want to share images directly via S3 or default Cloudfront URL
> AWS_CLOUDFRONT_PRIVATE_KEY_DEV=cloud-front-private-key // leave default if you want to share images directly via S3 or default Cloudfront URL  
> * The profile should be configured in the ~/.aws/config file for example by aws configure sso through AWS CLI. Then you can login by aws sso login --profile your-profile-name. More info here: https://docs.aws.amazon.com/sdkref/latest/guide/access-sso.html.
> * You can generate the private key by openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048.
> * You can generate the public key by openssl rsa -in private_key.pem -pubout -out public_key.pem.

***Docker Setup***
1. Build and run Docker containers:
> docker-compose up --build -d
> * The .env file and the ~/.aws/config file are mounted to the web app container.
2. The API will be available at http://localhost:8080.

## API Endpoints
http://localhost:8080/swagger-ui/index.html
