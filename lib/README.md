# Library Dependencies

## Required: MySQL Connector/J

Download MySQL Connector/J 9.4.0 from:
https://dev.mysql.com/downloads/connector/j/

**Instructions:**
1. Download the platform-independent ZIP archive
2. Extract `mysql-connector-j-9.4.0.jar` (o versión más reciente)
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
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.4.0</version>
</dependency>
```

**Note:** As per academic constraints, only MySQL Connector/J is allowed - no ORM frameworks.
