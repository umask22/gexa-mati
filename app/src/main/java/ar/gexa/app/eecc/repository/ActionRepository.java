package ar.gexa.app.eecc.repository;


public class ActionRepository {

    private static volatile ActionRepository instance;

    public static ActionRepository get() {
        if (instance == null) {
            synchronized (ActionRepository.class) {
                if (instance == null)
                    instance = new ActionRepository();
            }
        }
        return instance;
    }
}
