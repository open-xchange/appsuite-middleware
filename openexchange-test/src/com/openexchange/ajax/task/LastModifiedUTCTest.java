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
package com.openexchange.ajax.task;

import java.io.IOException;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.ajax.task.actions.SearchRequest;
import com.openexchange.ajax.task.actions.UpdatesRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class LastModifiedUTCTest extends AbstractTaskTest {
    private static final int[] LAST_MODIFIED_UTC = new int[]{Task.LAST_MODIFIED_UTC, Task.OBJECT_ID, Task.FOLDER_ID};

    private int id;
    private Date lastModified;

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    public LastModifiedUTCTest(final String name) throws JSONException, OXException, IOException, SAXException {
        super(name);
    }

    @Override
	public void setUp() throws Exception {
        super.setUp();
        final Task task = new Task();
        task.setTitle("lastModifiedUTC");
        task.setParentFolderID(getPrivateFolder());
        final InsertRequest request = new InsertRequest(task, getTimeZone());
        final InsertResponse response = Executor.execute(getClient(), request);
        id = response.getId();
        lastModified = response.getTimestamp();
    }

    @Override
	public void tearDown() throws Exception {
        final DeleteRequest deleteRequest = new DeleteRequest(getPrivateFolder(), getId(), lastModified);
        Executor.execute(getClient(), deleteRequest);
        super.tearDown();
    }

    public int getId() {
        return id;
    }


    public void testAll() throws JSONException, OXException, IOException, SAXException {
        final AllRequest request = new AllRequest(getPrivateFolder(), LAST_MODIFIED_UTC, -1, null );
        checkListSyleRequest(request);
    }

    public void testGet() throws JSONException, OXException, IOException, SAXException {
        final GetRequest getRequest = new GetRequest(getPrivateFolder(), getId());
        final GetResponse getResponse = Executor.execute(getClient(), getRequest);

        final JSONObject object = (JSONObject) getResponse.getData();

        assertTrue(object.has("last_modified_utc"));

    }

    public void testList() throws JSONException, OXException, IOException, SAXException {
        final ListRequest listRequest = new ListRequest(new int[][]{{getPrivateFolder(), getId()}}, LAST_MODIFIED_UTC);
        checkListSyleRequest(listRequest);
    }

    public void testSearch() throws JSONException, OXException, IOException, SAXException {
        final TaskSearchObject search = new TaskSearchObject();
        search.setFolder(getPrivateFolder());
        search.setPattern("*");
        final SearchRequest searchRequest = new SearchRequest(search, LAST_MODIFIED_UTC, true);
        checkListSyleRequest(searchRequest);
    }

    public void testUpdates() throws JSONException, OXException, IOException, SAXException {
        final UpdatesRequest updatesRequest = new UpdatesRequest(getPrivateFolder(), LAST_MODIFIED_UTC, -1, null, new Date(0));
        checkListSyleRequest(updatesRequest);
    }

    private void checkListSyleRequest(final AJAXRequest<? extends AbstractAJAXResponse> request) throws JSONException, OXException, IOException, SAXException {
        final AbstractAJAXResponse response = Executor.execute(getClient(), request);
        final JSONArray arr = (JSONArray)response.getData();
        final int size = arr.length();
        assertTrue(size > 0);
        for(int i = 0; i < size; i++) {
            final JSONArray row = arr.getJSONArray(i);
            assertTrue(row.toString(), row.length() == 3);
            assertTrue(row.toString(), row.optLong(0) > 0);
        }
    }
}
