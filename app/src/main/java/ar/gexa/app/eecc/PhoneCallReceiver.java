package ar.gexa.app.eecc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import java.util.Date;

import ar.gexa.app.eecc.bo.PhoneCallBO;

public class PhoneCallReceiver extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;

    protected void onIncomingCallReceived(Context context, String number, Date start) {
        PhoneCallBO.getInstance().onIncomingCallReceived(context, number, start);
    }

    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        PhoneCallBO.getInstance().onIncomingCallAnswered(context, number, start);
    }

    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        PhoneCallBO.getInstance().onIncomingCallEnded(context, number, start, end);
    }

    protected void onOutgoingCallStarted(Context context, String number, Date start) {
        PhoneCallBO.getInstance().onOutgoingCallStarted(context, number, start);
    }

    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
        PhoneCallBO.getInstance().onOutgoingCallEnded(context, number, start, end);
    }

    protected void onMissedCall(Context context, String number, Date start) {
        PhoneCallBO.getInstance().onMissedCall(context, number, start);
    }



    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getExtras() != null) {

            if ("android.intent.action.NEW_OUTGOING_CALL".equals(intent.getAction()))
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");

            else{
                String callState = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                int state = 0;
                if(TelephonyManager.EXTRA_STATE_IDLE.equals(callState)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(callState))
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                else if(TelephonyManager.EXTRA_STATE_RINGING.equals(callState))
                    state = TelephonyManager.CALL_STATE_RINGING;

                onCallStateChanged(context, state, number);
            }
        }
    }
    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {

            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                }
                else if(isIncoming){
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());

                } else{
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;

            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallReceived(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                else
                {
                    isIncoming = true;
                    callStartTime = new Date();
                    onIncomingCallAnswered(context, savedNumber, callStartTime);
                }

                break;

        }
        lastState = state;
    }
}
