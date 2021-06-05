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

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.groupware.tasks.Task;

/**
 * Confirm request that can send the task identifier in URL and body.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConfirmWith2IdsRequest extends AbstractConfirmRequest {

    private final Task task;
    private final int bodyId;

    public ConfirmWith2IdsRequest(Task task, int bodyId, int confirmStatus, String confirmMessage, boolean failOnError) {
        super(confirmStatus, confirmMessage, failOnError);
        this.task = task;
        this.bodyId = bodyId;
    }

    public ConfirmWith2IdsRequest(Task task, int bodyId, int confirmStatus, String confirmMessage) {
        this(task, bodyId, confirmStatus, confirmMessage, true);
    }

    @Override
    protected void addBodyParameter(JSONObject json) throws JSONException {
        json.put(TaskFields.ID, bodyId);
    }

    @Override
    protected void addRequestParameter(List<Parameter> params) {
        params.add(new Parameter(TaskFields.ID, task.getObjectID()));
    }
}
