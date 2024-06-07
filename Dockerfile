# Use a base image with Java 21
FROM openjdk:21

# Set the working directory
WORKDIR /app

# Copy the Java application JAR file
COPY app/build/libs/app-all.jar /app/supernotes.jar

# Expose the port on which the application will run
EXPOSE 8080

# Set the command to run the Java application
CMD ["java", "-jar", "supernotes.jar"]