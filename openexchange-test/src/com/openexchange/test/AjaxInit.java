package com.openexchange.test;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

public class AjaxInit {
    private static boolean ajaxPropertiesLoaded = false;
    private static boolean isAjaxDirInitialized = false;
    private static String[] ajaxPropFiles;
    public static Properties ajaxProps = null;

    private static void loadAJAXProperties() {
        TestInit.loadTestProperties();
        ajaxProps = new Properties();
        try {
            ajaxProps.load(new FileInputStream(getFileName()));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        ajaxPropertiesLoaded = true;
    }

    private static String getFileName() {

        String retval = null;
        if (TestInit.getTestProperties().getProperty("ajaxPropertiesDir") != null) {
            if (!isAjaxDirInitialized) {
                synchronized (AjaxInit.class) {
                    if (!isAjaxDirInitialized) {
                        initAjaxProperties();
                    }
                }
            }
            retval = ajaxPropFiles[(int) (Math.random() * ajaxPropFiles.length)];
        } else {
            retval = TestInit.getTestProperties().getProperty("ajaxPropertiesFile");
        }

        return retval;
    }

    private static void initAjaxProperties() {
        // Welches Verzeichnis soll gelesen werden?
        String ajaxPropertiesDir = TestInit.getTestProperties().getProperty("ajaxPropertiesDir");
        // ist der Pfad mit abschliessendem "/" ? Wenn nicht, packe den dazu:
        if (!ajaxPropertiesDir.endsWith(System.getProperty("file.separator"))) {
            ajaxPropertiesDir = new StringBuilder().append(ajaxPropertiesDir)
                    .append(System.getProperty("file.separator")).toString();
        }
        File dir = new File(ajaxPropertiesDir);
        File myFile;
        // Lese das Verzeichnis und packe alle Files in das Array ajaxPropFiles
        // Aber nur dann, wenn es ein File ist (kein Directory) und es auch
        // gelesen werden kann.
        if (dir.isDirectory()) {
            // Hilfsliste:
            List<String> fileList = new ArrayList<String>();
            // Pruefe jeden im Verzeichnis vorhandenen Namen:
            for (String fileName : dir.list()) {
                myFile = new File(new StringBuilder().append(ajaxPropertiesDir)
                        .append(fileName).toString());
                if (!myFile.isDirectory() && myFile.canRead()
                        && !myFile.getName().startsWith(".")) {
                    fileList.add(myFile.getAbsolutePath());
                }
            }
            // Umladen:
            ajaxPropFiles = new String[fileList.size()];
            System.arraycopy(fileList.toArray(), 0, ajaxPropFiles, 0, fileList
                    .size());
        }

        isAjaxDirInitialized = true;
    }

    public static Properties getAJAXProperties() {

        if (!ajaxPropertiesLoaded
                || TestInit.getTestProperties().getProperty("ajaxPropertiesDir") != null) {
            loadAJAXProperties();
        }
        return ajaxProps;
    }

    public static String getAJAXProperty(final String key) {
        return getAJAXProperties().getProperty(key);
    }
}
