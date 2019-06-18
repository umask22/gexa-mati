package ar.gexa.app.eecc.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import ar.gexa.app.eecc.models.User;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.utils.AndroidUtils;

public class FCMTokenService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        final String token = FirebaseInstanceId.getInstance().getToken();
        AndroidUtils.saveValueToPreferences("token", token, this);

        final User user = UserRepository.getInstance().find();
        if(user != null) {
            user.setDeviceToken(AndroidUtils.getValueToPreferences("token", this));
            UserRepository.getInstance().update(user);
        }
    }
}
