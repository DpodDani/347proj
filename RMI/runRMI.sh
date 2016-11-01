#!/bin/bash
# Compiles everything and makes it run

javac -d ./ MyRemote.java MyServer.java MyClient.java
java -classpath ./ -Djava.rmi.server.codebase=file:./ MyServer &
java -classpath ./ MyClient
