// Generated code from Butter Knife. Do not modify!
package com.github.gotify.login;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.github.gotify.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LoginActivity_ViewBinding implements Unbinder {
  private LoginActivity target;

  private View view7f09009e;

  @UiThread
  public LoginActivity_ViewBinding(LoginActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public LoginActivity_ViewBinding(final LoginActivity target, View source) {
    this.target = target;

    View view;
    target.usernameField = Utils.findRequiredViewAsType(source, R.id.username, "field 'usernameField'", EditText.class);
    target.urlField = Utils.findRequiredViewAsType(source, R.id.gotify_url, "field 'urlField'", EditText.class);
    target.passwordField = Utils.findRequiredViewAsType(source, R.id.password, "field 'passwordField'", EditText.class);
    target.toggleAdvanced = Utils.findRequiredViewAsType(source, R.id.advanced_settings, "field 'toggleAdvanced'", ImageView.class);
    target.checkUrlButton = Utils.findRequiredViewAsType(source, R.id.checkurl, "field 'checkUrlButton'", Button.class);
    target.loginButton = Utils.findRequiredViewAsType(source, R.id.login, "field 'loginButton'", Button.class);
    target.checkUrlProgress = Utils.findRequiredViewAsType(source, R.id.checkurl_progress, "field 'checkUrlProgress'", ProgressBar.class);
    target.loginProgress = Utils.findRequiredViewAsType(source, R.id.login_progress, "field 'loginProgress'", ProgressBar.class);
    view = Utils.findRequiredView(source, R.id.open_logs, "method 'openLogs'");
    view7f09009e = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.openLogs();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    LoginActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.usernameField = null;
    target.urlField = null;
    target.passwordField = null;
    target.toggleAdvanced = null;
    target.checkUrlButton = null;
    target.loginButton = null;
    target.checkUrlProgress = null;
    target.loginProgress = null;

    view7f09009e.setOnClickListener(null);
    view7f09009e = null;
  }
}
