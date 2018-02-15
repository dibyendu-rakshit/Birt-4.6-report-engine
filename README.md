# Project Title

This project helps to generate a report with birt 4.6 report engine.
It runs independently from command line.

## Getting Started

Clone this project with git clone command.

### Prerequisites

Java 8 is already installed
Maven is already installed
Aws account (access_key, access_secret, region and bucket name) where generated report will reside
Kafka is already running

### Installing

 1. Go to the downloaded location.
 2. Modify the values in application.yml file or you can override those valus at run time using -D{property_key}="value"
 3. Do mvn clean package.
 4. Go to the target folder.
 5. run using java -jar birt-report-1.0.0.jar
 

## Running the tests



### Break down into end to end tests



### And coding style tests



## Deployment
 Run as many processes in different jvm with the same kafka group.


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management


## Contributing



## Versioning



## Authors

* **Dibyendu Rakshit**  


## License



## Acknowledgments

