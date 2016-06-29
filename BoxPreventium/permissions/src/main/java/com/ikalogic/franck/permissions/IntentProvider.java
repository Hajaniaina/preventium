package com.ikalogic.franck.permissions;

import android.content.Context;
import android.content.Intent;

class IntentProvider {
  public Intent get(Context context, Class<?> clazz) {
    return new Intent(context, clazz);
  }
}
