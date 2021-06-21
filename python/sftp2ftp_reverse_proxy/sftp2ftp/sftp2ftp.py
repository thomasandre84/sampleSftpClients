#!/usr/bin/env python3
#
"""
@description: the mapper classes
"""
import os
import paramiko
from paramiko import ServerInterface, SFTPServerInterface, SFTPServer, SFTPAttributes, \
    SFTPHandle
from paramiko.common import AUTH_SUCCESSFUL, AUTH_FAILED, OPEN_SUCCEEDED
from paramiko.sftp import SFTP_OK, SFTP_FAILURE, SFTP_PERMISSION_DENIED, SFTP_OP_UNSUPPORTED
from paramiko.common import INFO, WARNING, DEBUG
from ftplib import FTP, all_errors, error_temp
import datetime
import time
# import pdb
import io
import pathlib

LOGGER = paramiko.util.get_logger('paramiko.transport')


class ServerImpl(ServerInterface):

    def __init__(self, **kwargs):
        LOGGER.log(DEBUG, "Server.__init__({}) called".format(kwargs))
        for key, value in kwargs.items():
            if key == "ftp":
                self._ftp = value

    def check_auth_password(self, username, password):
        LOGGER.log(DEBUG, "Server.check_auth_password({}) called".format(username))
        try:
            self._ftp.login(username, password)
        except:
            LOGGER.log(INFO, "authentication failed for user: [{}]".format(username))
            return AUTH_FAILED
        return AUTH_SUCCESSFUL

    def check_channel_request(self, kind, chanid):
        LOGGER.log(DEBUG, "Server.check_channel_request called")
        return OPEN_SUCCEEDED


class SFTPHandleImpl(SFTPHandle):

    def __init__(self, flags=0):
        super().__init__(flags)
        self.__flags = flags
        self.__tell = None
        self.writefile = None
        self._ftp = FTP()
        self._data_received = None
        self._data = io.BytesIO()
        self._buffer = None
        self.fpath = None

    def set_ftp(self, ftp):
        LOGGER.log(DEBUG, "SFTPHandleImpl.set_ftp called")
        self._ftp = ftp

    def stat(self):
        LOGGER.log(DEBUG, "SFTPHandleImpl.stat called")
        try:
            return SFTPAttributes.from_stat(os.fstat(self.readfile.fileno()))
        except OSError as e:
            return SFTPServer.convert_errno(e.errno)

    def chattr(self, attr):
        # python doesn't have equivalents to fchown or fchmod, so we have to
        # use the stored filename
        LOGGER.log(DEBUG, "SFTPHandleImpl.chattr called")
        try:
            SFTPServer.set_file_attr(self.filename, attr)
            return SFTP_OK
        except OSError as e:
            return SFTPServer.convert_errno(e.errno)

    def read(self, offset, length):
        LOGGER.log(DEBUG, "SFTPHandleImpl.read with offset[{}] length[{}] called".format(
            offset, length))
        try:
            if self._data_received is None:
                self._ftp.retrbinary("RETR " + self.filename, self._data.write)
                self._buffer = self._data.getvalue()
                self._data_received = len(self._buffer)
                if length <= self._data_received:
                    return self._buffer[0:length]
                return self._buffer[0:self._data_received]

            if offset + length <= self._data_received:
                return self._buffer[offset:offset + length]
            elif offset < self._data_received:
                return self._buffer[offset:self._data_received]
        except:
            LOGGER.log(WARNING, "SFTHandle read exception")
        self._buffer = None
        self._data_received = None
        if self._data is not None:
            self._data.truncate(0)
            self._data.seek(0)
            self._data = None
        return ""

    def write(self, offset, data):
        writefile = getattr(self, "writefile", None)
        if writefile is None:
            return SFTP_OP_UNSUPPORTED
        try:
            # in append mode, don't care about seeking
            LOGGER.log(paramiko.common.INFO, "Appending Data to file with Offset {}".
                       format(offset))
            if (self.__flags & os.O_APPEND) == 0:
                if self.__tell is None:
                    self.__tell = writefile.tell()
                if offset != self.__tell:
                    writefile.seek(offset)
                    self.__tell = offset
            writefile.write(data)
            writefile.flush()
        except IOError as e:
            self.__tell = None
            return SFTPServer.convert_errno(e.errno)
        if self.__tell is not None:
            self.__tell += len(data)
        return SFTP_OK

    def close(self):
        LOGGER.log(DEBUG, "SFTPHandleImpl.close called")
        try:
            if self.fpath is not None:
                LOGGER.log(paramiko.common.INFO, "Sending file {} to ftp".
                           format(self.fpath))
                self._ftp.storbinary("STOR " + self.fpath.split("/")[-1],
                                     open(self.fpath, "rb"))
                os.remove(self.fpath)
                self.fpath = None
                self.writefile.close()
                # pdb.set_trace()
        except:
            LOGGER.log(WARNING, "SFTHandle close exception")


class SFTPServerImpl(SFTPServerInterface):

    def __init__(self, *args, **kwargs):
        # On this FTP, we need to set a timeout
        self._ftp = FTP()
        self._clist = []

        LOGGER.log(DEBUG, "SFTPServer.__init__ called")
        for key, value in kwargs.items():
            if key == "ftp":
                self._ftp = value
                # self._clist[:]
                self._ftp.retrlines('LIST', self._clist.append)
            if key == "connection":
                self._transport = value

    def _valid_lpath(self, lpath):
        LOGGER.log(DEBUG, "FTPServer._valid_lpath({}) called".format(lpath))
        if ("total" in lpath or "data connection" in lpath):
            # or lpath.split()[-1].startswith('.')):
            return False
        return True

    def _create_os_stat(self, stmode, filesize, t):
        LOGGER.log(DEBUG, "SFTPServer._create_os_stat called")
        return os.stat_result((
            stmode,  # st_mode
            0,  # st_ino
            0,  # st_dev
            1,  # st_nlink
            1000,  # st_uid
            1000,  # st_gid
            filesize,  # st_size
            t,  # st_atime
            t,  # st_mtime
            t))  # st_ctime

    def list_folder(self, path):
        LOGGER.log(DEBUG, "SFTPServer.list_folder({}) called".format(path))
        try:
            self.stat(path)
            out = []
            flist = []
            self._ftp.retrlines('LIST', flist.append)
            LOGGER.log(DEBUG,
                             "ftp direcotry listing for {}".format(path))
            for fname in flist:
                LOGGER.log(DEBUG, fname)
                if not self._valid_lpath(fname):
                    continue
                attr = SFTPAttributes.from_stat(self.ftp_stat(fname))
                if "->" in fname:
                    attr.filename = fname.split()[-3]
                else:
                    attr.filename = fname.split()[-1]
                out.append(attr)

            return out
        except OSError as e:
            return SFTPServerImpl.convert_errno(e.errno)
        except all_errors as e:
            LOGGER.log(DEBUG, "Ohhh {}".format(e))

    def ftp_stat(self, fname):
        LOGGER.log(DEBUG, "SFTPServer.ftp_stat({}) called".format(fname))
        stmode = 16877
        # pdb.set_trace()
        if "DIR" not in fname and (fname.startswith("-") or fname[0].isdigit()):
            stmode = 33188
        name = fname.split()
        if len(name) == 1:
            t = int(time.time())
            return self._create_os_stat(stmode, 4096, t)
        elif len(name) == 4:
            s = " ".join(name[0:2])
            t = int(time.mktime(datetime.datetime.strptime
                                (s, "%m-%d-%y %I:%M%p").timetuple()))
            return self._create_os_stat(stmode, 4096, t)
        elif len(name) >= 9:
            s = '/'.join(name[5:8])
            if ":" in s:
                server_date = s + "/" + str(datetime.datetime.now().year)
                t = int(time.mktime(datetime.datetime.strptime
                                    (server_date, "%b/%d/%H:%M/%Y").timetuple()))
            else:
                t = int(time.mktime(datetime.datetime.strptime
                                    (s, "%b/%d/%Y").timetuple()))
            return self._create_os_stat(stmode, int(name[4]), t)

    def _find_attr(self, path):
        self._clist.clear()
        self._ftp.retrlines('LIST', self._clist.append)
        p = str(pathlib.Path(path).name)
        result = [fname for fname in self._clist if p in fname]
        LOGGER.log(DEBUG, "SFTPServer._find_attr() path: [{}] result: [{}] len: [{}] any: [{}]".
                         format(path, str(result), len(result), any(p in x for x in self._clist)))
        if any(p in x for x in self._clist) == True and len(result) == 1 and (p == str(result[0]).split()[-1]):
            attr = SFTPAttributes.from_stat(self.ftp_stat(str(result[0])))
            attr.filename = p
            return attr
        else:
            return SFTP_FAILURE

    def stat(self, path):
        LOGGER.log(DEBUG, "SFTPServer.stat({}) called".format(path))
        try:
            # pdb.set_trace()
            try:
                self._ftp.cwd(path)
                self._clist.clear()
                self._ftp.retrlines('LIST', self._clist.append)
                attr = SFTPAttributes.from_stat(self.ftp_stat(path))
                attr.filename = path.split()[-1]
                return attr
            except error_temp as e:
                LOGGER.log(INFO, "SFTPServer.stat({}) backend temporary unavailable".format(path))
                return SFTP_FAILURE
            except all_errors as e:
                try:
                    parent = str(pathlib.Path(path).parent)
                    # pdb.set_trace()
                    self._ftp.cwd(parent)
                    return self._find_attr(path)
                except all_errors as k:
                    LOGGER.log(INFO, "*** ERROR [{}]".format(k))
                    return SFTP_FAILURE
        except EOFError as e:
            LOGGER.log(INFO, "ftp connection timed out, terminating client")
            self._transport.close()
            return SFTP_FAILURE
        except OSError as e:
            LOGGER.log(INFO, "ftp connection cwd failed [{}]".format(e))
            return SFTPServerImpl.convert_errno(e.errno)
        except:
            return SFTP_PERMISSION_DENIED

    def lstat(self, path):
        LOGGER.log(DEBUG, "SFTPServer.lstat({}) called".format(path))
        f = path.split('/');
        try:
            for fname in self._clist:
                if f[-1] in fname:
                    if not self._valid_lpath(fname):
                        continue
                    attr = SFTPAttributes.from_stat(self.ftp_stat(fname))
                    attr.filename = f[-1]
                    return attr
            return SFTPAttributes.from_stat(self.ftp_stat(path))
        except OSError as e:
            return SFTPServerImpl.convert_errno(e.errno)

    def open(self, path, flags, attr):
        LOGGER.log(INFO, "SFTPServer.open({})".format(path))
        # Problem with sftp clients which cannot send realpath signal
        fobj = SFTPHandleImpl(flags)
        fobj.set_ftp(self._ftp)
        fobj.filename = path

        if flags != 0:
            # Issue with temp storage in /tmp - keep the destination filepath and use ftp callback
            fpath = "/tmp/" + path.split('/')[-1]
            fobj.fpath = fpath
            try:
                binary_flag = getattr(os, 'O_BINARY', 0)
                flags |= binary_flag
                mode = getattr(attr, 'st_mode', None)
                # write it locally
                if mode is not None:
                    fd = os.open(fpath, flags, mode)
                else:
                    # os.open() defaults to 0777 which is
                    # an odd default mode for files
                    fd = os.open(fpath, flags, 0o666)
            except OSError as e:
                return SFTPServer.convert_errno(e.errno)
            if (flags & os.O_CREAT) and (attr is not None):
                attr._flags &= ~attr.FLAG_PERMISSIONS
                SFTPServer.set_file_attr(fpath, attr)
            if flags & os.O_WRONLY:
                if flags & os.O_APPEND:
                    fstr = 'ab'
                else:
                    fstr = 'wb'
            elif flags & os.O_RDWR:
                if flags & os.O_APPEND:
                    fstr = 'a+b'
                else:
                    fstr = 'r+b'
            else:
                # O_RDONLY (== 0)
                fstr = 'rb'
            try:
                f = os.fdopen(fd, fstr)
            except OSError as e:
                return SFTPServerImpl.convert_errno(e.errno)
            fobj.writefile = f

        return fobj

    def remove(self, path):
        LOGGER.log(DEBUG, "SFTPServer.remove({}) called".format(path))
        try:
            self._ftp.delete(path)
        except:
            LOGGER.log(INFO, "SFTPServer.remove({}) failed".format(path))
        return SFTP_OK

    def rename(self, oldpath, newpath):
        LOGGER.log(DEBUG, "SFTPServer.rename({}, {}) called".format(oldpath, newpath))
        self._ftp.rename(oldpath, newpath)
        return SFTP_OK

    def mkdir(self, path, attr):
        LOGGER.log(DEBUG, "SFTPServer.mkdir({}) called".format(path))
        self._ftp.mkd(path)
        return SFTP_OK

    def rmdir(self, path):
        LOGGER.log(DEBUG, "SFTPServer.rmdir({}) called".format(path))
        self._ftp.rmd(path)
        return SFTP_OK

    def chattr(self, path, attr):
        LOGGER.log(DEBUG, "SFTPServer.chattr called [{}]".format(path))
        return SFTP_OK
