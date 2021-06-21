#!/usr/bin/env python3
#
import asyncssh
import os
from ftplib import FTP


class MySSHServer(asyncssh.SSHServer):

    def __init__(self, **kwargs):
        asyncssh.SSHServer.__init__(self)
        print("KW_Args: {}".format(kwargs))

    def connection_made(self, conn):
        print('SSH connection received from %s.' %
              conn.get_extra_info('peername')[0])

    def password_auth_supported(self):
        return True

    def validate_password(self, username, password):
        print("Username {} with password: {}".format(username, password))
        #print("Having ftp Connection to {}".format(self.ftp.host))
        return True


class MySFTPServer(asyncssh.SFTPServer):
    def __init__(self, chan):
        root = '/tmp/' + chan.get_extra_info('username')
        os.makedirs(root, exist_ok=True)
        super().__init__(chan, chroot=root)

