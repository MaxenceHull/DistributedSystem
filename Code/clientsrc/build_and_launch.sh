#!/usr/bin/env bash
#cp /Users/maxencehull/Documents/Courses/COMP512/Project/Code/servercode/ResInterface.jar /Users/maxencehull/Documents/Courses/COMP512/Project/Code/clientsrc/
#javac -cp "/Users/maxencehull/Documents/Courses/COMP512/Project/Code/clientsrc/ResInterface.jar" /Users/maxencehull/Documents/Courses/COMP512/Project/Code/clientsrc/client.java
#java -cp "/Users/maxencehull/Documents/Courses/COMP512/Project/Code/clientsrc/" -Djava.security.policy=java.policy client
rm *.class
javac client.java
java -Djava.security.policy=java.policy client