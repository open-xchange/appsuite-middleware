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

package com.openexchange.ajp13;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;

/**
 * {@link AJPv13Config} - The AJPv13 configuration
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13Config implements Initialization {

    // Final static fields
    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13Config.class));

    private static final String AJP_PROP_FILE_NAME = "ajp.properties";

    private static final AJPv13Config instance = new AJPv13Config();

    public static AJPv13Config getInstance() {
        return instance;
    }

    // fields
    private final AtomicBoolean started = new AtomicBoolean();

    private int serverThreadSize = 20;

    private int listenerPoolSize = 20;

    private int listenerReadTimeout = 60000;

    private int keepAliveTime = 20000;

    private int maxRequestParameterCount = 30;

    private boolean watcherEnabled;

    private boolean watcherPermission;

    private int watcherMaxRunningTime = 300000;

    private int watcherFrequency = 300000;

    private int servletPoolSize = 50;

    private int port = 8009;

    private String jvmRoute;

    private String servletConfigs;

    private InetAddress ajpBindAddr;

    private boolean logForwardRequest;

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error(this.getClass().getName() + " already started");
            return;
        }
        init();
    }

    @Override
    public void stop() throws OXException {
        if (!started.compareAndSet(true, false)) {
            LOG.error(this.getClass().getName() + " cannot be stopped since it has no been started before");
            return;
        }
        reset();
    }

    private void reset() {
        serverThreadSize = 20;
        listenerPoolSize = 20;
        listenerReadTimeout = 60000;
        keepAliveTime = 20000;
        maxRequestParameterCount = 30;
        watcherEnabled = false;
        watcherPermission = false;
        watcherMaxRunningTime = 300000;
        watcherFrequency = 300000;
        servletPoolSize = 50;
        port = 8009;
        jvmRoute = null;
        servletConfigs = null;
        ajpBindAddr = null;
        logForwardRequest = false;
    }

    private void init() throws AJPv13Exception {
        final Properties ajpProperties = new Properties();
        final ConfigurationService configurationService = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (configurationService == null) {
            LOG.warn("Missing configuration service.", new Throwable());
            return;
        }
        try {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(configurationService.getFileByName(AJP_PROP_FILE_NAME));
                ajpProperties.load(fis);
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
            final String falseStr = "false";
            final String trueStr = "true";
            /*
             * AJP_PORT
             */
            port = Integer.parseInt(ajpProperties.getProperty("AJP_PORT", "8009").trim());
            /*
             * AJP_SERVER_THREAD_SIZE
             */
            serverThreadSize = Integer.parseInt(ajpProperties.getProperty("AJP_SERVER_THREAD_SIZE", "20").trim());
            if (serverThreadSize < 0) {
                /*
                 * At least one server thread should accept opened sockets
                 */
                serverThreadSize = 1;
            }
            /*
             * AJP_LISTENER_POOL_SIZE
             */
            listenerPoolSize = Integer.parseInt(ajpProperties.getProperty("AJP_LISTENER_POOL_SIZE", "20").trim());
            if (listenerPoolSize < 0) {
                listenerPoolSize = 0;
            }
            /*
             * AJP_LISTENER_READ_TIMEOUT
             */
            listenerReadTimeout = Integer.parseInt(ajpProperties.getProperty("AJP_LISTENER_READ_TIMEOUT", "60000").trim());
            if (listenerReadTimeout < 0) {
                listenerReadTimeout = 0;
            }
            /*
             * AJP_KEEP_ALIVE_TIME
             */
            keepAliveTime = Integer.parseInt(ajpProperties.getProperty("AJP_KEEP_ALIVE_TIME", "20000").trim());
            if (keepAliveTime < 0) {
                keepAliveTime = 0;
            }
            /*
             * AJP_MAX_REQUEST_PARAMETER_COUNT
             */
            maxRequestParameterCount = Integer.parseInt(ajpProperties.getProperty("AJP_MAX_REQUEST_PARAMETER_COUNT", "30").trim());
            if (maxRequestParameterCount < 0) {
                maxRequestParameterCount = 0;
            }
            /*
             * AJP_WATCHER_ENABLED
             */
            watcherEnabled = trueStr.regionMatches(true, 0, ajpProperties.getProperty("AJP_WATCHER_ENABLED", falseStr).trim(), 0, 4);
            /*
             * AJP_WATCHER_PERMISSION
             */
            watcherPermission = trueStr.regionMatches(
                true,
                0,
                ajpProperties.getProperty("AJP_WATCHER_PERMISSION", falseStr).trim(),
                0,
                4);
            /*
             * AJP_WATCHER_MAX_RUNNING_TIME
             */
            watcherMaxRunningTime = Integer.parseInt(ajpProperties.getProperty("AJP_WATCHER_MAX_RUNNING_TIME", "30000").trim());
            if (watcherMaxRunningTime < 0) {
                watcherMaxRunningTime = 30000;
            }
            /*
             * AJP_WATCHER_FREQUENCY
             */
            watcherFrequency = Integer.parseInt(ajpProperties.getProperty("AJP_WATCHER_FREQUENCY", "30000").trim());
            if (watcherFrequency < 0) {
                watcherFrequency = 30000;
            }
            /*
             * SERVLET_POOL_SIZE
             */
            servletPoolSize = Integer.parseInt(ajpProperties.getProperty("SERVLET_POOL_SIZE", "50").trim());
            if (servletPoolSize < 0) {
                servletPoolSize = 1;
            }
            /*
             * AJP_JVM_ROUTE
             */
            jvmRoute = ajpProperties.getProperty("AJP_JVM_ROUTE");
            if (jvmRoute == null) {
                LOG.error(AJPv13Exception.AJPCode.MISSING_JVM_ROUTE.getMessage());
            } else {
                jvmRoute = jvmRoute.trim();
            }
            /*
             * AJP_SERVLET_CONFIG_DIR
             */
            servletConfigs = ajpProperties.getProperty("AJP_SERVLET_CONFIG_DIR");
            if (servletConfigs == null || "null".equalsIgnoreCase((servletConfigs = servletConfigs.trim()))) {
                servletConfigs = "servletConfig";
            }
            final File servletConfigsFile = configurationService.getDirectory(servletConfigs);
            if (LOG.isTraceEnabled() && ((null == servletConfigsFile) || !servletConfigsFile.exists() || !servletConfigsFile.isDirectory())) {
                LOG.trace(servletConfigsFile + " does not exist or is not a directory");
            }
            /*
             * AJP_BIND_ADDR
             */
            final String bindAddr = ajpProperties.getProperty("AJP_BIND_ADDR", "localhost").trim();
            ajpBindAddr = bindAddr.charAt(0) == '*' ? null : InetAddress.getByName(bindAddr);
            /*
             * AJP_LOG_FORWARD_REQUEST
             */
            logForwardRequest = trueStr.equalsIgnoreCase(ajpProperties.getProperty("AJP_LOG_FORWARD_REQUEST", falseStr).trim());
            /*
             * Log info
             */
            logInfo();
        } catch (final FileNotFoundException e) {
            throw new AJPv13Exception(AJPv13Exception.AJPCode.FILE_NOT_FOUND, true, e, AJP_PROP_FILE_NAME);
        } catch (final IOException e) {
            throw new AJPv13Exception(AJPv13Exception.AJPCode.IO_ERROR, true, e, e.getMessage());
        }
    }

    private static void logInfo() {
        if (LOG.isInfoEnabled()) {
            final StringBuilder logBuilder = new StringBuilder(1000);
            logBuilder.append("\nAJP CONFIGURATION:\n");
            logBuilder.append("\tAJP_PORT=").append(instance.port).append('\n');
            logBuilder.append("\tAJP_SERVER_THREAD_SIZE=").append(instance.serverThreadSize).append('\n');
            logBuilder.append("\tAJP_LISTENER_POOL_SIZE=").append(instance.listenerPoolSize).append('\n');
            logBuilder.append("\tAJP_LISTENER_READ_TIMEOUT=").append(instance.listenerReadTimeout).append('\n');
            logBuilder.append("\tAJP_KEEP_ALIVE_TIME=").append(instance.keepAliveTime).append('\n');
            logBuilder.append("\tAJP_MAX_REQUEST_PARAMETER_COUNT=").append(instance.maxRequestParameterCount).append('\n');
            logBuilder.append("\tAJP_WATCHER_ENABLED=").append(instance.watcherEnabled).append('\n');
            logBuilder.append("\tAJP_WATCHER_PERMISSION=").append(instance.watcherPermission).append('\n');
            logBuilder.append("\tAJP_WATCHER_MAX_RUNNING_TIME=").append(instance.watcherMaxRunningTime).append('\n');
            logBuilder.append("\tAJP_WATCHER_FREQUENCY=").append(instance.watcherFrequency).append('\n');
            logBuilder.append("\tSERVLET_POOL_SIZE=").append(instance.servletPoolSize).append('\n');
            logBuilder.append("\tAJP_JVM_ROUTE=").append(instance.jvmRoute).append('\n');
            logBuilder.append("\tAJP_LOG_FORWARD_REQUEST=").append(instance.logForwardRequest).append('\n');
            logBuilder.append("\tAJP_SERVLET_CONFIG_DIR=").append(instance.servletConfigs).append('\n');
            logBuilder.append("\tAJP_BIND_ADDR=").append(
                instance.ajpBindAddr == null ? "* (all interfaces)" : instance.ajpBindAddr.toString());
            LOG.info(logBuilder.toString());
        }
    }

    private AJPv13Config() {
        super();
    }

    public static int getAJPPort() {
        return instance.port;
    }

    public static int getAJPServerThreadSize() {
        return instance.serverThreadSize;
    }

    /**
     * Gets the capacity for listener pool
     *
     * @return The capacity for listener pool
     */
    public static int getAJPListenerPoolSize() {
        return instance.listenerPoolSize;
    }

    public static int getAJPListenerReadTimeout() {
        return instance.listenerReadTimeout;
    }

    /**
     * Gets the keep-alive time
     *
     * @return The keep-alive time
     */
    public static int getKeepAliveTime() {
        return instance.keepAliveTime;
    }

    /**
     * Gets the max. request parameter count allowed.
     * 
     * @return The max. request parameter count
     */
    public static int getMaxRequestParameterCount() {
        return instance.maxRequestParameterCount;
    }

    public static boolean getAJPWatcherEnabled() {
        return instance.watcherEnabled;
    }

    public static boolean getAJPWatcherPermission() {
        return instance.watcherPermission;
    }

    public static int getAJPWatcherMaxRunningTime() {
        return instance.watcherMaxRunningTime;
    }

    public static int getAJPWatcherFrequency() {
        return instance.watcherFrequency;
    }

    public static int getServletPoolSize() {
        return instance.servletPoolSize;
    }

    public static String getJvmRoute() {
        return instance.jvmRoute;
    }

    public static boolean isLogForwardRequest() {
        return instance.logForwardRequest;
    }

    public static String getServletConfigs() {
        return instance.servletConfigs;
    }

    /**
     * @return an instance if <code>java.net.InetAddress</code> if property AJP_BIND_ADDR is different to "*"; <code>null</code> otherwise
     */
    public static InetAddress getAJPBindAddress() {
        return instance.ajpBindAddr;
    }

    /**
     * Gets the specified server property.
     *
     * @param property The server property
     * @return The property value
     */
    public static String getServerProperty(final ServerConfig.Property property) {
        final ConfigurationService service = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
        return service == null ? property.getDefaultValue() : service.getProperty(property.getPropertyName(), property.getDefaultValue());
    }

    /**
     * Gets the specified system property.
     *
     * @param property The system property
     * @return The property value
     */
    public static String getSystemProperty(final SystemConfig.Property property) {
        final ConfigurationService service = AJPv13ServiceRegistry.getInstance().getService(ConfigurationService.class);
        return service == null ? null : service.getProperty(property.getPropertyName());
    }

}
