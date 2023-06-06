Information Security Project
============================

## Getting Started

### Requirements

* Maven
* Java 17 (JRE and JDK)

### How to Run

You can start the server by first navigating into either `insecure/` or `secure/` and then executing `mvn spring-boot:run` after having executed `mvn package`. For example, to download this project and start the web server for the secure application, run the following commands:
```
git clone https://github.com/rolandbernard/is-project
cd ./is-project/secure/
mvn spring-boot:run
```
After starting the server, the website will be accessible on `http://localhost:8080`, or at `https://localhost:8443` for the secure version. Since the default certificate used for HTTPS is self-signed, it would of course need to be replaced for a real deployment. The browser will warn you about the invalid certificate, and you should tell it to accept the risk and continue to `https://localhost:8443`.

### Source Code Structure

The repository contains two separate directories for the two versions of the project. The insecure version that contains deliberate vulnerabilities can be found in the `insecure/` directory, and the secure version for which these vulnerabilities have been fixed can be found in the `secure/` directory.

Both projects are written to use the Java framework [Spring](https://spring.io/) and use [Maven](https://maven.apache.org/) for dependency management. It uses the default structure for a Spring Boot application. The Java code can be found in the `{insecure,secure}/src/main/java/` directories, while the templates for the web pages can be found in `{insecure,secure}/src/main/resources/templates`. The initialization code for the database can be found in `{insecure,secure}/src/main/resources/sql`, but there is no need to initialize the database manually, as that is handled automatically during startup. We use [SQLite](https://www.sqlite.org/index.html) as the database, as it requires no separate DBMS to be installed. For the secure version we have additionally added a handful of test cases for the implementation of some cryptography algorithms. These are located in `secure/src/test/java`. They can be run using `mvn compile test`.

The Java implementation itself is split into the common and utility classes, model classes, controller classes and middleware. The common classes are directly in `{insecure,secure}/src/main/java/{insecure,secure}/`. The classes used for representing the model of the application and for storing and loading the associated data from the database can be found in `{insecure,secure}/src/main/java/{insecure,secure}/model/`. Classes that handle the incoming user request are located in `{insecure,secure}/src/main/java/{insecure,secure}/controller/`. Finally, we also use some middleware that performs code before and after requests are handled by a controller. These can be found in `{insecure,secure}/src/main/java/{insecure,secure}/middleware/` and perform authentication and CSRF token checks.

