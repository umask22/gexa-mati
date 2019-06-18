package ar.gexa.app.eecc.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.Activity;
import ar.gexa.app.eecc.utils.AndroidUtils;

public class SynchronizationItemView extends RecyclerView.ViewHolder {

    public final View view;
    public final TextView activityDescriptionView;
    public final TextView accountNameView;
    public final TextView activityScheduleDateView;
    public final TextView activityTypeView;
    public final TextView syncStateView;
    public final TextView stateTypeView;

    public Activity activity;

    public SynchronizationItemView(View view) {

        super(view);

        this.view = view;
        activityDescriptionView = AndroidUtils.findViewById(R.id.activityDescriptionView, TextView.class, this.view);
        accountNameView = AndroidUtils.findViewById(R.id.accountNameView, TextView.class, this.view);
        activityScheduleDateView = AndroidUtils.findViewById(R.id.activityScheduleDate, TextView.class, this.view);
        activityTypeView = AndroidUtils.findViewById(R.id.activityTypeView, TextView.class, this.view);
        syncStateView = AndroidUtils.findViewById(R.id.syncStateView, TextView.class, this.view);
        stateTypeView = AndroidUtils.findViewById(R.id.stateTypeView, TextView.class, this.view);
    }
}