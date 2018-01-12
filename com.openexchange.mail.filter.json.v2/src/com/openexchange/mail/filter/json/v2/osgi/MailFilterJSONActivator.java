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
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.jsieve.registry.ActionCommandRegistry;
import com.openexchange.jsieve.registry.TestCommandRegistry;
import com.openexchange.mail.filter.json.v2.actions.MailFilterActionFactory;
import com.openexchange.mail.filter.json.v2.json.RuleParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ActionCommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParser;
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
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.simplified.SimplifiedAction;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.AddressTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.AllOfTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.AnyOfTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.BodyTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.CurrentDateTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.DateTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.EnvelopeTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.ExistsTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.FalseTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.HasFlagCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.NotTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.SizeTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.TrueTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.simplified.SimplifiedHeaderTest;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.simplified.SimplifiedHeaderTestParser;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link MailFilterJSONActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
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
        return new Class<?>[] { ConfigurationService.class, MailFilterService.class, HttpService.class, SessiondService.class, DispatcherPrefixService.class, CapabilityService.class, TestCommandRegistry.class, ActionCommandRegistry.class, LeanConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerTestCommandParserRegistry();
        registerActionCommandParserRegistry();
        openTrackers();

        getService(CapabilityService.class).declareCapability(MailFilterChecker.CAPABILITY);

        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, MailFilterChecker.CAPABILITY);
        registerService(CapabilityChecker.class, new MailFilterChecker(), properties);

        RuleParser ruleParser = RuleParser.newInstance(this);
        registerModule(MailFilterActionFactory.newInstance(ruleParser, this), "mailfilter/v2");
    }

    /**
     * Registers the {@link TestCommandParserRegistry} along with all available {@link TestCommandParser}s
     */
    private void registerTestCommandParserRegistry() {
        SimplifiedHeaderTestParser simplifiedHeaderTestParser = SimplifiedHeaderTestParser.newInstance(this);

        TestCommandParserRegistry registry = new TestCommandParserRegistry();
        registry.register(Commands.ADDRESS.getCommandName(), new AddressTestCommandParser(this));
        registry.register(Commands.ALLOF.getCommandName(), new AllOfTestCommandParser(this));
        registry.register(Commands.ANYOF.getCommandName(), new AnyOfTestCommandParser(this));
        registry.register(Commands.BODY.getCommandName(), new BodyTestCommandParser(this));
        registry.register(Commands.DATE.getCommandName(), new DateTestCommandParser(this));
        registry.register(Commands.EXISTS.getCommandName(), new ExistsTestCommandParser(this));
        registry.register(Commands.CURRENTDATE.getCommandName(), new CurrentDateTestCommandParser(this));
        registry.register(Commands.ENVELOPE.getCommandName(), new EnvelopeTestCommandParser(this));
        registry.register(Commands.HEADER.getCommandName(), simplifiedHeaderTestParser);
        registry.register(Commands.NOT.getCommandName(), new NotTestCommandParser(this));
        registry.register(Commands.SIZE.getCommandName(), new SizeTestCommandParser(this));
        registry.register(Commands.TRUE.getCommandName(), new TrueTestCommandParser(this));
        registry.register(Commands.FALSE.getCommandName(), new FalseTestCommandParser(this));
        registry.register(Commands.HASFLAG.getCommandName(), new HasFlagCommandParser(this));

        registry.register(SimplifiedHeaderTest.From.getCommandName(), simplifiedHeaderTestParser);
        registry.register(SimplifiedHeaderTest.To.getCommandName(), simplifiedHeaderTestParser);
        registry.register(SimplifiedHeaderTest.Cc.getCommandName(), simplifiedHeaderTestParser);
        registry.register(SimplifiedHeaderTest.Subject.getCommandName(), simplifiedHeaderTestParser);
        registry.register(SimplifiedHeaderTest.AnyRecipient.getCommandName(), simplifiedHeaderTestParser);
        registry.register(SimplifiedHeaderTest.MailingList.getCommandName(), simplifiedHeaderTestParser);

        registerService(TestCommandParserRegistry.class, registry);
        trackService(TestCommandParserRegistry.class);

    }

    /**
     * Registers the {@link ActionCommandParserRegistry} along with all available {@link ActionCommandParser}s
     */
    private void registerActionCommandParserRegistry() {
        ActionCommandParserRegistry registry = new ActionCommandParserRegistry();
        registry.register(ActionCommand.Commands.KEEP.getJsonName(), new KeepActionCommandParser(this));
        registry.register(ActionCommand.Commands.DISCARD.getJsonName(), new DiscardActionCommandParser(this));
        registry.register(ActionCommand.Commands.REDIRECT.getJsonName(), new RedirectActionCommandParser(this));
        registry.register(ActionCommand.Commands.REJECT.getJsonName(), new RejectActionCommandParser(this));
        registry.register(ActionCommand.Commands.FILEINTO.getJsonName(), new FileIntoActionCommandParser(this));
        registry.register(ActionCommand.Commands.STOP.getJsonName(), new StopActionCommandParser(this));
        registry.register(ActionCommand.Commands.VACATION.getJsonName(), new VacationActionCommandParser(this));
        registry.register(ActionCommand.Commands.ENOTIFY.getJsonName(), new EnotifyActionCommandParser(this));
        registry.register(ActionCommand.Commands.SETFLAG.getJsonName(), new SetFlagActionCommandParser(this));
        registry.register(ActionCommand.Commands.ADDFLAG.getJsonName(), new AddFlagActionCommandParser(this));
        registry.register(ActionCommand.Commands.REMOVEFLAG.getJsonName(), new RemoveFlagActionCommandParser(this));
        registry.register(ActionCommand.Commands.PGP_ENCRYPT.getJsonName(), new PGPEncryptActionCommandParser(this));

        registry.register(SimplifiedAction.COPY.getCommandName(), new FileIntoActionCommandParser(this));

        registerService(ActionCommandParserRegistry.class, registry);
        trackService(ActionCommandParserRegistry.class);
    }
}
