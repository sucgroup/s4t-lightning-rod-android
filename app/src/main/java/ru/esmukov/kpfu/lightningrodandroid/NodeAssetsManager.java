package ru.esmukov.kpfu.lightningrodandroid;

import android.content.Context;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

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
    private final String JS_DIST_TARGZ_PATH = "js_target/dist.tar.gz_"; // see app/build.gradle
    private final String TERMUX_PATH = "termux";
    private final String SH_PATH = "sh";
    // Files in this list will not be overwritten if exist.
    private final Set<String> PRESERVE_FILES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "js/drivers.json",
            "js/plugins.json",
            "js/settings.json"
    )));
    private final Set<DefaultFilePair> DEFAULT_FILES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            new DefaultFilePair("js/settings.android.default.json", "js/settings.json")
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

        this.extractJsDist();
        this.extractJs();
        this.extractSh();
        this.extractTermux();
        this.extractDefaultFiles();

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
    }

    private void extractJsDist() {
        extractTarGz(JS_DIST_TARGZ_PATH, JS_PATH);
    }

    private void extractSh() {
        // todo recursive?
        extractAssetToFile(SH_PATH, SH_PATH, "wsst", "wsst", true);
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

    private void extractDefaultFiles() {
        for (DefaultFilePair defaultFilePair : DEFAULT_FILES) {
            File file = new File(mContext.getFilesDir(), defaultFilePair.to);
            if (file.exists())
                continue;

            extractAssetToFile(
                    defaultFilePair.getFromPath(), defaultFilePair.getToPath(),
                    defaultFilePair.getFromFilename(), defaultFilePair.getToFilename(),
                    defaultFilePair.isBin());
        }
    }

    private void extractTarGz(String tarGzPath, String topath) {
        // Tar unpack example: http://stackoverflow.com/a/7556307

        // Dalvik's faulty GzipInputStream implementation:
        // http://stackoverflow.com/questions/20661312/truncated-output-from-gzipinputstream-on-android
        // http://stackoverflow.com/questions/842896/gzipinputstream-decompression-did-not-work-fine-for-the-compressed-data-with-len
        // https://github.com/ymnk/jzlib/issues/12

        try (TarArchiveInputStream is = (TarArchiveInputStream) new ArchiveStreamFactory()
                .createArchiveInputStream("tar",
                        new GzipCompressorInputStream(
                                mContext.getAssets().open(tarGzPath))
                )) {
            TarArchiveEntry entry;
            while ((entry = is.getNextTarEntry()) != null) {
                final File outputFile = new File(mContext.getFilesDir(),
                        topath + "/" + entry.getName().replaceFirst("^\\./", ""));
                if (entry.isDirectory()) {
                    outputFile.mkdirs(); // mkdir -p
                } else {
                    extractAssetFromInputStreamToFile(is, outputFile,
                            (entry.getMode() & 111) != 0, (int)entry.getSize());
                }
            }
        } catch (IOException | ArchiveException e) {
            throw new RuntimeException(e); // todo ??
        }
    }

    private void extractAssetToFile(String frompath, String topath, String filename) {
        extractAssetToFile(frompath, topath, filename, filename, false);
    }

    private void extractAssetToFile(String frompath, String topath,
                                    String fromfilename,
                                    String tofilename,
                                    boolean setExecutable) {
        // mkdir -p
        new File(mContext.getFilesDir(), topath).mkdirs();

        File file = new File(mContext.getFilesDir(), topath + "/" + tofilename);

        try (InputStream is = mContext.getAssets().open(frompath + "/" + fromfilename)) {
            extractAssetFromInputStreamToFile(is, file, setExecutable);
        } catch (IOException e) {
            throw new RuntimeException(e); // todo ??
        }
    }

    private void extractAssetFromInputStreamToFile(
            InputStream is, File outFile, boolean setExecutable) throws IOException {
        extractAssetFromInputStreamToFile(is, outFile, setExecutable, -1);
    }


    private void extractAssetFromInputStreamToFile(
            InputStream is, File outFile, boolean setExecutable, int readSize) throws IOException {
        if (isPreservedFile(outFile) && outFile.exists())
            return;

        try (OutputStream os = new FileOutputStream(outFile)) {
            pipe(is, os, readSize);
        }

        if (setExecutable) {
            if (!outFile.setExecutable(true)) {
                throw new IOException("Unable to set +x bit on file: " + outFile.getPath());
            }
        }
    }

    private boolean isPreservedFile(File file) {
        for (String preserve : PRESERVE_FILES) {
            if (file.getPath().endsWith(preserve))
                return true;
        }
        return false;
    }

    private void pipe(InputStream is, OutputStream os, int readSize) throws IOException {
        int leftToRead = readSize;
        while (leftToRead > 0 || readSize < 0) {
            int read = is.read(mBuffer, 0,
                    readSize < 0 ? mBuffer.length : Math.min(leftToRead, mBuffer.length));
            if (read <= 0)
                break;
            leftToRead -= read;
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
                    extractAssetToFile(from, to, path, path, isBin(to));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e); // todo ??
        }
    }

    private static class DefaultFilePair {
        public String from;
        public String to;

        public DefaultFilePair(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFromPath() {
            return from.substring(0, from.lastIndexOf("/"));
        }

        public String getFromFilename() {
            return from.substring(from.lastIndexOf("/") + 1);
        }

        public String getToPath() {
            return to.substring(0, to.lastIndexOf("/"));
        }

        public String getToFilename() {
            return to.substring(to.lastIndexOf("/") + 1);
        }

        public boolean isBin() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DefaultFilePair that = (DefaultFilePair) o;

            if (!from.equals(that.from)) return false;
            return to.equals(that.to);

        }

        @Override
        public int hashCode() {
            int result = from.hashCode();
            result = 31 * result + to.hashCode();
            return result;
        }
    }
}
