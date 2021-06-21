#!/usr/bin/env python3
#
"""
@description: Init Scripts for starting the server and accepting client connections
@author: Thomas Andre
"""

import time
import socket
import optparse
import sys
import textwrap
# import pdb
import threading
import paramiko
from paramiko.ssh_exception import SSHException
from .sftp2ftp import ServerImpl, SFTPServerImpl, LOGGER
from ftplib import FTP

HOST, PORT = 'localhost', 1234
BACKLOG = 10


class ClientConnection(threading.Thread):
    def __init__(self, ftp, conn, addr, keyfile):
        threading.Thread.__init__(self)
        self.conn = conn
        self.addr = addr
        self.keyfile = keyfile
        self.ftp = ftp

    def run(self):

        try:
            ftp = FTP(self.ftp, timeout=30)
        except:
            LOGGER.log(paramiko.common.INFO,
                       "could not connect to ftp server [{}]".format(self.ftp))
            return
        try:
            host_key = paramiko.RSAKey.from_private_key_file(self.keyfile)
            transport = paramiko.Transport(self.conn)

        except:
            LOGGER.log(paramiko.common.INFO, "Some Failed Client Connection: {}:{}".format(self.addr[0], self.addr[1]))
            return


        transport.add_server_key(host_key)
        transport.set_subsystem_handler(
            'sftp', paramiko.SFTPServer, SFTPServerImpl, ftp=ftp,
            connection=transport)

        try:
            server = ServerImpl(ftp=ftp)
            transport.start_server(server=server)
        except (EOFError, SSHException, Exception, ConnectionResetError):
            LOGGER.log(paramiko.common.INFO, "Could not start server: {}:{}".format(self.addr[0], self.addr[1]))
            return

        try:
            _channel = transport.accept()
            LOGGER.log(paramiko.common.INFO, "accept client connection: {}:{}".format(self.addr[0], self.addr[1]))
            transport.join()
            while transport.is_active():# and timer < self.timeout:
                #timer += 1
                #print(transport) # here it is stuck we need an idle timeout
                time.sleep(1)
        except:
            LOGGER.log(paramiko.common.WARNING, "Transport has been interrupted")

        finally:
            LOGGER.log(paramiko.common.INFO,
                       "Send FTP Quit Command: {}:{}".format(self.addr[0], self.addr[1]))
            try:
                ftp.sendcmd("QUIT")
                LOGGER.log(paramiko.common.INFO,
                           "Send FTP Close Command: {}:{}".format(self.addr[0], self.addr[1]))
                ftp.close()
                LOGGER.log(paramiko.common.INFO,
                           "disconnecting client: {}:{}".format(self.addr[0], self.addr[1]))

                transport.close()
                self.conn.close()
            except:
                LOGGER.log(paramiko.common.INFO, "FTP connection could not be closed")


def start_server(host, ftp, port, keyfile, level):
    paramiko_level = getattr(paramiko.common, level)
    paramiko.common.logging.basicConfig(level=paramiko_level)

    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, True)
    server_socket.bind((host, port))
    server_socket.listen(BACKLOG)

    while True:
        try:
            conn, addr = server_socket.accept()
            t = ClientConnection(ftp, conn, addr, keyfile)
            t.start()
        except:
            conn.close()
            LOGGER.log(paramiko.common.WARNING,
                       "Connection Exception: {}:{}".format(addr[0], addr[1]))




def main():
    usage = """\
    usage: sftpfd [options]
    -k/--keyfile should be specified
    """
    parser = optparse.OptionParser(usage=textwrap.dedent(usage))
    parser.add_option(
        '--host', dest='host', default=HOST,
        help='listen on HOST [default: %default]')
    parser.add_option(
        '--ftp-server', dest='ftp', default="localhost",
        help='ftp frontdoor to connect to [default: %default]')
    parser.add_option(
        '-p', '--port', dest='port', type='int', default=PORT,
        help='listen on PORT [default: %default]'
    )
    parser.add_option(
        '-l', '--level', dest='level', default='DEBUG',
        help='Debug level: WARNING, INFO, DEBUG [default: %default]'
    )
    parser.add_option(
        '-f', '--logfile', dest='logfile', default='./sftp2ftp.log',
        help='Set the logfile [default: %default]'
    )
    parser.add_option(
        '-s', '--logfilesize', type='int', dest='logfilesize', default=1024*1024*100,
        help='Set the logfilesize in bytes [default: %default]')
    parser.add_option(
        '-k', '--keyfile', dest='keyfile', metavar='FILE',
        help='Path to private key, for example /tmp/test_rsa.key'
    )
    """
    parser.add_option(
        '--pid-file', dest='pidfile', metavar='FILE', default=os.getcwd()+'/sftp2ftp.run',
        help='PID file [default: %default]'
    )
    """
    options, _args = parser.parse_args()

    if options.keyfile is None:
        parser.print_help()
        sys.exit(-1)
    """
    if os.path.isfile(options.pidfile):
        print("{} already exists! Removing file".format(options.pidfile))
        os.unlink(options.pidfile)
    with open(options.pidfile, 'w') as f:
        f.write(str(os.getpid()))
    """
    start_server(options.host, options.ftp, options.port, options.keyfile, options.level)
                 #options.logfile, options.logfilesize)


if __name__ == '__main__':
    main()