## Requirements

* Maven
* Java 17 (JRE and JDK)


## How to Run

You can start the server by first navigating into either `insecure/` or `secure` and then executing `mvn spring-boot:run` after having executed `mvn package`. For example, to start the web server for the secure application, run the following commands:
```
git clone https://github.com/rolandbernard/is-project
cd ./is-project/secure/
mvn spring-boot:run
```
After starting the server, the website will be accessible on `http://localhost:8080`, or at `https://localhost:8443` for the secure version. Since the default certificate used for HTTPS is self-signed, it would of course need to be replaced for a real deployment. The browser will warn you about the invalid certificate, and you should tell it to accept the risk and continue to `https://localhost:8443`.

