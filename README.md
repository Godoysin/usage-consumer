# usage-consumer

This is a Java Spring Boot practice project to mix knowledge from different sources. In this case, it is a kafka consumer to read from a topic and safe into MySql database.

I'm using next **docker-compose.yaml** to initialize the environment:
```yaml
version: '3'
services:

  zookeeper:
    image: zookeeper:3.7.0
    restart: always
    hostname: zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
        ZOO_MY_ID: 1
        ZOO_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.0.1
    hostname: kafka
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_kafka_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:19092,PLAINTEXT_HOST://localhost:9092
      CONFLUENT_METRICS_ENABLE: 'false'
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_CONFLUENT_SCHEMA_REGISTRY_URL: http://schema-registry:8081

  schema-registry:
    image: confluentinc/cp-schema-registry:7.0.1
    hostname: schema-registry
    container_name: schema-registry
    depends_on:
      - kafka
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: 'kafka:19092'
      SCHEMA_REGISTRY_LISTENERS: http://0.0.0.0:8081

  control-center:
    image: confluentinc/cp-enterprise-control-center:7.0.1
    hostname: control-center
    container_name: control-center
    depends_on:
      - kafka
      - schema-registry
    ports:
      - "9021:9021"
    environment:
      CONTROL_CENTER_BOOTSTRAP_SERVERS: 'kafka:19092'
      CONTROL_CENTER_SCHEMA_REGISTRY_URL: "http://schema-registry:8081"
      CONTROL_CENTER_REPLICATION_FACTOR: 1
      CONTROL_CENTER_INTERNAL_TOPICS_PARTITIONS: 1
      CONTROL_CENTER_MONITORING_INTERCEPTOR_TOPIC_PARTITIONS: 1
      CONFLUENT_METRICS_TOPIC_REPLICATION: 1
      PORT: 9021

  rest-proxy:
    image: confluentinc/cp-kafka-rest:7.0.1
    depends_on:
      - kafka
      - schema-registry
    ports:
      - 8082:8082
    hostname: rest-proxy
    container_name: rest-proxy
    environment:
      KAFKA_REST_HOST_NAME: rest-proxy
      KAFKA_REST_BOOTSTRAP_SERVERS: 'kafka:19092'
      KAFKA_REST_LISTENERS: "http://0.0.0.0:8082"
      KAFKA_REST_SCHEMA_REGISTRY_URL: 'http://schema-registry:8081'

  akhq:
    # build:
    #   context: .
    image: tchiotludo/akhq:0.20.0
    container_name: akhq
    environment:
      AKHQ_CONFIGURATION: |
        akhq:
          connections:
            docker-kafka-server:
              properties:
                bootstrap.servers: "kafka:19092"
              schema-registry:
                url: "http://schema-registry:8081"
    ports:
      - 8180:8080
    links:
      - kafka
      - schema-registry

  mysql:
    image: mysql
    container_name: mysqlDB
    cap_add:
      - SYS_NICE
    environment:
      - MYSQL_ROOT_PASSWORD=password
    ports:
      - '3306:3306'
    volumes:
      - mysqlvolume:/var/lib/mysql
      - C:\Users\DGodoyChiclana\OneDrive - DXC Production\Desktop\practice\init.sql:/docker-entrypoint-initdb.d/init.sql

volumes:
  mysqlvolume:
    driver: local
```

I also use this SQL script to initialize DB tables and content: **init.sql**
```sql
CREATE DATABASE university;
CREATE USER 'springuser'@'%' IDENTIFIED BY 'ThePassword';
GRANT ALL ON university.* TO 'springuser'@'%';
USE university;
CREATE TABLE http_methods (method VARCHAR(7) NOT NULL PRIMARY KEY);
INSERT INTO http_methods (method) VALUES ('GET'), ('HEAD'), ('POST'), ('PUT'), ('DELETE'), ('CONNECT'), ('OPTIONS'), ('TRACE'), ('PATCH');
CREATE TABLE track (id INT(12) UNSIGNED AUTO_INCREMENT PRIMARY KEY, user VARCHAR(100) NOT NULL DEFAULT 'testUser', method VARCHAR(7) NOT NULL, resource VARCHAR(100), CONSTRAINT http_methods_method_fk FOREIGN KEY (method) REFERENCES http_methods(method));
```
