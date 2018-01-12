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

package com.openexchange.mail.filter.json.v2.actions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.JSONMatchType;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.test.ITestCommand;
import com.openexchange.mail.filter.json.v2.Action;
import com.openexchange.mail.filter.json.v2.config.Blacklist;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.BasicGroup;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.Field;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistService;
import com.openexchange.mail.filter.json.v2.config.OptionsProperty;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ActionCommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ExtendedFieldTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.action.simplified.SimplifiedAction;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ConfigMailFilterAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class ConfigMailFilterAction extends AbstractMailFilterAction {

    public static final Action ACTION = Action.CONFIG;
    MailFilterBlacklistService blacklistService;

    /**
     * Initializes a new {@link ConfigMailFilterAction}.
     */
    public ConfigMailFilterAction(ServiceLookup services) {
        super(services);
        this.blacklistService = new MailFilterBlacklistService(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        final Credentials credentials = new Credentials(session);
        final MailFilterService mailFilterService = services.getService(MailFilterService.class);
        final Set<String> capabilities = mailFilterService.getCapabilities(credentials);
        final Blacklist blacklists = blacklistService.getBlacklists(credentials.getUserid(), credentials.getContextid());

        try {
            final JSONObject result = getTestAndActionObjects(capabilities, blacklists);
            result.put("options", getOptions(session.getUserId(), session.getContextId(), mailFilterService.getExtendedProperties(credentials)));
            return new AJAXRequestResult(result);
        } catch (JSONException e) {
            throw MailFilterExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Fills up the config object filtered by blacklisted elements.
     *
     * @param hashSet A set of sieve capabilities
     * @param blacklists A {@link Blacklist} containing mail filter elements which shouldn't be exposed to clients
     * @return A json object containing the test and action objects
     * @throws JSONException
     * @throws OXException
     */
    private JSONObject getTestAndActionObjects(final Set<String> capabilities, Blacklist blacklists) throws JSONException, OXException {
        final JSONObject retval = new JSONObject();
        retval.put("tests", getTestArray(capabilities, blacklists));
        retval.put("actioncmds", getActionArray(capabilities, blacklists));
        return retval;
    }

    /**
     * Returns an options object containing additional options for the client.
     *
     * @param userId The userId
     * @param contextId The contextId
     * @return The options object
     * @throws JSONException
     */
    private JSONObject getOptions(int userId, int contextId, Map<String, Object> options) throws JSONException {
        JSONObject result = new JSONObject();
        LeanConfigurationService leanConfigurationService = services.getService(LeanConfigurationService.class);
        boolean property = leanConfigurationService.getBooleanProperty(userId, contextId, OptionsProperty.allowNestedTests);
        result.put(OptionsProperty.allowNestedTests.name(), property);
        if (options != null) {
            for (Entry<String, Object> entry : options.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private JSONArray getTestArray(final Set<String> capabilities, Blacklist blacklists) throws OXException, JSONException {
        TestCommandParserRegistry service = services.getService(TestCommandParserRegistry.class);
        final JSONArray testarray = new JSONArray();
        Map<String, TestCommandParser<TestCommand>> map = service.getCommandParsers();
        for (Map.Entry<String, TestCommandParser<TestCommand>> entry : map.entrySet()) {
            TestCommandParser<TestCommand> parser = entry.getValue();
            if (!blacklists.isBlacklisted(BasicGroup.tests, entry.getKey()) && parser.isCommandSupported(capabilities)) {
                ITestCommand command = parser.getCommand();
                final JSONObject object = new JSONObject();
                final JSONArray comparison = new JSONArray();
                object.put("id", entry.getKey());
                final List<JSONMatchType> jsonMatchTypes = command.getJsonMatchTypes();
                if (null != jsonMatchTypes) {
                    for (final JSONMatchType matchtype : jsonMatchTypes) {
                        final String value = matchtype.getRequired();
                        if (!blacklists.isBlacklisted(BasicGroup.tests, entry.getKey(), Field.comparisons, matchtype.getJsonName()) && !blacklists.isBlacklisted(BasicGroup.comparisons, matchtype.getJsonName())) {
                            if (matchtype.getVersionRequirement() <= 2 && ("".equals(value) || capabilities.contains(value))) {
                                comparison.put(matchtype.getJsonName());
                            }
                        }
                    }
                }
                if (!comparison.isEmpty()) {
                    object.put("comparisons", comparison);
                }

                // add additional fields
                if (parser instanceof ExtendedFieldTestCommandParser) {

                    Map<String, Set<String>> addtionalFields = ((ExtendedFieldTestCommandParser) parser).getAddtionalFields(capabilities);
                    for (String key : addtionalFields.keySet()) {
                        Field ele = Field.getFieldByName(key);
                        Set<String> listToAdd = addtionalFields.get(key);
                        Set<String> eleBlacklist = blacklists.get(BasicGroup.tests, entry.getKey(), ele);
                        if (eleBlacklist != null) {
                            listToAdd.removeAll(eleBlacklist);
                        }
                        if (!listToAdd.isEmpty()) {
                            object.put(key, listToAdd);
                        }
                    }

                }

                testarray.put(object);
            }
        }
        return testarray;
    }

    private JSONArray getActionArray(final Set<String> capabilities, Blacklist blacklists) throws OXException, JSONException {
        ActionCommandParserRegistry service = services.getService(ActionCommandParserRegistry.class);
        final JSONArray testarray = new JSONArray();
        Map<String, ActionCommandParser<ActionCommand>> map = service.getCommandParsers();
        for (Map.Entry<String, ActionCommandParser<ActionCommand>> entry : map.entrySet()) {
            ActionCommandParser<ActionCommand> parser = entry.getValue();

            // Check if the tag arguments of the simplified action command contains any 
            // of the sieve announced capabilities
            try {
                SimplifiedAction simplifiedAction = SimplifiedAction.valueOf(entry.getKey().toUpperCase());
                // skip if the capability is not announced by the sieve server
                if (false == capabilities.containsAll(simplifiedAction.requiredCapabilities())) {
                    continue;
                }
            } catch (IllegalArgumentException e) {
                //ignore, obviously the specified entry is not a simplified command
            }

            if (!blacklists.isBlacklisted(BasicGroup.actions, entry.getKey()) && parser.isCommandSupported(capabilities)) {
                final JSONObject object = new JSONObject();
                object.put("id", entry.getKey());
                testarray.put(object);
            }
        }
        return testarray;
    }

}
