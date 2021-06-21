#!/usr/bin/env python3
#
"""

"""
import optparse
import sys
import textwrap
import asyncio
import asyncssh
from .sftp2ftp import MySFTPServer, MySSHServer

HOST, PORT = 'localhost', 1234


def handle_client(process):
    process.stdout.write('Welcome to my SSH server, %s!\n' %
                         process.get_extra_info('username'))
    process.exit(0)


async def start_server(host, port, ftp_host, keyfile, loglevel):
    await asyncssh.listen(host, port, server_host_keys=[keyfile],
                          server_factory=MySSHServer,
                          keepalive_interval=30,
                          sftp_factory=MySFTPServer,
                          process_factory=handle_client)


def main():
    usage = """\
    usage: async_sftpfd [options]
    -k/--keyfile should be specified
    """
    parser = optparse.OptionParser(usage=textwrap.dedent(usage))
    parser.add_option(
        '--host', dest='host', default=HOST,
        help='listen on HOST [default: %default]')
    parser.add_option(
        '--ftp-server', dest='ftp', default="localhost",
        help='ftp server to connect to [default: %default]')
    parser.add_option(
        '-p', '--port', dest='port', type='int', default=PORT,
        help='listen on PORT [default: %default]'
    )
    parser.add_option(
        '-l', '--level', dest='level', default='DEBUG',
        help='Debug level: WARNING, INFO, DEBUG [default: %default]'
    )
    parser.add_option(
        '-k', '--keyfile', dest='keyfile', metavar='FILE',
        help='Path to private key, for example ssh/ssh_host_rsa_key'
    )

    options, _args = parser.parse_args()

    if options.keyfile is None:
        parser.print_help()
        sys.exit(-1)

    loop = asyncio.get_event_loop()

    try:
        loop.run_until_complete(start_server(options.host, options.port, options.ftp, options.keyfile, options.level))
    except (OSError, asyncssh.Error) as exc:
        sys.exit('Error starting server: ' + str(exc))

    loop.run_forever()
    #start_server(options.host, options.ftp, options.port, options.keyfile, options.level)


if __name__ == '__main__':
    main()