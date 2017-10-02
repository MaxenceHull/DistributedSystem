#!/usr/bin/env bash
#cd /Users/maxencehull/Documents/Courses/COMP512/Project/CodeSocket/ResourceManager/ResourceManagerImpl/
rm ResourceManagerImpl/*.class
javac ResourceManagerImpl/Server.java
java ResourceManagerImpl/Server $1