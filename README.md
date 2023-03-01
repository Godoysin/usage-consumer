# usage-consumer

This is a **Java Spring Boot** practice project to mix and test knowledge from different sources. In this case, it is a Kafka consumer to read from a topic and safe into MySql database.

To create the image from of the Java project using the Dockerfile and execute it, it can be used:
```shell
mvn clean install
docker image build -t usage-consumer:0.1 .
docker container run --rm --add-host=host.docker.internal:host-gateway --network practice_default -p 8991:8991 --name usage-consumer -e "SPRING_PROFILES_ACTIVE=dev" -e JAVA_TOOL_OPTIONS="-DMYSQL_HOST=<MYSQL_IP> -DKAFKA_IP=<KAFKA_IP>" usage-consumer:0.1
```
<MYSQL_IP> is got from the mysql container inspecting it.
<KAFKA_IP> is got from the host that serves the cluster, in Windows, I'm taking IPv4 direction using ipconfig in a terminal.

**docker-compose.yaml** is used to initialize the environment:
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
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:19092,PLAINTEXT_HOST://${KAFKA_IP}:9092
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
      - <init.sql_PATH>\init.sql:/docker-entrypoint-initdb.d/init.sql

volumes:
  mysqlvolume:
    driver: local
```
It is also used a .env file to specify the host IP where the Kafka cluster is running:
```yaml
KAFKA_IP=<KAFKA_IP>
```
<KAFKA_IP> is got from the host that serves the cluster, in Windows, I'm taking IPv4 direction using ipconfig in a terminal.

This SQL script is also used to initialize DB tables and populate it: **init.sql**
```sql
CREATE DATABASE university;
CREATE USER 'springuser'@'%' IDENTIFIED BY 'ThePassword';
GRANT ALL ON university.* TO 'springuser'@'%';
USE university;
CREATE TABLE http_methods (method VARCHAR(7) NOT NULL PRIMARY KEY);
INSERT INTO http_methods (method) VALUES ('GET'), ('HEAD'), ('POST'), ('PUT'), ('DELETE'), ('CONNECT'), ('OPTIONS'), ('TRACE'), ('PATCH');
CREATE TABLE track (id INT(12) UNSIGNED AUTO_INCREMENT PRIMARY KEY, user VARCHAR(100) NOT NULL DEFAULT 'testUser', method VARCHAR(7) NOT NULL, resource VARCHAR(100), CONSTRAINT http_methods_method_fk FOREIGN KEY (method) REFERENCES http_methods(method));
CREATE TABLE career (career_name VARCHAR(128) NOT NULL PRIMARY KEY, creation_date DATE);
INSERT INTO career (career_name, creation_date) VALUES ('Telematics engineering', '2023-01-01'), ('Electrical engineering', '2023-01-24');
CREATE TABLE subject (subject_name VARCHAR(128) NOT NULL PRIMARY KEY, creation_date DATE);
INSERT INTO subject (subject_name, creation_date) VALUES ('Introductory mathematics', '2023-01-01'), ('Internet protocols', '2023-01-01'), ('Electrical theory', '2023-01-01');
CREATE TABLE teacher (teacher_name VARCHAR(128) NOT NULL PRIMARY KEY);
INSERT INTO teacher (teacher_name) VALUES ('Ann'), ('Bob'), ('Charles');
CREATE TABLE career_subject (career_name VARCHAR(128) NOT NULL, subject_name VARCHAR(128) NOT NULL, PRIMARY KEY (career_name, subject_name), CONSTRAINT career_subject_career_name_career_fk FOREIGN KEY (career_name) REFERENCES career(career_name), CONSTRAINT career_subject_subject_name_subject_fk FOREIGN KEY (subject_name) REFERENCES subject(subject_name));
INSERT INTO career_subject (career_name, subject_name) VALUES ('Telematics engineering', 'Introductory mathematics'), ('Electrical engineering', 'Introductory mathematics'), ('Telematics engineering', 'Internet protocols'), ('Electrical engineering', 'Electrical theory');
CREATE TABLE subject_teacher (subject_name VARCHAR(128) NOT NULL, teacher_name VARCHAR(128) NOT NULL, PRIMARY KEY (subject_name, teacher_name), CONSTRAINT subject_teacher_teacher_name_teacher_fk FOREIGN KEY (teacher_name) REFERENCES teacher(teacher_name), CONSTRAINT subject_teacher_subject_name_subject_fk FOREIGN KEY (subject_name) REFERENCES subject(subject_name));
INSERT INTO subject_teacher (subject_name, teacher_name) VALUES ('Introductory mathematics', 'Ann'), ('Internet protocols', 'Bob'), ('Electrical theory', 'Charles');
CREATE TABLE user (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY, name VARCHAR(128) NOT NULL, password VARCHAR(128) NOT NULL);
INSERT INTO user (id, name, password) VALUES (1, 'test', 'test');
CREATE TABLE role (name VARCHAR(128) NOT NULL PRIMARY KEY);
INSERT INTO role (name) VALUES ('testRole');
CREATE TABLE user_role (user_id INT UNSIGNED, role_name VARCHAR(128), PRIMARY KEY (user_id, role_name), CONSTRAINT user_role_user_id_user_fk FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT user_role_role_name_role_fk FOREIGN KEY (role_name) REFERENCES role(name) ON DELETE CASCADE ON UPDATE CASCADE);
INSERT INTO user_role (user_id, role_name) VALUES (1, 'testRole');
CREATE TABLE configuration (id int NOT NULL AUTO_INCREMENT PRIMARY KEY, property_name varchar(200) NOT NULL, property_value varchar(200) NOT NULL, description varchar(200), environment varchar(4) DEFAULT 'dev', date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
INSERT INTO configuration (id, property_name, property_value, description, environment) VALUES (1, 'security.jwt.key', 'jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4', 'JWT key', 'dev'), (2, 'security.hash.algorithm', 'SHA-256', 'Hash used algorithm', 'dev'), (3, 'security.cipher.key', 'Adf@=m29hRqqQtkW9#_B', 'Cipher key', 'dev');
```
