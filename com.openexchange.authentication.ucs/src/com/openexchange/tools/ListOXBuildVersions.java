/**
 *
 */
package com.openexchange.tools;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author choeger
 *
 */
public class ListOXBuildVersions {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        Queue<Closeable> closeables = new LinkedList<Closeable>();
        try {
            int adminVersion = 0;
            int serverVersion = 0;

            URL manifestURL = new URL("jar:file:/opt/open-xchange/bundles/open_xchange_admin.jar!/META-INF/MANIFEST.MF");
            Attributes attrs = new Manifest(rememberCloseable(manifestURL.openStream(), closeables)).getMainAttributes();
            adminVersion = Integer.parseInt(attrs.getValue("Build"));

            manifestURL = new URL("jar:file:/opt/open-xchange/bundles/com.openexchange.server.jar!/META-INF/MANIFEST.MF");
            attrs = new Manifest(rememberCloseable(manifestURL.openStream(), closeables)).getMainAttributes();
            serverVersion = Integer.parseInt(attrs.getValue("Build"));

            BufferedReader guireader = rememberCloseable(new BufferedReader(new FileReader("/var/www/ox6/concat_init.js")), closeables);
            String line = null;
            int guiVersion = -1;
            do {
                line = guireader.readLine();
                if( line != null && line.contains("build:") ) {
                    guiVersion = Integer.parseInt(line.split("\\\"")[1]);
                }
            } while( guiVersion < 0 && line != null);

            System.out.println("Open-Xchange GUI: " + guiVersion);
            System.out.println("Open-Xchange Groupware: " + serverVersion);
            System.out.println("Open-Xchange Admindaemon: " + adminVersion);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            for (Closeable closeable; (closeable = closeables.poll()) != null;) {
                try { closeable.close(); } catch (IOException e) {/* Ignore */}
            }
        }
    }

    private static <T extends Closeable> T rememberCloseable(T closeable, Queue<Closeable> closeables) {
        closeables.offer(closeable);
        return closeable;
    }

}
