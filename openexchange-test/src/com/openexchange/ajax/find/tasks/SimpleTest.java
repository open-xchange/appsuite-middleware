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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.find.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.find.facet.Filter;


/**
 * {@link SimpleTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SimpleTest extends AbstractFindTest {

    /**
     * Initializes a new {@link SimpleTest}.
     */
    public SimpleTest(String name) {
        super(name);
    }
    
    @Test
    public void testSimpleSearch() {
        try {

            List<String> queries = Collections.singletonList("test");
            List<Filter> filters = Collections.emptyList();
            final QueryResponse queryResponse = getClient().execute(new QueryRequest(0, 10, queries, filters, "tasks"));

            assertNotNull(queryResponse);

            System.err.println(queryResponse.getData());

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testSimpleFilter() {
        try {

            List<String> queries = Collections.singletonList("%test%");
            List<Filter> filters = new ArrayList<Filter>();
            //filters.add(new Filter(Collections.singletonList("folder_type"), "shared"));
            //filters.add(new Filter(Collections.singletonList("folder_type"), "private"));
            //filters.add(new Filter(Collections.singletonList("type"), "single_task"));
            //filters.add(new Filter(Collections.singletonList("participant"), "5"));
            //filters.add(new Filter(Collections.singletonList("participant"), "foo@bar.org"));
            //filters.add(new Filter(Collections.singletonList("participant"), "bar@foo.org"));
            //filters.add(new Filter(Collections.singletonList("status"), "1"));
            //filters.add(new Filter(Collections.singletonList("status"), "2"));
            final QueryResponse queryResponse = getClient().execute(new QueryRequest(0, 10, queries, filters, "tasks"));

            assertNotNull(queryResponse);

            JSONObject j = (JSONObject) queryResponse.getData();
            System.err.println("RESULTS: " + j.getJSONArray("results").length());

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
