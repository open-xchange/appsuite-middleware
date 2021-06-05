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

package com.openexchange.i18n.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.java.Streams;
import com.openexchange.tools.Collections;

public abstract class TemplateListResourceBundle extends ResourceBundle {

    private static File templatePath = new File("templates");

    public static final void setTemplatePath(final File path) {
        templatePath = path;
    }

    protected static final Map<String, Template> templates = new HashMap<String, Template>();

    protected static final Properties properties = new Properties();

    protected static final List<String> keys = new ArrayList<String>();

    private static final Lock INIT_LOCK = new ReentrantLock();

    protected static volatile boolean initialized;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TemplateListResourceBundle.class);

    @Override
    protected Object handleGetObject(final String arg0) {
        if (!initialized) {
            init();
        }
        return (templates.containsKey(arg0)) ? templates.get(arg0) : properties.getProperty(arg0);
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.iter2enum(keys.iterator());
    }

    protected void init() {
        INIT_LOCK.lock();
        try {
            if (initialized) {
                return;
            }
            initialized = true;
            InputStream is = null;
            try {
                is = getPropertyStream();
                properties.load(is);
                is.close();
                is = null;

                final File[] templateFiles = templatePath.listFiles(new StartsWithFilter(uniqueName()));
                if (null != templateFiles) {
                    for (final File template : templateFiles) {
                        parseTemplate(template);
                    }
                }
            } catch (IOException e) {
                LOG.error("", e);
            } finally {
                Streams.close(is);
            }

            for (final Object key : properties.keySet()) {
                keys.add((String) key);
            }

            for (final String key : templates.keySet()) {
                if (!keys.contains(key)) {
                    keys.add(key);
                }
            }
        } finally {
            INIT_LOCK.unlock();
        }
    }

    protected void parseTemplate(final File template) {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(template));

            String key = null;
            final StringBuilder templateText = new StringBuilder();

            String line = null;
            while ((line = r.readLine()) != null) {
                if (key == null) {
                    key = line.trim();
                } else {
                    templateText.append(line);
                    templateText.append('\n');
                }
            }
            templates.put(key, new StringTemplate(templateText.toString()));
        } catch (IOException x) {
            LOG.error(x.toString());
        } finally {
            Streams.close(r);
        }
    }

    protected InputStream getPropertyStream() throws IOException {
        return stream(uniqueName() + ".properties");
    }

    protected final InputStream stream(final String fileName) throws IOException {
        return new BufferedInputStream(new FileInputStream(new File(templatePath, fileName)), 65536);
    }

    protected String uniqueName() {
        return getClass().getName();
    }

    private static final class StartsWithFilter implements FilenameFilter {

        private final String name;

        public StartsWithFilter(final String name) {
            this.name = name;
        }

        @Override
        public boolean accept(final File arg0, final String arg1) {
            return arg1.startsWith(name);
        }

    }

}
