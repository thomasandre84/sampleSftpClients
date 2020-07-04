//use ssh2::Sftp;
//use std::io::prelude::*;
use std::io;
use std::net::TcpStream;
//use std::path::Path;
use ssh2::Session;
use std::env;

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
        let con_host = [host, ":".to_string(), port.to_string()].concat();
        let tcp = TcpStream::connect(con_host).unwrap();
        //self.sess = Session::new().unwrap();
        self.sess.set_tcp_stream(tcp);
        self.sess.handshake().unwrap();

        self.sess.userauth_password(&username, &password).unwrap();
        assert!(self.sess.authenticated());
    }
}


pub struct Config {
    pub query: String,
    pub filename: String,
    pub case_sensitive: bool,
}

impl Config {
    pub fn new(args: &[String]) -> Result<Config, &'static str> {
        if args.len() < 3 {
            return Err("not enough arguments");
        }

        let query = args[1].clone();
        let filename = args[2].clone();

        let case_sensitive = env::var("CASE_INSENSITIVE").is_err();

        Ok(Config {
            query,
            filename,
            case_sensitive,
        })
    }
}

