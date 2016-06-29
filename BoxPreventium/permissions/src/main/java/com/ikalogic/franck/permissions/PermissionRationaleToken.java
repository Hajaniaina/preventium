package com.ikalogic.franck.permissions;

final class PermissionRationaleToken implements PermissionToken {

  private final PermInstance permInstance;
  private boolean isTokenResolved = false;

  public PermissionRationaleToken(PermInstance permInstance) {
    this.permInstance = permInstance;
  }

  @Override public void continuePermissionRequest() {
    if (!isTokenResolved) {
      permInstance.onContinuePermissionRequest();
      isTokenResolved = true;
    }
  }

  @Override public void cancelPermissionRequest() {
    if (!isTokenResolved) {
      permInstance.onCancelPermissionRequest();
      isTokenResolved = true;
    }
  }
}
