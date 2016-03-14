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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private final List<RuleFieldMapper> mappers;

    /**
     * Initialises a new {@link RuleParser}.
     */
    public RuleParser() {
        super();

        // Order of processing
        List<RuleFieldMapper> list = new ArrayList<RuleFieldMapper>(9);
        list.add(new IDRuleFieldMapper());
        list.add(new PositionRuleFieldMapper());
        list.add(new RuleNameRuleFieldMapper());
        list.add(new ActiveRuleFieldMapper());
        list.add(new FlagsRuleFieldMapper());
        list.add(new TestCommandRuleFieldMapper());
        list.add(new ActionCommandRuleFieldMapper());
        list.add(new TextRuleFieldMapper());
        list.add(new ErrorMessageRuleFieldMapper());

        mappers = Collections.unmodifiableList(list);
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
     * @return The parsed Rule object
     * @throws JSONException
     * @throws SieveException
     * @throws OXException
     */
    public Rule parse(Rule rule, JSONObject json) throws JSONException, SieveException, OXException {
        for (RuleFieldMapper mapper : mappers) {
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
