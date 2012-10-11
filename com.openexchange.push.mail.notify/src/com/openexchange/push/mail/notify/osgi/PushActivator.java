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

package com.openexchange.push.mail.notify.osgi;

import java.io.IOException;
import java.util.concurrent.Future;
import org.osgi.service.event.EventAdmin;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.mail.notify.MailNotifyPushDeleteListener;
import com.openexchange.push.mail.notify.MailNotifyPushListenerRegistry;
import com.openexchange.push.mail.notify.MailNotifyPushMailAccountDeleteListener;
import com.openexchange.push.mail.notify.MailNotifyPushManagerService;
import com.openexchange.push.mail.notify.MailNotifyPushUdpSocketListener;
import com.openexchange.push.mail.notify.services.PushServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link PushActivator} - The push activator.
 *
 */
public final class PushActivator extends HousekeepingActivator {

    private static final String CRLF = "\r\n";

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(PushActivator.class));

    private static final String PROP_UDP_LISTEN_MULTICAST = "com.openexchange.push.mail.notify.udp_listen_multicast";

    private static final String PROP_UDP_LISTEN_HOST = "com.openexchange.push.mail.notify.udp_listen_host";

    private static final String PROP_UDP_LISTEN_PORT = "com.openexchange.push.mail.notify.udp_listen_port";

    private static final String PROP_IMAP_LOGIN_DELIMITER = "com.openexchange.push.mail.notify.imap_login_delimiter";

    private static final String PROP_USE_OX_LOGIN = "com.openexchange.push.mail.notify.use_ox_login";

    private static final String PROP_USE_EMAIL_ADDRESS = "com.openexchange.push.mail.notify.use_full_email_address";

    private boolean multicast;

    private String udpListenHost;

    private String imapLoginDelimiter;

    private int udpListenPort;

    private boolean useOXLogin;

    private boolean useEmailAddress;

    private Future<Object> udpThread;

    /**
     * Initializes a new {@link PushActivator}.
     */
    public PushActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MailService.class, EventAdmin.class, ConfigurationService.class, ThreadPoolService.class, SessiondService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        PushServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        PushServiceRegistry.getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = PushServiceRegistry.getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                        registry.addService(classes[i], service);
                    }
                }
            }
            readConfiguration();
            /*
             * Register push manager
             */
            registerService(PushManagerService.class, new MailNotifyPushManagerService(useOXLogin, useEmailAddress), null);
            registerService(MailAccountDeleteListener.class, new MailNotifyPushMailAccountDeleteListener(), null);
            registerService(DeleteListener.class, new MailNotifyPushDeleteListener(), null);
            startUdpListener();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            stopUdpListener();
            /*
             * Unregister push manager
             */
            cleanUp();
            /*
             * Shut down
             */
            MailNotifyPushListenerRegistry.getInstance().clear();
            /*
             * Clear service registry
             */
            PushServiceRegistry.getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private void readConfiguration() throws OXException {
        final StringBuilder sb = new StringBuilder();
        sb.append(CRLF);
        sb.append("Properties for mail push:" + CRLF);
        sb.append("------------------------" + CRLF);
        /*
         * Read configuration
         */
        final ConfigurationService configurationService = getService(ConfigurationService.class);
        String tmp = configurationService.getProperty(PROP_UDP_LISTEN_MULTICAST);
        multicast = false;
        if (null != tmp) {
            if (tmp.trim().equals("true")) {
                sb.append("\t" + PROP_UDP_LISTEN_MULTICAST + ": true" + CRLF);
                multicast = true;
            }
        }

        tmp = configurationService.getProperty(PROP_UDP_LISTEN_HOST);
        if (null != tmp) {
            udpListenHost = tmp.trim();
            sb.append("\t" + PROP_UDP_LISTEN_HOST + ": " + udpListenHost + CRLF);
        } else {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(PROP_UDP_LISTEN_HOST);
        }

        tmp = configurationService.getProperty(PROP_IMAP_LOGIN_DELIMITER);
        if (null != tmp) {
            imapLoginDelimiter = tmp.trim();
            sb.append("\t" + PROP_IMAP_LOGIN_DELIMITER + ": " + imapLoginDelimiter + CRLF);
        } else {
            imapLoginDelimiter = null;
            sb.append("\t" + PROP_IMAP_LOGIN_DELIMITER + ": not set" + CRLF);
        }

        tmp = configurationService.getProperty(PROP_UDP_LISTEN_PORT);
        if (null != tmp) {
            try {
                udpListenPort = Integer.parseInt(tmp.trim());
                sb.append("\t" + PROP_UDP_LISTEN_PORT + ": " + udpListenPort + CRLF);
            } catch (final NumberFormatException e) {
                throw ConfigurationExceptionCodes.PROPERTY_NOT_AN_INTEGER.create(PROP_UDP_LISTEN_PORT);
            }
        } else {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(PROP_UDP_LISTEN_PORT);
        }

        tmp = configurationService.getProperty(PROP_USE_OX_LOGIN);
        useOXLogin = false;
        if (null != tmp) {
            if (tmp.trim().equals("true")) {
                sb.append("\t" + PROP_USE_OX_LOGIN + ": true" + CRLF);
                useOXLogin = true;
            }
        }

        tmp = configurationService.getProperty(PROP_USE_EMAIL_ADDRESS);
        useEmailAddress = false;
        if (null != tmp) {
            if (tmp.trim().equals("true")) {
                sb.append("\t" + PROP_USE_EMAIL_ADDRESS + ": true" + CRLF);
                useEmailAddress = true;
            }
        }

        LOG.info(sb);
    }

    private void startUdpListener() throws OXException, IOException {
        final ThreadPoolService threadPoolService = ThreadPools.getThreadPool();
        udpThread = threadPoolService.submit(ThreadPools.task(new MailNotifyPushUdpSocketListener(udpListenHost, udpListenPort, imapLoginDelimiter, multicast)));
    }

    private void stopUdpListener() {
        if (null != udpThread) {
            udpThread.cancel(true);
        }
    }

}
