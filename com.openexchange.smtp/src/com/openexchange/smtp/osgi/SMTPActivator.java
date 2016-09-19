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

package com.openexchange.smtp.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.activation.MailcapCommandMap;
import org.osgi.framework.BundleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.config.NoReplyConfigFactory;
import com.openexchange.mail.transport.listener.MailTransportListener;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.oauth.OAuthService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.smtp.ListenerChain;
import com.openexchange.smtp.SMTPProvider;
import com.openexchange.smtp.SmtpReloadable;
import com.openexchange.smtp.services.Services;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.UserService;

/**
 * {@link SMTPActivator} - The {@link BundleActivator activator} for SMTP bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMTPActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SMTPActivator.class);

    /**
     * Initializes a new {@link SMTPActivator}
     */
    public SMTPActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, MailAccountStorageService.class, ConfigViewFactory.class, ThreadPoolService.class, ContextService.class, UserService.class,
            SSLConfigurationService.class };
    }

    @Override
    public void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            RankingAwareNearRegistryServiceTracker<MailTransportListener> listing = new RankingAwareNearRegistryServiceTracker<MailTransportListener>(context, MailTransportListener.class);
            ListenerChain.initInstance(listing);
            rememberTracker(listing);

            trackService(HostnameService.class);
            trackService(NoReplyConfigFactory.class);
            trackService(AuditLogService.class);
            trackService(OAuthService.class);
            track(MailcapCommandMap.class, new MailcapServiceTracker(context));
            openTrackers();

            final Dictionary<String, String> dictionary = new Hashtable<String, String>(1);
            dictionary.put("protocol", SMTPProvider.PROTOCOL_SMTP.toString());
            registerService(TransportProvider.class, SMTPProvider.getInstance(), dictionary);

            registerService(Reloadable.class, SmtpReloadable.getInstance());
        } catch (final Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }

    }

    @Override
    public void stopBundle() throws Exception {
        try {
            ListenerChain.releaseInstance();
            cleanUp();
            Services.setServiceLookup(null);
        } catch (final Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }

}
