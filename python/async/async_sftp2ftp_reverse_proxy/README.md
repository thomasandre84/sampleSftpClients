# Example Package

This is a simple example package. You can use
[Github-flavored Markdown](https://guides.github.com/features/mastering-markdown/)
to write your content.

## Pre Requirements
```
python3 -m pip install --user --upgrade setuptools wheel
```

## Build with
```
python3 setup.py sdist bdist_wheel
```

## Host Key 
Create an example SSH Host Key with:

`ssh-keygen -q -N "" -t rsa -b 4096 -f ssh_host_rsa_key` 

## Start ftp server  with docker
```
docker run -d \
    -p 21:21 \
    -p 21000-21010:21000-21010 \
    -e USERS="test|1234" \
    -e ADDRESS=localhost \
    --name ftp-server \
    delfer/alpine-ftp-server
```