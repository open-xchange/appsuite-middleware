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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.mailfilter.json.ajax.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.jsieve.SieveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.mailfilter.json.ajax.json.fields.RuleField;
import com.openexchange.mailfilter.json.ajax.json.mapper.ActionCommandRuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.mapper.ActiveRuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.mapper.ErrorMessageRuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.mapper.FlagsRuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.mapper.IDRuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.mapper.PositionRuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.mapper.RuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.mapper.RuleNameRuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.mapper.TestCommandRuleFieldMapper;
import com.openexchange.mailfilter.json.ajax.json.mapper.TextRuleFieldMapper;

/**
 * {@link RuleParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RuleParser {

    private final Map<RuleField, RuleFieldMapper> mappers;

    /**
     * Initialises a new {@link RuleParser}.
     */
    public RuleParser() {
        super();

        Map<RuleField, RuleFieldMapper> map = new HashMap<RuleField, RuleFieldMapper>(9);
        map.put(RuleField.actioncmds, new ActionCommandRuleFieldMapper());
        map.put(RuleField.active, new ActiveRuleFieldMapper());
        map.put(RuleField.errormsg, new ErrorMessageRuleFieldMapper());
        map.put(RuleField.flags, new FlagsRuleFieldMapper());
        map.put(RuleField.id, new IDRuleFieldMapper());
        map.put(RuleField.position, new PositionRuleFieldMapper());
        map.put(RuleField.rulename, new RuleNameRuleFieldMapper());
        map.put(RuleField.test, new TestCommandRuleFieldMapper());
        map.put(RuleField.text, new TextRuleFieldMapper());

        mappers = Collections.unmodifiableMap(map);
    }

    /**
     * Parses the specified JSONObject into a new Rule object
     * 
     * @param json The JSONObject to parse
     * @return the Rule object
     * @throws OXException
     * @throws SieveException
     * @throws JSONException
     */
    public Rule parse(JSONObject json) throws JSONException, SieveException, OXException {
        Rule rule = new Rule();
        return parse(rule, json);
    }

    /**
     * Parses the specified JSONObject into the specified Rule object
     * 
     * @param rule The rule object
     * @param json The JSONObject
     * @return The parsed Rule objecet
     * @throws JSONException
     * @throws SieveException
     * @throws OXException
     */
    public Rule parse(Rule rule, JSONObject json) throws JSONException, SieveException, OXException {
        for (RuleFieldMapper mapper : mappers.values()) {
            String attributeName = mapper.getAttributeName().name();
            if (json.has(attributeName)) {
                try {
                    mapper.setAttribute(rule, json.get(attributeName));
                } catch (final ClassCastException e) {
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
    public JSONObject parse(Rule rule) throws JSONException {
        JSONObject object = new JSONObject();
        for (RuleFieldMapper mapper : mappers.values()) {
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
     * @throws OXException
     * @throws SieveException
     * @throws JSONException
     */
    public JSONArray write(Rule[] rules) throws JSONException, SieveException, OXException {
        int objectCount = rules.length;
        if (objectCount == 0) {
            return new JSONArray(0);
        }

        JSONArray array = new JSONArray(objectCount);
        for (Rule rule : rules) {
            array.put(parse(rule));
        }
        return array;
    }
}
