package com.ikalogic.franck.permissions.listener.single;

import com.ikalogic.franck.permissions.PermissionToken;
import com.ikalogic.franck.permissions.listener.PermissionDeniedResponse;
import com.ikalogic.franck.permissions.listener.PermissionGrantedResponse;
import com.ikalogic.franck.permissions.listener.PermissionRequest;

/**
 * Empty implementation of {@link PermissionListener} to allow extensions to implement only the
 * required methods
 */
public class EmptyPermissionListener implements PermissionListener {

  @Override public void onPermissionGranted(PermissionGrantedResponse response) {

  }

  @Override public void onPermissionDenied(PermissionDeniedResponse response) {

  }

  @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
      PermissionToken token) {

  }
}
