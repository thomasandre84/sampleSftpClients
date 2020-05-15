# Sample Sftp Clients
Some sample SFTP Client Library usage examples

## Java Sftp Clients
### Requirements
* JAVA JDK 11+
* Maven

### Run the tests 
Attention - read carefully about the LOCAL_DIR
* Have an SFTP Server
* change to the folder
* set environment Variables - for windows replace `export` with `set`
```
export SFTP_USERNAME=***
export SFTP_PASSWORD=***
export SFTP_HOST=***
export SFTP_PORT=**
export SFTP_REMOTE_DIR=**
export LOCAL_DIR=***  ## a folder without other important files - tests will delete every file in this folder
```
* Run the tests
```
mvn clean test
```
