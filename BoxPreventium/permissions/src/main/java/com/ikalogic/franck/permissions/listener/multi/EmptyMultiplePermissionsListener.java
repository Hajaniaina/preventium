package com.ikalogic.franck.permissions.listener.multi;

import com.ikalogic.franck.permissions.MultiplePermissionsReport;
import com.ikalogic.franck.permissions.PermissionToken;
import com.ikalogic.franck.permissions.listener.PermissionRequest;
import java.util.List;

/**
 * Empty implementation of {@link MultiplePermissionsListener} to allow extensions to implement
 * only the required methods
 */
public class EmptyMultiplePermissionsListener implements MultiplePermissionsListener {

  @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

  }

  @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
      PermissionToken token) {

  }
}
