# Setup

**1. rename the lib/tibjms.jar to tibjms-1.0.jar**

**2. import the tibjms-1.0.jar to this directory on your local maven repo**

- if the below path doesn't exist, create it first
- `/Users/<replace-user>/.m2/repository/com/tibco/tibjms/1.0`

**3. import the jms-2.0.jar to this directory on your local maven repo**

- if the below path doesn't exist, create it first
- `/Users/<replace-url>/.m2/repository/javax/jms/jms/2.0`

**4. run the pom.xml with below command**

- `mvn clean install -U`

# Execution

**1. run the java app**

- `mvn exec:java -Dexec.mainClass="com.example.App"`
