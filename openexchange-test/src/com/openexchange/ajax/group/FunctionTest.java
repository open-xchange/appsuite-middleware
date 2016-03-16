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

package com.openexchange.ajax.group;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.group.actions.AbstractGroupResponse;
import com.openexchange.ajax.group.actions.AllRequest;
import com.openexchange.ajax.group.actions.AllResponse;
import com.openexchange.ajax.group.actions.ChangeRequest;
import com.openexchange.ajax.group.actions.ChangeResponse;
import com.openexchange.ajax.group.actions.CreateRequest;
import com.openexchange.ajax.group.actions.CreateResponse;
import com.openexchange.ajax.group.actions.DeleteRequest;
import com.openexchange.ajax.group.actions.DeleteResponse;
import com.openexchange.ajax.group.actions.GetRequest;
import com.openexchange.ajax.group.actions.GetResponse;
import com.openexchange.ajax.group.actions.ListRequest;
import com.openexchange.ajax.group.actions.SearchRequest;
import com.openexchange.ajax.group.actions.SearchResponse;
import com.openexchange.ajax.group.actions.UpdatesRequest;
import com.openexchange.ajax.group.actions.UpdatesResponse;
import com.openexchange.group.Group;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FunctionTest extends AbstractAJAXSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FunctionTest.class);

    private Set<Integer> groupsToDelete;

    /**
     * @param name
     */
    public FunctionTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        groupsToDelete = new HashSet<Integer>();
    }

    @Override
    protected void tearDown() throws Exception {
        for(int id: groupsToDelete) {
            getClient().execute(new DeleteRequest(id, new Date(Long.MAX_VALUE), false));
        }
        super.tearDown();
    }

    public void testSearch() throws Throwable {
        SearchResponse response = getClient().execute(new SearchRequest("*"));
        final Group[] groups = response.getGroups();
        LOG.trace("Found " + groups.length + " groups.");
        assertTrue("Size of group array should be more than 0.",
            groups.length > 0);

        JSONArray arr = (JSONArray) response.getResponse().getData();
        assertContainsLastModifiedUTC(arr);
    }

    public void testRealSearch() throws Throwable {
        final Group[] groups = getClient().execute(new SearchRequest("*l*")).getGroups();
        LOG.trace("Found " + groups.length + " groups.");
        assertNotNull(groups);
    }

    public void testList() throws Throwable {
        Group[] groups = getClient().execute(new SearchRequest("*")).getGroups();
        LOG.trace("Found " + groups.length + " groups.");
        assertTrue("Size of group array should be more than 0.",
            groups.length > 0);
        final int[] groupIds = new int[groups.length];
        for (int i = 0; i < groupIds.length; i++) {
            groupIds[i] = groups[i].getIdentifier();
        }
        AbstractGroupResponse listResponse = getClient().execute(new ListRequest(groupIds));
        groups = listResponse
            .getGroups();
        LOG.trace("Listed " + groups.length + " groups.");
        assertTrue("Size of group array should be more than 0.",
            groups.length > 0);
        assertEquals("Size of requested groups and listed groups should be equal.",
            groupIds.length, groups.length);

        JSONArray arr = (JSONArray) listResponse.getResponse().getData();
        assertContainsLastModifiedUTC(arr);
    }

    public void testAllWithMembers() throws Throwable {
    	int groupLengthBySearch = getClient().execute(new SearchRequest("*")).getGroups().length;

        AllRequest allRequest = new AllRequest(Group.ALL_COLUMNS, true);
		AllResponse allResponse = getClient().execute(allRequest);
        JSONArray data = (JSONArray) allResponse.getData();

        int groupLengthByAll = data.length();

        assertEquals(groupLengthBySearch, groupLengthByAll);

        int memberPos = 4;
        int memberCount = 0;
		for(int i = 0; i < data.length(); i++){
			JSONArray row = data.getJSONArray(i);
			String[] members = row.getString(memberPos).split(",");
			memberCount += members.length;
        }
        assertTrue(memberCount > 0);
    }


    public void testAllWithoutMembers() throws Throwable {
    	int groupLengthBySearch = getClient().execute(new SearchRequest("*")).getGroups().length;

        AllResponse allResponse = getClient().execute(new AllRequest(Group.ALL_COLUMNS_EXCEPT_MEMBERS, true));
        JSONArray data = (JSONArray) allResponse.getData();

        int groupLengthByAll = data.length();

        assertEquals(groupLengthBySearch, groupLengthByAll);

        int arrLen = Group.ALL_COLUMNS_EXCEPT_MEMBERS.length;
		for(int i = 0; i < data.length(); i++){
			JSONArray row = data.getJSONArray(i);
			assertEquals(arrLen, row.length());
        }
    }

    public void testUpdatesViaComparingWithSearch() throws Exception {
        Group[] groupsViaSearch = getClient().execute(new SearchRequest("*")).getGroups();
        UpdatesResponse response = getClient().execute(new UpdatesRequest(new Date(0), false));
        List<Group> groupsViaUpdates = response.getModified();
        assertEquals("Should find the same amount of groups via *-search as via updates since day 0", groupsViaSearch.length, groupsViaUpdates.size());
    }

    public void testUpdatesViaCreateAndDelete() throws Exception {
        int staticGroupCount = 2; // "all users" & "guests" are always included in new/modified responses
        Group group = new Group();
        group.setSimpleName("simplename_"+new Date().getTime());
        group.setDisplayName("Group Updates Test"+new Date());

        CreateResponse createResponse = getClient().execute(new CreateRequest(group,true));
        int id = createResponse.getId();
        group.setIdentifier(id);
        groupsToDelete.add(id);
        group.setLastModified(createResponse.getTimestamp());
        Date lm = new Date(group.getLastModified().getTime() - 1);

        UpdatesResponse updatesResponseAfterCreate = getClient().execute(new UpdatesRequest(lm, true));
        int numberNewAfterCreation = updatesResponseAfterCreate.getNew().size();
        int numberModifiedAfterCreation = updatesResponseAfterCreate.getModified().size();
        int numberDeletedAfterCreation = updatesResponseAfterCreate.getDeleted().size();
        assertEquals("Amount of modified elements should have increased after creation", 1 + staticGroupCount, numberModifiedAfterCreation);
        assertEquals("Amount of deleted elements should not change after creation", 0 + staticGroupCount, numberDeletedAfterCreation);
        assertEquals("Amount of new elements should equal modfied elements, since we cannot distinguish between the two", numberNewAfterCreation, numberModifiedAfterCreation);

        DeleteResponse deleteResponse = getClient().execute(new DeleteRequest(group, true));
        if(deleteResponse.hasError()) {
            groupsToDelete.remove(id);
        }

        UpdatesResponse updatesResponseAfterDeletion = getClient().execute(new UpdatesRequest(lm, true));
        int numberNewAfterDeletion = updatesResponseAfterDeletion.getNew().size();
        int numberModifiedAfterDeletion = updatesResponseAfterDeletion.getModified().size();
        int numberDeletedAfterDeletion = updatesResponseAfterDeletion.getDeleted().size();
        assertEquals("Amount of modified elements should have decreased after deletion", 0 + staticGroupCount, numberModifiedAfterDeletion);
        assertEquals("Amount of deleted elements should have increased after deletion", 1 + staticGroupCount, numberDeletedAfterDeletion);
        assertEquals("Amount of new elements should equal modfied elements, since we cannot distinguish between the two", numberNewAfterDeletion, numberModifiedAfterDeletion);
    }


    public void assertContainsLastModifiedUTC(JSONArray arr) {
        for(int i = 0, size = arr.length(); i < size; i++) {
            JSONObject entry = arr.optJSONObject(i);
            assertNotNull(entry);
            assertTrue(entry.has("last_modified_utc"));
        }
    }

    public void testGet() throws Throwable {
        final Group groups[] = getClient().execute(new SearchRequest("*")).getGroups();
        LOG.trace("Found " + groups.length + " groups.");
        assertTrue("Size of group array should be more than 0.",
            groups.length > 0);
        final int pos = new Random(System.currentTimeMillis()).nextInt(groups
            .length);
        GetResponse response = getClient().execute(new GetRequest(groups[pos].getIdentifier()));
        final Group group = response.getGroup();
        LOG.trace("Loaded group: " + group.toString());
        JSONObject entry = (JSONObject) response.getData();
        assertTrue(entry.has("last_modified_utc"));
    }

    public void testCreateChangeDelete() throws Throwable {
        // Disabled due to missing permissions for the user.
        if (true) {
            return;
        }
        final AJAXClient client1 = getClient();
        final Group group = new Group();
        group.setSimpleName("createTest");
        group.setDisplayName("createTest");
        group.setMember(new int[] { client1.getValues().getUserId() });
        final CreateRequest request = new CreateRequest(group);
        try {
            final CreateResponse response = client1.execute(request);
            group.setIdentifier(response.getId());
            group.setLastModified(response.getTimestamp());
            LOG.trace("Created group with identifier: " + group.getIdentifier());
            final ChangeRequest change = new ChangeRequest(group);
            final ChangeResponse changed = client1.execute(change);
            group.setLastModified(changed.getTimestamp());
            LOG.trace("Changed group with identifier: " + group.getIdentifier());
        } finally {
            if (-1 != group.getIdentifier()) {
                client1.execute(new DeleteRequest(
                    group.getIdentifier(), group.getLastModified()));
            }
        }
    }
}
