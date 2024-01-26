# Parking system application
This project was created as a practice project after finishing this amazing course on Udemy: https://capgemini.udemy.com/course/microservices-clean-architecture-ddd-saga-outbox-kafka-kubernetes
The business logic I have created here is very very simple. The main focus was on the concepts discussed in the course.

## About the project:
**Concepts/technologies that are utilized**: 
- DDD
- Outbox pattern
- Kafka
- Choreography saga based on only domain events

**Services**:
- Parking
- Payment
- Zone
- Customer

**Project structure**: 
One root module with microservice modules as submodules. It utilizes some aspects of Clean Architecture, but its not a fully baked one. 
Microservice modules consists of (only the two main service: Parking and Payment follows this structure):
1. Container module
2. Domain module
    - Application Service module
    - Domain Core module
3. Infrastructure module(s) (messaging, dataaccess, external http client etc.)

Domain core module:
The domain core contains the entities, value objects and a domain service.

Application service module: 
Defines the input and output ports and implements the input ports.

Infrastructure:
Output port implementations reside here. These sort of serve as a plugin for each output port definition. 

Container module:
This module glues together all the other modules into a working application.

**The entry point is the Parking service API**:
- Start parking: POST /parkings
- Stop parking:  POST /parkings/{parkingId}/stop
- Track parking: GET  /parkings/{parkingId}/track

<p float="left">
  <img src="https://github.com/mrkhlo/parking-system-app/assets/38752660/aa2ced58-ceba-40c5-8e46-5d064b2a489c" alt="start-parking" width="45%">
  <img src="https://github.com/mrkhlo/parking-system-app/assets/38752660/e4c6d3f9-4bca-46e6-805a-1c318f3dcc02" alt="start-parking" width="45%">
</p>

## Run the application locally:
Two options: 
- **K8S** : Run the app in your local k8s cluster after generating the docker images by running mvn install from the root folder, then follow the steps at: https://github.com/mrkhlo/parking-system-infrastructure
- **IntelliJ** + **Docker**: Run the app with IntelliJ and Docker as detailed below

**Prerequisites**:
- Docker
- Maven
- Java 17
- Postgres DB running locally
- IntelliJ

**Steps**:
1. **Run Kafka Cluster, Zookeeper, Schema Registry and create the topics**:
     ```bash
      docker-compose -f common.yml -f zookeeper.yml up
     ```
     Wait for zookeeper to properly start up.
  
    ```bash
      docker-compose -f common.yml -f kafka_cluster.yml up
    ```
    Wait for the cluster to properly start up.

   ```bash
    docker-compose -f common.yml -f init_kafka.yml up
   ```
2. **Run all 4 service from IntelliJ**:
      Run ParkingServiceApplication, PaymentServiceApplication, ZoneServiceApplication, CustomerServiceApplication
