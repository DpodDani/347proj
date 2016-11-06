#!/bin/bash

javac Client.java
echo "Client successfully compiled"
javac Replica.java
echo "Replica successfully compiled"

pkill rmiregistry
rmiregistry &
echo "RMI registry started"

killall java 
java Replica &
echo "Replica started"

echo "Starting client..."
java Client
echo "Client finished executing"
