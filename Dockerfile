# Stage 1 -> build the app using maven
FROM maven:3.8.5-openjdk-21 AS builder

# the working directory
WORKDIR /app

# copy the pom.xml file to download dependencies
COPY pom.xml .

# Download all depen.
RUN mvn dependency:go-offline

# copy the rest of the src code
COPY src ./src

# Package the application -> compile, run tests and create the JAR file
RUN mvn clean package -DskipTests       # We skip tests here for faster builds

# Stage 2 -> build the jar file on this optimized image uses jre on ubuntu 22.04 jammy
FROM eclipse-temurin:21-jre-jammy

# set the working dir
WORKDIR /app

#Copy the JAR file from the builder stage
COPY --from-builder /app/target/springBlog-0.0.1-SNAPSHOT.jar app.jar

#Expose the port the app runs on
EXPOSE 8080

# To run the app when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]