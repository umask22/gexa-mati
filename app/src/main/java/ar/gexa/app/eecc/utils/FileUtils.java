package ar.gexa.app.eecc.utils;

import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ar.gexa.app.eecc.services.NotificationService;

public class FileUtils {

    private static final int BUFFER = 2048;

    public static void write(File source, File dest) throws Exception {
        write(new FileInputStream(source), new FileOutputStream(dest));
    }

    public static void write(InputStream inputStream, OutputStream outputStream) throws Exception {
        try {
            byte[] buffer = new byte[BUFFER];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            if(inputStream != null)
                inputStream.close();
            if(outputStream != null)
                outputStream.close();
        }
    }

    public static void writeJSONToFile(String json, String pathTo) throws Exception {
        final Writer outputStream = new BufferedWriter(new FileWriter(new File(pathTo)));
        outputStream.write(json);
        outputStream.close();
    }

    public static void zip(String fileToName, String ...filesToCompress) throws Exception {

        InputStream inputStream = null;
        final ZipOutputStream outputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory() + "/gexa/" + fileToName + ".zip")));
        try {
            byte data[] = new byte[BUFFER];

            for (String file : filesToCompress) {
                File f = new File(file);
                if(file.contains(".3gp") && !f.exists()){
                    NotificationService.getInstance().txtLog("File Utils\n"+"No esta grabando el audio de la llamada");
                    inputStream.close();
                }else{
                    inputStream = new BufferedInputStream(new FileInputStream(file), BUFFER);
                    ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                    outputStream.putNextEntry(entry);
                    int count;
                    while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
                        outputStream.write(data, 0, count);
                    }
                    inputStream.close();
                }
            }
        }finally {
            if(inputStream != null)
                inputStream.close();
            outputStream.finish();
            outputStream.close();
        }
    }

    public static boolean createDirectory(String path) {
        boolean isDirectoryCreated = true;
        final File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs())
                isDirectoryCreated = false;
        }
        return isDirectoryCreated;
    }

    public static boolean delete(String fileName) {
        final File fileToDelete = new File(fileName);
        return fileToDelete.delete();
    }
}
