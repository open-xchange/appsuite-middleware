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

package com.openexchange.mail.categories.sieve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.Token;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.Command;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.RuleComment;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.categories.MailCategoriesConstants;
import com.openexchange.mail.categories.MailCategoriesExceptionCodes;
import com.openexchange.mail.categories.ruleengine.MailCategoriesRuleEngine;
import com.openexchange.mail.categories.ruleengine.MailCategoriesRuleEngineExceptionCodes;
import com.openexchange.mail.categories.ruleengine.MailCategoryRule;
import com.openexchange.mail.categories.ruleengine.RuleType;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterProperties;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link SieveMailCategoriesRuleEngine}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class SieveMailCategoriesRuleEngine implements MailCategoriesRuleEngine {

    private final ServiceLookup services;


    /**
     * Initializes a new {@link SieveMailCategoriesRuleEngine}.
     */
    public SieveMailCategoriesRuleEngine(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void setRule(Session session, MailCategoryRule rule, RuleType type) throws OXException {
        setRule(session, rule, type, true);
    }

    public void setRule(Session session, MailCategoryRule rule, RuleType type, boolean reorder) throws OXException {
        MailFilterService mailFilterService = services.getService(MailFilterService.class);
        if (mailFilterService == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailFilterService.class);
        }

        Rule oldRule = null;
        String rulename = rule.getFlag() == null ? MailCategoriesConstants.GENERAL_CATEGORY_ID : rule.getFlag();
        Credentials creds = getCredentials(session);
        try {

            List<Rule> rules = mailFilterService.listRules(creds, type.getName());
            for (Rule tmpRule : rules) {
                if (tmpRule.getRuleComment().getRulename().equals(rulename)) {
                    oldRule = tmpRule;
                    break;
                }
            }

            Rule newRule = mailCategoryRule2SieveRule(rule, type);

            if (oldRule != null) {
                mailFilterService.updateFilterRule(creds, newRule, oldRule.getUniqueId());
            } else {
                mailFilterService.createFilterRule(creds, newRule);
                if (reorder) {
                    mailFilterService.reorderRules(creds, new int[0]);
                }
            }
        } catch (SieveException e) {
            throw MailCategoriesRuleEngineExceptionCodes.UNABLE_TO_SET_RULE.create(e.getMessage());
        }
    }

    @Override
    public void removeRule(Session session, String flag) throws OXException {
        MailFilterService mailFilterService = services.getService(MailFilterService.class);
        if (mailFilterService == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailFilterService.class);
        }

        Credentials creds = getCredentials(session);
        List<Rule> rules = mailFilterService.listRules(creds, "category");
        for (Rule rule : rules) {
            if (rule.getRuleComment().getRulename().equals(flag)) {
                mailFilterService.deleteFilterRule(creds, rule.getUniqueId());
                break;
            }
        }

    }

    private Credentials getCredentials(Session session) {
        String loginName;
        {
            ConfigurationService config = services.getService(ConfigurationService.class);
            String credsrc = config.getProperty(MailFilterProperties.Values.SIEVE_CREDSRC.property);
            loginName = MailFilterProperties.CredSrc.SESSION_FULL_LOGIN.name.equals(credsrc) ? session.getLogin() : session.getLoginName();
        }
        Subject subject = (Subject) session.getParameter("kerberosSubject");
        return new Credentials(loginName, session.getPassword(), session.getUserId(), session.getContextId(), null, subject);
    }

    private Rule mailCategoryRule2SieveRule(MailCategoryRule rule, RuleType type) throws SieveException {
        List<ActionCommand> actionCommands = new ArrayList<>(4);
        if (rule.getFlag() != null) {
            ArrayList<Object> argList = new ArrayList<>(1);
            argList.add(Collections.singletonList(rule.getFlag()));
            ActionCommand addFlagAction = new ActionCommand(ActionCommand.Commands.ADDFLAG, argList);
            actionCommands.add(addFlagAction);
        }

        String[] flagsToRemove = rule.getFlagsToRemove();
        if (flagsToRemove != null && flagsToRemove.length > 0) {
            ArrayList<Object> removeFlagList = new ArrayList<>(flagsToRemove.length);
            for (int i = 0; i < flagsToRemove.length; i++) {
                String flagToRemove = flagsToRemove[i];
                if (Strings.isNotEmpty(flagToRemove)) {
                    removeFlagList.add(flagToRemove);
                }
            }
            if (false == removeFlagList.isEmpty()) {
                ArrayList<Object> removeFlagArgList = new ArrayList<>();
                removeFlagArgList.add(removeFlagList);
                actionCommands.add(0, new ActionCommand(ActionCommand.Commands.REMOVEFLAG, removeFlagArgList));
            }
        }

        IfCommand ifCommand = new IfCommand(getCommand(rule), actionCommands);
        ArrayList<Command> commands = new ArrayList<Command>(Collections.singleton(ifCommand));
        int linenumber = 0;
        boolean commented = false;
        String ruleName = rule.getFlag() == null ? MailCategoriesConstants.GENERAL_CATEGORY_ID : rule.getFlag();
        RuleComment comment = new RuleComment(ruleName);
        comment.setFlags(Collections.singletonList(type.getName()));
        Rule result = new Rule(comment, commands, linenumber, commented);
        return result;
    }

    private TestCommand getCommand(MailCategoryRule rule) throws SieveException {
        if (!rule.hasSubRules()) {

            List<TestCommand> commands = new ArrayList<>(2);

            if (rule.getFlag() == null) {
                commands.add(new TestCommand(Commands.TRUE, Collections.emptyList(), new ArrayList<TestCommand>(0)));
            } else {
                List<Object> flagArgList = new ArrayList<Object>(4);
                flagArgList.add(createTagArgument("is"));
                flagArgList.add(Collections.singletonList(rule.getFlag()));
                commands.add(new TestCommand(Commands.NOT, new ArrayList<>(), Collections.singletonList(new TestCommand(Commands.HASFLAG, flagArgList, new ArrayList<TestCommand>()))));
            }
            List<Object> argList = new ArrayList<Object>(4);
            argList.add(createTagArgument("contains"));
            argList.add(rule.getHeaders());
            argList.add(rule.getValues());
            commands.add(new TestCommand(Commands.HEADER, argList, new ArrayList<TestCommand>()));
            return new TestCommand(Commands.ALLOF, new ArrayList<>(), commands);
        }

        ArrayList<TestCommand> subCommands = new ArrayList<>(rule.getSubRules().size());
        for (MailCategoryRule subTest : rule.getSubRules()) {
            subCommands.add(getCommand(subTest));
        }
        return rule.isAND() ? new TestCommand(Commands.ALLOF, new ArrayList<>(), subCommands) : new TestCommand(Commands.ANYOF, new ArrayList<>(), subCommands);
    }

    /**
     * Creates a {@link TagArgument} from the specified string value
     *
     * @param value The value of the {@link TagArgument}
     * @return the {@link TagArgument}
     */
    private static TagArgument createTagArgument(String value) {
        Token token = new Token();
        token.image = ":" + value;
        return new TagArgument(token);
    }

    @Override
    public MailCategoryRule getRule(Session session, String flag) throws OXException {
        MailFilterService mailFilterService = services.getService(MailFilterService.class);
        if (mailFilterService == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailFilterService.class);
        }

        Credentials creds = getCredentials(session);

        List<Rule> rules = mailFilterService.listRules(creds, "category");
        String name = flag;
        if (flag == null) {
            name = MailCategoriesConstants.GENERAL_CATEGORY_ID;
        }
        for (Rule tmpRule : rules) {
            if (tmpRule.getRuleComment().getRulename().equals(name)) {
                return parseRootRule(tmpRule.getTestCommand(), flag);
            }
        }
        return null;
    }

    private MailCategoryRule parseRootRule(TestCommand command, String flag) throws OXException {
        if (command == null) {
            throw MailCategoriesRuleEngineExceptionCodes.UNABLE_TO_RETRIEVE_RULE.create();
        }

        //  assume command contains an ALLOF testcommand with a not hasflag test and another test
        if (command.getTestCommands() == null || command.getTestCommands().isEmpty()) {
            throw MailCategoriesRuleEngineExceptionCodes.UNABLE_TO_RETRIEVE_RULE.create();
        } else {
            List<TestCommand> twoCommands = command.getTestCommands();
            if (twoCommands.size() != 2) {
                throw MailCategoriesRuleEngineExceptionCodes.UNABLE_TO_RETRIEVE_RULE.create();
            } else {
                // assume command 0 is the not hasflag command
                TestCommand realTest = twoCommands.get(1);
                return parseRule(realTest, flag);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private MailCategoryRule parseRule(TestCommand command, String flag) throws OXException {
        if (command == null) {
            throw MailCategoriesRuleEngineExceptionCodes.UNABLE_TO_RETRIEVE_RULE.create();
        }

        if (command.getTestCommands() != null && !command.getTestCommands().isEmpty()) {
            boolean isAND = false;
            // Any or All test
            if (command.getCommand().equals(Commands.ALLOF)) {
                isAND = true;
            }
            MailCategoryRule result = new MailCategoryRule(flag, isAND);
            for (TestCommand com : command.getTestCommands()) {
                result.addSubRule(parseRule(com, flag));
            }
            return result;
        }

        // header command
        if (!command.getCommand().equals(Commands.HEADER)) {
            throw MailCategoriesRuleEngineExceptionCodes.UNABLE_TO_RETRIEVE_RULE.create();
        }

        List<Object> argList = command.getArguments();
        if (argList == null || argList.isEmpty() || argList.size() != 3) {
            throw MailCategoriesRuleEngineExceptionCodes.UNABLE_TO_RETRIEVE_RULE.create();
        }

        List<String> headers = (List<String>) argList.get(1);
        List<String> values = (List<String>) argList.get(2);
        return new MailCategoryRule(headers, values, flag);

    }

    @Override
    public void removeValueFromHeader(Session session, String value, String header) throws OXException {
        MailFilterService mailFilterService = services.getService(MailFilterService.class);
        if (mailFilterService == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailFilterService.class);
        }
        Credentials creds = getCredentials(session);
        List<Rule> rules = mailFilterService.listRules(creds, "category");
        List<Rule> rules2update = new ArrayList<>();
        for (Rule rule : rules) {

            TestCommand test = rule.getTestCommand();
            Map<TestCommand, List<TestCommand>> toDeleteMap = new HashMap<>();
            boolean removed = removeValueFromHeader(null, test, value, header, toDeleteMap, rule.getIfCommand());
            if (removed) {
                rules2update.add(rule);
            }
            for (TestCommand parent : toDeleteMap.keySet()) {
                for (TestCommand deleteEntry : toDeleteMap.get(parent)) {
                    parent.removeTestCommand(deleteEntry);
                }
            }
        }

        for (Rule rule : rules2update) {
            TestCommand testCom = rule.getTestCommand();
            if (testCom != null) {
                if (testCom.getCommand() == TestCommand.Commands.ANYOF || testCom.getCommand() == TestCommand.Commands.ALLOF) {
                    if (testCom.getTestCommands().isEmpty()) {
                        mailFilterService.deleteFilterRule(creds, rule.getUniqueId());
                        continue;
                    } else {
                        // test whether it contains only the hasflag check
                        if (testCom.getTestCommands().size() == 1 && testCom.getTestCommands().get(0).getCommand().equals(Commands.NOT)) {
                            TestCommand com = testCom.getTestCommands().get(0);
                            if (com.getTestCommands().size() == 1 && com.getTestCommands().get(0).getCommand().equals(Commands.HASFLAG)) {
                                mailFilterService.deleteFilterRule(creds, rule.getUniqueId());
                                continue;
                            }
                        }
                        // test whether it is a empty rule of the 'general' category
                        if (testCom.getTestCommands().size() == 1 && testCom.getTestCommands().get(0).getCommand().equals(Commands.TRUE)) {
                            mailFilterService.deleteFilterRule(creds, rule.getUniqueId());
                            continue;
                        }
                    }
                }
            } else {
                mailFilterService.deleteFilterRule(creds, rule.getUniqueId());
                continue;
            }
            mailFilterService.updateFilterRule(creds, rule, rule.getUniqueId());
        }

    }

    @SuppressWarnings("unchecked")
    private boolean removeValueFromHeader(TestCommand parent, TestCommand child, String value, String header, Map<TestCommand, List<TestCommand>> deleteMap, IfCommand root) {
        boolean result = false;
        List<TestCommand> commands = child.getTestCommands();
        if (commands != null && commands.isEmpty() == false) {

            for (TestCommand subchild : commands) {
                boolean tmpResult = removeValueFromHeader(child, subchild, value, header, deleteMap, root);
                if (tmpResult) {
                    result = true;
                }
            }

        } else {
            List<Object> args = child.getArguments();
            if (!args.isEmpty()) {
                List<String> headers = (List<String>) args.get(1);
                if (headers.contains(header)) {
                    List<String> values = (List<String>) args.get(2);
                    while (values.contains(value)) {
                        boolean tmpResult = values.remove(value);
                        if (tmpResult == true) {
                            result = true;
                        }
                    }
                    if (values.isEmpty()) {
                        if (parent != null) {
                            List<TestCommand> deleteEntries = deleteMap.get(parent);
                            if (deleteEntries == null) {
                                deleteEntries = new ArrayList<>();
                                deleteMap.put(parent, deleteEntries);
                            }
                            deleteEntries.add(child);
                        } else {
                            root.setTestcommand(null);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void initRuleEngineForUser(final Session session, final List<MailCategoryRule> rules) throws OXException {
        final MailFilterService mailFilterService = services.getService(MailFilterService.class);
        if (mailFilterService == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailFilterService.class);
        }
        final Credentials creds = getCredentials(session);

        // Get old rules
        List<Rule> oldRules = mailFilterService.listRules(creds, RuleType.SYSTEM_CATEGORY.getName());
        final int[] uids = new int[oldRules.size()];
        int x = 0;
        for (Rule rule : oldRules) {
            uids[x++] = rule.getUniqueId();
        }

        // Run task
        if (!rules.isEmpty()) {
            // Remove possible old rules
            if (uids.length > 0) {
                mailFilterService.deleteFilterRules(creds, uids);
            }
            // Create new rules
            for (MailCategoryRule rule : rules) {
                setRule(session, rule, RuleType.SYSTEM_CATEGORY, false);
            }
            mailFilterService.reorderRules(creds, new int[] {});
        }
    }

    @Override
    public void cleanUp(List<String> flags, Session session) throws OXException {
        MailFilterService mailFilterService = services.getService(MailFilterService.class);
        if (mailFilterService == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailFilterService.class);
        }

        Credentials creds = getCredentials(session);
        List<Integer> uidList = new ArrayList<>();
        List<Rule> rules = mailFilterService.listRules(creds, RuleType.CATEGORY.getName());
        for (Rule rule : rules) {
            String name = rule.getRuleComment().getRulename();
            if (!flags.contains(name) && !name.equals(MailCategoriesConstants.GENERAL_CATEGORY_ID)) {
                uidList.add(rule.getRuleComment().getUniqueid());
            }
        }
        if (uidList.isEmpty()) {
            return;
        }
        int[] uids = new int[uidList.size()];
        int x = 0;
        for (Integer i : uidList) {
            uids[x++] = i;
        }

        mailFilterService.deleteFilterRules(creds, uids);

    }
}
