# 347proj
Primary-Backup Object Replications in Java

How to run:
 * Compile everything
 * Open 3 terminals
 * First - java Server 9002 db1.txt [this is the backup]
 * Second - java Server 9001 db2.txt [this is the primary]
 * Third - java Client localhost 9001
 * Enter commands into the client terminal!

Backlog (Replica-proxy):
 * Backup time-out (Was done, adding it onto new solution)
 * Primary time-out
 * Replica consistency (if necessary)
 * Naming Service
 * Dedicated communication channel set up between primary and backup (implemented but should it be done as a thread?)
 * Parent thread to accept client connections and spawn child threads do deal with them (DONE YAY)
 * Thread started by the primary to allow the backup to join at any time