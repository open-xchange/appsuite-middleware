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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.ajp13.AJPv13Server;
import com.openexchange.ajp13.Services;
import com.openexchange.ajp13.servlet.OXServletException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.server.Initialization;

/**
 * {@link HttpManagersInit} - {@link Initialization} for HTTP servlet management.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HttpManagersInit implements Initialization {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(HttpManagersInit.class));

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

    @Override
    public void start() throws OXException {
        AJPv13Server ajpServer;
        while (null == (ajpServer = AJPv13Server.getInstance()) || !ajpServer.isRunning()) {
            // Do nothing
        }
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

    @Override
    public void stop() throws OXException {
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

    private void initServletMappings(final boolean readFromFile) throws OXException {
        try {
            final Map<String, Constructor<?>> servletConstructorMap;
            if (readFromFile) {
                final File dir = Services.getService(ConfigurationService.class).getDirectory("servletmappings");
                if (null == dir || !dir.exists()) {
                    throw OXServletException.Code.DIR_NOT_EXISTS.create("servletmappings");
                } else if (!dir.isDirectory()) {
                    throw OXServletException.Code.NO_DIRECTORY.create("servletmappings");
                }
                final File[] propFiles = dir.listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(final File dir, final String name) {
                        return toLowerCase(name).endsWith(".properties");

                    }
                });
                servletConstructorMap = new HashMap<String, Constructor<?>>();
                for (int i = 0; i < propFiles.length; i++) {
                    /*
                     * Read properties from file
                     */
                    final Map<Object, Object> properties = getPropertiesFromFile(propFiles[i]);
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
            throw OXServletException.Code.SERVLET_MAPPINGS_NOT_LOADED.create(exc, exc.getMessage());
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
        return new com.openexchange.java.StringAllocator(servletPath.length() + 1).append('/').append(servletPath).toString();
    }

    private static Map<Object, Object> getPropertiesFromFile(final File f) throws IOException {
        final FileInputStream fis = new FileInputStream(f);
        try {
            final Properties properties = new Properties();
            properties.load(fis);
            return properties;
        } finally {
            Streams.close(fis);
        }
    }

    private final static Class<?>[] CLASS_ARR = new Class[] {};

    private static void addServletClass(final String name, final String className, final Map<String, Constructor<?>> servletConstructorMap) {
        try {
            if (!checkServletPath(name)) {
                LOG.error(new com.openexchange.java.StringAllocator("Invalid servlet path: ").append(name).toString());
                return;
            }
            if (servletConstructorMap.containsKey(name)) {
                final boolean isEqual = servletConstructorMap.get(name).toString().indexOf(className) != -1;
                if (!isEqual && LOG.isWarnEnabled()) {
                    final OXException e = OXServletException.Code.ALREADY_PRESENT.create(name, servletConstructorMap.get(name), className);
                    LOG.warn(e.getMessage(), e);
                }
            } else {
                servletConstructorMap.put(name, Class.forName(className).getConstructor(CLASS_ARR));
            }
        } catch (final SecurityException e) {
            if (LOG.isWarnEnabled()) {
                final OXException se = OXServletException.Code.SECURITY_ERR.create(e, className);
                LOG.warn(se.getMessage(), se);
            }
        } catch (final ClassNotFoundException e) {
            if (LOG.isWarnEnabled()) {
                final OXException se = OXServletException.Code.CLASS_NOT_FOUND.create(e, className);
                LOG.warn(se.getMessage(), se);
            }
        } catch (final NoSuchMethodException e) {
            if (LOG.isWarnEnabled()) {
                final OXException se = OXServletException.Code.NO_DEFAULT_CONSTRUCTOR.create(e, className);
                LOG.warn(se.getMessage(), se);
            }
        }
    }

    private static final Pattern PATTERN_SERVLET_PATH = Pattern.compile("([\\p{ASCII}&&[^\\p{Blank}]]+)\\*?");

    private static boolean checkServletPath(final String servletPath) {
        return PATTERN_SERVLET_PATH.matcher(servletPath).matches();
    }
}
