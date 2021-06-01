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

package com.openexchange.mailfilter.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.jsieve.registry.ActionCommandRegistry;
import com.openexchange.jsieve.registry.TestCommandRegistry;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailfilter.MailFilterInterceptorRegistry;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.mailfilter.internal.MailFilterCircuitBreakerReloadable;
import com.openexchange.mailfilter.internal.MailFilterInterceptorRegistryImpl;
import com.openexchange.mailfilter.internal.MailFilterPreferencesItem;
import com.openexchange.mailfilter.internal.MailFilterServiceImpl;
import com.openexchange.mailfilter.services.Services;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.user.UserService;

public class MailFilterActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailFilterActivator.class);

    /**
     * Initializes a new {@link MailFilterServletActivator}
     */
    public MailFilterActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class, LeanConfigurationService.class, UserService.class,
            MailAccountStorageService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            MailFilterServiceImpl mailFilterService = new MailFilterServiceImpl(this);

            registerService(MailFilterInterceptorRegistry.class, new MailFilterInterceptorRegistryImpl());
            trackService(MailFilterInterceptorRegistry.class);

            trackService(SSLSocketFactoryProvider.class);
            openTrackers();

            {
                EventHandler eventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(Event event) {
                        String topic = event.getTopic();
                        if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
                            Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                            if (null != contextId) {
                                Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                                if (null != userId) {
                                    MailFilterServiceImpl.removeFor(userId.intValue(), contextId.intValue());
                                }
                            }
                        }
                    }
                };

                Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
                registerService(EventHandler.class, eventHandler, dict);
            }
            registerService(PreferencesItemService.class, new MailFilterPreferencesItem(), null);
            registerService(MailFilterService.class, mailFilterService);
            registerService(Reloadable.class, new MailFilterCircuitBreakerReloadable(mailFilterService));
            registerTestCommandRegistry();
            registerActionCommandRegistry();

            Logger logger = org.slf4j.LoggerFactory.getLogger(MailFilterActivator.class);
            logger.info("Bundle successfully started: {}", context.getBundle().getSymbolicName());

        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            super.stopBundle();
            Services.setServiceLookup(null);
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    /**
     * Registers the {@link TestCommandParserRegistry} along with all available {@link TestCommand}s
     */
    private void registerTestCommandRegistry() {
        TestCommandRegistry registry = new TestCommandRegistry();

        for (Commands command : Commands.values()) {
            registry.register(command.getCommandName(), command);
        }

        registerService(TestCommandRegistry.class, registry);
        trackService(TestCommandRegistry.class);
        openTrackers();
    }

    /**
     * Registers the {@link ActionCommandRegistry} along with all available {@link ActionCommand}s
     */
    private void registerActionCommandRegistry() {
        ActionCommandRegistry registry = new ActionCommandRegistry();

        for (ActionCommand.Commands command : ActionCommand.Commands.values()) {
            registry.register(command.getCommandName(), command);
        }

        registerService(ActionCommandRegistry.class, registry);
        trackService(ActionCommandRegistry.class);
        openTrackers();
    }
}
