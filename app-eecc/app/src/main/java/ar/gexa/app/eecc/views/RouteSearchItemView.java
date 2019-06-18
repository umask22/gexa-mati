package ar.gexa.app.eecc.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.utils.AndroidUtils;
import common.models.resources.ActivityResource;

public class RouteSearchItemView extends RecyclerView.ViewHolder {

    public final View view;
    public final TextView visitDescriptionView;
    public final TextView accountNameView;
    public final TextView visitScheduleDate;
    public final TextView visitAddressView;

    public ActivityResource resource;

    public RouteSearchItemView(View view) {

        super(view);
        this.view = view;
        visitDescriptionView = AndroidUtils.findViewById(R.id.visitDescriptionView, TextView.class, this.view);
        accountNameView = AndroidUtils.findViewById(R.id.accountNameView, TextView.class, this.view);
        visitScheduleDate = AndroidUtils.findViewById(R.id.visitScheduleDateView, TextView.class, this.view);
        visitAddressView = AndroidUtils.findViewById(R.id.visitAddressView, TextView.class, this.view);
    }
}