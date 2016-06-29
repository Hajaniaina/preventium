package com.ikalogic.franck.permissions.listener.single;

import com.ikalogic.franck.permissions.PermissionToken;
import com.ikalogic.franck.permissions.listener.PermissionDeniedResponse;
import com.ikalogic.franck.permissions.listener.PermissionGrantedResponse;
import com.ikalogic.franck.permissions.listener.PermissionRequest;

/**
 * Interface that listens to updates to the permission requests
 */
public interface PermissionListener {

  /**
   * Method called whenever a requested permission has been granted
   *
   * @param response A response object that contains the permission that has been requested and
   * any additional flags relevant to this response
   */
  void onPermissionGranted(PermissionGrantedResponse response);

  /**
   * Method called whenever a requested permission has been denied
   *
   * @param response A response object that contains the permission that has been requested and
   * any additional flags relevant to this response
   */
  void onPermissionDenied(PermissionDeniedResponse response);

  /**
   * Method called whenever Android asks the application to inform the user of the need for the
   * requested permission. The request process won't continue until the token is properly used
   *
   * @param permission The permission that has been requested
   * @param token Token used to continue or cancel the permission request process. The permission
   * request process will remain blocked until one of the token methods is called
   */
  void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token);
}
