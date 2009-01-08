
package com.openexchange.i18n.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourceBundleDiscoverer extends FileDiscoverer {

    private static final Log LOG = LogFactory.getLog(ResourceBundleDiscoverer.class);

    public ResourceBundleDiscoverer(final File dir) throws FileNotFoundException {
        super(dir);
    }

    public List<ResourceBundle> getResourceBundles() throws java.util.MissingResourceException {
        final String[] files = getFilesFromLanguageFolder(".jar");
        if (files.length == 0) {
            return Collections.emptyList();
        }
        final List<ResourceBundle> list = new ArrayList<ResourceBundle>(files.length);
        for (final String file : files) {
            Locale l = null;

            try {
                l = getLocale(file);

                final URLClassLoader ul = new URLClassLoader(new URL[] { new URL("file:" + getDirectory() + File.separator + file) });
                final ResourceBundle rc = ResourceBundle.getBundle("com.openexchange.groupware.i18n.ServerMessages", l, ul);

                list.add(rc);

            } catch (final java.util.MissingResourceException mr) {
                LOG.error("Unable to init Language Bundle! This file seems to be broken: " + file);
                throw mr;
            } catch (final MalformedURLException e) {
                LOG.error("Cannot load file: " + file);
            }
        }
        return list;
    }
}
