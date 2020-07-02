use ssh2::Sftp;
use std::io::prelude::*;
use std::net::TcpStream;
use std::path::Path;
use ssh2::Session;

pub struct SftpClient {
    sess: Session;
    tcp: TcpStream;
}

impl SftpClient {
    pub fn connect(&mut self, host String, port i32, username String, password String) {
        tcp = TcpStream::connect("127.0.0.1:22").unwrap();
        sess = Session::new().unwrap();
        sess.set_tcp_stream(tcp);
        sess.handshake().unwrap();

        sess.userauth_password(username, password).unwrap();
        assert!(sess.authenticated());
    }
}

fn main() {
    println!("Hello, world!");
}
