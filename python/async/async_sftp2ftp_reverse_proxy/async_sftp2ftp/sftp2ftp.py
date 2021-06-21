#!/usr/bin/env python3
#
import asyncssh
import os


class MySFTPServer(asyncssh.SFTPServer):
    def __init__(self, chan):
        root = '/tmp/' + chan.get_extra_info('username')
        os.makedirs(root, exist_ok=True)
        super().__init__(chan, chroot=root)

    def connection_made(self, conn):
        print('SSH connection received from %s.' %
              conn.get_extra_info('peername')[0])

    def password_auth_supported(self):
        return True

    def validate_password(self, username, password):
        print("Username {} with password: {}".format(username, password))
        return True