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

package com.openexchange.mail.categories.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.Token;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.Command;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.RuleComment;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.categories.exception.MailCategoriesJSONExceptionCodes;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.HeaderTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;

/**
 * {@link SearchableMailFilterRule}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class SearchableMailFilterRule {

    private static final String SUBTESTS_STR = "subrules";
    private static final String OPERATOR_STR = "operator";
    private static final String HEADER_STR = "header";
    private static final String VALUE_STR = "value";

    private List<SearchableMailFilterRule> subRules;
    private boolean hasSubRules = false;
    private boolean isAND = false;
    private String header;
    private String value;
    private String flag;

    /**
     * Initializes a new {@link SearchableMailFilterRule}.
     * 
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    public SearchableMailFilterRule(JSONObject json, String flag) throws JSONException {
        super();
        this.flag = flag;
        if (json.has(SUBTESTS_STR)) {
            JSONArray array = (JSONArray) json.get(SUBTESTS_STR);
            if (json.has(OPERATOR_STR)) {
                if (json.getString(OPERATOR_STR).equalsIgnoreCase("and")) {
                    isAND = true;
                }
            }
            for (Object subObject : array.asList()) {
                if (!(subObject instanceof Map)) {
                    throw new JSONException("conditions element is not a JSONObject!");
                } else {
                    JSONObject subTest = new JSONObject((Map<String, ? extends Object>) subObject);
                    addSubRule(new SearchableMailFilterRule(subTest, flag));
                }
            }
            return;
        }
        this.header = json.getString(HEADER_STR);
        this.value = json.getString(VALUE_STR);

    }

    /**
     * Initializes a new {@link SearchableMailFilterRule}.
     * 
     * @param oldRule
     */
    @SuppressWarnings("unchecked")
    public SearchableMailFilterRule(TestCommand command, String flag) throws OXException {
        if (command == null) {
            throw MailCategoriesJSONExceptionCodes.UNABLE_TO_PARSE_TEST_COMMAND.create();
        }
        this.flag = flag;

        if (command.getTestCommands() != null && !command.getTestCommands().isEmpty()) {
            // Any or All test
            if (command.getCommand().equals(Commands.ALLOF)) {
                isAND = true;
            }

            for (TestCommand com : command.getTestCommands()) {
                this.addSubRule(new SearchableMailFilterRule(com, flag));
            }

        } else {
            // header command
            if (!command.getCommand().equals(Commands.HEADER)) {
                throw MailCategoriesJSONExceptionCodes.UNABLE_TO_PARSE_TEST_COMMAND.create();
            }

            List<Object> argList = command.getArguments();
            if (argList == null || argList.isEmpty() || argList.size() != 3) {
                throw MailCategoriesJSONExceptionCodes.UNABLE_TO_PARSE_TEST_COMMAND.create();
            }

            List<String> list = (List<String>) argList.get(1);
            this.header = list.get(0);
            list = (List<String>) argList.get(2);
            this.value = list.get(0);
        }

    }

    private void addSubRule(SearchableMailFilterRule rule) {
        if (this.subRules == null) {
            subRules = new ArrayList<>();
            subRules.add(rule);
            hasSubRules = true;
        } else {
            subRules.add(rule);
        }
    }

    public SearchTerm<?> getSearchTerm() {
        if (hasSubRules) {
            SearchTerm<?> result = null;
            if (isAND) {
                for (SearchableMailFilterRule subRule : subRules) {
                    if (result == null) {
                        result = subRule.getSearchTerm();
                    } else {
                        result = new ANDTerm(result, subRule.getSearchTerm());
                    }
                }
            } else {
                for (SearchableMailFilterRule subRule : subRules) {
                    if (result == null) {
                        result = subRule.getSearchTerm();
                    } else {
                        result = new ORTerm(result, subRule.getSearchTerm());
                    }
                }
            }
            return result;
        } else {
            return new HeaderTerm(header, value);
        }
    }

    public Rule getRule() throws SieveException {
        ArrayList<Object> argList = new ArrayList<>();
        List<String> flagList = new ArrayList<>();
        flagList.add(flag);
        argList.add(flagList);
        ActionCommand addFlagAction = new ActionCommand(ActionCommand.Commands.ADDFLAG, argList);
        IfCommand ifCommand = new IfCommand(getCommand(), Collections.singletonList(addFlagAction));
        ArrayList<Command> commands = new ArrayList<Command>(Collections.singleton(ifCommand));
        int linenumber = 0;
        boolean commented = false;
        RuleComment comment = new RuleComment(flag);
        comment.setFlags(Collections.singletonList("category"));
        Rule result = new Rule(comment, commands, linenumber, commented);
        return result;
    }

    private TestCommand getCommand() throws SieveException {
        if (hasSubRules) {

            ArrayList<TestCommand> subCommands = new ArrayList<>(subRules.size());
            for (SearchableMailFilterRule subtest : subRules) {
                subCommands.add(subtest.getCommand());
            }
            if (isAND) {
                return new TestCommand(Commands.ALLOF, new ArrayList<>(), subCommands);
            } else {
                return new TestCommand(Commands.ANYOF, new ArrayList<>(), subCommands);
            }
        } else {

            final List<Object> argList = new ArrayList<Object>();
            argList.add(createTagArgument("contains"));
            argList.add(Collections.singletonList(header));
            argList.add(Collections.singletonList(value));
            return new TestCommand(Commands.HEADER, argList, new ArrayList<TestCommand>());
        }
    }

    /**
     * Creates a {@link TagArgument} from the specified string value
     * 
     * @param value The value of the {@link TagArgument}
     * @return the {@link TagArgument}
     */
    private static final TagArgument createTagArgument(String value) {
        Token token = new Token();
        token.image = ":" + value;
        return new TagArgument(token);
    }

}
