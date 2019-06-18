package ar.gexa.app.eecc.bo;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Date;

import ar.gexa.app.eecc.utils.FileUtils;
import ar.gexa.app.eecc.views.CallSuccess;

public class PhoneCallBO {

    private static PhoneCallBO instance;

    public static PhoneCallBO getInstance() {
        if (instance == null) {
            synchronized (PhoneCallBO.class) {
                if (instance == null)
                    instance = new PhoneCallBO();
            }
        }
        return instance;
    }

    private Listener listener;

    private String number;
    private Date start;
    private Date finish;

    public String callIn_callOut;

    private boolean isCallFromGexa = false;


    public interface Listener {
        void onOutgoingCallEnded();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void onIncomingCallReceived(Context context, String number, Date start) {}

    public void onIncomingCallAnswered(Context context, String number, Date start) {}

    public void onIncomingCallEnded(final Context context, String number, Date start, Date end) {

        this.number = number;
        this.start = start;
        this.finish = end;
        this.callIn_callOut = "callIn";

        final Intent intent = new Intent(context, CallSuccess.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void onOutgoingCallStarted(Context context, String number, Date start) {}

    public void onOutgoingCallEnded(final Context context, String number, Date start, Date end) {

        this.number = number;
        this.start = start;
        this.finish = end;
        this.callIn_callOut= "callOut";

        if(isCallFromGexa()) {
            if(listener != null) {
                PhoneCallBO.getInstance().copyFileFromACR(context,"last");
                listener.onOutgoingCallEnded();
            }
        }else{
            final Intent intent = new Intent(context, CallSuccess.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void onMissedCall(Context context, String number, Date start) {

//        ActionResource action = new ActionResource();
//        action.contact = new ContactResource();
//        action.account = new AccountResource();
//        action.activity = new ActivityResource();
//
//        final Contact resource;
//        try {
//            resource = ContactRepository.getInstance().findByNumber(number);
//            if(resource != null && resource.isGexa) {
//                action.user = UserRepository.getInstance().find().getUsername();
//                action.creationDate = DateUtils.toString(new Date(),DateUtils.Pattern.DEFAULT);
//                action.contact.id = resource.getCode();
//                action.contact.description = resource.getDescription();
//                action.contact.phone = resource.getPhone();
//                action.contact.accountName = resource.getAccountName();
//                action.account.cuit = resource.getAccountCuit();
//                action.activity.stateType = CallConstants.StateType.COMPLETED.name();
//                action.activity.type = "Call";
//
//                ActivityService.getInstance().onMissedCall(action, new Callback<ResponseBody>(context) {
//                    @Override
//                    public void onSuccess(ResponseBody responseBody) { }
//                });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    public void onCallSuccess(String fileName) {
        final File from = new File(Environment.getExternalStorageDirectory() + "/gexa/calls/last.3gp");
        from.renameTo(new File(Environment.getExternalStorageDirectory() + "/gexa/calls/" + fileName + ".3gp"));
    }

    public void onCallUnsuccess() {
        final File file = new File(Environment.getExternalStorageDirectory() + "/gexa/calls/last.3gp");
        file.delete();
    }

    public void copyFileFromACR(Context context, String fileName){
        final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "ACRCalls");

        File file = null;
        if(dir.listFiles() != null)
            file = dir.listFiles()[dir.listFiles().length - 1];

        if(file != null) {
            if (file.isDirectory()) {
                deleteRecursive(file);
                copyFileFromACR(context,"last");
            }else {
                try {
                    FileUtils.write(file, new File(Environment.getExternalStorageDirectory() + "/gexa/calls/" + fileName + ".3gp"));
                } catch (Exception e) {
                    Log.e(PhoneCallBO.class.getSimpleName(), e.getMessage(), e);
                }
            }
        }
    }

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    public String getNumber() {
        return number;
    }

    public Date getStart() {
        return start;
    }

    public Date getFinish() {
        return finish;
    }

    public boolean isCallFromGexa() {
        return isCallFromGexa;
    }

    public void setCallFromGexa(boolean callFromGexa) {
        isCallFromGexa = callFromGexa;
    }
}
