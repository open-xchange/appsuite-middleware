
package com.openexchange.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JMXInit {

    private static boolean jmxPropertiesLoaded;

    private static boolean isJMXDirInitialized;

    private static String[] jmxPropFiles;

    public static Properties jmxProps;

    private static void loadJMXProperties() {
        TestInit.loadTestProperties();
        jmxProps = new Properties();
        try {
            jmxProps.load(new FileInputStream(getFileName()));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        jmxPropertiesLoaded = true;
    }

    private static String getFileName() {

        String retval = null;
        if (TestInit.getTestProperties().getProperty("jmxPropertiesDir") != null) {
            if (!isJMXDirInitialized) {
                synchronized (JMXInit.class) {
                    if (!isJMXDirInitialized) {
                        initJMXProperties();
                    }
                }
            }
            retval = jmxPropFiles[(int) (Math.random() * jmxPropFiles.length)];
        } else {
            retval = TestInit.getTestProperties().getProperty("jmxPropertiesFile");
        }

        return retval;
    }

    private static void initJMXProperties() {
        // Welches Verzeichnis soll gelesen werden?
        String jmxPropertiesDir = TestInit.getTestProperties().getProperty("jmxPropertiesDir");
        // ist der Pfad mit abschliessendem "/" ? Wenn nicht, packe den dazu:
        if (!jmxPropertiesDir.endsWith(System.getProperty("file.separator"))) {
            jmxPropertiesDir = new StringBuilder().append(jmxPropertiesDir).append(System.getProperty("file.separator")).toString();
        }
        final File dir = new File(jmxPropertiesDir);
        File myFile;
        // Lese das Verzeichnis und packe alle Files in das Array ajaxPropFiles
        // Aber nur dann, wenn es ein File ist (kein Directory) und es auch
        // gelesen werden kann.
        if (dir.isDirectory()) {
            // Hilfsliste:
            final List<String> fileList = new ArrayList<String>();
            // Pruefe jeden im Verzeichnis vorhandenen Namen:
            for (final String fileName : dir.list()) {
                myFile = new File(new StringBuilder().append(jmxPropertiesDir).append(fileName).toString());
                if (!myFile.isDirectory() && myFile.canRead() && (myFile.getName().length() == 0 || myFile.getName().charAt(0) != '.')) {
                    fileList.add(myFile.getAbsolutePath());
                }
            }
            // Umladen:
            jmxPropFiles = new String[fileList.size()];
            System.arraycopy(fileList.toArray(), 0, jmxPropFiles, 0, fileList.size());
        }

        isJMXDirInitialized = true;
    }

    public static Properties getJMXProperties() {

        if (!jmxPropertiesLoaded || TestInit.getTestProperties().getProperty("jmxPropertiesDir") != null) {
            loadJMXProperties();
        }
        return jmxProps;
    }

    public static String getJMXProperty(final String key) {
        return getJMXProperties().getProperty(key);
    }

    public static String getJMXProperty(final Property property) {
        return getJMXProperties().getProperty(property.toString());
    }

    public static enum Property {
        JMX_HOST("jmxHost"), JMX_PORT("jmxPort"), JMX_LOGIN("jmxLogin"), JMX_PASSWORD("jmxPassword");

        private final String name;

        private Property(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
