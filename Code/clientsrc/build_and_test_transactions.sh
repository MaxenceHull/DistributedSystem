#!/usr/bin/env bash

rm *.class
javac ClientTestTransaction.java
java -Djava.security.policy=java.policy ClientTestTransaction