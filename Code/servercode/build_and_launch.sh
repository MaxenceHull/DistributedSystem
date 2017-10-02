#!/bin/bash

# You must export the right class path in your terminal

echo Creating RMI Registry
rmiregistry -J-DJava.rmi.server.useCodebaseOnly=false 1099 &

echo Build Server
#cd /Users/maxencehull/Documents/Courses/COMP512/Project/Code/servercode/
# export CLASSPATH=/Users/maxencehull/Documents/Courses/COMP512/Project/Code/servercode
javac ResInterface/ResourceManager.java
jar cvf ResInterface.jar ResInterface/*.class
javac ResImpl/ResourceManagerImpl.java
# chmod -R 777 /Users/maxencehull/Documents/Courses/COMP512/Project/Code/servercode

echo Run Server
#java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:/Users/maxencehull/Documents/Courses/COMP512/Project/Code/servercode/ ResImpl.ResourceManagerImpl 1099 $1
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:/home/2016/mhull2/COMP512/Code/servercode/ ResImpl.ResourceManagerImpl 1099 $1