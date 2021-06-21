#!/usr/bin/env python3
#
"""

"""
import optparse
import sys
import textwrap
import asyncio
import asyncssh

HOST, PORT = 'localhost', 1234

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
        '-f', '--logfile', dest='logfile', default='./sftp2ftp.log',
        help='Set the logfile [default: %default]'
    )

    options, _args = parser.parse_args()

    if options.keyfile is None:
        parser.print_help()
        sys.exit(-1)

    start_server(options.host, options.ftp, options.port, options.keyfile, options.level)


if __name__ == '__main__':
    main()