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

package com.openexchange.service.messaging.internal;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.service.messaging.MessagingServiceExceptionCode;

/**
 * {@link MessagingConfig} - The configuration for messaging service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingConfig {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessagingConfig.class));

    private static volatile MessagingConfig instance;

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static MessagingConfig getInstance() {
        return instance;
    }

    /**
     * Initializes the instance with given bundle configuration.
     *
     * @param configurationService The configuration service
     * @throws OXException If re-initialization fails
     */
    public static void initInstance(final ConfigurationService configurationService) throws OXException {
        synchronized (MessagingConfig.class) {
            MessagingConfig config = instance;
            if (null == config) {
                config = new MessagingConfig();
                readFromConfigurationService(config, configurationService, false);
                instance = config;
            } else {
                readFromConfigurationService(config, configurationService, true);
            }
        }
    }

    private static void readFromConfigurationService(final MessagingConfig config, final ConfigurationService configurationService, final boolean update) throws OXException {
        try {
            {
                final String property = getProperty("com.openexchange.service.message.bindAddress", "*", configurationService).trim();
                config.setBindAddress("*".equals(property) ? null : InetAddress.getByName(property));
            }
            {
                final String property = getProperty("com.openexchange.service.message.mdnsEnabled", "true", configurationService).trim();
                config.setMdnsEnabled(Boolean.parseBoolean(property));
            }
            {
                final String property =
                    getProperty("com.openexchange.service.message.listenerPort", String.valueOf(Constants.DEFAULT_PORT), configurationService).trim();
                config.setListenerPort(Integer.parseInt(property));
            }
            {
                final String property =
                    getProperty("com.openexchange.service.message.numOfServerThreads", "1", configurationService).trim();
                config.setNumberOfServerThreads(Integer.parseInt(property));
            }
            {
                final String property =
                    getProperty("com.openexchange.service.message.servers", "", configurationService).trim();
                if (isEmpty(property)) {
                    config.setRemoteMessagingServers(Collections.<InetSocketAddress> emptyList());
                } else {
                    final String[] array = property.split(" *, *");
                    final List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>(array.length);
                    for (int i = 0; i < array.length; i++) {
                        final String server = array[i];
                        final int delim = server.indexOf(':');
                        if (delim > 0) {
                            try {
                                servers.add(new InetSocketAddress(InetAddress.getByName(server.substring(0, delim)), Integer.parseInt(server.substring(delim + 1))));
                            } catch (final UnknownHostException e) {
                                LOG.warn("Invalid server address: " + server, e);
                            } catch (final RuntimeException e) {
                                LOG.warn("Invalid server address: " + server, e);
                            }
                        } else {
                            LOG.warn("Invalid server address. Missing port in: " + server);
                        }
                    }
                    config.setRemoteMessagingServers(servers);
                }
            }
        } catch (final RuntimeException e) {
            throw MessagingServiceExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final UnknownHostException e) {
            throw MessagingServiceExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        synchronized (MessagingConfig.class) {
            if (null != instance) {
                instance = null;
            }
        }
    }

    private static String getProperty(final String name, final String defaultValue, final ConfigurationService configurationService) {
        final Object property = configurationService.getProperty(name);
        if (null == property) {
            return defaultValue;
        }
        return property.toString();
    }

    /*-
     * --------------------- Member section ------------------------
     */

    private boolean mdnsEnabled;

    private int listenerPort;

    private InetAddress bindAddress;

    private int numberOfServerThreads;

    private List<InetSocketAddress> remoteMessagingServers;

    /**
     * Initializes a new {@link MessagingConfig}.
     */
    private MessagingConfig() {
        super();
        listenerPort = Constants.DEFAULT_PORT;
        mdnsEnabled = true;
    }

    /**
     * Gets the listener port.
     * <p>
     * If not set {@link Constants#DEFAULT_PORT} is returned.
     *
     * @return The listener port
     */
    public int getListenerPort() {
        return listenerPort;
    }

    /**
     * Sets the listener port.
     * <p>
     * The port must be between 0 and 65535 inclusive.
     *
     * @param multicastPort The listener port to set
     * @throws IllegalArgumentException If port is invalid
     */
    public void setListenerPort(final int listenerPort) {
        if (listenerPort <= 0 || listenerPort > 65535) {
            throw new IllegalArgumentException(new StringBuilder(64).append("Invalid port: ").append(listenerPort).append(
                ". The port must be between 0 and 65535 inclusive.").toString());
        }
        this.listenerPort = listenerPort;
    }

    /**
     * Sets whether mDNS is enabled.
     *
     * @param multicastEnabled <code>true</code> if mDNS is enabled; otherwise <code>false</code>
     */
    public void setMdnsEnabled(final boolean mdnsEnabled) {
        this.mdnsEnabled = mdnsEnabled;
    }

    /**
     * Checks if mDNS is enabled.
     *
     * @return <code>true</code> if mDNS is enabled; otherwise <code>false</code>
     */
    public boolean isMdnsEnabled() {
        return mdnsEnabled;
    }

    /**
     * Gets the bind address.
     *
     * @return The bind address or <code>null</code> for all available interfaces.
     */
    public InetAddress getBindAddress() {
        return bindAddress;
    }

    /**
     * Sets the bind address.
     *
     * @param bindAddress The bind address to set or <code>null</code> for all available interfaces.
     */
    public void setBindAddress(final InetAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    /**
     * Gets the number of server threads.
     *
     * @return The number of server threads
     */
    public int getNumberOfServerThreads() {
        return numberOfServerThreads;
    }

    /**
     * Sets the number of server threads.
     *
     * @param numberOfServerThreads The number of server threads to set
     */
    public void setNumberOfServerThreads(final int numberOfServerThreads) {
        this.numberOfServerThreads = numberOfServerThreads;
    }

    /**
     * Gets the statically configured remote messaging servers.
     *
     * @return The remote messaging servers.
     */
    public List<InetSocketAddress> getRemoteMessagingServers() {
        return null == remoteMessagingServers ? Collections.<InetSocketAddress> emptyList() : Collections.unmodifiableList(remoteMessagingServers);
    }

    /**
     * Sets the statically configured remote messaging servers.
     *
     * @param remoteMessagingServers The remote messaging servers to set.
     */
    public void setRemoteMessagingServers(final List<InetSocketAddress> remoteMessagingServers) {
        this.remoteMessagingServers = remoteMessagingServers;
    }

}
