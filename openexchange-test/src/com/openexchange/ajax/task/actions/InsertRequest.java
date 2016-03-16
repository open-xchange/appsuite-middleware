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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.tasks.Task;

/**
 * Stores the parameters for inserting the task.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class InsertRequest extends AbstractTaskRequest<InsertResponse> {

    private final Task task;
    private final TimeZone timeZone;
    private final boolean timeZoneParam;
    private final boolean failOnError;
    private final boolean useLegacyDates;

    /**
     * Initializes a new {@link InsertRequest}.
     *
     * @param task The task to insert
     * @param timeZone The timezone to use
     * @param timeZoneParam <code>true</code> to add the timezone as request parameter, <code>false</code>, otherwise
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     * @param useLegacyDates <code>true</code> to convert the start- and end-date in legacy mode with <code>Date</code>-types,
     *                       <code>false</code> to write start- and end-time properties along with the full-time flag
     */
    public InsertRequest(Task task, TimeZone timeZone, boolean timeZoneParam, boolean failOnError, boolean useLegacyDates) {
        super();
        this.task = task;
        this.timeZone = timeZone;
        this.timeZoneParam = timeZoneParam;
        this.failOnError = failOnError;
        this.useLegacyDates = useLegacyDates;
    }

    public InsertRequest(Task task, TimeZone timeZone, boolean timeZoneParam, boolean failOnError) {
        this(task, timeZone, timeZoneParam, failOnError, true);
    }

    public InsertRequest(Task task, TimeZone timeZone, boolean failOnError) {
        this(task, timeZone, false, failOnError);
    }

    public InsertRequest(Task task, TimeZone timeZone) {
        this(task, timeZone, false, true);
    }

    @Override
    public JSONObject getBody() throws JSONException {
        return useLegacyDates ? convert(task, timeZone) : convertNew(task, timeZone);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        List<Parameter> retval = new ArrayList<Parameter>(3);
        retval.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        retval.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(task.getParentFolderID())));
        if (timeZoneParam) {
            retval.add(new Parameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID()));
        }
        return retval.toArray(new Parameter[retval.size()]);
    }

    @Override
    public InsertParser getParser() {
        return new InsertParser(failOnError, task.getParentFolderID());
    }
}
