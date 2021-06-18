#!/usr/bin/env python3
#
"""
@description: Tests for paramiko 
"""
import unittest
import os
from sftp_clients import paramiko_sample


USER = os.getenv("SFTP_USERNAME")
PASSWORD = os.getenv("SFTP_PASSWORD")
HOST = os.getenv("SFTP_HOST")
PORT = os.getenv("SFTP_PORT")
REMOTE_FOLDER = os.getenv("SFTP_REMOTE_FOLDER")
LOCAL_FOLDER = os.getenv("LOCAL_FOLDER")


class TestParamiko(unittest.TestCase):

    def test_connect(self):
        client = paramiko_sample.SftpClient()
        client.connect(HOST, PORT, USER, PASSWORD)
        self.assertTrue(client.sftp is not None)
        client.logout()


if __name__ == "__main__":
    unittest.main()

