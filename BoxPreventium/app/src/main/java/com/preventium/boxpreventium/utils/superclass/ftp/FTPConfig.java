package com.preventium.boxpreventium.utils.superclass.ftp;

import org.apache.commons.net.ftp.FTP;

/**
 * Created by Franck on 21/09/2016.
 */

public class FTPConfig {

    private String hostname;
    private String username;
    private String password;
    private int portnum;
    private String workDirectory = "";

    public FTPConfig(){
        this.hostname = "";
        this.username = "";
        this.password = "";
        this.portnum = FTP.DEFAULT_PORT;
        this.workDirectory = "";
    }

    public FTPConfig(String hostname, String username, String password){
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.portnum = FTP.DEFAULT_PORT;
        this.workDirectory = "";
    }

    public FTPConfig(String hostname, String username, String password, int portnum){
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.portnum = portnum;
        this.workDirectory = "";
    }

    public FTPConfig(String hostname, String username, String password, int portnum, String workDirectory){
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.portnum = portnum;
        this.workDirectory = workDirectory;
    }

    // Setters
    public void setFtpServer(String hostname) { this.hostname = hostname; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setPort(int portnum) { this.portnum = portnum; }
    public void setWorkDirectory(String workDirectory) {
        if( workDirectory.equals("/") )
            this.workDirectory = "";
        else
            this.workDirectory = workDirectory;
    }

    // Getters
    public String getFtpServer() { return hostname; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Integer getPort() { return portnum; }
    public String getWorkDirectory() {
        return workDirectory;
    }

}
