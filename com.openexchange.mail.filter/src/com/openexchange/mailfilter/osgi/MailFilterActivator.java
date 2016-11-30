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
 *    trademarks of the OX Software GmbH. group of companies.
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
package com.openexchange.mailfilter.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.jsieve.registry.TestCommandRegistry;
import com.openexchange.mailfilter.MailFilterProperties;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.internal.MailFilterPreferencesItem;
import com.openexchange.mailfilter.internal.MailFilterReloadable;
import com.openexchange.mailfilter.internal.MailFilterServiceImpl;
import com.openexchange.mailfilter.services.Services;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondEventConstants;

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
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            checkConfigfile();

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

            registerService(Reloadable.class, new MailFilterReloadable(), null);

            registerService(MailFilterService.class, new MailFilterServiceImpl());

            registerTestCommandRegistry();

            Logger logger = org.slf4j.LoggerFactory.getLogger(MailFilterActivator.class);
            logger.info("Bundle successfully started: {}", context.getBundle().getSymbolicName());

        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            super.stopBundle();
            //MailFilterServletInit.getInstance().stop();
            Services.setServiceLookup(null);
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }


    /**
     * This method checks for a valid configfile and throws and exception if now configfile is there or one of the properties is missing
     * @throws Exception
     */
    // protected to be able to test this
    public static void checkConfigfile() throws Exception {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final Properties file = config.getFile("mailfilter.properties");
        if (file.isEmpty()) {
            throw new Exception("No configfile found for mailfilter bundle");
        }
        for (final MailFilterProperties.Values type : MailFilterProperties.Values.values()) {
            if (null == file.getProperty(type.property)) {
                throw new Exception("Property for mailfilter not found: " + type.property);
            }
        }
        try {
            Integer.parseInt(file.getProperty(MailFilterProperties.Values.SIEVE_CONNECTION_TIMEOUT.property));
        } catch (final NumberFormatException e) {
            throw new Exception("Property " + MailFilterProperties.Values.SIEVE_CONNECTION_TIMEOUT.property + " is no integer value");
        }

        final String passwordsrc = config.getProperty(MailFilterProperties.Values.SIEVE_PASSWORDSRC.property);
        if (null != passwordsrc) {
            if (MailFilterProperties.PasswordSource.GLOBAL.name.equals(passwordsrc)) {
                final String masterpassword = config.getProperty(MailFilterProperties.Values.SIEVE_MASTERPASSWORD.property);
                if (masterpassword.length() == 0) {
                    throw MailFilterExceptionCode.NO_MASTERPASSWORD_SET.create();
                }
            } else if (!MailFilterProperties.PasswordSource.SESSION.name.equals(passwordsrc)) {
                throw MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.create();
            }
        } else {
            throw MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.create();
        }

    }

    /**
     * Registers the {@link TestCommandParserRegistry} along with all available {@link TestCommandParser}s
     */
    private void registerTestCommandRegistry() {
        TestCommandRegistry registry = new TestCommandRegistry();

        // add own Tests:
        for (Commands command : Commands.values()) {
            registry.register(command.getCommandName(), command);
        }

        registerService(TestCommandRegistry.class, registry);
        trackService(TestCommandRegistry.class);
        openTrackers();
    }
}
