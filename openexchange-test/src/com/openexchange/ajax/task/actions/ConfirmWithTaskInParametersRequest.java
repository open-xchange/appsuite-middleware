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
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link ConfirmWithTaskInParametersRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfirmWithTaskInParametersRequest extends AbstractConfirmRequest {

    private final Task task;

    public ConfirmWithTaskInParametersRequest(Task task, int confirmStatus, String confirmMessage) {
        this(task, confirmStatus, confirmMessage, true);
    }

    public ConfirmWithTaskInParametersRequest(Task task, int confirmStatus, String confirmMessage, boolean failOnError) {
        super(confirmStatus, confirmMessage, failOnError);
        this.task = task;
    }

    @Override
    protected void addBodyParameter(JSONObject json) {
        // Nothing to add.
    }

    @Override
    protected void addRequestParameter(List<Parameter> params) {
        params.add(new Parameter(AJAXServlet.PARAMETER_ID, task.getObjectID()));
    }
}
