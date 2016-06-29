package com.ikalogic.franck.permissions.listener.single;

import com.ikalogic.franck.permissions.PermissionToken;
import com.ikalogic.franck.permissions.listener.PermissionDeniedResponse;
import com.ikalogic.franck.permissions.listener.PermissionGrantedResponse;
import com.ikalogic.franck.permissions.listener.PermissionRequest;
import java.util.Arrays;
import java.util.Collection;

/**
 * Listener that composes multiple listeners into one
 * All inner listeners will be called for a given event unless one of them throws an exception or
 * is blocked
 */
public class CompositePermissionListener implements PermissionListener {

  private final Collection<PermissionListener> listeners;

  /**
   * Creates a {@link CompositePermissionListener} containing all the provided listeners.
   * This constructor does not guaranty any calling order on inner listeners.
   */
  public CompositePermissionListener(PermissionListener... listeners) {
    this(Arrays.asList(listeners));
  }

  /**
   * Creates a {@link CompositePermissionListener} containing all the provided listeners.
   * This constructor will guaranty that inner listeners are called following the iterator order
   * of the collection.
   */
  public CompositePermissionListener(Collection<PermissionListener> listeners) {
    this.listeners = listeners;
  }

  @Override public void onPermissionGranted(PermissionGrantedResponse response) {
    for (PermissionListener listener : listeners) {
      listener.onPermissionGranted(response);
    }
  }

  @Override public void onPermissionDenied(PermissionDeniedResponse response) {
    for (PermissionListener listener : listeners) {
      listener.onPermissionDenied(response);
    }
  }

  @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
      PermissionToken token) {
    for (PermissionListener listener : listeners) {
      listener.onPermissionRationaleShouldBeShown(permission, token);
    }
  }
}
