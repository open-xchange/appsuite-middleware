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

package com.openexchange.mailfilter.json.ajax.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.test.ITestCommand;
import com.openexchange.jsieve.registry.TestCommandRegistry;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.mailfilter.MailFilterService.FilterType;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.json.ajax.Parameter;
import com.openexchange.mailfilter.json.ajax.actions.AbstractRequest.Parameters;
import com.openexchange.mailfilter.json.ajax.json.RuleParser;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
 */
public class MailFilterAction extends AbstractAction<Rule, MailFilterRequest> {

    private static final MailFilterAction INSTANCE = new MailFilterAction();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static MailFilterAction getInstance() {
        return INSTANCE;
    }

    private static final RuleParser CONVERTER = new RuleParser();

    // -------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Default constructor.
     */
    public MailFilterAction() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONObject actionConfig(final MailFilterRequest request) throws OXException {
        final Credentials credentials = request.getCredentials();
        final MailFilterService mailFilterService = Services.getService(MailFilterService.class);
        final Set<String> capabilities = mailFilterService.getCapabilities(credentials);
        try {
            final JSONObject tests = getTestAndActionObjects(capabilities);
            return tests;
        } catch (JSONException e) {
            throw MailFilterExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void actionDelete(final MailFilterRequest request) throws OXException {
        final Credentials credentials = request.getCredentials();
        final MailFilterService mailFilterService = Services.getService(MailFilterService.class);
        try {
            final JSONObject json = getJsonBody(request);
            final int uid = getUniqueId(json);
            mailFilterService.deleteFilterRule(credentials, uid);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONArray actionList(final MailFilterRequest request) throws OXException {
        final Parameters parameters = request.getParameters();
        final Credentials credentials = request.getCredentials();
        final MailFilterService mailFilterService = Services.getService(MailFilterService.class);
        final String flag = parameters.getParameter(Parameter.FLAG);
        FilterType filterType;
        if (flag != null) {
            try {
                filterType = FilterType.valueOf(flag);
            } catch (IllegalArgumentException e) {
                throw MailFilterExceptionCode.INVALID_FILTER_TYPE_FLAG.create(flag);
            }
        } else {
            filterType = FilterType.all;
        }
        final List<Rule> rules = mailFilterService.listRules(credentials, filterType);
        try {
            return CONVERTER.write(rules.toArray(new Rule[rules.size()]));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int actionNew(final MailFilterRequest request) throws OXException {
        final MailFilterService mailFilterService = Services.getService(MailFilterService.class);
        try {
            final Credentials credentials = request.getCredentials();
            final Rule rule = CONVERTER.parse(getJsonBody(request));
            return mailFilterService.createFilterRule(credentials, rule);
        } catch (final SieveException e) {
            throw MailFilterExceptionCode.handleSieveException(e);
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject getJsonBody(final MailFilterRequest request) throws JSONException, OXException {
        final JSONObject jsonObject = new JSONObject(request.getBody());
        checkJsonValue(jsonObject, null, jsonObject);
        return jsonObject;
    }

    private static final Set<String> MUST_NOT_BE_EMPTY = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("values")));

    private void checkJsonValue(final JSONValue jValue, final String name, final JSONValue parent) throws OXException {
        if (null != jValue) {
            if (jValue.isArray()) {
                final JSONArray jArray = jValue.toArray();
                final int length = jArray.length();
                if (0 == length) {
                    if (null != name && MUST_NOT_BE_EMPTY.contains(name)) {
                        // SIEVE does not support empty arrays
                        throw MailFilterExceptionCode.INVALID_SIEVE_RULE.create(parent.toString());
                    }
                }
                for (int i = 0; i < length; i++) {
                    final Object object = jArray.opt(i);
                    if (object instanceof JSONValue) {
                        checkJsonValue((JSONValue) object, null, parent);
                    }
                }
            } else if (jValue.isObject()) {
                final JSONObject jObject = jValue.toObject();
                for (final Entry<String, Object> entry : jObject.entrySet()) {
                    final Object object = entry.getValue();
                    if (object instanceof JSONValue) {
                        checkJsonValue((JSONValue) object, entry.getKey(), parent);
                    }
                }
            }
        }
    }

    @Override
    protected void actionReorder(final MailFilterRequest request) throws OXException {
        final Credentials credentials = request.getCredentials();
        final MailFilterService mailFilterService = Services.getService(MailFilterService.class);

        try {
            final String body = request.getBody();
            final JSONArray json = new JSONArray(body);
            final int[] uids = new int[json.length()];
            for (int i = 0; i < json.length(); i++) {
                uids[i] = json.getInt(i);
            }
            mailFilterService.reorderRules(credentials, uids);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void actionUpdate(final MailFilterRequest request) throws OXException {
        final Credentials credentials = request.getCredentials();
        final MailFilterService mailFilterService = Services.getService(MailFilterService.class);
        try {
            final JSONObject json = getJsonBody(request);
            final Integer uid = getUniqueId(json);
            final Rule rule = mailFilterService.getFilterRule(credentials, uid);
            if (rule == null) {
                throw MailFilterExceptionCode.NO_SUCH_ID.create(uid, credentials.getRightUsername(), credentials.getContextString());
            }
            CONVERTER.parse(rule, json);
            mailFilterService.updateFilterRule(credentials, rule, uid);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        } catch (SieveException e) {
            throw MailFilterExceptionCode.handleSieveException(e);
        }
    }

    @Override
    protected void actionDeleteScript(final MailFilterRequest request) throws OXException {
        final Credentials credentials = request.getCredentials();
        final MailFilterService mailFilterService = Services.getService(MailFilterService.class);
        mailFilterService.purgeFilters(credentials);
    }

    @Override
    protected String actionGetScript(final MailFilterRequest request) throws OXException {
        final Credentials credentials = request.getCredentials();
        final MailFilterService mailFilterService = Services.getService(MailFilterService.class);
        return mailFilterService.getActiveScript(credentials);
    }

    private JSONArray getActionArray(final Set<String> capabilities) {
        final JSONArray actionarray = new JSONArray();
        for (final ActionCommand.Commands command : ActionCommand.Commands.values()) {
            final List<String> required = command.getRequired();
            if (required.isEmpty()) {
                actionarray.put(command.getJsonName());
            } else {
                for (final String req : required) {
                    if (capabilities.contains(req)) {
                        actionarray.put(command.getJsonName());
                        break;
                    }
                }
            }
        }
        return actionarray;
    }

    /**
     * Fills up the config object
     *
     * @param hashSet A set of sieve capabilities
     * @return
     * @throws JSONException
     */
    private JSONObject getTestAndActionObjects(final Set<String> capabilities) throws JSONException {
        final JSONObject retval = new JSONObject();
        retval.put("tests", getTestArray(capabilities));
        retval.put("actioncommands", getActionArray(capabilities));
        return retval;
    }

    private JSONArray getTestArray(final Set<String> capabilities) throws JSONException {
        TestCommandRegistry testCommandRegistry = Services.getService(TestCommandRegistry.class);
        final JSONArray testarray = new JSONArray();
        for (final ITestCommand command : testCommandRegistry.getCommands()) {
            final JSONObject object = new JSONObject();
            if (null == command.getRequired() || capabilities.contains(command.getRequired())) {
                final JSONArray comparison = new JSONArray();
                object.put("test", command.getCommandName());
                final List<String[]> jsonMatchTypes = command.getJsonMatchTypes();
                if (null != jsonMatchTypes) {
                    for (final String[] matchtype : jsonMatchTypes) {
                        final String value = matchtype[0];
                        if ("".equals(value) || capabilities.contains(value)) {
                            comparison.put(matchtype[1]);
                        }
                    }
                }
                object.put("comparison", comparison);
                testarray.put(object);
            }
        }
        return testarray;
    }

    private Integer getUniqueId(final JSONObject json) throws OXException {
        if (json.has("id") && !json.isNull("id")) {
            try {
                return Integer.valueOf(json.getInt("id"));
            } catch (final JSONException e) {
                throw MailFilterExceptionCode.ID_MISSING.create();
            }
        }
        throw MailFilterExceptionCode.MISSING_PARAMETER.create("id");
    }

}
