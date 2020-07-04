

fn main() {

    let mut sftp = sftp_client::SftpClient::new();
    sftp.connect("host".to_string(), 22, "user".to_string(), "password".to_string());
}
