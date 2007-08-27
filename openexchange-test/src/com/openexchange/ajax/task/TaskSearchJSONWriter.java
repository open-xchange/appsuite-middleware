/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.ajax.task;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.TaskException;
import com.openexchange.groupware.tasks.TaskException.Code;

/**
 * Writes task search object to a JSON.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskSearchJSONWriter {

    /**
     * Prevent instantiation
     */
    private TaskSearchJSONWriter() {
        super();
    }

    /**
     * Writes a task search object as its JSON representation.
     * @param search task search object.
     * @return a JSON representation of the task search object.
     * @throws TaskException if the task search object contains invalid values.
     * @throws JSONException if writing json gives errors.
     */
    public static JSONObject write(final TaskSearchObject search)
        throws TaskException, JSONException {
        final Date[] range = search.getRange();
        if (range != null && range.length != 2) {
            throw new TaskException(Code.WRONG_DATE_RANGE, range.length);
        }
        final JSONObject json = new JSONObject();
        if (TaskSearchObject.NO_FOLDER == search.getFolder()) {
            json.put(AJAXServlet.PARAMETER_INFOLDER, search.getFolder());
        }
        if (TaskSearchObject.NO_RANGE != range) {
            json.put(AJAXServlet.PARAMETER_START, range[0]);
            json.put(AJAXServlet.PARAMETER_END, range[1]);
        }
        if (TaskSearchObject.NO_PATTERN != search.getPattern()) {
            json.put("pattern", search.getPattern());
        }
        if (TaskSearchObject.NO_TITLE != search.getTitle()) {
            json.put(TaskFields.TITLE, search.getTitle());
        }
        if (TaskSearchObject.NO_PRIORITY != search.getPriority()) {
            json.put(TaskFields.PRIORITY, search.getPriority());
        }
        if (search.isSearchInNote()) {
            json.put("searchinnote", true);
        }
        if (TaskSearchObject.NO_STATUS != search.getStatus()) {
            json.put(TaskFields.STATUS, search.getStatus());
        }
        if (TaskSearchObject.NO_CATEGORIES != search.getCatgories()) {
            json.put(TaskFields.CATEGORIES, search.getCatgories());
        }
        if (search.isSubfolderSearch()) {
            json.put("subfoldersearch", search.isSubfolderSearch());
        }
        /*
         * TODO Fix writing participants
        if (null != search.getParticipants()) {
            json.put(TaskFields.TITLE, TaskWriter.writeParticipants(
                search.getParticipants()));
        }
        */
        return json;
    }
}
