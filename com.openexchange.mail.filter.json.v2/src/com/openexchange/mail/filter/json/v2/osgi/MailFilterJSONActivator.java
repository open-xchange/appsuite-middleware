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

package com.openexchange.mail.filter.json.v2.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.http.HttpService;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.jsieve.registry.ActionCommandRegistry;
import com.openexchange.jsieve.registry.TestCommandRegistry;
import com.openexchange.mail.filter.json.v2.actions.MailFilterActionFactory;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ActionCommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.AddFlagActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.DiscardActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.EnotifyActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.FileIntoActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.KeepActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.PGPEncryptActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.RedirectActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.RejectActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.RemoveFlagActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.SetFlagActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.StopActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.VacationActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.AddressTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.AllOfTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.AnyOfTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.BodyTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.CurrentDateTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.DateTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.EnvelopeTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.ExistsTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.HasFlagCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.NotTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.SizeTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.TrueTestCommandParser;
import com.openexchange.mail.filter.json.v2.mapper.parser.test.simplified.SimplifiedHeaderTestParser;
import com.openexchange.mail.filter.json.v2.mapper.parser.test.simplified.SimplifiedHeaderTest;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link MailFilterJSONActivator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class MailFilterJSONActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link MailFilterJSONActivator}.
     */
    public MailFilterJSONActivator() {
        super();

    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, MailFilterService.class, HttpService.class, SessiondService.class, DispatcherPrefixService.class, CapabilityService.class, TestCommandRegistry.class, ActionCommandRegistry.class};
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        registerTestCommandParserRegistry();
        registerActionCommandParserRegistry();

        getService(CapabilityService.class).declareCapability(MailFilterChecker.CAPABILITY);

        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, MailFilterChecker.CAPABILITY);
        registerService(CapabilityChecker.class, new MailFilterChecker(), properties);
        registerModule(MailFilterActionFactory.getInstance(), "mailfilter/v2");

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
        registry.register(Commands.HEADER.getCommandName(), SimplifiedHeaderTestParser.getInstance());
        registry.register(Commands.NOT.getCommandName(), new NotTestCommandParser());
        registry.register(Commands.SIZE.getCommandName(), new SizeTestCommandParser());
        registry.register(Commands.TRUE.getCommandName(), new TrueTestCommandParser());
        registry.register(Commands.HASFLAG.getCommandName(), new HasFlagCommandParser());

        registry.register(SimplifiedHeaderTest.From.getCommandName(), SimplifiedHeaderTestParser.getInstance());
        registry.register(SimplifiedHeaderTest.To.getCommandName(), SimplifiedHeaderTestParser.getInstance());
        registry.register(SimplifiedHeaderTest.Cc.getCommandName(), SimplifiedHeaderTestParser.getInstance());
        registry.register(SimplifiedHeaderTest.Subject.getCommandName(), SimplifiedHeaderTestParser.getInstance());
        registry.register(SimplifiedHeaderTest.AnyRecipient.getCommandName(), SimplifiedHeaderTestParser.getInstance());
        registry.register(SimplifiedHeaderTest.MailingList.getCommandName(), SimplifiedHeaderTestParser.getInstance());

        registerService(TestCommandParserRegistry.class, registry);
        trackService(TestCommandParserRegistry.class);
        openTrackers();
    }

    /**
     * Registers the {@link ActionCommandParserRegistry} along with all available {@link ActionCommandParser}s
     */
    private void registerActionCommandParserRegistry() {
        ActionCommandParserRegistry registry = new ActionCommandParserRegistry();
        registry.register(ActionCommand.Commands.KEEP.getJsonName(), new KeepActionCommandParser());
        registry.register(ActionCommand.Commands.DISCARD.getJsonName(), new DiscardActionCommandParser());
        registry.register(ActionCommand.Commands.REDIRECT.getJsonName(), new RedirectActionCommandParser());
        registry.register(ActionCommand.Commands.REJECT.getJsonName(), new RejectActionCommandParser());
        registry.register(ActionCommand.Commands.FILEINTO.getJsonName(), new FileIntoActionCommandParser());
        registry.register(ActionCommand.Commands.STOP.getJsonName(), new StopActionCommandParser());
        registry.register(ActionCommand.Commands.VACATION.getJsonName(), new VacationActionCommandParser());
        registry.register(ActionCommand.Commands.ENOTIFY.getJsonName(), new EnotifyActionCommandParser());
        registry.register(ActionCommand.Commands.SETFLAG.getJsonName(), new SetFlagActionCommandParser());
        registry.register(ActionCommand.Commands.ADDFLAG.getJsonName(), new AddFlagActionCommandParser());
        registry.register(ActionCommand.Commands.REMOVEFLAG.getJsonName(), new RemoveFlagActionCommandParser());
        registry.register(ActionCommand.Commands.PGP_ENCRYPT.getJsonName(), new PGPEncryptActionCommandParser());

        registerService(ActionCommandParserRegistry.class, registry);
        trackService(ActionCommandParserRegistry.class);
        openTrackers();
    }
}
