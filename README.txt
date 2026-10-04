ONLINE DONATION MANAGEMENT SYSTEM  (Java Servlets + MySQL)
CS2307 - Internet Programming Lab Mini Project

REQUIREMENTS
 1. JDK 11 or above
 2. Apache Tomcat 10 or above   (uses the jakarta.servlet package)
      -> For Tomcat 9 or lower: replace "jakarta.servlet" with "javax.servlet"
         in all .java files and use the javax web.xml header.
 3. MySQL Server
 4. MySQL Connector/J jar (mysql-connector-j-x.x.x.jar)

STEPS
 1. Open MySQL and run:      source database.sql;
 2. Open src/donation/DBConnection.java and set your MySQL user name and password.
 3. Copy the Connector/J jar into  WebContent/WEB-INF/lib/
 4. Compile the servlets (change the path of servlet-api.jar to your Tomcat folder):
        javac -cp "TOMCAT_HOME/lib/servlet-api.jar" -d WebContent/WEB-INF/classes src/donation/*.java
 5. Copy the WebContent folder into  TOMCAT_HOME/webapps/  and rename it to  DonationApp
    (or export it as a WAR file from Eclipse / NetBeans and deploy it).
 6. Start Tomcat and open:   http://localhost:8080/DonationApp/

ADMIN LOGIN
   username: admin      password: admin123
