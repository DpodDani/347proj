# Synopsis

This is a Java RMI implementation of the Primary-Backup protocol.


# Usage

1. Compile everything using the bash script provided:
   * `./compile_everything.sh`
2. Run `java MyReplica &` to create Primary and Backup nodes
   * A single run of that command creates a single node, so run that command twice
   * 1st time will create a Primary node, the second time will create a Backup node
3. Send client requests using `java Client`
   * Currently the client requests are hardcoded, but in the next release we'll include an _InputStream_ to make it more "accessible"
4. To kill either the Primary or the Backup node, use `kill <process_id>`
   * When you use `java MyReplica &`, it'll usually display the _process ID_ of the created node in square brackets
      * For example: `[8563]`
   * Killing the Primary node will make the Backup node take over, whereas killing the Backup node will result in nothing really (for now)
   
# Notes

Let us know if you want us to add more comments to the code, or explain things further
