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

package com.openexchange.mailfilter.json.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.service.http.HttpService;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.jsieve.registry.TestCommandRegistry;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.AddressTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.AllOfTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.AnyOfTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.BodyTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.CurrentDateTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.DateTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.EnvelopeTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.ExistsTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.HasFlagCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.HeaderTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.NotTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.SizeTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.TrueTestCommandParser;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link MailFilterJSONActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailFilterJSONActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MailFilterJSONActivator}.
     */
    public MailFilterJSONActivator() {
        super();

    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, MailFilterService.class, LeanConfigurationService.class, HttpService.class, SessiondService.class, DispatcherPrefixService.class, CapabilityService.class, TestCommandRegistry.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        // Dependently registers Servlets
        {
            Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + HttpService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + DispatcherPrefixService.class.getName() + "))");
            ServletRegisterer registerer = new ServletRegisterer(context);
            track(filter, registerer);
        }
        openTrackers();

        registerTestCommandParserRegistry();

        getService(CapabilityService.class).declareCapability(MailFilterChecker.CAPABILITY);

        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, MailFilterChecker.CAPABILITY);
        registerService(CapabilityChecker.class, new MailFilterChecker(), properties);
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    /**
     * Registers the {@link TestCommandParserRegistry} along with all available {@link TestCommandParser}s
     */
    private void registerTestCommandParserRegistry() {
        TestCommandParserRegistry registry = new TestCommandParserRegistry();
        registry.register(Commands.ADDRESS.getCommandName(), new AddressTestCommandParser());
        registry.register(Commands.ALLOF.getCommandName(), new AllOfTestCommandParser());
        registry.register(Commands.ANYOF.getCommandName(), new AnyOfTestCommandParser());
        registry.register(Commands.BODY.getCommandName(), new BodyTestCommandParser());
        registry.register(Commands.DATE.getCommandName(), new DateTestCommandParser());
        registry.register(Commands.EXISTS.getCommandName(), new ExistsTestCommandParser());
        registry.register(Commands.CURRENTDATE.getCommandName(), new CurrentDateTestCommandParser());
        registry.register(Commands.ENVELOPE.getCommandName(), new EnvelopeTestCommandParser());
        registry.register(Commands.HEADER.getCommandName(), new HeaderTestCommandParser());
        registry.register(Commands.NOT.getCommandName(), new NotTestCommandParser());
        registry.register(Commands.SIZE.getCommandName(), new SizeTestCommandParser());
        registry.register(Commands.TRUE.getCommandName(), new TrueTestCommandParser());
        registry.register(Commands.HASFLAG.getCommandName(), new HasFlagCommandParser());

        registerService(TestCommandParserRegistry.class, registry);
        trackService(TestCommandParserRegistry.class);
        openTrackers();
    }
}
