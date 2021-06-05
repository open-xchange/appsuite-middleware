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

package com.openexchange.mail.filter.json.v2.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceReference;
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
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.SetActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.SetFlagActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.StopActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.VacationActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.external.FilterActionParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.external.FilterActionRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.external.SieveFilterAction;
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
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.StringTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.TrueTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.external.ExecuteTestParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.external.ExecuteTestRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.external.SieveExecuteTest;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.simplified.SimplifiedHeaderTest;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.test.simplified.SimplifiedHeaderTestParser;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.osgi.SimpleRegistryListener;
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
        return new Class<?>[] { ConfigurationService.class, MailFilterService.class, HttpService.class, SessiondService.class, DispatcherPrefixService.class, CapabilityService.class, TestCommandRegistry.class, ActionCommandRegistry.class,
            LeanConfigurationService.class };
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

        final TestCommandParserRegistry registry = new TestCommandParserRegistry();
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
        registry.register(Commands.STRING.getCommandName(), new StringTestCommandParser(this));
        // Commands that use the execute sieve action
        final ExecuteTestRegistry executeRegistry = new ExecuteTestRegistry(this);
        registerService(ExecuteTestRegistry.class, executeRegistry);
        final ExecuteTestParser parser = new ExecuteTestParser(this, executeRegistry);
        registry.register(executeRegistry.getCommandName(), parser);
        getService(TestCommandRegistry.class).register(executeRegistry.getCommandName(), executeRegistry);
        track(SieveExecuteTest.class, new SimpleRegistryListener<SieveExecuteTest>() {
            @Override
            public void added(final ServiceReference<SieveExecuteTest> ref, final SieveExecuteTest service) {
                executeRegistry.registerService(service);
                registry.register(service.getJsonName(), parser);
            }

            @Override
            public void removed(final ServiceReference<SieveExecuteTest> ref, final SieveExecuteTest service) {
                executeRegistry.unRegisterService(service);
                registry.unregister(service.getJsonName());
            }
        });

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
        registry.register(ActionCommand.Commands.SET.getJsonName(), new SetActionCommandParser(this));

        // Commands that use the sieve filter command
        final FilterActionRegistry filterRegistry = new FilterActionRegistry();
        registerService(FilterActionRegistry.class, filterRegistry);
        final FilterActionParser filterParser = new FilterActionParser(this, filterRegistry);
        registry.register(filterRegistry.getCommandName(), filterParser);
        track(SieveFilterAction.class, new SimpleRegistryListener<SieveFilterAction>() {

            @Override
            public void added(final ServiceReference<SieveFilterAction> ref, final SieveFilterAction service) {
                filterRegistry.registerService(service);
                registry.register(service.getJsonName(), filterParser);
            }

            @Override
            public void removed(final ServiceReference<SieveFilterAction> ref, final SieveFilterAction service) {
                filterRegistry.unRegisterService(service);
                registry.unregister(service.getJsonName());

            }
        });

        registerService(ActionCommandParserRegistry.class, registry);
        trackService(ActionCommandParserRegistry.class);
    }
}
