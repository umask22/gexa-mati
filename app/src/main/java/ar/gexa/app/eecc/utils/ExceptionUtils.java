package ar.gexa.app.eecc.utils;

public class ExceptionUtils {

    public static String getTitle(Throwable throwable) {
        if("java.net.ConnectException".equals(throwable.getClass().getName()))
            return "Error de conexion";
        return "Error";
    }
}
