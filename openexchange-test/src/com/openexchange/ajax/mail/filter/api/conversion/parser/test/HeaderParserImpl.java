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

package com.openexchange.ajax.mail.filter.api.conversion.parser.test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.ComparisonParser;
import com.openexchange.ajax.mail.filter.api.conversion.parser.comparison.ComparisonParserRegistry;
import com.openexchange.ajax.mail.filter.api.dao.MatchType;
import com.openexchange.ajax.mail.filter.api.dao.comparison.Comparison;
import com.openexchange.ajax.mail.filter.api.dao.test.AbstractTest;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;

/**
 * HeaderParserImpl
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class HeaderParserImpl implements TestParser {

    @Override
    public AbstractTest parseTest(String name, JSONObject jsonObject) throws JSONException {
        final JSONArray jsonHeaderArray = jsonObject.getJSONArray("headers");
        final String[] headers = new String[jsonHeaderArray.length()];
        for (int a = 0; a < headers.length; a++) {
            headers[a] = jsonHeaderArray.getString(a);
        }

        final JSONArray jsonValueArray = jsonObject.getJSONArray("values");
        final String[] values = new String[jsonValueArray.length()];
        for (int a = 0; a < values.length; a++) {
            values[a] = jsonValueArray.getString(a);
        }

        final String comparisonName = jsonObject.getString("comparison");
        MatchType matchType = MatchType.valueOf(comparisonName);

        final ComparisonParser compParser = ComparisonParserRegistry.getParser(matchType); //FIXME: Will throw a CCE, fix when registry is implemented
        final Comparison comparison = compParser.parse(jsonObject);

        return new HeaderTest(comparison, headers, values);
    }
}
