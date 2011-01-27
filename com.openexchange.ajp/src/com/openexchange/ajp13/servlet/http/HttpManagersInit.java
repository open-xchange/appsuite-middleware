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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajp13.servlet.http;

import static com.openexchange.i18n.LocaleTools.toLowerCase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.servlet.OXServletException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;

/**
 * {@link HttpManagersInit} - {@link Initialization} for HTTP servlet management.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HttpManagersInit implements Initialization {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(HttpManagersInit.class);

    private static final HttpManagersInit instance = new HttpManagersInit();

    private final AtomicBoolean started = new AtomicBoolean();

    /**
     * No instantiation
     */
    private HttpManagersInit() {
        super();
    }

    /**
     * Gets the singleton instance of {@link HttpManagersInit}
     * 
     * @return
     */
    public static HttpManagersInit getInstance() {
        return instance;
    }

    public void start() throws AbstractOXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error(this.getClass().getName() + " already started");
            return;
        }
        initServletMappings(false);
        HttpSessionManagement.init();
        if (LOG.isInfoEnabled()) {
            LOG.info("HTTP servlet manager successfully initialized");
        }
    }

    public void stop() throws AbstractOXException {
        if (!started.compareAndSet(true, false)) {
            LOG.error(this.getClass().getName() + " cannot be stopped since it has not been started before");
            return;
        }
        HttpSessionManagement.reset();
        HttpServletManager.shutdownHttpServletManager();
        if (LOG.isInfoEnabled()) {
            LOG.info("HTTP servlet manager successfully stopped");
        }
    }

    private void initServletMappings(final boolean readFromFile) throws OXServletException {
        try {
            final Map<String, Constructor<?>> servletConstructorMap;
            if (readFromFile) {
                final String servletMappingDir = AJPv13Config.getSystemProperty(SystemConfig.Property.ServletMappingDir);
                if (servletMappingDir == null) {
                    throw new OXServletException(
                        OXServletException.Code.MISSING_SERVLET_DIR,
                        SystemConfig.Property.ServletMappingDir.getPropertyName());
                }
                final File dir = new File(servletMappingDir);
                if (!dir.exists()) {
                    throw new OXServletException(OXServletException.Code.DIR_NOT_EXISTS, servletMappingDir);
                } else if (!dir.isDirectory()) {
                    throw new OXServletException(OXServletException.Code.NO_DIRECTORY, servletMappingDir);
                }
                final File[] propFiles = dir.listFiles(new FilenameFilter() {

                    public boolean accept(final File dir, final String name) {
                        return toLowerCase(name).endsWith(".properties");

                    }
                });
                servletConstructorMap = new HashMap<String, Constructor<?>>();
                for (int i = 0; i < propFiles.length; i++) {
                    /*
                     * Read properties from file
                     */
                    final Properties properties = getPropertiesFromFile(propFiles[i]);
                    /*
                     * Initialize servlets' default constructors
                     */
                    final int size = properties.size();
                    final Iterator<Entry<Object, Object>> iterator = properties.entrySet().iterator();
                    for (int k = 0; k < size; k++) {
                        final Entry<Object, Object> entry = iterator.next();
                        addServletClass(
                            prepareServletPath(entry.getKey().toString().trim()),
                            entry.getValue().toString().trim(),
                            servletConstructorMap);
                    }
                }
            } else {
                servletConstructorMap = Collections.emptyMap();
            }
            /*
             * Initialize HTTP servlet manager
             */
            HttpServletManager.initHttpServletManager(servletConstructorMap, false);
        } catch (final IOException exc) {
            throw new OXServletException(OXServletException.Code.SERVLET_MAPPINGS_NOT_LOADED, exc, exc.getMessage());
        }
    }

    /**
     * Ensures that servlet path starts with "/" character.
     * 
     * @param servletPath The servlet path
     * @return The prepared servlet path
     */
    private static String prepareServletPath(final String servletPath) {
        if ('/' == servletPath.charAt(0)) {
            return servletPath;
        }
        return new StringBuilder(servletPath.length() + 1).append('/').append(servletPath).toString();
    }

    private static Properties getPropertiesFromFile(final File f) throws IOException {
        final FileInputStream fis = new FileInputStream(f);
        try {
            final Properties properties = new Properties();
            properties.load(fis);
            return properties;
        } finally {
            try {
                fis.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private final static Class<?>[] CLASS_ARR = new Class[] {};

    private static void addServletClass(final String name, final String className, final Map<String, Constructor<?>> servletConstructorMap) {
        try {
            if (!checkServletPath(name)) {
                LOG.error(new StringBuilder("Invalid servlet path: ").append(name).toString());
                return;
            }
            if (servletConstructorMap.containsKey(name)) {
                final boolean isEqual = servletConstructorMap.get(name).toString().indexOf(className) != -1;
                if (!isEqual && LOG.isWarnEnabled()) {
                    final OXServletException e =
                        new OXServletException(OXServletException.Code.ALREADY_PRESENT, name, servletConstructorMap.get(name), className);
                    LOG.warn(e.getMessage(), e);
                }
            } else {
                servletConstructorMap.put(name, Class.forName(className).getConstructor(CLASS_ARR));
            }
        } catch (final SecurityException e) {
            if (LOG.isWarnEnabled()) {
                final OXServletException se = new OXServletException(OXServletException.Code.SECURITY_ERR, e, className);
                LOG.warn(se.getMessage(), se);
            }
        } catch (final ClassNotFoundException e) {
            if (LOG.isWarnEnabled()) {
                final OXServletException se = new OXServletException(OXServletException.Code.CLASS_NOT_FOUND, e, className);
                LOG.warn(se.getMessage(), se);
            }
        } catch (final NoSuchMethodException e) {
            if (LOG.isWarnEnabled()) {
                final OXServletException se = new OXServletException(OXServletException.Code.NO_DEFAULT_CONSTRUCTOR, e, className);
                LOG.warn(se.getMessage(), se);
            }
        }
    }

    private static final Pattern PATTERN_SERVLET_PATH = Pattern.compile("([\\p{ASCII}&&[^\\p{Blank}]]+)\\*?");

    private static boolean checkServletPath(final String servletPath) {
        return PATTERN_SERVLET_PATH.matcher(servletPath).matches();
    }
}
