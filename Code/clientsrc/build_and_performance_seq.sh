#!/usr/bin/env bash

rm *.class
javac PerformanceTestSequential.java
java -Djava.security.policy=java.policy PerformanceTestSequential