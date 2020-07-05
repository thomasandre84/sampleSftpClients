#!/usr/bin/env python3
#
"""
@description: python paramiko sample SFTP Client
"""
import paramiko
import logging


class SftpClient:
    '''
    Simple SFTP Client with Python Paramiko
    '''
    LOGGER = logging.getLogger('paramiko_sample')

    def __init__(self):
        self.client = paramiko.SSHClient()
        self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy)
        self.sftp = None

    def connect(self, host, port, user, password, timeout=10.0):
        self.client.connect(host, port, user, password, timeout=timeout)
        self.sftp = self.client.open_sftp()
        self.LOGGER.info("Logged in")

    def logout(self):
        self.client.close()

    def cd(self, folder):
        self.sftp.chdir(folder)

    def ls(self):
        return self.sftp.listdir()

    def mkdir(self, folder):
        self.sftp.mkdir(folder)

    def rmdir(self, folder):
        self.sftp.rmdir(folder)

    def rm(self, file):
        self.sftp.remove(file)

    def get(self, source, target):
        self.sftp.get(source, target)

    def put(self, source, target):
        self.sftp.put(source, target)

