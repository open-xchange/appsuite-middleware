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

package com.openexchange.caldav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Patches;
import com.openexchange.caldav.Tools;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.data.conversion.ical.SimpleMode;
import com.openexchange.data.conversion.ical.ZoneInfo;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.IncorrectString;
import com.openexchange.exception.OXException.Truncated;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.protocol.WebdavPath;

/**
 * {@link TaskResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TaskResource extends CalDAVResource<Task> {

    /**
     * All task fields that may be set in iCal files
     */
    private static final int[] CALDAV_FIELDS = {
        Task.END_DATE, // DUE
        Task.TITLE, // SUMMARY
        Task.PRIORITY, // PRIORITY
        Task.NOTE, // DESCRIPTION
        Task.ALARM, // VALARM
        Task.STATUS, // STATUS
        Task.PERCENT_COMPLETED, // PERCENT-COMPLETE
        Task.DATE_COMPLETED, // COMPLETED
    };

    private TasksSQLInterface taskInterface = null;
    private final TaskCollection parent;
    private Task taskToSave = null;

    public TaskResource(final GroupwareCaldavFactory factory, final TaskCollection parent, final Task object, final WebdavPath url) throws OXException {
        super(factory, parent, object, url);
        this.parent = parent;
    }

    private TasksSQLInterface getTaskInterface() {
        if (null == this.taskInterface) {
            this.taskInterface = factory.getTaskInterface();
        }
        return this.taskInterface;
    }

    @Override
    protected void saveObject() throws OXException {
        Task originalTask = parent.load(object);
        checkForExplicitRemoves(originalTask, taskToSave);
        Patches.Incoming.adjustTaskStatus(originalTask, taskToSave);
        Patches.Incoming.adjustTaskStart(originalTask, taskToSave);
        getTaskInterface().updateTaskObject(taskToSave, parentFolderID, object.getLastModified());
        handleAttachments(originalTask, taskToSave);
    }

    @Override
    protected void deleteObject() throws OXException {
        getTaskInterface().deleteTaskObject(object.getObjectID(), object.getParentFolderID(), object.getLastModified());
    }

    @Override
    protected void createObject() throws OXException {
        taskToSave.removeObjectID(); // in case it's already assigned due to retry operations
        taskToSave.setParentFolderID(null != object ? object.getParentFolderID() : parentFolderID);
        getTaskInterface().insertTaskObject(taskToSave);
        handleAttachments(null, taskToSave);
    }

    @Override
    protected void move(CalDAVFolderCollection<Task> target) throws OXException {
        final Task task = new Task();
        task.setObjectID(object.getObjectID());
        task.setParentFolderID(Tools.parse(target.getFolder().getID()));
        getTaskInterface().updateTaskObject(task, parentFolderID, object.getLastModified());
    }

    @Override
    protected byte[] generateICal() throws OXException {
        ICalEmitter icalEmitter = factory.getIcalEmitter();
        ICalSession session = icalEmitter.createSession(new SimpleMode(ZoneInfo.OUTLOOK, null));
        Task task = parent.load(object);
        applyAttachments(task);
        icalEmitter.writeTask(session, task, factory.getContext(), new LinkedList<ConversionError>(), new LinkedList<ConversionWarning>());
        return serialize(session);
    }

    @Override
    protected void deserialize(InputStream body) throws OXException {
        List<Task> tasks = getICalParser().parseTasks(body, getTimeZone(), factory.getContext(), new LinkedList<ConversionError>(), new LinkedList<ConversionWarning>());
        if (null == tasks || 1 != tasks.size()) {
            throw protocolException(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        } else {
            taskToSave = tasks.get(0);
            taskToSave.removeLastModified();
            if (null != object) {
                taskToSave.setParentFolderID(object.getParentFolderID());
                taskToSave.setObjectID(object.getObjectID());
                taskToSave.removeUid();
            } else {
                taskToSave.setParentFolderID(parentFolderID);
            }
        }
    }

    @Override
    protected boolean trimTruncatedAttribute(final Truncated truncated) {
        if (null != this.taskToSave) {
            int field = truncated.getId();
            if (field <= 0) {
                return false;
            }

            Object value = this.taskToSave.get(field);
            if (null != value && String.class.isInstance(value)) {
                String stringValue = (String)value;
                if (stringValue.length() > truncated.getMaxSize()) {
                    taskToSave.set(field, stringValue.substring(0, truncated.getMaxSize()));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean replaceIncorrectStrings(IncorrectString incorrectString, String replacement) {
        if (null != taskToSave) {
            Object value = taskToSave.get(incorrectString.getId());
            if (null != value && String.class.isInstance(value)) {
                String stringValue = (String) value;
                String replacedString = stringValue.replaceAll(incorrectString.getIncorrectString(), replacement);
                if (false == stringValue.equals(replacedString)) {
                    taskToSave.set(incorrectString.getId(), replacedString);
                    return true;
                }
            }
        }
        return false;
    }

    private void checkForExplicitRemoves(final Task originalTask, final Task updatedTask) {
        /*
         * reset previously set task fields
         */
        for (final int field : CALDAV_FIELDS) {
            if (originalTask.contains(field) && false == updatedTask.contains(field)) {
                if (Task.STATUS == field) {
                    // '1' is the default value for state
                    updatedTask.setStatus(Task.NOT_STARTED);
                } else if (Task.PERCENT_COMPLETED == field) {
                    // treat non-existant percentage as no-change (bug #24812)
                } else {
                    // use generic setter with default value
                    updatedTask.set(field, updatedTask.get(field));
                }
            }
        }
    }

}
