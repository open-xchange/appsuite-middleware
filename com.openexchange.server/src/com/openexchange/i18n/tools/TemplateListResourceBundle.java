/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
            } catch (final IOException e) {
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
        } catch (final IOException x) {
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
