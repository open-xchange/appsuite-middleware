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

package com.openexchange.mailfilter.json.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
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
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.EnvelopeTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.HasFlagCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.HeaderTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.NotTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.SizeTestCommandParser;
import com.openexchange.mailfilter.json.ajax.json.mapper.parser.test.TrueTestCommandParser;
import com.openexchange.mailfilter.json.ajax.servlet.MailFilterServletInit;
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
        return new Class<?>[] { ConfigurationService.class, MailFilterService.class, HttpService.class, SessiondService.class, DispatcherPrefixService.class, CapabilityService.class, TestCommandRegistry.class};
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        registerTestCommandParserRegistry();

        MailFilterServletInit.getInstance().start();

        getService(CapabilityService.class).declareCapability(MailFilterChecker.CAPABILITY);

        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, MailFilterChecker.CAPABILITY);
        registerService(CapabilityChecker.class, new MailFilterChecker(), properties);
    }

    @Override
    protected void stopBundle() throws Exception {
        Logger LOG = LoggerFactory.getLogger(MailFilterJSONActivator.class);
        try {
            super.stopBundle();
            MailFilterServletInit.getInstance().stop();
            Services.setServiceLookup(null);

            cleanUp();
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
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
