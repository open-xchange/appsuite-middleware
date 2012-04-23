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

package com.openexchange.unifiedinbox.osgi;

import static com.openexchange.unifiedinbox.services.UnifiedInboxServiceRegistry.getServiceRegistry;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.i18n.I18nService;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.unifiedinbox.Enabled;
import com.openexchange.unifiedinbox.UnifiedInboxProvider;
import com.openexchange.unifiedinbox.utility.UnifiedInboxSynchronousQueueProvider;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedInboxActivator} - The {@link BundleActivator activator} for Unified Mail bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(UnifiedInboxActivator.class));

    /**
     * Initializes a new {@link UnifiedInboxActivator}
     */
    public UnifiedInboxActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ConfigurationService.class, CacheService.class, UserService.class, MailAccountStorageService.class, ContextService.class,
            ThreadPoolService.class, ConfigViewFactory.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        getServiceRegistry().addService(clazz, getService(clazz));
    }

    @Override
    public void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (final Class<?> classe : classes) {
                    final Object service = getService(classe);
                    if (null != service) {
                        registry.addService(classe, service);
                    }
                }
            }
            /*
             * Create & open trackers
             */
            track(I18nService.class, new I18nCustomizer(context));
            openTrackers();
            /*
             * Register service(s)
             */
            final Dictionary<String, String> dictionary = new Hashtable<String, String>(1);
            dictionary.put("protocol", UnifiedInboxProvider.PROTOCOL_UNIFIED_INBOX.toString());
            registerService(MailProvider.class, UnifiedInboxProvider.getInstance(), dictionary);
            registerService(PreferencesItemService.class, new Enabled(getService(ConfigViewFactory.class)));
            /*
             * Detect what SynchronousQueue to use
             */
            String property = System.getProperty("java.specification.version");
            if (null == property) {
                property = System.getProperty("java.runtime.version");
                if (null == property) {
                    // JRE not detectable, use fallback
                    UnifiedInboxSynchronousQueueProvider.initInstance(false);
                } else {
                    // "java.runtime.version=1.6.0_0-b14" OR "java.runtime.version=1.5.0_18-b02"
                    UnifiedInboxSynchronousQueueProvider.initInstance(!property.startsWith("1.5"));
                }
            } else {
                // "java.specification.version=1.5" OR "java.specification.version=1.6"
                UnifiedInboxSynchronousQueueProvider.initInstance("1.5".compareTo(property) < 0);
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        try {
            UnifiedInboxSynchronousQueueProvider.releaseInstance();
            cleanUp();
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

}
