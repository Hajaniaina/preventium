package com.preventium.boxpreventium.utils.superclass.ftp;

public class FTPConfig {
    private String hostname;
    private String password;
    private int portnum;
    private String username;
    private String workDirectory;

    public FTPConfig() {
        this.workDirectory = "";
        this.hostname = "";
        this.username = "";
        this.password = "";
        this.portnum = 21;
        this.workDirectory = "";
    }

    public FTPConfig(String hostname, String username, String password) {
        this.workDirectory = "";
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.portnum = 21;
        this.workDirectory = "";
    }

    public FTPConfig(String hostname, String username, String password, int portnum) {
        this.workDirectory = "";
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.portnum = portnum;
        this.workDirectory = "";
    }

    public FTPConfig(String hostname, String username, String password, int portnum, String workDirectory) {
        this.workDirectory = "";
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.portnum = portnum;
        this.workDirectory = workDirectory;
    }

    public void setFtpServer(String hostname) {
        this.hostname = hostname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int portnum) {
        this.portnum = portnum;
    }

    public void setWorkDirectory(String workDirectory) {
        if (workDirectory.equals("/")) {
            this.workDirectory = "";
        } else {
            this.workDirectory = workDirectory;
        }
    }

    public String getFtpServer() {
        return this.hostname;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public Integer getPort() {
        return Integer.valueOf(this.portnum);
    }

    public String getWorkDirectory() {
        return this.workDirectory;
    }
}
