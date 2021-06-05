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

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.tasks.Task;

/**
 * Stores the reponse values of a task insert request and provides methods for
 * working with them.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class InsertResponse extends CommonInsertResponse {

    private final int folderId;

    /**
     * @param response
     */
    InsertResponse(final Response response, final int folderId) {
        super(response);
        this.folderId = folderId;
    }

    /**
     * @return the folderId
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * Puts the data of this insert response into a task object. This are
     * especially the task identifier and the modified time stamp.
     */
    public void fillTask(final Task task) {
        fillObject(task);
    }
}
