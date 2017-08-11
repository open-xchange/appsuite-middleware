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
import java.util.Set;
import org.apache.commons.collections.map.MultiKeyMap;
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
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistService;
import com.openexchange.mail.filter.json.v2.config.OptionsProperty;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.BasicGroup;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.Field;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ActionCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ActionCommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.ExtendedFieldTestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParserRegistry;
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
public class ConfigMailFilterAction extends AbstractMailFilterAction{

    public static final Action ACTION = Action.CONFIG;

    /**
     * Initializes a new {@link ConfigMailFilterAction}.
     */
    public ConfigMailFilterAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        final Credentials credentials = new Credentials(session);
        final MailFilterService mailFilterService = services.getService(MailFilterService.class);
        final Set<String> capabilities = mailFilterService.getCapabilities(credentials);
        final MultiKeyMap blacklists = MailFilterBlacklistService.getInstance().getBlacklists(credentials);

        try {
            final JSONObject result = getTestAndActionObjects(capabilities, blacklists);
            result.put("options", getOptions(session.getUserId(), session.getContextId()));
            return new AJAXRequestResult(result);
        } catch (JSONException e) {
            throw MailFilterExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Fills up the config object filtered by blacklisted elements.
     *
     * @param hashSet A set of sieve capabilities
     * @param blacklists A map of blacklisted elements
     * @return
     * @throws JSONException
     * @throws OXException
     */
    private JSONObject getTestAndActionObjects(final Set<String> capabilities, MultiKeyMap blacklists) throws JSONException, OXException {
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
    private JSONObject getOptions(int userId, int contextId) throws JSONException {
        JSONObject result = new JSONObject();
        LeanConfigurationService leanConfigurationService = services.getService(LeanConfigurationService.class);
        boolean property = leanConfigurationService.getBooleanProperty(userId, contextId, OptionsProperty.allowNestedTests);
        result.put(OptionsProperty.allowNestedTests.name(), property);
        return result;
    }

    @SuppressWarnings("unchecked")
    private JSONArray getTestArray(final Set<String> capabilities, MultiKeyMap blacklists) throws OXException, JSONException {
        TestCommandParserRegistry service = services.getService(TestCommandParserRegistry.class);
        final JSONArray testarray = new JSONArray();
        Map<String, TestCommandParser<TestCommand>> map = service.getCommandParsers();
        Set<String> basicTestBlacklist = (Set<String>) blacklists.get(BasicGroup.tests, null, null);
        Set<String> basicComparisonBlacklist = (Set<String>) blacklists.get(BasicGroup.comparisons, null, null);
        for (Map.Entry<String, TestCommandParser<TestCommand>> entry : map.entrySet()) {
            TestCommandParser<TestCommand> parser = entry.getValue();
            if ((null == basicTestBlacklist || !basicTestBlacklist.contains(entry.getKey())) && parser.isCommandSupported(capabilities)) {
                ITestCommand command = parser.getCommand();
                final JSONObject object = new JSONObject();
                final JSONArray comparison = new JSONArray();
                object.put("id", entry.getKey());
                final List<JSONMatchType> jsonMatchTypes = command.getJsonMatchTypes();
                if (null != jsonMatchTypes) {
                    Set<String> subComparisonBlackList = null;
                    if(blacklists.containsKey(BasicGroup.tests, entry.getKey(), Field.comparisons )) {
                        subComparisonBlackList = (Set<String>) blacklists.get(BasicGroup.tests, entry.getKey(), Field.comparisons);
                    }
                    for (final JSONMatchType matchtype : jsonMatchTypes) {
                        final String value = matchtype.getRequired();
                        if ((null == basicComparisonBlacklist || !basicComparisonBlacklist.contains(matchtype.getJsonName())) && matchtype.getVersionRequirement()<=2 && ("".equals(value) || capabilities.contains(value))) {
                            if(null == subComparisonBlackList || !subComparisonBlackList.contains(matchtype.getJsonName())){
                                comparison.put(matchtype.getJsonName());
                            }
                        }
                    }
                }
                if(!comparison.isEmpty()){
                    object.put("comparisons", comparison);
                }

                // add additional fields
                if(parser instanceof ExtendedFieldTestCommandParser){

                    Map<String, Set<String>> addtionalFields = ((ExtendedFieldTestCommandParser) parser).getAddtionalFields(capabilities);
                    for(String key: addtionalFields.keySet()){
                        Field ele = Field.getFieldByName(key);
                        Set<String> listToAdd = addtionalFields.get(key);
                        if(blacklists.containsKey(BasicGroup.tests, entry.getKey(), ele)){
                            Set<String> eleBlacklist = (Set<String>) blacklists.get(BasicGroup.tests, entry.getKey(), ele);
                            listToAdd.removeAll(eleBlacklist);
                        }
                        if(!listToAdd.isEmpty()){
                            object.put(key, listToAdd);
                        }
                    }

                }

                testarray.put(object);
            }
        }
        return testarray;
    }

    @SuppressWarnings("unchecked")
    private JSONArray getActionArray(final Set<String> capabilities, MultiKeyMap blacklists) throws OXException, JSONException {

        ActionCommandParserRegistry service = services.getService(ActionCommandParserRegistry.class);
        final JSONArray testarray = new JSONArray();
        Set<String> basicActionBlacklist = (Set<String>) blacklists.get(BasicGroup.actions, null, null);
        Map<String, ActionCommandParser<ActionCommand>> map = service.getCommandParsers();
        for (Map.Entry<String, ActionCommandParser<ActionCommand>> entry : map.entrySet()) {
            ActionCommandParser<ActionCommand> parser = entry.getValue();

            if ((basicActionBlacklist == null || !basicActionBlacklist.contains(entry.getKey())) && parser.isCommandSupported(capabilities)) {
                final JSONObject object = new JSONObject();
                object.put("id", entry.getKey());
                testarray.put(object);
            }
        }
        return testarray;
    }

}
