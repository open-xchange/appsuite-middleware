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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.event.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.api2.OXException;
import com.openexchange.event.CommonEvent;
import com.openexchange.event.EventException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderPermissionException;

/**
 * EventClient
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class EventClient {

    private static final Log LOG = LogFactory.getLog(EventClient.class);

    public static final int CREATED = 5;
    public static final int CHANGED = 6;
    public static final int DELETED = 7;
    public static final int MOVED = 8;
    public static final int CONFIRM_ACCEPTED = 9;
    public static final int CONFIRM_DECLINED = 10;
    public static final int CONFIRM_TENTATIVE = 11;

    private final Session session;

    private final int userId;

    private final int contextId;

    public EventClient(final Session session) {
        this.session = session;
        userId = session.getUserId();
        contextId = session.getContextId();
    }

    public void create(final Appointment appointmentObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointmentObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            create(appointmentObj, folderObj);
        }
    }

    public void create(final Appointment appointmentObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.INSERT, Types.APPOINTMENT, appointmentObj, null, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/insert", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(appointmentObj, CREATED, session);
        EventQueue.add(eventObject);
    }

    public void modify(final Appointment appointmentObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointmentObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            modify(null, appointmentObj, folderObj);
        }
    }

    public void modify(final Appointment oldAppointmentObj, final Appointment newAppointmentObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.UPDATE, Types.APPOINTMENT, newAppointmentObj, oldAppointmentObj, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/update", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newAppointmentObj, CHANGED, session);
        EventQueue.add(eventObject);
    }

    public void accepted(final Appointment appointmentObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointmentObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            accepted(null, appointmentObj, folderObj);
        }
    }

    public void accepted(final Appointment oldAppointmentObj, final Appointment newAppointmentObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.CONFIRM_ACCEPTED, Types.APPOINTMENT, newAppointmentObj, oldAppointmentObj, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/accepted", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newAppointmentObj, CONFIRM_ACCEPTED, session);
        EventQueue.add(eventObject);
    }

    public void declined(final Appointment appointmentObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointmentObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            declined(null, appointmentObj, folderObj);
        }
    }

    public void declined(final Appointment oldAppointmentObj, final Appointment newAppointmentObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.CONFIRM_DECLINED, Types.APPOINTMENT, newAppointmentObj, oldAppointmentObj, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/declined", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newAppointmentObj, CONFIRM_DECLINED, session);
        EventQueue.add(eventObject);
    }

    public void tentative(final Appointment appointmentObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointmentObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            tentative(null, appointmentObj, folderObj);
        }
    }

    public void tentative(final Appointment oldAppointmentObj, final Appointment newAppointmentObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.CONFIRM_TENTATIVE, Types.APPOINTMENT, newAppointmentObj, oldAppointmentObj, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/tentative", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(newAppointmentObj, CONFIRM_TENTATIVE, session);
        EventQueue.add(eventObject);
    }

    public void delete(final Appointment appointmentObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = appointmentObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            delete(appointmentObj, folderObj);
        }
    }

    public void delete(final Appointment appointmentObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.DELETE, Types.APPOINTMENT, appointmentObj, null, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/delete", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(appointmentObj, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void move(final Appointment appointmentObj, final FolderObject sourceFolder, final FolderObject destinationFolder) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.MOVE, Types.APPOINTMENT, appointmentObj, null, sourceFolder, destinationFolder, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/appointment/move", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(appointmentObj, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void create(final Task taskObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);


        final int folderId = taskObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            create(taskObj, folderObj);
        }
    }

    public void create(final Task taskObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.INSERT, Types.TASK, taskObj, null, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/insert", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(taskObj, CREATED, session);
        EventQueue.add(eventObject);
    }

    public void modify(final Task oldTask, final Task newTask, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.UPDATE, Types.TASK, newTask, oldTask, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/update", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(oldTask, CHANGED, session);
        EventQueue.add(eventObject);
    }

    public void accept(final Task task) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = task.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            accept(null, task, folderObj);
        }
    }

    public void accept(final Task oldTask, final Task newTask, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.CONFIRM_ACCEPTED, Types.TASK, newTask, oldTask, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/accepted", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(oldTask, CONFIRM_ACCEPTED, session);
        EventQueue.add(eventObject);
    }

    public void declined(final Task task) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = task.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            declined(null, task, folderObj);
        }
    }

    public void declined(final Task oldTask, final Task newTask, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.CONFIRM_DECLINED, Types.TASK, newTask, oldTask, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/declined", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(oldTask, CONFIRM_DECLINED, session);
        EventQueue.add(eventObject);
    }

    public void tentative(final Task task) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = task.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            declined(null, task, folderObj);
        }
    }

    public void tentative(final Task oldTask, final Task newTask, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.CONFIRM_TENTATIVE, Types.TASK, newTask, oldTask, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/tentative", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(oldTask, CONFIRM_TENTATIVE, session);
        EventQueue.add(eventObject);
    }

    public void delete(final Task taskObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);


        final int folderId = taskObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            delete(taskObj, folderObj);
        }
    }

    public void delete(final Task taskObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.DELETE, Types.TASK, taskObj, null, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/delete", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(taskObj, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void move(final Task taskObj, final FolderObject sourceFolder, final FolderObject destinationFolder) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.MOVE, Types.TASK, taskObj, null, sourceFolder, destinationFolder, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/task/move", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(taskObj, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void create(final Contact contactObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = contactObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            create(contactObj, folderObj);
        }
    }

    public void create(final Contact contactObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.INSERT, Types.CONTACT, contactObj, null, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/contact/insert", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(contactObj, CREATED, session);
        EventQueue.add(eventObject);
    }

    public void modify(final Contact oldObj, final Contact contactObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.UPDATE, Types.CONTACT, contactObj, oldObj, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/contact/update", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(contactObj, CHANGED, session);
        EventQueue.add(eventObject);
    }

    public void delete(final Contact contactObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = contactObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject folderObj = getFolder(folderId, ctx);
            delete(contactObj, folderObj);
        }
    }

    public void delete(final Contact contactObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.DELETE, Types.CONTACT, contactObj, null, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/contact/delete", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(contactObj, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void move(final Contact contactObj, final FolderObject folderObj) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.MOVE, Types.CONTACT, contactObj, null, folderObj, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/contact/move", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(contactObj, MOVED, session);
        EventQueue.add(eventObject);
    }

    public void create(final FolderObject folderObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = folderObj.getParentFolderID();
        if (folderId > 0) {
            final FolderObject parentFolderObj = getFolder(folderId, ctx);
            create(folderObj, parentFolderObj);
        }
    }

    public void create(final FolderObject folderObj, final FolderObject parentFolder) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.INSERT, Types.FOLDER, folderObj, null, parentFolder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/folder/insert", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(folderObj, CREATED, session);
        EventQueue.add(eventObject);
    }

    public void modify(final FolderObject oldObj, final FolderObject folderObj, final FolderObject parentFolder) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.UPDATE, Types.FOLDER, folderObj, oldObj, parentFolder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/folder/update", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(folderObj, CHANGED, session);
        EventQueue.add(eventObject);
    }

    public void delete(final FolderObject folderObj) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final int folderId = folderObj.getParentFolderID();
        if (folderId > 0) {
            FolderObject parentFolderObj = null;
            try {
                parentFolderObj = getFolder(folderId, ctx);
            } catch (final OXFolderPermissionException exc) {
                LOG.error("cannot load folder", exc);
            }
            delete(folderObj, parentFolderObj);
        }
    }

    public void delete(final FolderObject folderObj, final FolderObject parentFolder) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.DELETE, Types.FOLDER, folderObj, null, parentFolder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/folder/delete", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(folderObj, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void create(final DocumentMetadata document) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final long folderId = document.getFolderId();
        if (folderId > 0) {
            final FolderObject parentFolderObj = getFolder((int)folderId, ctx);
            create(document, parentFolderObj);
        }
    }

    public void create(final DocumentMetadata document, final FolderObject parentFolder) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.INSERT, Types.INFOSTORE, document, null, parentFolder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/infostore/insert", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(document, CREATED, session);
        EventQueue.add(eventObject);
    }

    public void modify(final DocumentMetadata document) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);

        final long folderId = document.getFolderId();
        if (folderId > 0) {
            final FolderObject parentFolderObj = getFolder((int)folderId, ctx);
            modify(null, document, parentFolderObj);
        }
    }

    public void modify(final DocumentMetadata oldDocument, final DocumentMetadata document, final FolderObject parentFolder) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.UPDATE, Types.INFOSTORE, document, oldDocument, parentFolder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/infostore/update", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(document, CHANGED, session);
        EventQueue.add(eventObject);
    }

    public void delete(final DocumentMetadata document) throws EventException, OXException, ContextException {
        final Context ctx = ContextStorage.getInstance().getContext(contextId);
        //FolderSQLInterface folderSql = new RdbFolderSQLInterface(session, ctx);

        final long folderId = document.getFolderId();
        if (folderId > 0) {
            final FolderObject parentFolderObj = getFolder((int)folderId, ctx);
            delete(document, parentFolderObj);
        }
    }

    public void delete(final DocumentMetadata document, final FolderObject parentFolder) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.DELETE, Types.INFOSTORE, document, null, parentFolder, null, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/infostore/delete", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(document, DELETED, session);
        EventQueue.add(eventObject);
    }

    public void move(final DocumentMetadata document, final FolderObject sourceFolder, final FolderObject destinationFolder) throws EventException {
        final CommonEvent genericEvent = new CommonEventImpl(userId, contextId, CommonEvent.MOVE, Types.INFOSTORE, document, null, sourceFolder, destinationFolder, session);

        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>();
        ht.put(CommonEvent.EVENT_KEY, genericEvent);

        final Event event = new Event("com/openexchange/groupware/infostore/move", ht);
        triggerEvent(event);

        final EventObject eventObject = new EventObject(document, MOVED, session);
        EventQueue.add(eventObject);
    }

    protected void triggerEvent(final Event event) throws EventException {
        final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
        if (eventAdmin == null) {
            throw new EventException("event service not available");
        }
        eventAdmin.postEvent(event);
    }

    private FolderObject getFolder(final int folderId, final Context ctx) throws OXException {
        return new OXFolderAccess(ctx).getFolderObject(folderId);
    }
}
