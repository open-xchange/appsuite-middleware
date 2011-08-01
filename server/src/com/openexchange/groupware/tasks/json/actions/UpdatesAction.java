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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.groupware.tasks.json.actions;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.tasks.json.TaskRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdatesAction extends AbstractTaskAction {

    /**
     * Initializes a new {@link UpdatesAction}.
     * @param services
     */
    public UpdatesAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final TaskRequest req) throws OXException, JSONException {
        final int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
        final int[] columnsToLoad = removeVirtualColumns(columns);
        final Date requestedTimestamp = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        Date timestamp = new Date(requestedTimestamp.getTime());
        final int folderId = req.checkInt(AJAXServlet.PARAMETER_FOLDERID);
        String ignore = req.getParameter(AJAXServlet.PARAMETER_IGNORE);

        if (ignore == null) {
            ignore = "deleted";
        }

        boolean bIgnoreDelete = false;

        if (ignore.indexOf("deleted") != -1) {
            bIgnoreDelete = true;
        }

        Date lastModified = null;

        final JSONArray jsonResponseArray = new JSONArray();
        SearchIterator<Task> it = null;
        try {
            final int[] internalColumns = new int[columnsToLoad.length+1];
            System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
            internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

            final TasksSQLInterface taskssql = new TasksSQLImpl(req.getSession());
            final TaskWriter taskWriter = new TaskWriter(req.getTimeZone());

            it = taskssql.getModifiedTasksInFolder(folderId, internalColumns, requestedTimestamp);
            while (it.hasNext()) {
                final Task taskObj = it.next();

                taskWriter.writeArray(taskObj, columns, jsonResponseArray);

                lastModified = taskObj.getLastModified();

                if (timestamp.getTime() < lastModified.getTime()) {
                    timestamp = lastModified;
                }
            }

            if (!bIgnoreDelete) {
                it.close();
                it = taskssql.getDeletedTasksInFolder(folderId, internalColumns, requestedTimestamp);
                while (it.hasNext()) {
                    final Task taskObj = it.next();

                    jsonResponseArray.put(taskObj.getObjectID());

                    lastModified = taskObj.getLastModified();

                    if (timestamp.getTime() < lastModified.getTime()) {
                        timestamp = lastModified;
                    }
                }
            }

            return new AJAXRequestResult(jsonResponseArray, timestamp, "json");
        } finally {
            if(it!=null) {
                it.close();
            }
        }
    }

}
