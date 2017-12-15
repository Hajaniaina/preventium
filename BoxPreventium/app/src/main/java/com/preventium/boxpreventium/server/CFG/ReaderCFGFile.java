package com.preventium.boxpreventium.server.CFG;

import android.content.Context;
import android.util.Log;

import com.preventium.boxpreventium.utils.BytesUtils;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Franck on 23/09/2016.
 */

public class ReaderCFGFile {

    private String FTP = "";
    private String FTP_Login = "";
    private String FTP_pwd = "";
    private int FTP_Port = 21;
    private String FTP_Path = "";
    private String SMS_CALL_1 = "";
    private String SMS_CALL_2 = "";
    private String SMS_CALL_3 = "";
    private String SMS_CALL_4 = "";
    private String SMS_CALL_5 = "";
    private String SERVER_URL = "";
    private boolean reception_trajet_en_temps_reel = false;
    private boolean envoi_de_tous_les_points_gps = false;

    public ReaderCFGFile(){ clear(); }

    private void clear(){
        FTP = "";
        FTP_Login = "";
        FTP_pwd = "";
        FTP_Port = 21;
        FTP_Path = "";
        SMS_CALL_1 = "";
        SMS_CALL_2 = "";
        SMS_CALL_3 = "";
        SMS_CALL_4 = "";
        SMS_CALL_5 = "";
        reception_trajet_en_temps_reel = false;
        envoi_de_tous_les_points_gps = false;
        SERVER_URL = "";
    }

    public boolean read( String filename ) {
        boolean ret = false;
        try {
            FileInputStream in = new FileInputStream(filename);
            int total_size = in.available();
            byte[] data = new byte[total_size];
            if( in.read( data ) == total_size ){
                String txt = BytesUtils.dataToString(data);
                String[] split = txt.split(",");

                Log.e("taille f : ", String.valueOf(split));

                if( split.length == 18 ) {
                    int i = 5;
                    FTP = split[i++];
                    FTP_Login = split[i++];
                    FTP_pwd = split[i++];
                    FTP_Port = Integer.parseInt( split[i++] );
                    FTP_Path = split[i++];
                    SMS_CALL_1 = split[i++];
                    SMS_CALL_2 = split[i++];
                    SMS_CALL_3 = split[i++];
                    SMS_CALL_4 = split[i++];
                    SMS_CALL_5 = split[i++];
                    reception_trajet_en_temps_reel = split[i++].equals("1");
                    envoi_de_tous_les_points_gps = split[i++].equals("1");
                    SERVER_URL = split[i];
                    ret = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String getServerUrl(){

        return FTP;

    }

    public boolean loadFromApp( Context ctx ) {
        boolean ret = false;
        clear();
        FTPConfig config = DataCFG.getFptConfig(ctx);
        if( config != null ) {
            FTP = config.getFtpServer();
            FTP_Login = config.getUsername();
            FTP_pwd = config.getPassword();
            FTP_Port = config.getPort();
            FTP_Path = config.getWorkDirectory();
            SMS_CALL_1 = DataCFG.get_SMS_CALL(ctx,1);
            SMS_CALL_2 = DataCFG.get_SMS_CALL(ctx,1);
            SMS_CALL_3 = DataCFG.get_SMS_CALL(ctx,1);
            SMS_CALL_4 = DataCFG.get_SMS_CALL(ctx,1);
            SMS_CALL_5 = DataCFG.get_SMS_CALL(ctx,1);
            reception_trajet_en_temps_reel = DataCFG.get_SEND_IN_REAL_TIME(ctx);
            envoi_de_tous_les_points_gps = DataCFG.get_SEND_ALL_GPS_POINTS(ctx);
            ret = true;
        }
        return ret;
    }

    public void applyToApp( Context ctx ) {
        FTPConfig config = new FTPConfig(FTP,FTP_Login,FTP_pwd, FTP_Port,FTP_Path);
        DataCFG.setFTPConfig(ctx, config);
        DataCFG.set_SMS_CALL(ctx,1,SMS_CALL_1);
        DataCFG.set_SMS_CALL(ctx,2,SMS_CALL_2);
        DataCFG.set_SMS_CALL(ctx,3,SMS_CALL_3);
        DataCFG.set_SMS_CALL(ctx,4,SMS_CALL_4);
        DataCFG.set_SMS_CALL(ctx,5,SMS_CALL_5);
        DataCFG.set_SEND_IN_REAL_TIME(ctx,reception_trajet_en_temps_reel);
        DataCFG.set_SEND_ALL_GPS_POINTS(ctx,envoi_de_tous_les_points_gps);
        DataCFG.set_prefs_to_UI(ctx);
    }

    public FTPConfig getFptConfig(){ return new FTPConfig(FTP,FTP_Login,FTP_pwd, FTP_Port,FTP_Path); }

    public String get_SMS_CALL(int i){
        switch( i ) {
            case 1: return SMS_CALL_1;
            case 2: return SMS_CALL_2;
            case 3: return SMS_CALL_3;
            case 4: return SMS_CALL_4;
            case 5: return SMS_CALL_5;
        }
        return "";
    }

    public boolean get_SEND_IN_REAL_TIME(Context ctx){ return reception_trajet_en_temps_reel; }

    public boolean get_SEND_ALL_GPS_POINTS(){ return  envoi_de_tous_les_points_gps; }
}
