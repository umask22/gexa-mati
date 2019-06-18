package ar.gexa.app.eecc.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.utils.AndroidUtils;
import common.models.resources.UserResource;

public class UserSearchItemView extends RecyclerView.ViewHolder  {

    public final View view;
    public final TextView nameView;
    public final TextView supervisorNameView;
    public final TextView roleNameView;
    public UserResource user;

    public UserSearchItemView(View view) {
        super(view);
        this.view = view;
        nameView = AndroidUtils.findViewById(R.id.nameView, TextView.class, this.view);
        roleNameView = AndroidUtils.findViewById(R.id.roleNameView, TextView.class, this.view);
        supervisorNameView = AndroidUtils.findViewById(R.id.supervisorNameView, TextView.class, this.view);
    }
}
