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

package com.openexchange.messaging.json.osgi;

import java.io.ByteArrayInputStream;
import org.osgi.service.http.HttpService;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.java.Charsets;
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
    protected Class<?>[] getOptionalServices() {
        return new Class[] { I18nServiceRegistry.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        try {
            register();
        } catch (Exception e) {
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

            openTrackers();

            fileInputStreamRegistry = ManagedFileInputStreamRegistry.getInstance();
            fileInputStreamRegistry.start(context);

            register();
        } catch (Exception x) {
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
                "jcs.region." + regionName + ".elementattributes.IsLateral=false\n").getBytes(Charsets.ISO_8859_1);
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
        } catch (Exception x) {
            LOG.error("", x);
            throw x;
        }
    }

}
