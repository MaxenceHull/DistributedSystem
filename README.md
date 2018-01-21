# Distributed Systems: Trip reservation

## Introduction
* This project is a part of the distributed system course (COMP512) @McGill University
* The main idea is to distibute three different services accross different servers:
  * One server manages rooms reservation
  * One server manages flights reservation
  * One server manages cars reservation
  * One server acts as a middleware
* During this project, we implemented some well-known conpcepts in distrubted systems such as
  * 2 phase-commit (2PC)
  * 2 phase-locking (2PL)
  * Data shadowing

## Notes
* We used the Java RMI library to enable communication between servers
* ![Architecture](https://github.com/hulm2701/COMP512/blob/master/Architecture.png)
