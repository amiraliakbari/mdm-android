// Generated code from Butter Knife. Do not modify!
package com.github.gotify.messages;

import android.view.View;
import android.widget.ListView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.github.gotify.R;
import com.google.android.material.navigation.NavigationView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MessagesActivity_ViewBinding implements Unbinder {
  private MessagesActivity target;

  @UiThread
  public MessagesActivity_ViewBinding(MessagesActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MessagesActivity_ViewBinding(MessagesActivity target, View source) {
    this.target = target;

    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolbar, "field 'toolbar'", Toolbar.class);
    target.drawer = Utils.findRequiredViewAsType(source, R.id.drawer_layout, "field 'drawer'", DrawerLayout.class);
    target.navigationView = Utils.findRequiredViewAsType(source, R.id.nav_view, "field 'navigationView'", NavigationView.class);
    target.messagesView = Utils.findRequiredViewAsType(source, R.id.messages_view, "field 'messagesView'", ListView.class);
    target.swipeRefreshLayout = Utils.findRequiredViewAsType(source, R.id.swipe_refresh, "field 'swipeRefreshLayout'", SwipeRefreshLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MessagesActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
    target.drawer = null;
    target.navigationView = null;
    target.messagesView = null;
    target.swipeRefreshLayout = null;
  }
}
