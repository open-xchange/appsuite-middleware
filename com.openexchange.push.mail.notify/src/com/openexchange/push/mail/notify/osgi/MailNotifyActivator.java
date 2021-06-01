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

package com.openexchange.push.mail.notify.osgi;

import java.io.IOException;
import java.util.concurrent.Future;
import org.osgi.service.event.EventAdmin;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.java.Strings;
import com.openexchange.lock.LockService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.mail.notify.MailNotifyPushDeleteListener;
import com.openexchange.push.mail.notify.MailNotifyPushListenerRegistry;
import com.openexchange.push.mail.notify.MailNotifyPushMailAccountDeleteListener;
import com.openexchange.push.mail.notify.MailNotifyPushManagerService;
import com.openexchange.push.mail.notify.MailNotifyPushUdpSocketListener;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link MailNotifyActivator} - The push activator.
 *
 */
public final class MailNotifyActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailNotifyActivator.class);

    private static final String PROP_UDP_LISTEN_MULTICAST = "com.openexchange.push.mail.notify.udp_listen_multicast";

    private static final String PROP_UDP_LISTEN_HOST = "com.openexchange.push.mail.notify.udp_listen_host";

    private static final String PROP_UDP_LISTEN_PORT = "com.openexchange.push.mail.notify.udp_listen_port";

    private static final String PROP_IMAP_LOGIN_DELIMITER = "com.openexchange.push.mail.notify.imap_login_delimiter";

    private static final String PROP_USE_OX_LOGIN = "com.openexchange.push.mail.notify.use_ox_login";

    private static final String PROP_USE_EMAIL_ADDRESS = "com.openexchange.push.mail.notify.use_full_email_address";

    private static final class Config {
        boolean multicast;
        String udpListenHost;
        String imapLoginDelimiter;
        int udpListenPort;
        boolean useOXLogin;
        boolean useEmailAddress;

        Config() {
            super();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private MailNotifyPushListenerRegistry registry;
    private MailNotifyPushUdpSocketListener udpListener;

    /**
     * Initializes a new {@link MailNotifyActivator}.
     */
    public MailNotifyActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MailService.class, EventAdmin.class, ConfigurationService.class, ThreadPoolService.class,
            SessiondService.class, TimerService.class, PushListenerService.class, ContextService.class, UserService.class,
            LockService.class, ConfigViewFactory.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        try {
            Services.set(this);

            // Read configuration
            Config config = readConfiguration();

            // Initialize listener registry
            MailNotifyPushListenerRegistry registry = new MailNotifyPushListenerRegistry(config.useOXLogin, config.useEmailAddress);
            this.registry = registry;

            // Track optional services
            trackService(PushNotificationService.class);
            trackService(HazelcastInstance.class);
            trackService(ObfuscatorService.class);
            openTrackers();

            // Register push manager
            registerService(PushManagerService.class, new MailNotifyPushManagerService(registry), null);

            // Register groupware stuff
            registerService(MailAccountDeleteListener.class, new MailNotifyPushMailAccountDeleteListener(registry), null);
            registerService(DeleteListener.class, new MailNotifyPushDeleteListener(registry), null);

            // Start UPD listener
            startUdpListener(registry, config);
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        try {
            // Stop UPD listener
            stopUdpListener();

            // Unregister push manager
            super.stopBundle();

            // Shut down
            MailNotifyPushListenerRegistry registry = this.registry;
            if (null != registry) {
                registry.cancel();
                registry.clear();
                this.registry = null;
            }

            Services.set(null);
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    private Config readConfiguration() throws OXException {
        final String ls = Strings.getLineSeparator();
        final StringBuilder sb = new StringBuilder();
        sb.append(ls);
        sb.append("Properties for mail push:").append(ls);
        sb.append("------------------------").append(ls);
        /*
         * Read configuration
         */
        Config config = new Config();
        final ConfigurationService configurationService = getService(ConfigurationService.class);

        String tmp = configurationService.getProperty(PROP_UDP_LISTEN_MULTICAST);
        config.multicast = false;
        if (null != tmp) {
            if (tmp.trim().equals("true")) {
                sb.append("\t").append(PROP_UDP_LISTEN_MULTICAST).append(": true").append(ls);
                config.multicast = true;
            }
        }

        tmp = configurationService.getProperty(PROP_UDP_LISTEN_HOST);
        if (null != tmp) {
            config.udpListenHost = tmp.trim();
            sb.append("\t").append(PROP_UDP_LISTEN_HOST).append(": ").append(config.udpListenHost).append(ls);
        } else {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(PROP_UDP_LISTEN_HOST);
        }

        tmp = configurationService.getProperty(PROP_IMAP_LOGIN_DELIMITER);
        if (null != tmp) {
            config.imapLoginDelimiter = tmp.trim();
            sb.append("\t").append(PROP_IMAP_LOGIN_DELIMITER).append(": ").append(config.imapLoginDelimiter).append(ls);
        } else {
            config.imapLoginDelimiter = null;
            sb.append("\t").append(PROP_IMAP_LOGIN_DELIMITER).append(": not set").append(ls);
        }

        tmp = configurationService.getProperty(PROP_UDP_LISTEN_PORT);
        if (null != tmp) {
            try {
                config.udpListenPort = Integer.parseInt(tmp.trim());
                sb.append("\t").append(PROP_UDP_LISTEN_PORT).append(": ").append(config.udpListenPort).append(ls);
            } catch (NumberFormatException e) {
                throw ConfigurationExceptionCodes.PROPERTY_NOT_AN_INTEGER.create(PROP_UDP_LISTEN_PORT);
            }
        } else {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(PROP_UDP_LISTEN_PORT);
        }

        tmp = configurationService.getProperty(PROP_USE_OX_LOGIN);
        config.useOXLogin = false;
        if (null != tmp) {
            if (tmp.trim().equals("true")) {
                sb.append("\t").append(PROP_USE_OX_LOGIN).append(": true").append(ls);
                config.useOXLogin = true;
            }
        }

        tmp = configurationService.getProperty(PROP_USE_EMAIL_ADDRESS);
        config.useEmailAddress = false;
        if (null != tmp) {
            if (tmp.trim().equals("true")) {
                sb.append("\t").append(PROP_USE_EMAIL_ADDRESS).append(": true").append(ls);
                config.useEmailAddress = true;
            }
        }

        LOG.info(sb.toString());

        return config;
    }

    private void startUdpListener(MailNotifyPushListenerRegistry registry, Config config) throws OXException, IOException {
        // Initialize UDP listener
        MailNotifyPushUdpSocketListener udpListener = new MailNotifyPushUdpSocketListener(registry, config.udpListenHost, config.udpListenPort, config.imapLoginDelimiter, config.multicast);
        this.udpListener = udpListener;

        // Submit to thread pool
        Future<Object> udpThread = ThreadPools.getThreadPool().submit(ThreadPools.task(udpListener));
        udpListener.setFuture(udpThread);
    }

    private void stopUdpListener() {
        MailNotifyPushUdpSocketListener udpListener = this.udpListener;
        if (null != udpListener) {
            udpListener.close();
            this.udpListener = null;
        }
    }

}
