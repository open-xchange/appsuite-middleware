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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.TaskPatches;
import com.openexchange.caldav.Tools;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.data.conversion.ical.SimpleMode;
import com.openexchange.data.conversion.ical.ZoneInfo;
import com.openexchange.dav.resources.CommonResource;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.IncorrectString;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.exception.OXException.Truncated;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link TaskResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TaskResource extends CommonResource<Task> {

    /**
     * All task fields that may be set in iCal files
     */
    private static final int[] CALDAV_FIELDS = {
        Task.START_DATE, // DTSTART
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
    private TaskCollection parent;
    private Task taskToSave = null;

    public static final String EXTENSION_ICS = ".ics";
    private static final String CONTENT_TYPE = "text/calendar; charset=UTF-8";
    private static final int MAX_RETRIES = 3;

    protected final GroupwareCaldavFactory factory;

    private byte[] iCalFile;
    private int retryCount;

    public TaskResource(GroupwareCaldavFactory factory, TaskCollection parent, Task object, WebdavPath url) throws OXException {
        super(parent, object, url);
        this.parent = parent;
        this.factory = factory;
    }

    private TasksSQLInterface getTaskInterface() {
        if (null == this.taskInterface) {
            this.taskInterface = factory.getTaskInterface();
        }
        return this.taskInterface;
    }

    protected byte[] getICalFile() throws WebdavProtocolException {
        if (null == iCalFile) {
            try {
                iCalFile = generateICal();
            } catch (OXException e) {
                throw protocolException(getUrl(), e);
            }
        }
        return iCalFile;
    }

    protected byte[] serialize(ICalSession session) throws ConversionError {
        ICalEmitter icalEmitter = factory.getIcalEmitter();
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = Streams.newByteArrayOutputStream();
            icalEmitter.writeSession(session, outputStream);
            return outputStream.toByteArray();
        } finally {
            Streams.close(outputStream);
        }
    }

    private TimeZone getTimeZone() {
        String timeZoneID = factory.getUser().getTimeZone();
        return TimeZone.getTimeZone(null != timeZoneID ? timeZoneID : "UTC");
    }

    @Override
    protected String getFileExtension() {
        return EXTENSION_ICS;
    }

    @Override
    public Long getLength() throws WebdavProtocolException {
        byte[] iCalFile = getICalFile();
        return new Long(null != iCalFile ? iCalFile.length : 0);
    }

    @Override
    public String getContentType() throws WebdavProtocolException {
        return CONTENT_TYPE;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        byte[] iCalFile = getICalFile();
        if (null != iCalFile) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("iCal file: {}", new String(iCalFile, Charsets.UTF_8));
            }
            return Streams.newByteArrayInputStream(iCalFile);
        }
        return null;
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        if (CaldavProtocol.CAL_NS.getURI().equals(namespace) && "calendar-data".equals(name)) {
            WebdavProperty property = new WebdavProperty(namespace, name);
            byte[] iCalFile = getICalFile();
            if (null != iCalFile) {
                property.setXML(true);
                property.setValue("<![CDATA[" + new String(iCalFile, Charsets.UTF_8) + "]]>");
            }
            return property;
        }
        if (CaldavProtocol.CALENDARSERVER_NS.getURI().equals(namespace) && ("created-by".equals(name) || "updated-by".equals(name))) {
            WebdavProperty property = new WebdavProperty(namespace, name);
            if (null != object) {
                int entityID;
                Date timestamp;
                if ("created-by".equals(name)) {
                    entityID = object.getCreatedBy();
                    timestamp = object.getCreationDate();
                } else {
                    entityID = object.getModifiedBy();
                    timestamp = object.getLastModified();
                }
                try {
                    User user = factory.getService(UserService.class).getUser(entityID, factory.getContext());
                    property.setXML(true);
                    property.setValue(new StringBuilder().append("<CS:first-name>").append(user.getGivenName()).append("</CS:first-name>").append("<CS:last-name>").append(user.getSurname()).append("</CS:last-name>").append("<CS:dtstamp>").append(Tools.formatAsUTC(timestamp)).append("</CS:dtstamp>").append("<D:href>mailto:").append(user.getMail()).append("</D:href>").toString());
                } catch (OXException e) {
                    LOG.warn("error resolving user '{}'", entityID, e);
                }
            }
            return property;
        }
        return null;
    }

    @Override
    public TaskResource move(WebdavPath dest, boolean noroot, boolean overwrite) throws WebdavProtocolException {
        WebdavResource destinationResource = factory.resolveResource(dest);
        DAVCollection destinationCollection = destinationResource.isCollection() ? (DAVCollection) destinationResource : factory.resolveCollection(dest.parent());
        if (false == parent.getClass().isInstance(destinationCollection)) {
            throw protocolException(getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        TaskCollection targetCollection = null;
        try {
            targetCollection = (TaskCollection) destinationCollection;
        } catch (ClassCastException e) {
            throw protocolException(getUrl(), e, HttpServletResponse.SC_FORBIDDEN);
        }
        try {
            move(targetCollection);
        } catch (OXException e) {
            if (handle(e)) {
                return move(dest, noroot, overwrite);
            } else {
                throw protocolException(getUrl(), e);
            }
        }
        this.parent = targetCollection;
        return this;
    }

    /**
     * Handles given {@link OXException} instance and either throws an appropriate {@link WebdavProtocolException} instance or checks if a
     * retry attempt is supposed to be performed.
     *
     * @param e The exception to handle
     * @return <code>true</code> to signal that the operation should be retried; otherwise <code>false</code> if no retry should be performed
     * @throws WebdavProtocolException The appropriate {@link WebdavProtocolException} instance in case no retry is feasible
     */
    protected boolean handle(OXException e) throws WebdavProtocolException {
        boolean retry = false;
        if (Tools.isDataTruncation(e)) {
            /*
             * handle by trimming truncated fields
             */
            if (this.trimTruncatedAttributes(e)) {
                LOG.warn("{}: {} - trimming fields and trying again.", this.getUrl(), e.getMessage());
                retry = true;
            }
        } else if (Tools.isIncorrectString(e)) {
            /*
             * handle by removing problematic characters
             */
            if (replaceIncorrectStrings(e, "")) {
                LOG.warn("{}: {} - removing problematic characters and trying again.", this.getUrl(), e.getMessage());
                retry = true;
            }
        } else if (e.equalsCode(93, "APP")) { // APP-0093
            /*
             * 'Moving a recurring appointment to another folder is not supported.'
             */
            throw protocolException(getUrl(), e, HttpServletResponse.SC_CONFLICT);
        } else if (e.equalsCode(100, "APP")) { // APP-0100
            /*
             * 'Cannot insert appointment ABC. An appointment with the unique identifier (123) already exists.'
             */
            throw protocolException(getUrl(), e, HttpServletResponse.SC_CONFLICT);
        } else if (e.equalsCode(70, "APP")) { // APP-0070
            /*
             * 'You can not use the private flag in a non private folder.'
             */
            throw protocolException(getUrl(), e, HttpServletResponse.SC_FORBIDDEN);
        } else if (e.equalsCode(99, "APP")) { // APP-0099
            /*
             * Changing an exception into a series is not supported.
             */
            throw protocolException(getUrl(), e, HttpServletResponse.SC_FORBIDDEN);
        } else {
            throw protocolException(getUrl(), e);
        }

        if (!retry) {
            return false;
        }

        return ++retryCount <= MAX_RETRIES;
    }

    private boolean trimTruncatedAttributes(OXException e) {
        boolean hasTrimmed = false;
        if (null != e.getProblematics()) {
            for (ProblematicAttribute problematic : e.getProblematics()) {
                if (Truncated.class.isInstance(problematic)) {
                    hasTrimmed |= this.trimTruncatedAttribute((Truncated) problematic);
                }
            }
        }
        return hasTrimmed;
    }

    private boolean replaceIncorrectStrings(OXException e, String replacement) {
        boolean hasReplaced = false;
        if (null != e.getProblematics()) {
            for (ProblematicAttribute problematic : e.getProblematics()) {
                if (IncorrectString.class.isInstance(problematic)) {
                    hasReplaced |= this.replaceIncorrectStrings((IncorrectString) problematic, replacement);
                }
            }
        }
        return hasReplaced;
    }

    @Override
    public void create() throws WebdavProtocolException {
        if (exists()) {
            throw protocolException(getUrl(), HttpServletResponse.SC_CONFLICT);
        }
        try {
            this.createObject();
        } catch (OXException e) {
            if (handle(e)) {
                create();
            } else {
                throw protocolException(getUrl(), e);
            }
        }
    }

    @Override
    public void delete() throws WebdavProtocolException {
        if (false == exists()) {
            throw protocolException(getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
        try {
            deleteObject();
        } catch (OXException e) {
            if (handle(e)) {
                delete();
            } else {
                throw protocolException(getUrl(), e);
            }
        }
    }

    @Override
    public void save() throws WebdavProtocolException {
        if (false == exists()) {
            throw protocolException(getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
        try {
            saveObject();
        } catch (OXException e) {
            if (handle(e)) {
                save();
            } else {
                throw protocolException(getUrl(), e);
            }
        }
    }

    protected void saveObject() throws OXException {
        Task originalTask = parent.load(object);
        checkForExplicitRemoves(originalTask, taskToSave);
        TaskPatches.Incoming.adjustTaskStatus(originalTask, taskToSave);
        TaskPatches.Incoming.adjustTaskStart(originalTask, taskToSave);
        getTaskInterface().updateTaskObject(taskToSave, getId(parent), object.getLastModified());
        handleAttachments(originalTask, taskToSave);
    }

    protected void deleteObject() throws OXException {
        getTaskInterface().deleteTaskObject(object.getObjectID(), object.getParentFolderID(), object.getLastModified());
    }

    protected void createObject() throws OXException {
        taskToSave.removeObjectID(); // in case it's already assigned due to retry operations
        taskToSave.setParentFolderID(null != object ? object.getParentFolderID() : getId(parent));
        getTaskInterface().insertTaskObject(taskToSave);
        handleAttachments(null, taskToSave);
    }

    protected void move(CalDAVFolderCollection<Task> target) throws OXException {
        final Task task = new Task();
        task.setObjectID(object.getObjectID());
        task.setParentFolderID(Tools.parse(target.getFolder().getID()));
        getTaskInterface().updateTaskObject(task, getId(parent), object.getLastModified());
    }

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
        List<Task> tasks = factory.getIcalParser().parseTasks(body, getTimeZone(), factory.getContext(), new LinkedList<ConversionError>(), new LinkedList<ConversionWarning>()).getImportedObjects();
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
                taskToSave.setParentFolderID(getId(parent));
            }
        }
    }

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
