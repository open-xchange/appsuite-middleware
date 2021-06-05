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

package com.openexchange.mail.filter.json.v2.json;

import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.mail.filter.json.v2.json.fields.RuleField;
import com.openexchange.mail.filter.json.v2.mapper.ActionCommandRuleFieldMapper;
import com.openexchange.mail.filter.json.v2.mapper.ActiveRuleFieldMapper;
import com.openexchange.mail.filter.json.v2.mapper.ErrorMessageRuleFieldMapper;
import com.openexchange.mail.filter.json.v2.mapper.FlagsRuleFieldMapper;
import com.openexchange.mail.filter.json.v2.mapper.IDRuleFieldMapper;
import com.openexchange.mail.filter.json.v2.mapper.PositionRuleFieldMapper;
import com.openexchange.mail.filter.json.v2.mapper.RuleFieldMapper;
import com.openexchange.mail.filter.json.v2.mapper.RuleNameRuleFieldMapper;
import com.openexchange.mail.filter.json.v2.mapper.TestCommandRuleFieldMapper;
import com.openexchange.mail.filter.json.v2.mapper.TextRuleFieldMapper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RuleParser}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RuleParser {

    public static RuleParser newInstance(ServiceLookup services){
        return new RuleParser(services);
    }

    // ---------------------------------------------------------------------------

    private final List<RuleFieldMapper> mappers;

    /**
     * Initialises a new {@link RuleParser}.
     */
    private RuleParser(ServiceLookup services) {
        super();

        // Order of processing
        ImmutableList.Builder<RuleFieldMapper> list = ImmutableList.builder();
        list.add(new IDRuleFieldMapper());
        list.add(new PositionRuleFieldMapper());
        list.add(new RuleNameRuleFieldMapper());
        list.add(new ActiveRuleFieldMapper());
        list.add(new FlagsRuleFieldMapper());
        list.add(new TestCommandRuleFieldMapper(services));
        list.add(new ActionCommandRuleFieldMapper(services));
        list.add(new TextRuleFieldMapper());
        list.add(new ErrorMessageRuleFieldMapper());

        mappers = list.build();
    }

    /**
     * Parses the specified JSONObject into a new Rule object
     *
     * @param json The JSONObject to parse
     * @param session The session
     * @return the Rule object
     * @throws OXException
     * @throws SieveException
     * @throws JSONException
     */
    public Rule parse(JSONObject json, ServerSession session) throws JSONException, SieveException, OXException {
        Rule rule = new Rule();
        return parse(rule, json, session);
    }

    /**
     * Parses the specified JSONObject into the specified Rule object
     *
     * @param rule The rule object
     * @param json The JSONObject
     * @param session The session
     * @return The parsed Rule object
     * @throws JSONException
     * @throws SieveException
     * @throws OXException
     */
    public Rule parse(Rule rule, JSONObject json, ServerSession session) throws JSONException, SieveException, OXException {
        for (RuleFieldMapper mapper : mappers) {
            String attributeName = mapper.getAttributeName().name();
            if (json.has(attributeName)) {
                try {
                    mapper.setAttribute(rule, json.get(attributeName), session);
                } catch (ClassCastException e) {
                    throw new JSONException(e);
                }
            }
        }
        return rule;
    }

    /**
     * Parses the specified {@link Rule} object into a {@link JSONObject}
     *
     * @param rule The {@link Rule} object to parse
     * @return The parsed {@link JSONObject}
     * @throws JSONException if a JSON parsing error occurs
     */
    public JSONObject parse(Rule rule) throws JSONException, OXException {
        JSONObject object = new JSONObject();
        for (RuleFieldMapper mapper : mappers) {
            if (!mapper.isNull(rule)) {
                object.put(mapper.getAttributeName().name(), mapper.getAttribute(rule));
            }
        }
        return object;
    }

    /**
     * Writes all {@link RuleField} fields of the specified rules into separate {@link JSONObject}s which in
     * turn are put into a surrounding {@link JSONArray}
     *
     * @param rules The rules
     * @return the JSONArray
     * @throws JSONException
     */
    public JSONArray write(Rule[] rules) throws JSONException, OXException {
        JSONArray array = new JSONArray(rules.length);
        for (Rule rule : rules) {
            array.put(parse(rule));
        }
        return array;
    }
}
