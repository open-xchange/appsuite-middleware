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

import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;

/**
 * Stores the response of getting a task from the server.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GetResponse extends AbstractAJAXResponse {

    /**
     * @param response
     */
    GetResponse(final Response response) {
        super(response);
    }

    /**
     * @return the task
     * @throws OXException parsing the task out of the response fails.
     */
    public Task getTask(final TimeZone timeZone) throws OXException {
        return getTask(timeZone, true);
    }

    /**
     * Parses the task from the response.
     *
     * @param timeZone The client timezone
     * @param useLegacyDates <code>true</code> to convert the start- and end-date in legacy mode with <code>Date</code>-types,
     *                       <code>false</code> to write start- and end-time properties along with the full-time flag
     * @return The task
     * @throws OXException
     */
    public Task getTask(final TimeZone timeZone, boolean useLegacyDates) throws OXException {
        JSONObject json = (JSONObject) getData();
        if (useLegacyDates) {
            json = new JSONObject(json);
            json.remove(CalendarFields.FULL_TIME);
            json.remove(TaskFields.START_TIME);
            json.remove(TaskFields.END_TIME);
        }
        return parseTask(json, timeZone);
    }

    private Task parseTask(JSONObject json, TimeZone timeZone) throws OXException {
        Task task = new Task();
        new TaskParser(true, timeZone).parse(task, json, Locale.ENGLISH);
        return task;
    }

}
