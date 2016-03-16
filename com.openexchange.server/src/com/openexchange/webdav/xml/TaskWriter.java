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

package com.openexchange.webdav.xml;

import static com.openexchange.webdav.xml.fields.CalendarFields.ALARM;
import static com.openexchange.webdav.xml.fields.CalendarFields.ALARM_FLAG;
import static com.openexchange.webdav.xml.fields.CalendarFields.END_DATE;
import static com.openexchange.webdav.xml.fields.CalendarFields.START_DATE;
import static com.openexchange.webdav.xml.fields.DataFields.LAST_MODIFIED;
import static com.openexchange.webdav.xml.fields.DataFields.OBJECT_ID;
import static com.openexchange.webdav.xml.fields.TaskFields.ACTUAL_COSTS;
import static com.openexchange.webdav.xml.fields.TaskFields.ACTUAL_DURATION;
import static com.openexchange.webdav.xml.fields.TaskFields.BILLING_INFORMATION;
import static com.openexchange.webdav.xml.fields.TaskFields.COMPANIES;
import static com.openexchange.webdav.xml.fields.TaskFields.CURRENCY;
import static com.openexchange.webdav.xml.fields.TaskFields.DATE_COMPLETED;
import static com.openexchange.webdav.xml.fields.TaskFields.PERCENT_COMPLETED;
import static com.openexchange.webdav.xml.fields.TaskFields.PRIORITY;
import static com.openexchange.webdav.xml.fields.TaskFields.STATUS;
import static com.openexchange.webdav.xml.fields.TaskFields.TARGET_COSTS;
import static com.openexchange.webdav.xml.fields.TaskFields.TARGET_DURATION;
import static com.openexchange.webdav.xml.fields.TaskFields.TRIP_METER;
import java.io.OutputStream;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * The WebDAV/XML writer for tasks.
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class TaskWriter extends CalendarWriter {

    protected final static int[] changeFields = {
        DataObject.OBJECT_ID,
        DataObject.CREATED_BY,
        DataObject.CREATION_DATE,
        DataObject.LAST_MODIFIED,
        DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID,
        CommonObject.PRIVATE_FLAG,
        CommonObject.CATEGORIES,
        CalendarObject.TITLE,
        CalendarObject.START_DATE,
        CalendarObject.END_DATE,
        CalendarObject.NOTE,
        CalendarObject.RECURRENCE_TYPE,
        CalendarObject.PARTICIPANTS,
        CalendarObject.UID,
        Task.ACTUAL_COSTS,
        Task.ACTUAL_DURATION,
        Task.ALARM,
        Task.BILLING_INFORMATION,
        Task.CATEGORIES,
        Task.COMPANIES,
        Task.CURRENCY,
        Task.DATE_COMPLETED,
        Task.IN_PROGRESS,
        Task.PERCENT_COMPLETED,
        Task.PRIORITY,
        Task.STATUS,
        Task.TARGET_COSTS,
        Task.TARGET_DURATION,
        Task.TRIP_METER,
        Task.COLOR_LABEL,
        Task.NUMBER_OF_ATTACHMENTS
    };

    protected final static int[] deleteFields = {
        DataObject.OBJECT_ID,
        DataObject.LAST_MODIFIED
    };

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskWriter.class);

    /**
     * Initializes a new {@link TaskWriter}
     */
    public TaskWriter() {
        super();
    }

    /**
     * Initializes a new {@link TaskWriter}
     * @param userObj The user object
     * @param ctx The user's context
     * @param sessionObj The session providing needed user data
     */
    public TaskWriter(final User userObj, final Context ctx, final Session sessionObj) {
        this.userObj = userObj;
        this.ctx = ctx;
        this.sessionObj = sessionObj;
    }

    public void startWriter(final int objectId, final int folderId, final OutputStream os) throws Exception {
        final Element eProp = new Element("prop", "D", "DAV:");
        final XMLOutputter xo = new XMLOutputter();
        try {
            final TasksSQLInterface tasksql = new TasksSQLImpl(sessionObj);
            final Task taskobject = tasksql.getTaskById(objectId, folderId);
            writeObject(taskobject, false, xo, os);
        } catch (final OXException exc) {
            if (exc.isGeneric(Generic.NOT_FOUND)) {
                writeResponseElement(eProp, 0, HttpServletResponse.SC_NOT_FOUND, XmlServlet.OBJECT_NOT_FOUND_EXCEPTION, xo, os);
            } else {
                writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(XmlServlet.SERVER_ERROR_EXCEPTION, XmlServlet.SERVER_ERROR_STATUS), xo, os);
            }
        } catch (final Exception ex) {
            LOG.error("", ex);
            writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(XmlServlet.SERVER_ERROR_EXCEPTION, XmlServlet.SERVER_ERROR_STATUS), xo, os);
        }
    }

    public void startWriter(final boolean modified, final boolean deleted, final boolean bList, final int folder_id, final Date lastsync, final OutputStream os) throws Exception {
        final TasksSQLInterface tasksql = new TasksSQLImpl(sessionObj);
        final XMLOutputter xo = new XMLOutputter();
        /*
         * Fist send all 'deletes', than all 'modified'
         */
        if (deleted) {
            SearchIterator<Task> it = null;
            try {
                it = tasksql.getDeletedTasksInFolder(folder_id, deleteFields, lastsync);
                writeIterator(it, true, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }

        if (modified) {
            SearchIterator<Task> it = null;
            try {
                it = tasksql.getModifiedTasksInFolder(folder_id, changeFields, lastsync);
                writeIterator(it, false, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }

        if (bList) {
            SearchIterator<Task> it = null;
            try {
                it = tasksql.getTaskList(folder_id, -1, -1, 0, null, deleteFields);
                writeList(it, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }
    }

    public void writeIterator(final SearchIterator<Task> it, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        while (it.hasNext()) {
            writeObject(it.next(), delete, xo, os);
        }
    }

    public void writeObject(final Task taskObj, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        writeObject(taskObj, new Element("prop", "D", "DAV:"), delete, xo, os);
    }

    public void writeObject(final Task taskObj, final Element e_prop, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        int status = 200;
        String description = "OK";
        int object_id = 0;

        try {
            object_id = taskObj.getObjectID();
            addContent2PropElement(e_prop, taskObj, delete);
        } catch (final Exception exc) {
            LOG.error("writeObject", exc);
            status = 500;
            description = "Server Error: " + exc.getMessage();
            object_id = 0;
        }

        writeResponseElement(e_prop, object_id, status, description, xo, os);
    }

    public void addContent2PropElement(final Element e_prop, final Task taskObj, final boolean delete) throws Exception {
        if (delete) {
            addElement(OBJECT_ID, taskObj.getObjectID(), e_prop);
            addElement(LAST_MODIFIED, taskObj.getLastModified(), e_prop);
            addElement("object_status", "DELETE", e_prop);
        } else {
            addElement("object_status", "CREATE", e_prop);
            if (taskObj.containsStartDate()) {
                addElement(START_DATE, taskObj.getStartDate(), e_prop);
            }
            if (taskObj.containsEndDate()) {
                addElement(END_DATE, taskObj.getEndDate(), e_prop);
            }
            if (taskObj.containsActualCosts()) {
                addElement(ACTUAL_COSTS, taskObj.getActualCosts(), e_prop);
            }
            if (taskObj.containsActualDuration()) {
                addElement(ACTUAL_DURATION, taskObj.getActualDuration(), e_prop);
            }
            addElement(BILLING_INFORMATION, taskObj.getBillingInformation(), e_prop);
            addElement(COMPANIES, taskObj.getCompanies(), e_prop);
            if (taskObj.containsCurrency()) {
                addElement(CURRENCY, taskObj.getCurrency(), e_prop);
            }
            addElement(DATE_COMPLETED, taskObj.getDateCompleted(), e_prop);
            if (taskObj.containsPercentComplete()) {
                addElement(PERCENT_COMPLETED, taskObj.getPercentComplete(), e_prop);
            }
            if (taskObj.containsPriority()) {
                addElement(PRIORITY, taskObj.getPriority(), e_prop);
            }
            if (taskObj.containsStatus()) {
                addElement(STATUS, taskObj.getStatus(), e_prop);
            }
            if (taskObj.containsTargetCosts()) {
                addElement(TARGET_COSTS, taskObj.getTargetCosts(), e_prop);
            }
            if (taskObj.containsTargetDuration()) {
                addElement(TARGET_DURATION, taskObj.getTargetDuration(), e_prop);
            }
            if (taskObj.containsTripMeter()) {
                addElement(TRIP_METER, taskObj.getTripMeter(), e_prop);
            }
            if (taskObj.containsAlarm()) {
                addElement(ALARM_FLAG, true, e_prop);
                addElement(ALARM, taskObj.getAlarm(), e_prop);
            } else {
                addElement(ALARM_FLAG, false, e_prop);
            }
            writeCalendarElements(taskObj, e_prop);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getModule() {
        return Types.TASK;
    }
}
