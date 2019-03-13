// Generated code from Butter Knife. Do not modify!
package com.github.gotify.messages;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.github.gotify.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ListMessageAdapter$ViewHolder_ViewBinding implements Unbinder {
  private ListMessageAdapter.ViewHolder target;

  @UiThread
  public ListMessageAdapter$ViewHolder_ViewBinding(ListMessageAdapter.ViewHolder target,
      View source) {
    this.target = target;

    target.image = Utils.findRequiredViewAsType(source, R.id.message_image, "field 'image'", ImageView.class);
    target.message = Utils.findRequiredViewAsType(source, R.id.message_text, "field 'message'", TextView.class);
    target.title = Utils.findRequiredViewAsType(source, R.id.message_title, "field 'title'", TextView.class);
    target.date = Utils.findRequiredViewAsType(source, R.id.message_date, "field 'date'", TextView.class);
    target.delete = Utils.findRequiredViewAsType(source, R.id.message_delete, "field 'delete'", ImageButton.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ListMessageAdapter.ViewHolder target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.image = null;
    target.message = null;
    target.title = null;
    target.date = null;
    target.delete = null;
  }
}
