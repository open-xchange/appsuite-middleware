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

package com.openexchange.messaging.json.osgi;

import java.io.ByteArrayInputStream;
import org.osgi.service.http.HttpService;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.i18n.I18nService;
import com.openexchange.messaging.json.Enabled;
import com.openexchange.messaging.json.GUI;
import com.openexchange.messaging.json.ManagedFileInputStreamRegistry;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;
import com.openexchange.messaging.json.Services;
import com.openexchange.messaging.json.actions.accounts.AccountActionFactory;
import com.openexchange.messaging.json.actions.messages.MessagingActionFactory;
import com.openexchange.messaging.json.actions.services.ServicesActionFactory;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.oauth.OAuthService;

/**
 * {@link MessagingJSONActivator} - The messaging JSON activator.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingJSONActivator extends AJAXModuleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessagingJSONActivator.class);

    private MessagingServiceRegistry registry;

    private MessagingMessageParser parser;

    private MessagingMessageWriter writer;

    private CacheService cacheService;

    private boolean cacheConfigured;

    private ManagedFileInputStreamRegistry fileInputStreamRegistry;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { MessagingServiceRegistry.class, HttpService.class, CacheService.class, ConfigViewFactory.class, OAuthService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        try {
            register();
        } catch (final Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (clazz == MessagingServiceRegistry.class) {
            hide();
        }
    }

    private void hide() {
        unregisterServices();
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            parser = new MessagingMessageParser();

            rememberTracker(new ContentParserTracker(context, parser));
            rememberTracker(new HeaderParserTracker(context, parser));

            writer = new MessagingMessageWriter();
            rememberTracker(new ContentWriterTracker(context, writer));
            rememberTracker(new HeaderWriterTracker(context, writer));
            track(I18nService.class, new I18nServiceCustomizer(context));

            openTrackers();

            fileInputStreamRegistry = ManagedFileInputStreamRegistry.getInstance();
            fileInputStreamRegistry.start(context);

            register();
        } catch (final Exception x) {
            LOG.error("", x);
            throw x;
        }
    }

    private void register() throws Exception {
        boolean initializeFacs = false;
        if (cacheService == null) {
            cacheService = getService(CacheService.class);
            initializeFacs = true;
        }
        if (registry == null) {
            registry = getService(MessagingServiceRegistry.class);
            initializeFacs = true;
        }

        if (!allAvailable()) {
            return;
        }

        if (initializeFacs) {
            AccountActionFactory.INSTANCE = new AccountActionFactory(registry);
            MessagingActionFactory.INSTANCE = new MessagingActionFactory(registry, writer, parser, getCache());
            ServicesActionFactory.INSTANCE = new ServicesActionFactory(registry);
        }
        if (!hasRegisteredServices()) {
            registerModule(AccountActionFactory.INSTANCE, "messaging/account");
            registerModule(MessagingActionFactory.INSTANCE, "messaging/message");
            registerModule(ServicesActionFactory.INSTANCE, "messaging/service");
            registerService(PreferencesItemService.class, new Enabled(getService(ConfigViewFactory.class)));
            registerService(PreferencesItemService.class, new GUI());
        }
    }

    /**
     * @return
     * @throws OXException
     */
    private Cache getCache() throws OXException {
        final String regionName = "com.openexchange.messaging.json.messageCache";
        if (!cacheConfigured) {
            cacheConfigured = true;
            final byte[] ccf = (
                "jcs.region." + regionName + "=LTCP\n" +
                "jcs.region." + regionName + ".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                "jcs.region." + regionName + ".cacheattributes.MaxObjects=10000000\n" +
                "jcs.region." + regionName + ".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
                "jcs.region." + regionName + ".cacheattributes.UseMemoryShrinker=true\n" +
                "jcs.region." + regionName + ".cacheattributes.MaxMemoryIdleTimeSeconds=360\n" +
                "jcs.region." + regionName + ".cacheattributes.ShrinkerIntervalSeconds=60\n" +
                "jcs.region." + regionName + ".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                "jcs.region." + regionName + ".elementattributes.IsEternal=false\n" +
                "jcs.region." + regionName + ".elementattributes.MaxLifeSeconds=-1\n" +
                "jcs.region." + regionName + ".elementattributes.IdleTime=360\n" +
                "jcs.region." + regionName + ".elementattributes.IsSpool=false\n" +
                "jcs.region." + regionName + ".elementattributes.IsRemote=false\n" +
                "jcs.region." + regionName + ".elementattributes.IsLateral=false\n").getBytes();
            cacheService.loadConfiguration(new ByteArrayInputStream(ccf));
        }
        return cacheService.getCache(regionName);
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            if (null != fileInputStreamRegistry) {
                fileInputStreamRegistry.stop();
                ManagedFileInputStreamRegistry.dropInstance();
            }
            Services.setServiceLookup(null);
            super.stopBundle();
        } catch (final Exception x) {
            LOG.error("", x);
            throw x;
        }
    }

}
