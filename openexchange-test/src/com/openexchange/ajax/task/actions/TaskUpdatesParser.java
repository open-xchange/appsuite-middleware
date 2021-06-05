/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.task.actions;

import static com.openexchange.java.Autoboxing.l;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    protected TaskUpdatesParser(boolean failOnError, int[] columns) {
        super(failOnError, columns);
        this.columns = columns;
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
            if (!JSONArray.class.isInstance(arrayOrId)) {
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
