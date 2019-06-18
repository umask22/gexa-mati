package ar.gexa.app.eecc.views;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.utils.AndroidUtils;

public class MapsFilterView extends DialogFragment {

    interface Listener {
        void onFilter();
    }

    private Listener listener;
    private FragmentManager fragmentManager;
    public SeekBar seekBar;
    private TextView value;
    private int vprogress;

    public static MapsFilterView newInstance(FragmentManager fragmentManager) {
        final MapsFilterView mapsFilterView = new MapsFilterView();
        mapsFilterView.fragmentManager = fragmentManager;
        return mapsFilterView;

    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.maps_filter, null, false);

        seekBar=AndroidUtils.findViewById(R.id.seekBar,SeekBar.class,view);
        value=AndroidUtils.findViewById(R.id.value,TextView.class,view);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.activity_search_filter_filter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                listener.onFilter();
                value.setText("Km: "+seekBar.getProgress()+"/"+ seekBar.getMax());
                Toast.makeText(getContext(), "Value: " + seekBar.getProgress()
                        , Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.activity_search_filter_back, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        value.setText("Km: "+seekBar.getProgress()+"/"+ seekBar.getMax());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                vprogress=i;
                value.setText("Km: "+vprogress+"/"+ seekBar.getMax() );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return builder.create();
    }

    public void doFilter(Listener listener) {
        this.listener = listener;
        show(fragmentManager, "");
    }
}