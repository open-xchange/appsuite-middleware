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

package com.openexchange.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.output.XMLOutputter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.login.Interface;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.webdav.tasks.QueuedTask;
import com.openexchange.webdav.xml.AppointmentParser;
import com.openexchange.webdav.xml.AppointmentWriter;
import com.openexchange.webdav.xml.DataParser;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * calendar - The WebDAV/XML servlet for calendar module.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class calendar extends XmlServlet<AppointmentSQLInterface> {

    private static final long serialVersionUID = 5779820324953825111L;

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(calendar.class);

    /**
     * Initializes a new {@link calendar}.
     */
    public calendar() {
        super();
    }

    @Override
    protected Interface getInterface() {
        return Interface.WEBDAV_XML;
    }

    @Override
    protected boolean isServletDisabled() {
        return true;
    }

    @Override
    protected void parsePropChilds(final HttpServletRequest req, final HttpServletResponse resp,
            final XmlPullParser parser, final PendingInvocations<AppointmentSQLInterface> pendingInvocations)
            throws XmlPullParserException, IOException, OXException {
        final Session session = getSession(req);

        if (isTag(parser, "prop", "DAV:")) {
            /*
             * Adjust parser
             */
            parser.nextTag();

            final Context ctx = ContextStorage.getInstance().getContext(session.getContextId());

            final CalendarDataObject appointmentobject = new CalendarDataObject();

            final AppointmentParser ap = new AppointmentParser(session);
            ap.parse(parser, appointmentobject);

            final int method = ap.getMethod();

            appointmentobject.setContext(ctx);

            final Date lastModified = appointmentobject.getLastModified();
            appointmentobject.removeLastModified();

            final int inFolder = ap.getFolder();

            /*
             * Prepare appointment for being queued
             */
            switch (method) {
            case DataParser.SAVE:
                if (appointmentobject.containsObjectID()) {
                    sanitize(appointmentobject);

                    pendingInvocations.add(new QueuedAppointment(appointmentobject, ap.getClientID(),
                            DataParser.SAVE, lastModified, inFolder));
                } else {
                    if (!appointmentobject.getAlarmFlag()) {
                        appointmentobject.removeAlarm();
                    }

                    appointmentobject.setParentFolderID(inFolder);

                    pendingInvocations.add(new QueuedAppointment(appointmentobject, ap.getClientID(),
                            DataParser.SAVE, lastModified, inFolder));
                }
                break;
            case DataParser.DELETE:
                LOG.debug("delete appointment: {} in folder: {}", appointmentobject.getObjectID(), inFolder);

                pendingInvocations.add(new QueuedAppointment(appointmentobject, ap.getClientID(),
                        DataParser.DELETE, lastModified, inFolder));
                break;
            case DataParser.CONFIRM:
                pendingInvocations.add(new QueuedAppointment(appointmentobject, ap.getClientID(),
                        DataParser.CONFIRM, lastModified, inFolder));
                break;
            default:
                LOG.debug("invalid method: {}", method);
            }
        } else {
            parser.next();
        }
    }

    // Sets default values as needed
    private void sanitize(final Appointment appointmentobject) {
        if (!appointmentobject.getAlarmFlag()) {
            appointmentobject.setAlarm(-1);
        }
        // For updates to series when switching from a limited series with until and occurrences to an unlimited series, we have to set "until" to null.
        if (appointmentobject.containsRecurrenceType() && appointmentobject.getRecurrenceType() != CalendarObject.NO_RECURRENCE && ! isLimitedSeries(appointmentobject)) {
            appointmentobject.setUntil(null);
        }
        // For removing the sequence information we need to tell the server that the appointment no longer has a recurrence type.
        if (!appointmentobject.containsRecurrenceType()) {
            appointmentobject.setRecurrenceType(CalendarObject.NO_RECURRENCE);
        }
    }

    private boolean isLimitedSeries(final Appointment appointmentobject) {
        return appointmentobject.containsOccurrence() || appointmentobject.containsUntil();
    }

    @Override
    protected void performActions(final OutputStream os, final Session session,
            final PendingInvocations<AppointmentSQLInterface> pendingInvocations) throws IOException {
        final AppointmentSQLInterface appointmentsSQL = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
        while (!pendingInvocations.isEmpty()) {
            final QueuedAppointment qapp = (QueuedAppointment) pendingInvocations.poll();
            if (null != qapp) {
                qapp.setLastModifiedCache(pendingInvocations.getLastModifiedCache());
                qapp.actionPerformed(appointmentsSQL, os, session.getUserId());
            }
        }
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int objectId, final int folderId,
            final OutputStream os) throws Exception {
        final User userObj = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
        final AppointmentWriter appointmentwriter = new AppointmentWriter(userObj, ctx, sessionObj);
        appointmentwriter.startWriter(objectId, folderId, os);
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int folderId,
            final boolean bModified, final boolean bDelete, final Date lastsync, final OutputStream os)
            throws Exception {
        startWriter(sessionObj, ctx, folderId, bModified, bDelete, false, lastsync, os);
    }

    @Override
    protected void startWriter(final Session sessionObj, final Context ctx, final int folderId,
            final boolean bModified, final boolean bDelete, final boolean bList, final Date lastsync,
            final OutputStream os) throws Exception {
        final User userObj = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
        final AppointmentWriter appointmentwriter = new AppointmentWriter(userObj, ctx, sessionObj);
        appointmentwriter.startWriter(bModified, bDelete, bList, folderId, lastsync, os);
    }

    @Override
    protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
        final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(
                sessionObj.getUserId(), ctx);
        return (uc.hasWebDAVXML() && uc.hasCalendar());
    }

    private final class QueuedAppointment implements QueuedAction<AppointmentSQLInterface> {

        private final CalendarDataObject appointmentobject;

        private final String clientId;

        private final int action;

        private final Date lastModified;

        private final int inFolder;

        private LastModifiedCache lastModifiedCache;

        /**
         * Initializes a new {@link QueuedTask}
         *
         * @param appointmentobject The appointment object
         * @param clientId The client ID
         * @param confirm The confirm status
         * @param action The desired action
         * @param lastModified The last-modified date
         * @param inFolder The appointment's folder
         */
        public QueuedAppointment(final CalendarDataObject appointmentobject, final String clientId,
                final int action, final Date lastModified, final int inFolder) {
            super();
            this.appointmentobject = appointmentobject;
            this.clientId = clientId;
            this.action = action;
            this.lastModified = lastModified;
            this.inFolder = inFolder;
            this.lastModifiedCache = new LastModifiedCache();
        }

        @Override
        public void actionPerformed(final AppointmentSQLInterface appointmentsSQL, final OutputStream os, final int user)
                throws IOException {

            final XMLOutputter xo = new XMLOutputter();

            try {
                boolean hasConflicts = false;
                Appointment[] conflicts = null;
                switch (action) {
                case DataParser.SAVE:
                    if (appointmentobject.containsObjectID()) {
                        if (lastModified == null) {
                            throw WebdavExceptionCode.MISSING_FIELD.create(DataFields.LAST_MODIFIED);
                        }

                        final Date currentLastModified = lastModifiedCache.getLastModified(appointmentobject.getObjectID(), lastModified);
                        lastModifiedCache.update(appointmentobject.getObjectID(), appointmentobject.getRecurrenceID(), lastModified);
                        conflicts = appointmentsSQL.updateAppointmentObject(appointmentobject, inFolder, currentLastModified);
                        hasConflicts = (conflicts != null);
                        if (!hasConflicts) {
                            lastModifiedCache.update(appointmentobject.getObjectID(), appointmentobject.getRecurrenceID(), appointmentobject.getLastModified());
                        }
                    } else {
                        conflicts = appointmentsSQL.insertAppointmentObject(appointmentobject);
                        hasConflicts = (conflicts != null);
                        if (!hasConflicts) {
                            lastModifiedCache.update(appointmentobject.getObjectID(), appointmentobject.getRecurrenceID(), appointmentobject.getLastModified());
                        }
                    }
                    break;
                case DataParser.DELETE:
                    LOG.debug("delete appointment: {} in folder: {}", appointmentobject.getObjectID(), inFolder);

                    if (lastModified == null) {
                        throw WebdavExceptionCode.MISSING_FIELD.create(DataFields.LAST_MODIFIED);
                    }

                    appointmentsSQL.deleteAppointmentObject(appointmentobject, inFolder, lastModified);
                    break;
                case DataParser.CONFIRM:
                    appointmentsSQL.setUserConfirmation(appointmentobject.getObjectID(), appointmentobject.getParentFolderID(), user, appointmentobject.getConfirm(),
                            appointmentobject.getConfirmMessage());
                    break;
                default:
                    throw WebdavExceptionCode.INVALID_ACTION.create(Integer.valueOf(action));
                }

                if (hasConflicts) {
                    writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, APPOINTMENT_CONFLICT_EXCEPTION,
                            clientId, os, xo, conflicts);
                } else {
                    writeResponse(appointmentobject, HttpServletResponse.SC_OK, OK, clientId, os, xo);
                }
            }
//            catch (final OXCalendarException exc) {
//                if (exc.getCategory() == Category.USER_INPUT) {
//                    LOG.debug(_parsePropChilds, exc);
//                    writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
//                            USER_INPUT_EXCEPTION), clientId, os, xo);
//                } else if (exc.getCategory() == Category.TRUNCATED) {
//                    LOG.debug(_parsePropChilds, exc);
//                    writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
//                            USER_INPUT_EXCEPTION), clientId, os, xo);
//                } else {
//                    LOG.error(_parsePropChilds, exc);
//                    writeResponse(appointmentobject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(exc,
//                            SERVER_ERROR_EXCEPTION)
//                            + exc.toString(), clientId, os, xo);
//                }
//            }
            catch (final OXException exc) {
                if (exc.isMandatory()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
                            MANDATORY_FIELD_EXCEPTION), clientId, os, xo);
                } else if (exc.isNoPermission()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(appointmentobject, HttpServletResponse.SC_FORBIDDEN, getErrorMessage(exc,
                            PERMISSION_EXCEPTION), clientId, os, xo);
                } else if (exc.isConflict()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, MODIFICATION_EXCEPTION, clientId, os, xo);
                } else if (exc.isNotFound()) {
                    LOG.debug(_parsePropChilds, exc);
                    writeResponse(appointmentobject, HttpServletResponse.SC_NOT_FOUND, OBJECT_NOT_FOUND_EXCEPTION,
                            clientId, os, xo);
                } else {
                    if (exc.getCategory() == Category.CATEGORY_TRUNCATED) {
                        LOG.debug(_parsePropChilds, exc);
                        writeResponse(appointmentobject, HttpServletResponse.SC_CONFLICT, getErrorMessage(exc,
                                USER_INPUT_EXCEPTION), clientId, os, xo);
                    } else {
                        LOG.error(_parsePropChilds, exc);
                        writeResponse(appointmentobject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(exc,
                                SERVER_ERROR_EXCEPTION)
                                + exc.toString(), clientId, os, xo);
                    }
                }
            } catch (final Exception exc) {
                LOG.error(_parsePropChilds, exc);
                writeResponse(appointmentobject, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(
                        SERVER_ERROR_EXCEPTION, "undefinied error")
                        + exc.toString(), clientId, os, xo);
            }
        }

        public void setLastModifiedCache(final LastModifiedCache lastModifiedCache) {
            this.lastModifiedCache = lastModifiedCache;
        }
    }

    @Override
    protected void decrementRequests() {
        MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.OUTLOOK);
    }

    @Override
    protected void incrementRequests() {
        MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.OUTLOOK);
    }
}
