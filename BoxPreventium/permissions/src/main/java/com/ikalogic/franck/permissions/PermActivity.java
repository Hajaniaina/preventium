package com.ikalogic.franck.permissions;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import java.util.Collection;
import java.util.LinkedList;

public final class PermActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Perm.onActivityReady(this);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Perm.onActivityReady(this);
  }

  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    Collection<String> grantedPermissions = new LinkedList<>();
    Collection<String> deniedPermissions = new LinkedList<>();

    for (int i = 0; i < permissions.length; i++) {
      String permission = permissions[i];
      if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
        deniedPermissions.add(permission);
      } else {
        grantedPermissions.add(permission);
      }
    }

    Perm.onPermissionsRequested(grantedPermissions, deniedPermissions);
  }
}
