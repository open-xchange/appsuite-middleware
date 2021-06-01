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

package com.openexchange.mailfilter.json.ajax.actions;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.JSONMatchType;
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
import com.openexchange.mailfilter.json.ajax.servlet.MailFilterExtensionCapabilities;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSessionAdapter;

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
            final JSONObject result = getTestAndActionObjects(capabilities);
            addSupportedCapabilities(capabilities, result);
            return result;
        } catch (JSONException e) {
            throw MailFilterExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Adds all supported capabilities to the {@link JSONObject}
     *
     * @param capabilities All sieve capabilities
     * @param jsonObject The json object
     * @throws JSONException
     */
    private void addSupportedCapabilities(Set<String> capabilities, JSONObject jsonObject) throws JSONException {
        JSONArray caps = new JSONArray();
        for (MailFilterExtensionCapabilities cap : MailFilterExtensionCapabilities.values()) {
            if (capabilities.contains(cap.name())) {
                caps.put(cap.name());
            }
        }
        jsonObject.putOpt("capabilities", caps);

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
            final int uid = getUniqueId(json).intValue();
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
            final Rule rule = CONVERTER.parse(getJsonBody(request), ServerSessionAdapter.valueOf(request.getSession()));
            return mailFilterService.createFilterRule(credentials, rule);
        } catch (SieveException e) {
            throw MailFilterExceptionCode.handleSieveException(e);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject getJsonBody(final MailFilterRequest request) throws JSONException, OXException {
        final JSONObject jsonObject = new JSONObject(request.getBody());
        checkJsonValue(jsonObject, null, jsonObject);
        return jsonObject;
    }

    private static final Set<String> MUST_NOT_BE_EMPTY = ImmutableSet.of("values");

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
            final int uid = getUniqueId(json).intValue();
            final Rule rule = mailFilterService.getFilterRule(credentials, uid);
            if (rule == null) {
                throw MailFilterExceptionCode.NO_SUCH_ID.create(I(uid), credentials.getRightUsername(), credentials.getContextString());
            }
            CONVERTER.parse(rule, json, ServerSessionAdapter.valueOf(request.getSession()));
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
        Collection<ITestCommand> commands = testCommandRegistry.getCommands();
        JSONArray jTestArray = new JSONArray(commands.size());
        for (ITestCommand command : commands) {
            if (isSupported(command, capabilities)) {
                JSONObject jTestObject = new JSONObject(4);
                jTestObject.put("test", command.getCommandName());
                List<JSONMatchType> jsonMatchTypes = command.getJsonMatchTypes();
                JSONArray jComparisons;
                if (null != jsonMatchTypes) {
                    jComparisons = new JSONArray(jsonMatchTypes.size());
                    for (JSONMatchType matchtype : jsonMatchTypes) {
                        String value = matchtype.getRequired();
                        if (matchtype.getVersionRequirement() <= 1 && ("".equals(value) || capabilities.contains(value))) {
                            jComparisons.put(matchtype.getJsonName());
                        }
                    }
                } else {
                    jComparisons = JSONArray.EMPTY_ARRAY;
                }
                jTestObject.put("comparison", jComparisons);
                jTestArray.put(jTestObject);
            }
        }
        return jTestArray;
    }

    private boolean isSupported(ITestCommand command, Set<String> capabilities) {
        List<String> requiredCapabilities = command.getRequired();
        if (null == requiredCapabilities || requiredCapabilities.isEmpty()) {
            return true;
        }

        for (String requiredCapability : requiredCapabilities) {
            if (false == capabilities.contains(requiredCapability)) {
                return false;
            }
        }
        return true;
    }

    private Integer getUniqueId(final JSONObject json) throws OXException {
        if (json.hasAndNotNull("id")) {
            try {
                return Integer.valueOf(json.getInt("id"));
            } catch (JSONException e) {
                throw MailFilterExceptionCode.ID_MISSING.create();
            }
        }
        throw MailFilterExceptionCode.MISSING_PARAMETER.create("id");
    }

}
