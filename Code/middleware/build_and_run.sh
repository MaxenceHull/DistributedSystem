#!/usr/bin/env bash

echo Creating RMI Registry
rmiregistry -J-DJava.rmi.server.useCodebaseOnly=false 1099 &

echo Build Server
rm MiddlewareImpl/*.class
rm LockManager/*.class
rm TransactionManager/*.class
#cd /Users/maxencehull/Documents/Courses/COMP512/Project/Code/middleware/
# export CLASSPATH=/Users/maxencehull/Documents/Courses/COMP512/Project/Code/servercode

javac MiddlewareImpl/MiddlewareManagerImpl.java
# chmod -R 777 /Users/maxencehull/Documents/Courses/COMP512/Project/Code/servercode

#java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:/Users/maxencehull/Documents/Courses/COMP512/Project/Code/middleware/ MiddlewareImpl.MiddlewareManagerImpl
#java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:/home/2016/mhull2/COMP512/Code/middleware/ MiddlewareImpl.MiddlewareManagerImpl
#echo Run Server
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:/Users/maxencehull/Documents/Courses/COMP512/Project/Code/middleware/ MiddlewareImpl.MiddlewareManagerImpl 1099