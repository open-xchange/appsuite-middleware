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

package com.openexchange.ajax.task.actions;

import static com.openexchange.java.Autoboxing.l;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonUpdatesParser;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link TaskUpdatesParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TaskUpdatesParser extends CommonUpdatesParser<TaskUpdatesResponse> {

    private final int[] columns;

    private final TimeZone timeZone;

    protected TaskUpdatesParser(boolean failOnError, int[] columns, TimeZone timeZone) {
        super(failOnError, columns);
        this.columns = columns;
        this.timeZone = timeZone;
    }

    @Override
    protected TaskUpdatesResponse createResponse(Response response) throws JSONException {
        /*
         * Calling super.createResponse initiates the modified and deleted ids for the update response
         */
        TaskUpdatesResponse taskUpdatesResponse = super.createResponse(response);
        JSONArray rows = (JSONArray) response.getData();
        if (rows == null) {
            return taskUpdatesResponse;
        }
        List<Task> tasks = new ArrayList<Task>();
        for (int i = 0, size = rows.length(); i < size; i++) {
            Object arrayOrId = rows.get(i);
            if(!JSONArray.class.isInstance(arrayOrId)) {
                continue;
            }
            JSONArray row = (JSONArray) arrayOrId;
            Task task = new Task();
            for (int colIndex = 0; colIndex < columns.length; colIndex++) {
                Object value = row.get(colIndex);
                if (value == JSONObject.NULL) {
                    continue;
                }
                int column = columns[colIndex];
                if (column == Task.LAST_MODIFIED_UTC) {
                    continue;
                }
                value = transform(value, column);
                task.set(column, value);
            }
            tasks.add(task);
        }
        taskUpdatesResponse.setTasks(tasks);
        return taskUpdatesResponse;
    }

    @Override
    protected TaskUpdatesResponse instantiateResponse(Response response) {
        return new TaskUpdatesResponse(response);
    }

    private Object transform(Object actual, int column) throws JSONException {
        switch (column) {
        case Task.START_DATE:
        case Task.END_DATE:
        case Task.CREATION_DATE:
        case Task.LAST_MODIFIED:
            return new Date(l((Long) actual));
        case Task.PARTICIPANTS:
            return buildParticipantArray((JSONArray) actual);
        }
        return actual;
    }

    private Object buildParticipantArray(JSONArray actual) throws JSONException {
        List<Participant> participants = new ArrayList<Participant>();
        for (int i = 0, size = actual.length(); i < size; i++) {
            JSONObject jsonParticipant = actual.getJSONObject(i);
            int type = jsonParticipant.getInt("type");
            switch (type) {
            case Participant.USER:
                UserParticipant userParticipant = new UserParticipant(jsonParticipant.getInt("id"));
                userParticipant.setConfirm(jsonParticipant.getInt("confirmation"));
                participants.add(userParticipant);
                break;
            case Participant.GROUP:
                GroupParticipant groupParticipant = new GroupParticipant(jsonParticipant.getInt("id"));
                participants.add(groupParticipant);
                break;
            case Participant.EXTERNAL_USER:
                ExternalUserParticipant externalUserParticipant = new ExternalUserParticipant(jsonParticipant.optString("mail"));
                participants.add(externalUserParticipant);
                break;
            }
        }
        return participants.toArray(new Participant[participants.size()]);
    }
}
