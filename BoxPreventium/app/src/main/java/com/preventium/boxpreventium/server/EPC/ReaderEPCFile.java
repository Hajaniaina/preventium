package com.preventium.boxpreventium.server.EPC;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.utils.BytesUtils;
import com.preventium.boxpreventium.utils.ComonUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

public class ReaderEPCFile {
    private static final String TAG = "ReaderEPCFile";
    private boolean lat_long = false;
    private ForceSeuil[] seuil = new ForceSeuil[20];

    private String EPC_name1 = "";
    private String EPC_name2 = "";
    private String EPC_name3 = "";
    private String EPC_name4 = "";
    private String EPC_name5 = "";

    public ReaderEPCFile(){clear();}

    private void clear(){
        EPC_name1 = "";
        EPC_name2 = "";
        EPC_name3 = "";
        EPC_name4 = "";
        EPC_name5 = "";
    }


    public String getEPCFileName(Context ctx, int i, boolean acknowledge) {
        if (acknowledge) {
            return String.format(Locale.getDefault(), ComonUtils.getIMEInumber(ctx) + "_%d_ok.EPC", new Object[]{Integer.valueOf(i)});
        }
        return String.format(Locale.getDefault(), ComonUtils.getIMEInumber(ctx) + "_%d.EPC", new Object[]{Integer.valueOf(i)});
    }

    public String getNameFileName(Context ctx) {
        //if( acknowledge )return String.format(Locale.getDefault(), ComonUtils.getIMEInumber(ctx) + "_%d_ok.EPC", i);
        return String.format(Locale.getDefault(), ComonUtils.getIMEInumber(ctx) + "_EPC.NAME");
    }




    public String getEPCFilePath(Context ctx, int i) {
        String fileName = getEPCFileName(ctx, i, false);
        return String.format(Locale.getDefault(), "%s/%s", new Object[]{ctx.getFilesDir(), fileName});
    }

    public boolean read(Context ctx, int i) {
        return read(getEPCFilePath(ctx, i));
    }

    public boolean read(String filename) {
        FileInputStream fileInputStream;
        FileNotFoundException e;
        boolean ret = false;
        try {
            FileInputStream in = new FileInputStream(filename);
            try {
                byte[] data = new byte[122];
                if (in.read(data) == 122) {
                    int i = 0;
                    for (int s = 0; s < this.seuil.length; s++) {
                        this.seuil[s] = new ForceSeuil();
                        int i2 = i + 1;
                        this.seuil[s].IDAlert = (short) data[i];
                        i = i2 + 1;
                        this.seuil[s].TPS = (short) data[i2];
                        i2 = i + 1;
                        i = i2 + 1;
                        this.seuil[s].mG_low = (double) ((data[i] & 255) | ((data[i2] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK));
                        i2 = i + 1;
                        i = i2 + 1;
                        this.seuil[s].mG_high = (double) ((data[i] & 255) | ((data[i2] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK));
                        switch (s) {
                            case 0:
                            case 5:
                            case 10:
                            case 15:
                                this.seuil[s].level = LEVEL_t.LEVEL_1;
                                break;
                            case 1:
                            case 6:
                            case 11:
                            case 16:
                                this.seuil[s].level = LEVEL_t.LEVEL_2;
                                break;
                            case 2:
                            case 7:
                            case 12:
                            case 17:
                                this.seuil[s].level = LEVEL_t.LEVEL_3;
                                break;
                            case 3:
                            case 8:
                            case 13:
                            case 18:
                                this.seuil[s].level = LEVEL_t.LEVEL_4;
                                break;
                            case 4:
                            case 9:
                            case 14:
                            case 19:
                                this.seuil[s].level = LEVEL_t.LEVEL_5;
                                break;
                            default:
                                this.seuil[s].level = LEVEL_t.LEVEL_UNKNOW;
                                break;
                        }
                        switch (s) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                                this.seuil[s].type = FORCE_t.ACCELERATION;
                                break;
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                                this.seuil[s].type = FORCE_t.BRAKING;
                                break;
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                                this.seuil[s].type = FORCE_t.TURN_RIGHT;
                                break;
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                                this.seuil[s].type = FORCE_t.TURN_LEFT;
                                break;
                            default:
                                this.seuil[s].type = FORCE_t.UNKNOW;
                                break;
                        }
                    }
                    this.lat_long = data[i] != (byte) 0;
                    ret = true;
                }
                fileInputStream = in;
            } catch (FileNotFoundException e3) {
                e = e3;
                fileInputStream = in;
                e.printStackTrace();
                return ret;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e4) {
            e = e4;
            e.printStackTrace();
            return ret;
        }
        return ret;
    }



    public boolean readname( String filename ) {
        boolean ret = false;
        try {
            FileInputStream in = new FileInputStream(filename);
            int total_size = in.available();
            byte[] data = new byte[total_size];
            if( in.read( data ) == total_size ){
                String txt = BytesUtils.dataToString(data);
                String[] split = txt.split(",");
                if( split.length == 5 ) {
                    int i = 0;
                    EPC_name1 = split[i++];
                    EPC_name2 = split[i++];
                    EPC_name3 = split[i++];
                    EPC_name4 = split[i++];
                    EPC_name5 = split[i++];

                    ret = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }



    public int[] get_all_alertID() {
        int[] ret = new int[20];
        for (int i = 0; i < 20; i++) {
            ret[i] = this.seuil[i].IDAlert;
        }
        return ret;
    }

    public short get_TPS(int index) {
        return (index < 0 || index >= this.seuil.length) ? (short) -1 : this.seuil[index].TPS;
    }

    public long get_TPS_ms(int index) {
        return (index < 0 || index >= this.seuil.length) ? -1 : (long) (this.seuil[index].TPS * 1000);
    }

    public ForceSeuil getForceSeuil(int index) {
        return (index < 0 || index >= this.seuil.length) ? null : this.seuil[index];
    }

    public ForceSeuil getForceSeuil(double XmG, double YmG) {
        return ComonUtils.interval(0.0d, XmG) >= ComonUtils.interval(0.0d, YmG) ? getForceSeuilForX(XmG) : getForceSeuilForY(YmG);
    }

    public ForceSeuil getForceSeuilForX(double XmG) {
        int s;
        if (XmG >= 0.0d) {
            s = 0;
            while (s < 5) {
                if (XmG >= this.seuil[s].mG_low && XmG <= this.seuil[s].mG_high) {
                    return this.seuil[s];
                }
                s++;
            }
            return null;
        }
        s = 5;
        while (s < 10) {
            if ((-XmG) >= this.seuil[s].mG_low && (-XmG) <= this.seuil[s].mG_high) {
                return this.seuil[s];
            }
            s++;
        }
        return null;
    }

    public ForceSeuil getForceSeuilForY(double YmG) {
        int s;
        if (YmG >= 0.0d) {
            s = 10;
            while (s < 15) {
                if (YmG >= this.seuil[s].mG_low && YmG <= this.seuil[s].mG_high) {
                    return this.seuil[s];
                }
                s++;
            }
            return null;
        }
        s = 15;
        while (s < 20) {
            if ((-YmG) >= this.seuil[s].mG_low && (-YmG) <= this.seuil[s].mG_high) {
                return this.seuil[s];
            }
            s++;
        }
        return null;
    }

    public ForceSeuil getForceSeuilByID(short IDAlert) {
        for (int i = 0; i < 20; i++) {
            if (this.seuil[i].IDAlert == IDAlert) {
                return this.seuil[i];
            }
        }
        return null;
    }

    public void print() {
        for (ForceSeuil aSeuil : this.seuil) {
            Log.d(TAG, aSeuil.toString());
        }
        Log.d(TAG, "lat/long enable: " + this.lat_long);
    }

    public int selectedEPC(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(ctx.getResources().getString(R.string.epc_selected_key), ctx.getResources().getInteger(R.integer.epc_selected_def_key));
    }

    public boolean loadFromApp(Context ctx) {
        return loadFromApp(ctx, PreferenceManager.getDefaultSharedPreferences(ctx).getInt(ctx.getResources().getString(R.string.epc_selected_key), ctx.getResources().getInteger(R.integer.epc_selected_def_key)));
    }

    public boolean loadFromApp(Context ctx, int epc) {
        if (!DataEPC.preferenceFileExist(ctx, epc)) {
            return false;
        }
        for (int i = 0; i < this.seuil.length; i++) {
            this.seuil[i] = DataEPC.getEPCLine(ctx, epc, i);
        }
        this.lat_long = DataEPC.getLatLong(ctx, epc);
        return true;
    }

    public boolean applyToApp(Context ctx, int epc) {
        if (epc < 1 || epc > 5) {
            return false;
        }
        for (int i = 0; i < this.seuil.length; i++) {
            DataEPC.setEPCLine(ctx, epc, i, this.seuil[i]);
        }
        DataEPC.setLatLong(ctx, epc, this.lat_long);
        return true;
    }




    public boolean loadNameFromApp( Context ctx) {
        clear();
        boolean ret = false;
        EPC_name1 = NameEPC.get_EPC_Name(ctx,1);
        EPC_name2 = NameEPC.get_EPC_Name(ctx,2);
        EPC_name3 = NameEPC.get_EPC_Name(ctx,3);
        EPC_name4 = NameEPC.get_EPC_Name(ctx,4);
        EPC_name5 = NameEPC.get_EPC_Name(ctx,5);
        ret = true;
        return ret;
    }


    public void applyNameToApp( Context ctx ) {
        NameEPC.set_EPC_Name(ctx, EPC_name1, 1);
        NameEPC.set_EPC_Name(ctx, EPC_name2, 2);
        NameEPC.set_EPC_Name(ctx, EPC_name3, 3);
        NameEPC.set_EPC_Name(ctx, EPC_name4, 4);
        NameEPC.set_EPC_Name(ctx, EPC_name5, 5);
    }


}
