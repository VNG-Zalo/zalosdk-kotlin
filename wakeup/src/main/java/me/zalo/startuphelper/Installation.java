package me.zalo.startuphelper;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

/**
 * Installation
 * Created by khanhtm on 3/14/18.
 */

class Installation {
    private static final String INSTALLATION = "ZALO-STARTUP-HELPER-INSTALLATION";
    private static String sID = null;

    public synchronized static boolean isFirstCall(Context context) {
        boolean firstLaunch = false;
        if (sID == null) {
            File installation = new File(Build.VERSION.SDK_INT >= 21 ? context.getNoBackupFilesDir() : context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists()) {
                    writeInstallationFile(installation);
                    firstLaunch = true;
                }

                try {
                    sID = readInstallationFile(installation);
                } catch (IOException e) {
                    e.printStackTrace();

                    //try one more time
                    if (installation.delete()) {
                        writeInstallationFile(installation);
                        sID = readInstallationFile(installation);
                        firstLaunch = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return firstLaunch;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes, "utf-8");
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes("utf-8"));
        out.close();
    }
}
