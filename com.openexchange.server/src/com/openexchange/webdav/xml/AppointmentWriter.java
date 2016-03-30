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

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.webdav.xml.fields.AppointmentFields;
import com.openexchange.webdav.xml.fields.CalendarFields;
import com.openexchange.webdav.xml.fields.CommonFields;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * The WebDAV/XML writer for calendar module.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class AppointmentWriter extends CalendarWriter {

    protected final static int[] changeFields = { DataObject.OBJECT_ID, DataObject.CREATED_BY,
            DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY, FolderChildObject.FOLDER_ID,
            CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION,
            CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE,
            CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.SHOWN_AS, Appointment.FULL_TIME,
            Appointment.COLOR_LABEL, Appointment.NUMBER_OF_ATTACHMENTS,
            Appointment.CHANGE_EXCEPTIONS, Appointment.DELETE_EXCEPTIONS, Appointment.RECURRENCE_ID,
            Appointment.RECURRENCE_POSITION, Appointment.RECURRENCE_CALCULATOR, Appointment.TIMEZONE, Appointment.UID };

    protected final static int[] deleteFields = { DataObject.OBJECT_ID, DataObject.LAST_MODIFIED,
            Appointment.RECURRENCE_ID };

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentWriter.class);

    /**
     * Initializes a new {@link AppointmentWriter}.
     */
    public AppointmentWriter() {
        super();
        userObj = null;
        ctx = null;
        sessionObj = null;
    }

    /**
     * Initializes a new {@link AppointmentWriter}.
     *
     * @param userObj The user
     * @param ctx The context
     * @param sessionObj The session providing needed user data
     */
    public AppointmentWriter(final User userObj, final Context ctx, final Session sessionObj) {
        super();
        this.userObj = userObj;
        this.ctx = ctx;
        this.sessionObj = sessionObj;
    }

    public void startWriter(final int objectId, final int folderId, final OutputStream os) throws Exception {
        final AppointmentSqlFactoryService factoryService = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class);
        if (null == factoryService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AppointmentSqlFactoryService.class.getName());
        }
        final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(sessionObj);
        final Element eProp = new Element("prop", "D", "DAV:");
        final XMLOutputter xo = new XMLOutputter();
        try {
            final Appointment appointmentobject = appointmentsql.getObjectById(objectId, folderId);
            writeObject(appointmentobject, eProp, false, xo, os);
        } catch (final OXException exc) {
            if (exc.isGeneric(Generic.NOT_FOUND)) {
                writeResponseElement(eProp, 0, HttpServletResponse.SC_NOT_FOUND, XmlServlet.OBJECT_NOT_FOUND_EXCEPTION, xo,
                    os);
            } else {
                writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(XmlServlet.SERVER_ERROR_EXCEPTION, XmlServlet.SERVER_ERROR_STATUS), xo, os);
            }
        } catch (final Exception ex) {
            LOG.error("AppointmentWriter.startWriter()", ex);
            writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(XmlServlet.SERVER_ERROR_EXCEPTION, XmlServlet.SERVER_ERROR_STATUS), xo, os);
        }
    }

    public void startWriter(final boolean bModified, final boolean bDeleted, final boolean bList, final int folder_id, final Date lastsync, final OutputStream os) throws Exception {
        final AppointmentSqlFactoryService factoryService = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class);
        if (null == factoryService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AppointmentSqlFactoryService.class.getName());
        }
        final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(sessionObj);
        final XMLOutputter xo = new XMLOutputter();
        SearchIterator<? extends Appointment> it = null;
        /*
         * Fist send all 'deletes', than all 'modified'
         */
        if (bDeleted) {
            try {
                it = appointmentsql.getDeletedAppointmentsInFolder(folder_id, deleteFields, lastsync);
                writeIterator(it, true, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }
        if (bModified || bDeleted) {
            try {
                it = appointmentsql.getModifiedAppointmentsInFolder(folder_id, changeFields, lastsync);
                final Queue<Appointment> modifiedQueue = new LinkedList<Appointment>();
                while (it.hasNext()) {
                    /*
                     * Check whether 'modify' or 'delete' shall be sent
                     */
                    final Appointment appObject = it.next();
                    if (appObject.getPrivateFlag() && sessionObj.getUserId() != appObject.getCreatedBy()) {
                        if (bDeleted) {
                            /*
                             * Appointment has been set to private: send as
                             * 'delete' for requesting user
                             */
                            writeObject(appObject, true, xo, os);
                        }
                    } else {
                        if (bModified) {
                            /*
                             * Test for non-owning recurring appointment
                             */
                            if (appObject.isMaster() && appObject.getCreatedBy() != sessionObj.getUserId()) {
                                /*
                                 * Check its change exceptions
                                 */
                                final int[] ids = checkChangeExceptions(folder_id, appObject, sessionObj);
                                if (ids.length > 0) {
                                    /*
                                     * Load withheld change exceptions by IDs
                                     */
                                    final CalendarCollectionService calColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
                                    if (null == calColl) {
                                        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(CalendarCollectionService.class.getName());
                                    }
                                    final Appointment[] ces = calColl.getAppointmentsByID(folder_id, ids, deleteFields, sessionObj);
                                    for (final Appointment ce : ces) {
                                        if (null != ce) {
                                            /*
                                             * Compose 'delete' for withheld
                                             * change exceptions to ensure it's
                                             * removed in MS Outlook
                                             */
                                            writeObject(ce, true, xo, os);
                                        }
                                    }
                                }
                            }
                            /*
                             * Enqueue as 'modified' for current appointment
                             */
                            modifiedQueue.add(appObject);
                        }
                    }
                }
                while (!modifiedQueue.isEmpty()) {
                    final Appointment appObject = modifiedQueue.poll();
                    if (null != appObject) {
                        /*
                         * Send common 'modify' for current appointment
                         */
                        writeObject(appObject, false, xo, os);
                    }
                }
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }
        if (bList) {
            try {
                it = appointmentsql.getModifiedAppointmentsInFolder(folder_id, changeFields, new Date(0));
                writeList(it, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }
    }

    public void writeIterator(final SearchIterator<? extends Appointment> it, final boolean delete,
            final XMLOutputter xo, final OutputStream os) throws Exception {
        while (it.hasNext()) {
            writeObject(it.next(), delete, xo, os);
        }
    }

    public void writeObject(final Appointment appointmentobject, final boolean delete, final XMLOutputter xo,
            final OutputStream os) throws Exception {
        writeObject(appointmentobject, new Element("prop", "D", "DAV:"), delete, xo, os);
    }

    public void writeObject(final Appointment appointmentobject, final Element e_prop, final boolean delete,
            final XMLOutputter xo, final OutputStream os) throws Exception {
        int status = 200;
        String description = "OK";
        int object_id = 0;
        try {
            object_id = appointmentobject.getObjectID();
            addContent2PropElement(e_prop, appointmentobject, delete);
        } catch (final Exception exc) {
            LOG.error("writeObject", exc);
            status = 500;
            description = "Server Error: " + exc.toString();
            object_id = 0;
        }
        writeResponseElement(e_prop, object_id, status, description, xo, os);
    }

    protected void addContent2PropElement(final Element e_prop, final Appointment ao, final boolean delete)
            throws Exception {
        addContent2PropElement(e_prop, ao, delete, false);
    }

    public void addContent2PropElement(final Element e_prop, final Appointment ao, final boolean delete,
            final boolean externalUse) throws OXException, SearchIteratorException, UnsupportedEncodingException {
        if (delete) {
            addElement(DataFields.OBJECT_ID, ao.getObjectID(), e_prop);
            addElement(DataFields.LAST_MODIFIED, ao.getLastModified(), e_prop);

            if (ao.containsRecurrenceID()) {
                addElement(CalendarFields.RECURRENCE_ID, ao.getRecurrenceID(), e_prop);
            }

            addElement("object_status", "DELETE", e_prop);
        } else {
            addElement("object_status", "CREATE", e_prop);

            final boolean fullTime = ao.getFullTime();

            if (ao.getRecurrenceType() == CalendarObject.NONE) {
                addElement(CalendarFields.START_DATE, ao.getStartDate(), e_prop);
                addElement(CalendarFields.END_DATE, ao.getEndDate(), e_prop);
            } else {
                if (!externalUse) {
                    RecurringResultsInterface recuResults = null;
                    try {
                        final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
                        recuResults = recColl.calculateFirstRecurring(ao);
                    } catch (final OXException x) {
                        LOG.error("Can not calculate recurrence {}:{}", ao.getObjectID(), sessionObj.getContextId(), x);
                    }
                    if (recuResults != null && recuResults.size() == 1) {
                        ao.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
                        ao.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));
                    } else {
                        LOG.warn("Cannot load first recurring appointment from appointment object: {} / {}\n\n\n", ao.getRecurrenceType(), ao.getObjectID());
                    }
                }
                addElement(CalendarFields.START_DATE, ao.getStartDate(), e_prop);
                addElement(CalendarFields.END_DATE, ao.getEndDate(), e_prop);
            }

            if (ao.containsDeleteExceptions()) {
                final Date[] deleteExceptions = ao.getDeleteException();
                if (deleteExceptions != null) {
                    final StringBuilder stringBuilder = new StringBuilder();
                    for (int a = 0; a < deleteExceptions.length; a++) {
                        if (a > 0) {
                            stringBuilder.append(',');
                        }

                        stringBuilder.append(deleteExceptions[a].getTime());
                    }
                    addElement(CalendarFields.DELETE_EXCEPTIONS, stringBuilder.toString(), e_prop);
                }
            }

            if (ao.containsChangeExceptions()) {
                final Date[] changeException = ao.getChangeException();
                if (changeException != null) {
                    final StringBuilder stringBuilder = new StringBuilder();
                    for (int a = 0; a < changeException.length; a++) {
                        if (a > 0) {
                            stringBuilder.append(',');
                        }

                        stringBuilder.append(changeException[a].getTime());
                    }
                    addElement(CalendarFields.CHANGE_EXCEPTIONS, stringBuilder.toString(), e_prop);
                }
            }

            addElement(AppointmentFields.LOCATION, ao.getLocation(), e_prop);
            addElement(AppointmentFields.FULL_TIME, fullTime, e_prop);
            addElement(AppointmentFields.SHOW_AS, ao.getShownAs(), e_prop);

            if (ao.containsRecurrenceDatePosition()) {
                addElement(CalendarFields.RECURRENCE_DATE_POSITION, ao.getRecurrenceDatePosition(), e_prop);
            }

            if (ao.containsAlarm()) {
                addElement(CalendarFields.ALARM_FLAG, true, e_prop);
                addElement(CalendarFields.ALARM, ao.getAlarm(), e_prop);
            } else {
                addElement(CalendarFields.ALARM_FLAG, false, e_prop);
            }

            if (ao.getIgnoreConflicts()) {
                addElement(AppointmentFields.IGNORE_CONFLICTS, true, e_prop);
            }

            if (ao.containsUid()) {
                addElement(AppointmentFields.UID, ao.getUid(), e_prop);
            }

            addElement(CommonFields.COLORLABEL, ao.getLabel(), e_prop);

            writeCalendarElements(ao, e_prop);
        }
    }

    @Override
    protected int getModule() {
        return Types.APPOINTMENT;
    }

    private static int[] checkChangeExceptions(final int folderId, final Appointment master, final Session session)
            throws OXException {
        final Date[] changeExceptions = master.getChangeException();
        if (null == changeExceptions || changeExceptions.length == 0) {
            return new int[0];
        }
        /*
         * Check for each change exception if it is visible to session user. If
         * not it is moved from change exceptions to delete exceptions
         */
        final Date[] deleteExceptions = master.getDeleteException();
        final List<Date> dlist = null == deleteExceptions ? new ArrayList<Date>(changeExceptions.length)
                : new ArrayList<Date>(Arrays.asList(deleteExceptions));
        final List<Date> clist = new ArrayList<Date>(Arrays.asList(changeExceptions));
        final int[] ids = new int[changeExceptions.length];
        int c = 0;
        boolean applyNewExceptions = false;
        for (final Iterator<Date> iterator = clist.iterator(); iterator.hasNext();) {
            final Date changeException = iterator.next();
            final CalendarCollectionService calColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            final Appointment app = calColl.getChangeExceptionByDate(folderId, master
                    .getRecurrenceID(), changeException, changeFields, session);
            if (null != app && !isVisibleAsParticipantOrOwner(app, session.getUserId())) {
                // Remove from change exceptions
                iterator.remove();
                // Add to delete exceptions
                dlist.add(changeException);
                // ... and re-sort delete exceptions
                Collections.sort(dlist);
                // Remember change exception's ID
                ids[c++] = app.getObjectID();
                applyNewExceptions = true;
            }
        }
        if (!applyNewExceptions) {
            /*
             * Return unchanged
             */
            return new int[0];
        }
        /*
         * Alter master's change/delete exceptions appropriate for session user
         */
        master.setChangeExceptions(clist.isEmpty() ? null : clist.toArray(new Date[clist.size()]));
        master.setDeleteExceptions(dlist.isEmpty() ? null : dlist.toArray(new Date[dlist.size()]));
        final int[] retval = new int[c];
        System.arraycopy(ids, 0, retval, 0, c);
        return retval;
    }

    private static boolean isVisibleAsParticipantOrOwner(final Appointment appointment, final int userId) {
        if (appointment.getCreatedBy() == userId) {
            // Owner/moderator may see appointment
            return true;
        }
        final UserParticipant[] users = appointment.getUsers();
        if(users == null) {
            return false;
        }
        for (final UserParticipant user : users) {
            if (user.getIdentifier() == userId) {
                return true;
            }
        }
        return false;
    }

}
