// Generated code from Butter Knife. Do not modify!
package com.github.gotify.login;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.github.gotify.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class AdvancedDialog$ViewHolder_ViewBinding implements Unbinder {
  private AdvancedDialog.ViewHolder target;

  @UiThread
  public AdvancedDialog$ViewHolder_ViewBinding(AdvancedDialog.ViewHolder target, View source) {
    this.target = target;

    target.disableSSL = Utils.findRequiredViewAsType(source, R.id.disableSSL, "field 'disableSSL'", CheckBox.class);
    target.toggleCaCert = Utils.findRequiredViewAsType(source, R.id.toggle_ca_cert, "field 'toggleCaCert'", Button.class);
    target.selectedCaCertificate = Utils.findRequiredViewAsType(source, R.id.seleceted_ca_cert, "field 'selectedCaCertificate'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    AdvancedDialog.ViewHolder target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.disableSSL = null;
    target.toggleCaCert = null;
    target.selectedCaCertificate = null;
  }
}
