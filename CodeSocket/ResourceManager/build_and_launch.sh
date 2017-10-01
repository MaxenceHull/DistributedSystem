#!/usr/bin/env bash
#cd /Users/maxencehull/Documents/Courses/COMP512/Project/CodeSocket/ResourceManager/ResourceManagerImpl/
rm *.class
javac Server.java
cd ..
java ResourceManagerImpl/Server $1