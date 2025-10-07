# Library Dependencies

## Required: MySQL Connector/J

Download MySQL Connector/J 8.0.x from:
https://dev.mysql.com/downloads/connector/j/

**Instructions:**
1. Download the platform-independent ZIP archive
2. Extract `mysql-connector-java-8.0.xx.jar`
3. Place it in this `lib/` directory
4. In IntelliJ IDEA:
   - File → Project Structure → Libraries
   - Click '+' → Java
   - Select the JAR file
   - Click OK

**Alternative (if Maven is allowed):**
Add to pom.xml (if created):
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

**Note:** As per academic constraints, only MySQL Connector/J is allowed - no ORM frameworks.
