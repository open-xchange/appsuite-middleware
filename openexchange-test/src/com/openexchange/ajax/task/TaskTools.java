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
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.SearchRequest;
import com.openexchange.ajax.task.actions.SearchResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;

/**
 * Utility class that contains all methods for making task requests to the server.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TaskTools extends ParticipantTools {

    /**
     * This method implements storing of a task through the AJAX interface.
     *
     * @param client the AJAXClient
     * @param task Task to store.
     * @return the reponse object of inserting the task.
     * @throws JSONException if parsing of serialized json fails.
     * @throws IOException if the communication with the server fails.
     * @deprecated use {@link AJAXClient#execute(com.openexchange.ajax.framework.AJAXRequest)}
     */
    @Deprecated
    public static Response insertTask(AJAXClient client, final Task task) throws JSONException, IOException, OXException {
        final InsertResponse insertR = client.execute(new InsertRequest(task, client.getValues().getTimeZone()));
        return insertR.getResponse();
    }

    /**
     * @deprecated use {@link AJAXClient#execute(com.openexchange.ajax.framework.AJAXRequest)}
     */
    @Deprecated
    public static UpdateResponse update(final AJAXClient client, final UpdateRequest request) throws OXException, IOException, JSONException {
        return Executor.execute(client, request);
    }

    /**
     * @deprecated use {@link #get(AJAXSession, GetRequest, long)}
     */
    @Deprecated
    public static Response getTask(AJAXClient client, final int folderId, final int taskId) throws IOException, JSONException, OXException, OXException {
        final GetResponse getR = get(client, new GetRequest(folderId, taskId));
        final Response response = getR.getResponse();
        response.setData(getR.getTask(client.getValues().getTimeZone()));
        return response;
    }

    /**
     * @deprecated use {@link AJAXClient#execute(com.openexchange.ajax.framework.AJAXRequest)}.
     */
    @Deprecated
    public static GetResponse get(AJAXClient client, GetRequest request) throws OXException, IOException, JSONException {
        return client.execute(request);
    }

    /**
     * @param folderAndTaskId Contains the folder identifier with the index <code>0</code> and the task identifier with the index
     *            <code>1</code>.
     * @throws JSONException if parsing of serialized json fails.
     * @throws IOException if the communication with the server fails.
     * @deprecated use {@link #delete(AJAXSession, DeleteRequest)}
     */
    @Deprecated
    public static void deleteTask(AJAXClient client, final Date lastUpdate, final int folder, final int task) throws IOException, JSONException {
        final DeleteRequest request = new DeleteRequest(folder, task, lastUpdate);
        try {
            client.execute(request);
        } catch (final OXException e) {
            throw new JSONException(e);
        }
    }

    public static CommonAllResponse all(final AJAXClient client, final AllRequest request) throws OXException, IOException, JSONException {
        return client.execute(request);
    }

    public static SearchResponse search(final AJAXClient client, final SearchRequest request) throws OXException, IOException, JSONException {
        return Executor.execute(client, request);
    }

    public static void compareAttributes(final Task task, final Task reload) {
        assertEquals("Title differs", task.containsTitle(), reload.containsTitle());
        assertEquals("Title differs", task.getTitle(), reload.getTitle());
        assertEquals("Private Flag differs", task.containsPrivateFlag(), reload.containsPrivateFlag());
        assertEquals("Private Flag differs", task.getPrivateFlag(), reload.getPrivateFlag());
        /*
         * Not implemented in parser assertEquals("Creation date differs", task.containsCreationDate(), reload.containsCreationDate());
         * assertEquals("Creation date differs", task.getCreationDate(), reload.getCreationDate()); assertEquals("Last modified differs",
         * task.containsLastModified(), reload.containsLastModified()); assertEquals("Last modified differs", task.getLastModified(),
         * reload.getLastModified());
         */
        assertEquals("Start date differs", task.containsStartDate(), reload.containsStartDate());
        assertEquals("Start date differs", task.getStartDate(), reload.getStartDate());
        assertEquals("End date differs", task.containsEndDate(), reload.containsEndDate());
        assertEquals("End date differs", task.getEndDate(), reload.getEndDate());
        // assertEquals("After complete differs", task.containsAfterComplete(),
        // reload.containsAfterComplete());
        // assertEquals("After complete differs", task.getAfterComplete(),
        // reload.getAfterComplete());
        // task.setNote("Description");
        assertEquals("Status differs", task.containsStatus(), reload.containsStatus());
        assertEquals("Status differs", task.getStatus(), reload.getStatus());
        assertEquals("Priority differs", task.containsPriority(), reload.containsPriority());
        assertEquals("Priority differs", task.getPriority(), reload.getPriority());
        assertEquals("PercentComplete differs", task.containsPercentComplete(), reload.containsPercentComplete());
        assertEquals("PercentComplete differs", task.getPercentComplete(), reload.getPercentComplete());
        // task.setCategories("Categories");
        assertEquals("TargetDuration differs", task.containsTargetDuration(), reload.containsTargetDuration());
        assertEquals("TargetDuration differs", task.getTargetDuration(), reload.getTargetDuration());
        assertEquals("ActualDuration differs", task.containsActualDuration(), reload.containsActualDuration());
        assertEquals("ActualDuration differs", task.getActualDuration(), reload.getActualDuration());
        // task.setTargetCosts(1.0f);
        // task.setActualCosts(1.0f);
        // task.setCurrency("\u20ac");
        // task.setTripMeter("trip meter");
        // task.setBillingInformation("billing information");
        // task.setCompanies("companies");
    }

    public static void insert(final AJAXClient client, final Task... tasks) throws OXException, IOException, JSONException {
        final TimeZone tz = client.getValues().getTimeZone();
        final InsertRequest[] inserts = new InsertRequest[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            inserts[i] = new InsertRequest(tasks[i], tz);
        }
        final MultipleRequest<InsertResponse> request = MultipleRequest.create(inserts);
        final MultipleResponse<InsertResponse> response = client.execute(request);
        for (int i = 0; i < tasks.length; i++) {
            response.getResponse(i).fillTask(tasks[i]);
        }
    }

    public static Task valuesForUpdate(final Task task) {
        return valuesForUpdate(task, task.getParentFolderID());
    }

    public static Task valuesForUpdate(Task task, int folderId) {
        final Task retval = new Task();
        retval.setObjectID(task.getObjectID());
        retval.setParentFolderID(folderId);
        retval.setLastModified(task.getLastModified());
        return retval;
    }

    /**
     * Compares the specified objects
     * 
     * @param taskObj1 The expected {@link Task}
     * @param taskObj2 The actual {@link Task}
     * @throws Exception if an error is occurred
     */
    public static void compareObject(final Task taskObj1, final Task taskObj2) throws Exception {
        assertEquals("id is not equals", taskObj1.getObjectID(), taskObj2.getObjectID());
        assertEqualsAndNotNull("title is not equals", taskObj1.getTitle(), taskObj2.getTitle());
        assertEqualsAndNotNull("start is not equals", taskObj1.getStartDate(), taskObj2.getStartDate());
        assertEqualsAndNotNull("end is not equals", taskObj1.getEndDate(), taskObj2.getEndDate());
        assertEquals("folder id is not equals", taskObj1.getParentFolderID(), taskObj2.getParentFolderID());
        assertEquals("private flag is not equals", taskObj1.getPrivateFlag(), taskObj2.getPrivateFlag());
        assertEquals("alarm is not equals", taskObj1.getAlarm(), taskObj2.getAlarm());
        assertEqualsAndNotNull("note is not equals", taskObj1.getNote(), taskObj2.getNote());
        assertEqualsAndNotNull("categories is not equals", taskObj1.getCategories(), taskObj2.getCategories());
        assertEqualsAndNotNull("actual costs is not equals", taskObj1.getActualCosts(), taskObj2.getActualCosts());
        assertEqualsAndNotNull("actual duration", taskObj1.getActualDuration(), taskObj2.getActualDuration());
        assertEqualsAndNotNull("billing information", taskObj1.getBillingInformation(), taskObj2.getBillingInformation());
        assertEqualsAndNotNull("companies", taskObj1.getCompanies(), taskObj2.getCompanies());
        assertEqualsAndNotNull("currency", taskObj1.getCurrency(), taskObj2.getCurrency());
        assertEqualsAndNotNull("date completed", taskObj1.getDateCompleted(), taskObj2.getDateCompleted());
        assertEqualsAndNotNull("percent complete", taskObj1.getPercentComplete(), taskObj2.getPercentComplete());
        assertEqualsAndNotNull("priority", taskObj1.getPriority(), taskObj2.getPriority());
        assertEqualsAndNotNull("status", taskObj1.getStatus(), taskObj2.getStatus());
        assertEqualsAndNotNull("target costs", taskObj1.getTargetCosts(), taskObj2.getTargetCosts());
        assertEqualsAndNotNull("target duration", taskObj1.getTargetDuration(), taskObj2.getTargetDuration());
        assertEqualsAndNotNull("trip meter", taskObj1.getTripMeter(), taskObj2.getTripMeter());

        assertEqualsAndNotNull("participants are not equals", participants2String(taskObj1.getParticipants()), participants2String(taskObj2.getParticipants()));
        assertEqualsAndNotNull("users are not equals", users2String(taskObj1.getUsers()), users2String(taskObj2.getUsers()));
    }
}
