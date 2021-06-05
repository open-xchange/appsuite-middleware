/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */


package com.openexchange.i18n.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleDiscoverer extends FileDiscoverer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResourceBundleDiscoverer.class);

    public ResourceBundleDiscoverer(final File dir) throws FileNotFoundException {
        super(dir);
    }

    public List<ResourceBundle> getResourceBundles() throws java.util.MissingResourceException {
        String[] files = getFilesFromLanguageFolder(".jar");
        if (files.length == 0) {
            return Collections.emptyList();
        }

        List<ResourceBundle> list = new ArrayList<ResourceBundle>(files.length);
        for (String file : files) {
            try {
                Locale l = getLocale(file);

                URLClassLoader ul = AccessController.doPrivileged(new PrivilegedExceptionAction<URLClassLoader>() {

                    @Override
                    public URLClassLoader run() throws MalformedURLException {
                        return new URLClassLoader(new URL[] { new URL("file:" + getDirectory() + File.separator + file) });
                    }
                });
                ResourceBundle rc = ResourceBundle.getBundle("com.openexchange.groupware.i18n.ServerMessages", l, ul);

                list.add(rc);

            } catch (PrivilegedActionException e) {
                Exception exception = e.getException();
                if (exception instanceof MalformedURLException) {
                    LOG.error("Cannot load file: {}", file);
                } else {
                    LOG.error("Not permitted to access file: {}", file, exception);
                }
            } catch (java.util.MissingResourceException mr) {
                LOG.error("Unable to init Language Bundle! This file seems to be broken: {}", file);
                throw mr;
            } catch (RuntimeException e) {
                LOG.error("Runtime error while loading file: {}", file, e);
            }
        }
        return list;
    }
}
