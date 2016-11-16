package com.preventium.boxpreventium.server.DOBJ;

import android.content.Context;

import com.preventium.boxpreventium.utils.ComonUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

/**
 * Created by Franck on 23/09/2016.
 */

public class ReaderDOBJFile {

    public ReaderDOBJFile(){}

    public static String getOBJFileName(Context ctx, boolean acknowledge ) {
        if( acknowledge )return ComonUtils.getIMEInumber(ctx) + "_ok.DOBJ";
        return ComonUtils.getIMEInumber(ctx) + ".DOBJ";
    }

    public static String getOBJFilePath(Context ctx) {
        String fileName = getOBJFileName(ctx,false);
        return String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), fileName);
    }

    public boolean read( Context ctx, String filename, boolean apply ) {
        boolean ret = false;
        try {
            FileInputStream in = new FileInputStream(filename);
            byte[] data = new byte[43];
            try {
                if( in.read( data ) == 43 ){
                    int i = 0;
                    if( data[i++] == 0x00 ) { // Check version
                        byte[] tmp = new byte[4];
                        float[] coeff_general = new float[3];
                        int[] coeff_green = new int[3];
                        int[] coeff_blue = new int[3];
                        int[] coeff_yellow = new int[3];
                        int[] coeff_orange = new int[3];
                        int[] coeff_red = new int[3];
                        int[] obj_green = new int[3];
                        int[] obj_blue = new int[3];
                        int[] obj_yellow = new int[3];
                        int[] obj_orange = new int[3];
                        int[] obj_red = new int[3];
                        for( int t = 0; t < 3; t++ ){
                            tmp[0] = data[i++];
                            tmp[1] = data[i++];
                            tmp[2] = data[i++];
                            tmp[3] = data[i++];
                            coeff_general[t] = ByteBuffer.wrap(tmp).getFloat();
                            coeff_green[t] = data[i++];
                            coeff_blue[t] = data[i++];
                            coeff_yellow[t] = data[i++];
                            coeff_orange[t] = data[i++];
                            coeff_red[t] = data[i++];
                            obj_green[t] = data[i++];
                            obj_blue[t] = data[i++];
                            obj_yellow[t] = data[i++];
                            obj_orange[t] = data[i++];
                            obj_red[t] = data[i++];
                        }

                        ret = true;
                        for( int t = 0; t < 3; t++ ){
                            if( obj_green[t] + obj_blue[t] + obj_yellow[t]
                                    + obj_orange[t] + obj_red[t] != 100 ){
                                ret = false;
                                break;
                            }
                        }

                        if( ret && apply ){
                            String[] types = new String[]{
                                    DataDOBJ.ACCELERATIONS,
                                    DataDOBJ.FREINAGES,
                                    DataDOBJ.VIRAGES };
                            for( int t = 0; t < types.length; t++ ){
                                DataDOBJ.set_coefficient_general(ctx, types[t], coeff_general[t] );
                                DataDOBJ.set_coefficient(ctx, types[t], DataDOBJ.VERT, coeff_green[t] );
                                DataDOBJ.set_coefficient(ctx, types[t], DataDOBJ.BLEU, coeff_blue[t] );
                                DataDOBJ.set_coefficient(ctx, types[t], DataDOBJ.JAUNE, coeff_yellow[t] );
                                DataDOBJ.set_coefficient(ctx, types[t], DataDOBJ.ORANGE, coeff_orange[t] );
                                DataDOBJ.set_coefficient(ctx, types[t], DataDOBJ.ROUGE, coeff_red[t] );
                                DataDOBJ.set_objectif(ctx, types[t], DataDOBJ.VERT, obj_green[t] );
                                DataDOBJ.set_objectif(ctx, types[t], DataDOBJ.BLEU, obj_blue[t] );
                                DataDOBJ.set_objectif(ctx, types[t], DataDOBJ.JAUNE, obj_yellow[t] );
                                DataDOBJ.set_objectif(ctx, types[t], DataDOBJ.ORANGE, obj_orange[t] );
                                DataDOBJ.set_objectif(ctx, types[t], DataDOBJ.ROUGE, obj_red[t] );
                            }
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }

}
