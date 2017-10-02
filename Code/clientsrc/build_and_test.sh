#!/usr/bin/env bash

rm *.class
javac ClientTest.java
java -Djava.security.policy=java.policy ClientTest