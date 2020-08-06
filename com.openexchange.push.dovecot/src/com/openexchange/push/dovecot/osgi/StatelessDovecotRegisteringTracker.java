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

package com.openexchange.push.dovecot.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.mail.MailProviderRegistration;
import com.openexchange.mail.Protocol;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.dovecot.AbstractDovecotPushManagerService;
import com.openexchange.push.dovecot.DovecotPushConfiguration;
import com.openexchange.push.dovecot.DovecotPushDeleteListener;
import com.openexchange.push.dovecot.stateless.StatelessDovecotPushManagerService;

/**
 * {@link StatelessDovecotRegisteringTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class StatelessDovecotRegisteringTracker implements ServiceTrackerCustomizer<MailProviderRegistration, MailProviderRegistration>, DovecotPushManagerLifecycle {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(StatelessDovecotRegisteringTracker.class);

    private final DovecotPushConfiguration config;
    private final BundleContext context;
    private final DovecotPushActivator activator;
    private ServiceRegistration<PushManagerService> reg;
    private ServiceRegistration<DeleteListener> deLiReg;
    private MailProviderRegistration imapRegistration;

    private StatelessDovecotPushManagerService pushManager;


    /**
     * Initializes a new {@link StatelessDovecotRegisteringTracker}.
     */
    public StatelessDovecotRegisteringTracker(DovecotPushConfiguration config, DovecotPushActivator activator, BundleContext context) {
        super();
        this.config = config;
        this.context = context;
        this.activator = activator;
    }

    private static boolean isIMAPProvider(MailProviderRegistration mailProviderRegistration) throws OXException {
        String protocol = mailProviderRegistration.getRegisteredProvider();
        return protocol != null && Protocol.parseProtocol(protocol).isSupported("imap");
    }

    @Override
    public synchronized MailProviderRegistration addingService(ServiceReference<MailProviderRegistration> reference) {
        MailProviderRegistration mailProviderRegistration = context.getService(reference);

        try {
            if (!isIMAPProvider(mailProviderRegistration)) {
                context.ungetService(reference);
                return null;
            }

            this.imapRegistration = mailProviderRegistration;
        } catch (OXException e) {
            LOG.error("Failed to handle registered MailProviderRegistration", e);
        }

        if (imapRegistration != null) {
            init();
        }

        return mailProviderRegistration;
    }

    @Override
    public void modifiedService(ServiceReference<MailProviderRegistration> reference, MailProviderRegistration mailProviderRegistration) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<MailProviderRegistration> reference, MailProviderRegistration mailProviderRegistration) {
        boolean someServiceMissing = false;

        if (imapRegistration != null) {
            try {
                if (isIMAPProvider(mailProviderRegistration)) {
                    imapRegistration = null;
                    someServiceMissing = true;
                }
            } catch (Exception e) {
                LOG.error("Failed to handle unregistered MailProviderRegistration", e);
            }
        }

        if (null != reg && someServiceMissing) {
            stop();
        }

        context.ungetService(reference);
    }

    private void init() {
        if (null != reg) {
            // Already registered
            return;
        }

        try {
            StatelessDovecotPushManagerService pushManager = StatelessDovecotPushManagerService.newInstance(config, activator);
            this.pushManager = pushManager;
            deLiReg = context.registerService(DeleteListener.class, new DovecotPushDeleteListener(pushManager), null);
            reg = context.registerService(PushManagerService.class, pushManager, null);
        } catch (Exception e) {
            LOG.warn("Failed start-up for {}", context.getBundle().getSymbolicName(), e);
        }

    }

    private void stop() {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }

        if (deLiReg != null) {
            deLiReg.unregister();
            deLiReg = null;
        }

        if (pushManager != null) {
            pushManager = null;
        }
    }

    @Override
    public boolean isActive() {
        return pushManager != null;
    }

    @Override
    public AbstractDovecotPushManagerService getActiveInstance() {
        return pushManager;
    }

    @Override
    public synchronized void shutDown() {
        stop();
    }

}
