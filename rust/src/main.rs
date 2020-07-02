//use ssh2::Sftp;
//use std::io::prelude::*;
use std::io;
use std::net::TcpStream;
//use std::path::Path;
use ssh2::Session;

pub struct SftpClient {
    sess: Session
    //tcp: io::Result<TcpStream>
}

impl SftpClient {
    pub fn new() -> SftpClient {
        SftpClient {
            sess: Session::new().unwrap()
            //tcp: io::Result
        }
    }

    pub fn connect(&mut self, host: String, port: i32, username: String, password: String) {
        let tcp = TcpStream::connect("").unwrap();
        //self.sess = Session::new().unwrap();
        self.sess.set_tcp_stream(tcp);
        self.sess.handshake().unwrap();

        self.sess.userauth_password(&username, &password).unwrap();
        assert!(self.sess.authenticated());
    }
}

fn main() {
    //println!("Hello, world!");
    //let mut sftp = SftpClient::new();
    //sftp.connect("some-server", 22, "user", "secret_pw");
    let tcp = TcpStream::connect("host:22").unwrap();
    let mut sess = Session::new().unwrap();
    sess.set_tcp_stream(tcp);
    sess.handshake().unwrap();

    sess.userauth_password("user","secret_pw").unwrap();
    assert!(sess.authenticated());
}
