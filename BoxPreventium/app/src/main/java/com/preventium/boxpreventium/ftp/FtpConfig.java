package com.preventium.boxpreventium.ftp;

import org.apache.commons.net.ftp.FTP;

/**
 * Created by Franck on 31/05/2016.
 */

public class FtpConfig {

    private String hostname;
    private String username;
    private String password;
    private int portnum;

    public FtpConfig(){
        this.hostname = "";
        this.username = "";
        this.password = "";
        this.portnum = FTP.DEFAULT_PORT;
    }

    public FtpConfig(String hostname, String username, String password){
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.portnum = FTP.DEFAULT_PORT;
    }

    public FtpConfig(String hostname, String username, String password, int portnum){
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.portnum = portnum;
    }

    // Setters
    public void setFtpServer(String hostname) { this.hostname = hostname; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setPort(int portnum) { this.portnum = portnum; }

    // Getters
    public String getFtpServer() { return hostname; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Integer getPort() { return portnum; }

}
