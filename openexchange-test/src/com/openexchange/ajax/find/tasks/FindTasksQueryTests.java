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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.tasks.TasksFacetType;


/**
 * {@link FindTasksQueryTests}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindTasksQueryTests extends AbstractFindTasksTest {

    /**
     * Initializes a new {@link FindTasksQueryTests}.
     */
    public FindTasksQueryTests(String name) {
        super(name);
    }

    /**
     * Test with simple query with no filters
     * Should find 30 tasks.
     *
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     *
     * @see {@link FindTasksTestEnvironment.createAndInsertTasks}
     */
    @Test
    public void testWithSimpleQuery() throws OXException, IOException, JSONException {
        assertResults(30, Collections.<ActiveFacet>emptyList(), -1, 30);
    }

    /**
     * Test pagination
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testPagination() throws OXException, IOException, JSONException {
        assertResults(5, Collections.<ActiveFacet>emptyList(), 5, 10);
    }

    /**
     * Test query attachment name
     *
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testQueryAttachmentName() throws OXException, IOException, JSONException {
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(new ActiveFacet(TasksFacetType.TASK_ATTACHMENT_NAME, "attachment", new Filter(Collections.singletonList("attachment"), "cool")));
        assertResults(5, facets);
    }
}
