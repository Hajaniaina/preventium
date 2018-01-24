package com.preventium.boxpreventium.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.drive.DriveFile;
import com.preventium.boxpreventium.gui.MainActivity;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context ctx, Intent intent) {
        Intent i = new Intent(ctx, MainActivity.class);
        i.addFlags(DriveFile.MODE_READ_ONLY);
        ctx.startActivity(i);
    }
}
