package ru.esmukov.kpfu.lightningrodandroid;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kostya on 03/03/2017.
 */


// http://stackoverflow.com/questions/4447477/how-to-copy-files-from-assets-folder-to-sdcard
// http://stackoverflow.com/questions/19218775/android-copy-assets-to-internal-storage
// http://stackoverflow.com/questions/22903540/android-copy-files-from-assets-to-data-data-folder

// https://developer.android.com/guide/topics/resources/providing-resources.html#ResourceTypes


public class NodeAssetsManager {
    private final String JS_PATH = "js";
    private final String TERMUX_PATH = "termux";
    private final String SH_PATH = "sh";
    // Files in this list will not be overwritten if exist.
    private final Set<String> PRESERVE_FILES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "js/drivers.json",
            "js/plugins.json",
            "js/settings.json"
    )));

    private static boolean mExtracted = false;

    private Context mContext;
    private byte[] mBuffer = new byte[32000];

    public NodeAssetsManager(Context context) {
        this.mContext = context;
    }

    public void extractAll() {
        if (mExtracted)
            return;

        this.extractJs();
        this.extractSh();
        this.extractTermux();
        mExtracted = true;
    }

    public String getTermuxPath() {
        return mContext.getFilesDir().getPath() + "/" + TERMUX_PATH;
    }

    public String getJsPath() {
        return mContext.getFilesDir().getPath() + "/" + JS_PATH;
    }

    private void extractJs() {
        extractAssetTree(JS_PATH, JS_PATH);
        //extractAssetToFile(JS_PATH, JS_PATH, "index.js");
    }

    private void extractSh() {
        extractAssetToFile(SH_PATH, SH_PATH, "wsst", true);
    }

    private void extractTermux() {
        // todo detect platform
        // http://stackoverflow.com/questions/11989629/api-call-to-get-processor-architecture
        //
        // Should match platform notation used by termux.
        // See /data/data/com.termux/files/usr/etc/apt/sources.list
        String platform = "arm";

        extractAssetTree(TERMUX_PATH + "/" + platform, TERMUX_PATH);
    }

    private void extractAssetToFile(String frompath, String topath, String filename) {
        extractAssetToFile(frompath, topath, filename, false);
    }

    private void extractAssetToFile(String frompath, String topath, String filename,
                                    boolean setExecutable) {
        // mkdir -p
        new File(mContext.getFilesDir(), topath).mkdirs();

        File file = new File(mContext.getFilesDir(), topath + "/" + filename);

        try {
            if (PRESERVE_FILES.contains(topath + "/" + filename)
                    && file.exists())
                return;

            InputStream is = mContext.getAssets().open(frompath + "/" + filename);
            OutputStream os = new FileOutputStream(file);
            pipe(is, os);
            is.close();
            os.close();

            if (setExecutable) {
                if (!file.setExecutable(true)) {
                    throw new IOException("Unable to set +x bit on file: " + topath + "/" + filename);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // todo ??
        }
    }

    private void pipe(InputStream is, OutputStream os) throws IOException {
        while (true) {
            int read = is.read(mBuffer);
            if (read <= 0)
                break;
            os.write(mBuffer, 0, read);
        }
    }

    private boolean isBin(String dir_path) {
        // todo custom shell scripts like our socat wrapper??

        return dir_path.endsWith("bin");
    }

    private void extractAssetTree(String from, String to) {
        try {
            String[] paths = mContext.getAssets().list(from);

            for (String path : paths) {
                // is directory
                if (mContext.getAssets().list(from + "/" + path).length > 0) {
                    extractAssetTree(from + "/" + path, to + "/" + path);
                } else {
                    extractAssetToFile(from, to, path, isBin(to));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e); // todo ??
        }
    }
}
