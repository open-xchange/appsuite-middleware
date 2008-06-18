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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.group.actions.ChangeRequest;
import com.openexchange.ajax.group.actions.ChangeResponse;
import com.openexchange.ajax.group.actions.CreateRequest;
import com.openexchange.ajax.group.actions.CreateResponse;
import com.openexchange.ajax.group.actions.DeleteRequest;
import com.openexchange.ajax.group.actions.GetRequest;
import com.openexchange.ajax.group.actions.ListRequest;
import com.openexchange.ajax.group.actions.SearchRequest;
import com.openexchange.group.Group;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FunctionTest extends AbstractAJAXSession {

    private static final Log LOG = LogFactory.getLog(FunctionTest.class);

    /**
     * @param name
     */
    public FunctionTest(final String name) {
        super(name);
    }

    public void testSearch() throws Throwable {
        final Group[] groups = GroupTools.search(getClient(),
            new SearchRequest("*")).getGroups();
        LOG.info("Found " + groups.length + " groups.");
        assertTrue("Size of group array should be more than 0.",
            groups.length > 0);
    }

    public void testRealSearch() throws Throwable {
        final Group[] groups = GroupTools.search(getClient(),
            new SearchRequest("*l*")).getGroups();
        LOG.info("Found " + groups.length + " groups.");
        assertNotNull(groups);
    }

    public void testList() throws Throwable {
        Group[] groups = GroupTools.search(getClient(),
            new SearchRequest("*")).getGroups();
        LOG.info("Found " + groups.length + " groups.");
        assertTrue("Size of group array should be more than 0.",
            groups.length > 0);
        final int[] groupIds = new int[groups.length];
        for (int i = 0; i < groupIds.length; i++) {
            groupIds[i] = groups[i].getIdentifier();
        }
        groups = GroupTools.list(getClient(), new ListRequest(groupIds))
            .getGroups();
        LOG.info("Listed " + groups.length + " groups.");
        assertTrue("Size of group array should be more than 0.",
            groups.length > 0);
        assertEquals("Size of requested groups and listed groups should be equal.",
            groupIds.length, groups.length);
    }

    public void testGet() throws Throwable {
        final Group groups[] = GroupTools.search(getClient(),
            new SearchRequest("*")).getGroups();
        LOG.info("Found " + groups.length + " groups.");
        assertTrue("Size of group array should be more than 0.",
            groups.length > 0);
        final int pos = new Random(System.currentTimeMillis()).nextInt(groups
            .length); 
        final Group group = GroupTools.get(getClient(),
            new GetRequest(groups[pos].getIdentifier())).getGroup();
        LOG.info("Loaded group: " + group.toString());
    }

    public void testCreateChangeDelete() throws Throwable {
        // Disabled due to missing permissions for the user.
        if (true) {
            return;
        }
        final AJAXClient client = getClient();
        final Group group = new Group();
        group.setSimpleName("createTest");
        group.setDisplayName("createTest");
        group.setMember(new int[] { client.getValues().getUserId() });
        final CreateRequest request = new CreateRequest(group);
        try {
            final CreateResponse response = GroupTools.create(client, request);
            group.setIdentifier(response.getId());
            group.setLastModified(response.getTimestamp());
            LOG.info("Created group with identifier: " + group.getIdentifier());
            final ChangeRequest change = new ChangeRequest(group);
            final ChangeResponse changed = GroupTools.change(client, change);
            group.setLastModified(changed.getTimestamp());
            LOG.info("Changed group with identifier: " + group.getIdentifier());
        } finally {
            if (-1 != group.getIdentifier()) {
                GroupTools.delete(client, new DeleteRequest(
                    group.getIdentifier(), group.getLastModified()));
            }
        }
    }
}
