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

package com.openexchange.ajax.mail.filter.api.parser;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.filter.api.dao.ActionCommand;
import com.openexchange.ajax.mail.filter.api.dao.MatchType;
import com.openexchange.ajax.mail.filter.api.dao.MailFilterConfiguration;
import com.openexchange.ajax.mail.filter.api.dao.TestCondition;
import com.openexchange.ajax.mail.filter.api.dao.TestCommand;
import com.openexchange.ajax.mail.filter.api.response.ConfigResponse;

/**
 * {@link ConfigParser}
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ConfigParser extends AbstractAJAXParser<ConfigResponse> {

    /**
     * Initialises a new {@link ConfigParser}.
     * 
     * @param failOnError
     */
    public ConfigParser(final boolean failOnError) {
        super(failOnError);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#createResponse(com.openexchange.ajax.container.Response)
     */
    @Override
    protected ConfigResponse createResponse(final Response response) throws JSONException {
        final JSONObject jsonObj = (JSONObject) response.getData();

        final JSONArray jsonTestArray = jsonObj.getJSONArray("tests");
        final JSONArray jsonActionArray = jsonObj.getJSONArray("actioncommands");

        // Parse tests
        List<TestCondition> tests = new ArrayList<TestCondition>(jsonTestArray.length());
        for (int a = 0; a < jsonTestArray.length(); a++) {
            final JSONObject jsonTestObj = jsonTestArray.getJSONObject(a);

            // Parse test command
            final String testString = jsonTestObj.getString("test");
            TestCommand testCommand = TestCommand.valueOf(testString.toUpperCase());

            // Parse comparisons
            final JSONArray jsonComparisonArray = jsonTestObj.getJSONArray("comparison");
            List<MatchType> comparisons = new ArrayList<>(jsonComparisonArray.length());
            for (int b = 0; b < jsonComparisonArray.length(); b++) {
                comparisons.add(MatchType.valueOf(jsonComparisonArray.getString(b)));
            }

            tests.add(new TestCondition(testCommand, comparisons));
        }

        // Parse action commands
        List<ActionCommand> actionCommands = new ArrayList<ActionCommand>(jsonActionArray.length());
        for (int a = 0; a < jsonActionArray.length(); a++) {
            actionCommands.add(ActionCommand.valueOf(jsonActionArray.getString(a)));
        }

        return new ConfigResponse(response, new MailFilterConfiguration(tests, actionCommands));
    }
}
