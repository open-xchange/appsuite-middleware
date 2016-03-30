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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.ServiceException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalItem;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.login.Interface;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;

/**
 * ical
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public final class ical extends PermissionServlet {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 8198514314235297665L;

    /**
     * Logger.
     */
    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ical.class);

    private final static int[] APPOINTMENT_FIELDS = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.MODIFIED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED,
        FolderChildObject.FOLDER_ID, CommonObject.CATEGORIES, CommonObject.PRIVATE_FLAG, CommonObject.COLOR_LABEL, CalendarObject.TITLE,
        CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_ID,
        CalendarObject.RECURRENCE_TYPE, CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.LOCATION,
        Appointment.FULL_TIME, Appointment.SHOWN_AS, Appointment.TIMEZONE };

    private final static int[] TASK_FIELDS = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, CalendarObject.START_DATE,
        CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE, CalendarObject.RECURRENCE_COUNT,
        CalendarObject.UNTIL, CalendarObject.PARTICIPANTS, Task.ACTUAL_COSTS, Task.ACTUAL_DURATION, CalendarObject.ALARM,
        Task.BILLING_INFORMATION, CommonObject.CATEGORIES, Task.COMPANIES, Task.CURRENCY, Task.DATE_COMPLETED, Task.IN_PROGRESS,
        Task.PERCENT_COMPLETED, Task.PRIORITY, Task.STATUS, Task.TARGET_COSTS, Task.TARGET_DURATION, Task.TRIP_METER,
        CommonObject.COLOR_LABEL };

    private static final String CALENDARFOLDER = "calendarfolder";

    private static final String TASKFOLDER = "taskfolder";

    // private static final String ENABLEDELETE = "enabledelete";

    private static final String SQL_PRINCIPAL_SELECT = "SELECT object_id, calendarfolder, taskfolder FROM ical_principal WHERE cid = ? AND principal = ?";

    private static final String SQL_PRINCIPAL_INSERT = "INSERT INTO ical_principal (object_id,cid,principal,calendarfolder,taskfolder) VALUES (?,?,?,?,?)";

    private static final String SQL_PRINCIPAL_UPDATE = "UPDATE ical_principal SET calendarfolder=?,taskfolder=? WHERE cid=? AND object_id=?";

    // private static final String SQL_ENTRIES_LOAD =
    // "SELECT object_id, client_id, target_object_id, module FROM ical_ids WHERE cid = ? AND principal_id = ?";
    // private static final String SQL_ENTRY_INSERT =
    // "INSERT INTO ical_ids (object_id, cid, principal_id, client_id, target_object_id, module) VALUES (?, ?, ?, ? ,?, ?)";
    // private static final String SQL_ENTRY_DELETE =
    // "DELETE FROM ical_ids WHERE cid=? AND principal_id=? AND target_object_id=? AND module=?";

    @Override
    protected Interface getInterface() {
        return Interface.WEBDAV_ICAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        LOG.debug("GET");
        final Session sessionObj = getSession(req);
        try {
            final Context context = ContextStorage.getInstance().getContext(sessionObj.getContextId());
            final User user = UserStorage.getInstance().getUser(sessionObj.getUserId(), context);

            int calendarfolderId = getCalendarFolderID(req);
            int taskfolderId = getTaskFolderID(req);
            if (calendarfolderId == 0 && taskfolderId == 0) {
                final OXFolderAccess oAccess = new OXFolderAccess(context);
                calendarfolderId = oAccess.getDefaultFolderID(user.getId(), FolderObject.CALENDAR);
                taskfolderId = oAccess.getDefaultFolderID(user.getId(), FolderObject.TASK);
            }

            final String user_agent = getUserAgent(req);
            final String principalS = user_agent + '_' + sessionObj.getUserId();
            Principal principal = loadPrincipal(context, principalS);

            // final Mapping mapping;
            // final Map<String, Integer> entriesApp = new HashMap<String, Integer>();
            // final Map<String, Integer> entriesTask = new HashMap<String, Integer>();
            // if (null != principal) {
            // mapping = loadDBEntriesNew(context, principal);
            // } else {
            // mapping = new Mapping();
            // }

            final ICalEmitter emitter = ServerServiceRegistry.getInstance().getService(ICalEmitter.class);
            if (null == emitter) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ICalEmitter.class.getName());
            }
            final ICalSession iSession = emitter.createSession();
            final List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
            final List<ConversionError> errors = new LinkedList<ConversionError>();

            final AppointmentSQLInterface appointmentSql = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(sessionObj);
            final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            SearchIterator<Appointment> iter = null;
            try {
                final TIntObjectMap<SeriesUIDPatcher> patchers = new TIntObjectHashMap<SeriesUIDPatcher>();
                iter = appointmentSql.getModifiedAppointmentsInFolder(calendarfolderId, APPOINTMENT_FIELDS, new Date(0));
                while (iter.hasNext()) {
                    final Appointment appointment = iter.next();
                    if (CalendarObject.NO_RECURRENCE != appointment.getRecurrenceType()) {
                        if (!appointment.containsTimezone()) {
                            appointment.setTimezone(user.getTimeZone());
                        }
                        recColl.replaceDatesWithFirstOccurence(appointment);
                    }
                    final ICalItem item = emitter.writeAppointment(iSession, appointment, context, errors, warnings);
                    // // First check if the appointment has been synchronized before.
                    // final int appId = appointment.getObjectID();
                    // String clientId = mapping.getClientAppId(appId);
                    // if (null == clientId) {
                    // clientId = item.getUID();
                    // entriesApp.put(clientId, Integer.valueOf(appId));
                    // } else {
                    // item.setUID(clientId);
                    // }
                    // Patch UID if change exceptions to be the same ID as of the series.
                    if (appointment.isMaster() || appointment.isException()) {
                        final int recurrenceId = appointment.getRecurrenceID();
                        SeriesUIDPatcher patcher = patchers.get(recurrenceId);
                        if (null == patcher) {
                            patcher = new SeriesUIDPatcher();
                            patchers.put(recurrenceId, patcher);
                        }
                        if (appointment.isMaster()) {
                            patcher.setSeries(item);
                        } else if (appointment.isException()) {
                            patcher.addChangeException(item);
                        }
                    }
                }
                patchers.forEachValue(PATCH_PROCEDURE);
            } catch (final OXException e) {
                LOG.error("", e);
            } finally {
                SearchIterators.close(iter);
            }
            final TasksSQLInterface taskInterface = new TasksSQLImpl(sessionObj);
            SearchIterator<Task> itTask = null;
            try {
                itTask = taskInterface.getModifiedTasksInFolder(taskfolderId, TASK_FIELDS, new Date(0));
                while (itTask.hasNext()) {
                    final Task task = itTask.next();
                    emitter.writeTask(iSession, task, context, errors, warnings);
                    // final ICalItem item = emitter.writeTask(iSession, task,
                    // context, errors, warnings);
                    // final int taskId = task.getObjectID();
                    // String clientId = mapping.getClientTaskId(taskId);
                    // if (null == clientId) {
                    // clientId = item.getUID();
                    // entriesTask.put(clientId, Integer.valueOf(taskId));
                    // } else {
                    // item.setUID(clientId);
                    // }
                }
            } catch (final OXException e) {
                LOG.error("", e);
            } finally {
                SearchIterators.close(itTask);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/calendar");
            try {
                emitter.writeSession(iSession, resp.getOutputStream());
            } catch (final ConversionError e) {
                LOG.error("", e);
            }

            if (null == principal) {
                principal = new Principal(0, principalS, calendarfolderId, taskfolderId);
                insertPrincipal(context, principal);
            } else {
                if (principal.getCalendarFolder() != calendarfolderId || principal.getTaskFolder() != taskfolderId) {
                    principal.setCalendarFolder(calendarfolderId);
                    principal.setTaskFolder(taskfolderId);
                    updatePrincipal(context, principal);
                }
            }

            // addEntries(context, principal, entriesApp, entriesTask);
            // deleteEntries(context, principal, mapping, entriesApp, entriesTask);
        } catch (final OXException e) {
            LOG.error("", e);
            doError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (final ServiceException e) {
            LOG.error("", e);
            doError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static final TObjectProcedure<SeriesUIDPatcher> PATCH_PROCEDURE = new TObjectProcedure<SeriesUIDPatcher>() {

        @Override
        public boolean execute(final SeriesUIDPatcher patcher) {
            patcher.patchUIDs();
            return true;
        }
    };

    private static final class SeriesUIDPatcher {

        private ICalItem series;

        private final List<ICalItem> changeExceptions = new ArrayList<ICalItem>();

        public SeriesUIDPatcher() {
            super();
        }

        public void setSeries(final ICalItem series) {
            this.series = series;
        }

        public void addChangeException(final ICalItem changeException) {
            changeExceptions.add(changeException);
        }

        public void patchUIDs() {
            if (null == series) {
                return;
            }
            String uid = series.getUID();
            if (null == uid) {
                uid = UUID.randomUUID().toString();
                series.setUID(uid);
            }
            for (final ICalItem changeException : changeExceptions) {
                changeException.setUID(uid);
            }
        }
    }

    // @Override
    // public void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("PUT");
    // }
    //
    // int calendarfolder_id = 0;
    // int taskfolder_id = 0;
    //
    // String user_agent = null;
    // String principal = null;
    //
    // String content_type = null;
    //
    // final Session sessionObj = getSession(req);
    //
    // try {
    // final Context context = ContextStorage.getInstance().getContext(sessionObj.getContextId());
    //
    // user_agent = getUserAgent(req);
    // content_type = req.getContentType();
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("read ical content_type: {}", content_type);
    // }
    //
    // calendarfolder_id = getCalendarFolderID(req);
    // taskfolder_id = getTaskFolderID(req);
    //
    // if (calendarfolder_id == 0 && taskfolder_id == 0) {
    // final OXFolderAccess oAccess = new OXFolderAccess(context);
    // calendarfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.CALENDAR).getObjectID();
    // taskfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.TASK).getObjectID();
    // /*calendarfolder_id = OXFolderTools.getCalendarDefaultFolder(sessionObj.getUserObject().getId(), context);
    // taskfolder_id = OXFolderTools.getTaskDefaultFolder(sessionObj.getUserObject().getId(), context);*/
    // }
    //
    // if (content_type == null) {
    // content_type = "text/calendar";
    // }
    //
    // if (user_agent == null) {
    // throw "missing header field: user-agent";
    // }
    //
    // principal = user_agent + '_' + sessionObj.getUserId();
    //
    // calendarfolder_id = getCalendarFolderID(req);
    // taskfolder_id = getTaskFolderID(req);
    //
    // if (calendarfolder_id == 0 && taskfolder_id == 0) {
    // final OXFolderAccess oAccess = new OXFolderAccess(context);
    // calendarfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.CALENDAR).getObjectID();
    // taskfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.TASK).getObjectID();
    // /*calendarfolder_id = OXFolderTools.getCalendarDefaultFolder(sessionObj.getUserObject().getId(), context);
    // taskfolder_id = OXFolderTools.getTaskDefaultFolder(sessionObj.getUserObject().getId(), context);*/
    // }
    //
    // final boolean enabledelete = getEnableDelete(req);
    //
    // Map<String, String> entries_db = new HashMap<String, String>();
    // Map<String, String> entries_module = new HashMap<String, String>();
    // final HashSet<String> entries = new HashSet<String>();
    //
    // Connection readCon = null;
    // PreparedStatement ps = null;
    // ResultSet rs = null;
    //
    // int principal_id = 0;
    //
    // try {
    // readCon = DBPool.pickup(context);
    // ps = readCon.prepareStatement(SQL_PRINCIPAL_SELECT);
    // ps.setInt(1, context.getContextId());
    // ps.setString(2, principal);
    // rs = ps.executeQuery();
    //
    // int db_calendarfolder_id = 0;
    // int db_taskfolder_id = 0;
    //
    // final boolean exists = rs.next();
    //
    // if (exists) {
    // principal_id = rs.getInt(1);
    // db_calendarfolder_id = rs.getInt(2);
    // db_taskfolder_id = rs.getInt(3);
    //
    // if (!(db_calendarfolder_id == calendarfolder_id && db_taskfolder_id == taskfolder_id)) {
    // throw "no principal found for the given folders: " + principal;
    // }
    //
    // final Map<String, String>[] h = loadDBEntries(context, principal_id);
    // entries_db = h[0];
    // entries_module = h[1];
    // } else {
    // throw "no principal found: " + principal;
    // }
    // } finally {
    // if (rs != null) {
    // rs.close();
    // }
    //
    // if (ps != null) {
    // ps.close();
    // }
    //
    // if (readCon != null) {
    // DBPool.closeReaderSilent(context, readCon);
    // }
    // }
    //
    // final VersitDefinition def = Versit.getDefinition(content_type);
    // final VersitDefinition.Reader r = def.getReader(req.getInputStream(), "UTF-8");
    // final OXContainerConverter oxc = new OXContainerConverter(sessionObj);
    //
    // final AppointmentSQLInterface appointmentInterface = new CalendarSql(sessionObj);
    // final TasksSQLInterface taskInterface = new TasksSQLInterfaceImpl(sessionObj);
    //
    // final Date timestamp = new Date();
    //
    // try {
    // final VersitObject root_vo = def.parseBegin(r);
    // VersitObject vo = def.parseChild(r, root_vo);
    // while (vo != null) {
    // int object_id = 0;
    //
    // try {
    // final Property property = vo.getProperty("UID");
    //
    // String client_id = null;
    //
    //
    // if (property != null) {
    // client_id = property.getValue().toString();
    // }
    //
    // if ("VEVENT".equals(vo.name)) {
    // final CalendarDataObject appointmentObj = oxc.convertAppointment(vo);
    // appointmentObj.setContext(context);
    //
    // if (client_id != null && entries_db.containsKey(client_id)) {
    // try {
    // object_id = Integer.parseInt(entries_db.get(client_id).toString());
    // } catch (final NumberFormatException exc) {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("object id is not an int");
    // }
    // }
    //
    // if (object_id > 0) {
    // appointmentObj.setObjectID(object_id);
    // }
    //
    // appointmentObj.setParentFolderID(calendarfolder_id);
    //
    // if (appointmentObj.containsObjectID()) {
    // appointmentInterface.updateAppointmentObject(appointmentObj, calendarfolder_id, timestamp);
    // } else {
    // appointmentInterface.insertAppointmentObject(appointmentObj);
    // }
    //
    // entries.add(client_id);
    // } else {
    // appointmentObj.setParentFolderID(calendarfolder_id);
    //
    // if (appointmentObj.containsObjectID()) {
    // appointmentInterface.updateAppointmentObject(appointmentObj, calendarfolder_id, timestamp);
    // } else {
    // appointmentInterface.insertAppointmentObject(appointmentObj);
    // }
    //
    // if (client_id != null) {
    // entries.add(client_id);
    // object_id = appointmentObj.getObjectID();
    // addEntry(context, principal_id, object_id, client_id, Types.APPOINTMENT);
    //
    // if (!entries_module.containsKey(String.valueOf(object_id))) {
    // entries_module.put(String.valueOf(object_id), String.valueOf(Types.APPOINTMENT));
    // }
    // }
    // }
    //
    // LOG.debug("STATUS: OK");
    // } else if ("VTODO".equals(vo.name)) {
    // final Task taskObj = oxc.convertTask(vo);
    //
    // if (client_id != null && entries_db.containsKey(client_id)) {
    // try {
    // object_id = Integer.parseInt(entries_db.get(client_id).toString());
    // } catch (final NumberFormatException exc) {
    // LOG.debug("object id is not an int");
    // }
    //
    // if (object_id > 0) {
    // taskObj.setObjectID(object_id);
    // }
    //
    // taskObj.setParentFolderID(taskfolder_id);
    //
    // if (taskObj.containsObjectID()) {
    // taskInterface.updateTaskObject(taskObj, taskfolder_id, timestamp);
    // } else {
    // taskInterface.insertTaskObject(taskObj);
    // }
    //
    // entries.add(client_id);
    // } else {
    // taskObj.setParentFolderID(taskfolder_id);
    //
    // if (taskObj.containsObjectID()) {
    // taskInterface.updateTaskObject(taskObj, taskfolder_id, timestamp);
    // } else {
    // taskInterface.insertTaskObject(taskObj);
    // }
    //
    // if (client_id != null) {
    // entries.add(client_id);
    // object_id = taskObj.getObjectID();
    // addEntry(context, principal_id, object_id, client_id, Types.TASK);
    //
    // if (!entries_module.containsKey(String.valueOf(object_id))) {
    // entries_module.put(String.valueOf(object_id), String.valueOf(Types.TASK));
    // }
    // }
    // }
    //
    // LOG.debug("STATUS: OK");
    // } else {
    // LOG.warn("invalid versit object: {}", vo.name);
    // }
    // } catch (final OXObjectNotFoundException exc) {
    // LOG.debug("object was already delete", exc);
    // }
    //
    // vo = def.parseChild(r, root_vo);
    // }
    // } finally {
    // oxc.close();
    // }
    //
    // final CalendarDataObject appointmentObject = new CalendarDataObject();
    //
    // final Iterator<String> it = entries_db.keySet().iterator();
    // while (it.hasNext()) {
    // final String tmp = it.next();
    // if (!entries.contains(tmp)) {
    // final int object_id = Integer.parseInt(entries_db.get(tmp).toString());
    // final int i_module = Integer.parseInt(entries_module.get(String.valueOf(object_id)));
    //
    // deleteEntry(context, principal_id, object_id, i_module);
    //
    // if (enabledelete) {
    // try {
    // if (i_module == Types.APPOINTMENT) {
    // appointmentObject.reset();
    // appointmentObject.setObjectID(object_id);
    // appointmentObject.setParentFolderID(calendarfolder_id);
    // appointmentInterface.deleteAppointmentObject(appointmentObject, calendarfolder_id, timestamp);
    // } else if (i_module == Types.TASK) {
    // taskInterface.deleteTaskObject(object_id, taskfolder_id, timestamp);
    // } else {
    // throw "unknown module: " + i_module;
    // }
    // } catch (final OXObjectNotFoundException exc) {
    // LOG.debug("object was already delete", exc);
    // }
    // }
    // }
    // }
    //
    // resp.setStatus(HttpServletResponse.SC_OK);
    // } catch (final OXMandatoryFieldException exc) {
    // LOG.debug(_doPut, exc);
    // doError(resp, HttpServletResponse.SC_CONFLICT, exc.getMessage());
    // } catch (final OXPermissionException exc) {
    // LOG.debug(_doPut, exc);
    // doError(resp, HttpServletResponse.SC_FORBIDDEN, exc.getMessage());
    // } catch (final OXConflictException exc) {
    // LOG.debug(_doPut, exc);
    // doError(resp, HttpServletResponse.SC_CONFLICT, exc.getMessage());
    // } catch (final Exception exc) {
    // LOG.error(_doPut, exc);
    // doError(resp);
    // }
    // }

    // private void doError(final HttpServletResponse resp) throws IOException {
    // doError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
    // }

    private void doError(final HttpServletResponse resp, final int code, final String msg) throws IOException {
        resp.setStatus(code);
        resp.setContentType("text/html; ; charset=UTF-8");
        final Writer writer = resp.getWriter();
        writer.write("<html><body>" + msg + "</body></html>");
    }

    private String getUserAgent(final HttpServletRequest req) throws OXException {
        final Enumeration<?> e = req.getHeaderNames();
        final String userAgent = "user-agent";
        while (e.hasMoreElements()) {
            if (userAgent.equals(e.nextElement().toString().toLowerCase())) {
                return req.getHeader(userAgent);
            }
        }
        throw WebdavExceptionCode.MISSING_HEADER_FIELD.create(userAgent);
    }

    private int getCalendarFolderID(final HttpServletRequest req) throws OXException {
        if (req.getParameter(CALENDARFOLDER) != null) {
            try {
                return Integer.parseInt(req.getParameter(CALENDARFOLDER));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.NOT_A_NUMBER.create(exc, CALENDARFOLDER);
            }
        }
        return 0;
    }

    private int getTaskFolderID(final HttpServletRequest req) throws OXException {
        if (req.getParameter(TASKFOLDER) != null) {
            try {
                return Integer.parseInt(req.getParameter(TASKFOLDER));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.NOT_A_NUMBER.create(exc, TASKFOLDER);
            }
        }
        return 0;
    }

    // private boolean getEnableDelete(final HttpServletRequest req) {
    // return "yes".equalsIgnoreCase(req.getParameter(ENABLEDELETE));
    // }

    private static final class Principal {

        private int id;

        private final String userAgent;

        private int calendarFolder;

        private int taskFolder;

        public Principal(final int id, final String userAgent, final int calendarFolder, final int taskFolder) {
            super();
            this.id = id;
            this.userAgent = userAgent;
            this.calendarFolder = calendarFolder;
            this.taskFolder = taskFolder;
        }

        int getId() {
            return id;
        }

        void setId(final int id) {
            this.id = id;
        }

        String getUserAgent() {
            return userAgent;
        }

        int getCalendarFolder() {
            return calendarFolder;
        }

        void setCalendarFolder(final int calendarFolder) {
            this.calendarFolder = calendarFolder;
        }

        int getTaskFolder() {
            return taskFolder;
        }

        void setTaskFolder(final int taskFolder) {
            this.taskFolder = taskFolder;
        }
    }

    private Principal loadPrincipal(final Context ctx, final String userAgent) throws OXException {
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Principal retval;
        try {
            ps = con.prepareStatement(SQL_PRINCIPAL_SELECT);
            ps.setLong(1, ctx.getContextId());
            ps.setString(2, userAgent);
            rs = ps.executeQuery();
            if (rs.next()) {
                retval = new Principal(rs.getInt(1), userAgent, rs.getInt(2), rs.getInt(3));
            } else {
                retval = null;
            }
        } catch (final SQLException e) {
            throw WebdavExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, ps);
            DBPool.closeReaderSilent(ctx, con);
        }
        return retval;
    }

    private void insertPrincipal(final Context ctx, final Principal principal) throws OXException {
        final Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement ps = null;
        try {
            con.setAutoCommit(false);
            principal.setId(IDGenerator.getId(ctx, Types.ICAL, con));
            ps = con.prepareStatement(SQL_PRINCIPAL_INSERT);
            ps.setInt(1, principal.getId());
            ps.setLong(2, ctx.getContextId());
            ps.setString(3, principal.getUserAgent());
            ps.setInt(4, principal.getCalendarFolder());
            ps.setInt(5, principal.getTaskFolder());
            ps.executeUpdate();
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw WebdavExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(null, ps);
            DBUtils.autocommit(con);
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    private void updatePrincipal(final Context ctx, final Principal principal) throws OXException {
        final Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(SQL_PRINCIPAL_UPDATE);
            ps.setInt(1, principal.getCalendarFolder());
            ps.setInt(2, principal.getTaskFolder());
            ps.setInt(3, ctx.getContextId());
            ps.setInt(4, principal.getId());
            ps.executeUpdate();
        } catch (final SQLException e) {
            throw WebdavExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(null, ps);
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    /*
     * private void addEntry(final Context context, final int principal_id, final int object_target_id, final String client_id, final int
     * module) throws Exception { Connection writeCon = null; PreparedStatement ps = null; try { writeCon = DBPool.pickupWriteable(context);
     * writeCon.setAutoCommit(false); final int objectId = IDGenerator.getId(context, Types.ICAL, writeCon); ps =
     * writeCon.prepareStatement(SQL_ENTRY_INSERT); ps.setInt(1, objectId); ps.setLong(2, context.getContextId()); ps.setInt(3,
     * principal_id); ps.setString(4, client_id); ps.setInt(5, object_target_id); ps.setInt(6, module); ps.executeUpdate();
     * writeCon.commit(); } catch (final SQLException exc) { if (writeCon != null) { writeCon.rollback(); } throw exc; } finally { if (ps !=
     * null) { ps.close(); } if (writeCon != null) { if (!writeCon.getAutoCommit()) { writeCon.setAutoCommit(true); }
     * DBPool.closeWriterSilent(context, writeCon); } } }
     */

    /*
     * private void addEntries(final Context ctx, final Principal principal, final Map<String, Integer> entriesApp, final Map<String,
     * Integer> entriesTask) throws OXException { final Connection con; try { con = DBPool.pickupWriteable(ctx); } catch (final
     * DBPoolingException e) { throw new OXException(e); } PreparedStatement ps = null; try { con.setAutoCommit(false); ps =
     * con.prepareStatement(SQL_ENTRY_INSERT); for (final Map.Entry<String, Integer> entry : entriesApp.entrySet()) { final int objectId =
     * IDGenerator.getId(ctx, Types.ICAL, con); ps.setInt(1, objectId); ps.setLong(2, ctx.getContextId()); ps.setInt(3, principal.getId());
     * ps.setString(4, entry.getKey()); ps.setInt(5, entry.getValue().intValue()); ps.setInt(6, Types.APPOINTMENT); ps.addBatch(); } for
     * (final Map.Entry<String, Integer> entry : entriesTask.entrySet()) { final int objectId = IDGenerator.getId(ctx, Types.ICAL, con);
     * ps.setInt(1, objectId); ps.setLong(2, ctx.getContextId()); ps.setInt(3, principal.getId()); ps.setString(4, entry.getKey());
     * ps.setInt(5, entry.getValue().intValue()); ps.setInt(6, Types.TASK); ps.addBatch(); } ps.executeBatch(); con.commit(); } catch (final
     * SQLException e) { DBUtils.rollback(con); throw new OXException(EnumComponent.ICAL, Category.CODE_ERROR, 9999, e.getMessage(), e); }
     * finally { DBUtils.closeSQLStuff(null, ps); DBUtils.autocommit(con); DBPool.closeWriterSilent(ctx, con); } }
     */

    /*
     * private void deleteEntries(final Context ctx, final Principal principal, final Mapping mapping, final Map<String, Integer>
     * entriesApp, final Map<String, Integer> entriesTask) throws OXException { final Connection con; try { con =
     * DBPool.pickupWriteable(ctx); } catch (final DBPoolingException e) { throw new OXException(e); } PreparedStatement ps = null; try {
     * con.setAutoCommit(false); ps = con.prepareStatement(SQL_ENTRY_DELETE); for (final String clientId : mapping.client2App.keySet()) { if
     * (!entriesApp.containsKey(clientId)) { ps.setInt(1, ctx.getContextId()); ps.setInt(2, principal.getId()); ps.setInt(3,
     * mapping.client2App.get(clientId).intValue()); ps.setInt(4, Types.APPOINTMENT); ps.addBatch(); } } for (final String clientId :
     * mapping.client2Task.keySet()) { if (!entriesTask.containsKey(clientId)) { ps.setInt(1, ctx.getContextId()); ps.setInt(2,
     * principal.getId()); ps.setInt(3, mapping.client2Task.get(clientId).intValue()); ps.setInt(4, Types.TASK); ps.addBatch(); } }
     * ps.executeBatch(); con.commit(); } catch (final SQLException e) { DBUtils.rollback(con); throw new OXException(EnumComponent.ICAL,
     * Category.CODE_ERROR, 9999, e.getMessage(), e); } finally { DBUtils.closeSQLStuff(null, ps); DBUtils.autocommit(con);
     * DBPool.closeWriterSilent(ctx, con); } }
     */

    /*
     * private void deleteEntry(final Context context, final int principal_id, final int object_target_id, final int module) throws
     * Exception { Connection writeCon = null; PreparedStatement ps = null; try { writeCon = DBPool.pickupWriteable(context); ps =
     * writeCon.prepareStatement(SQL_ENTRY_DELETE); ps.setInt(1, context.getContextId()); ps.setInt(2, principal_id); ps.setInt(3,
     * object_target_id); ps.setInt(4, module); ps.executeUpdate(); } finally { if (ps != null) { ps.close(); } if (writeCon != null) {
     * DBPool.closeWriterSilent(context, writeCon); } } }
     */

    /*
     * private Map<String, String>[] loadDBEntries(final Context context, final int principal_object_id) throws Exception { final
     * HashMap<String, String> entries_db = new HashMap<String, String>(); final HashMap<String, String> entries_db_reverse = new
     * HashMap<String, String>(); final HashMap<String, String> entries_module = new HashMap<String, String>(); final Connection readCon =
     * DBPool.pickup(context); PreparedStatement ps = null; ResultSet rs = null; try { ps = readCon.prepareStatement(SQL_ENTRIES_LOAD);
     * ps.setInt(1, principal_object_id); ps.setLong(2, context.getContextId()); rs = ps.executeQuery(); String client_id = null; int
     * target_id = 0; int module = 0; while (rs.next()) { client_id = rs.getString(2); target_id = rs.getInt(3); module = rs.getInt(4);
     * entries_db.put(client_id, String.valueOf(target_id)); entries_db_reverse.put(String.valueOf(target_id), client_id);
     * entries_module.put(String.valueOf(target_id), String.valueOf(module)); } } finally { DBUtils.closeSQLStuff(rs, ps);
     * DBPool.closeReaderSilent(context, readCon); } final Map<String, String>[] h = new Map[3]; h[0] = entries_db; h[1] = entries_module;
     * h[2] = entries_db_reverse; return h; }
     */

    /*
     * private class Mapping { private final Map<String, Integer> client2App = new HashMap<String, Integer>(); private final Map<String,
     * Integer> client2Task = new HashMap<String, Integer>(); private final Map<Integer, String> app2Client = new HashMap<Integer,
     * String>(); private final Map<Integer, String> task2Client = new HashMap<Integer, String>(); private Mapping() { super(); } public
     * void addAppointment(final String clientId, final int targetId) { client2App.put(clientId, Integer.valueOf(targetId));
     * app2Client.put(Integer.valueOf(targetId), clientId); } public void addTask(final String clientId, final int targetId) {
     * client2Task.put(clientId, Integer.valueOf(targetId)); task2Client.put(Integer.valueOf(targetId), clientId); } public String
     * getClientAppId(final int appId) { return app2Client.get(Integer.valueOf(appId)); } public String getClientTaskId(final int taskId) {
     * return task2Client.get(Integer.valueOf(taskId)); } }
     */

    /*
     * private Mapping loadDBEntriesNew(final Context context, final Principal principal) throws OXException { final Connection readCon; try
     * { readCon = DBPool.pickup(context); } catch (final DBPoolingException e) { throw new OXException(e); } PreparedStatement ps = null;
     * ResultSet rs = null; final Mapping mapping = new Mapping(); try { ps = readCon.prepareStatement(SQL_ENTRIES_LOAD); ps.setInt(1,
     * principal.getId()); ps.setLong(2, context.getContextId()); rs = ps.executeQuery(); while (rs.next()) { final String client_id =
     * rs.getString(2); final int target_id = rs.getInt(3); final int module = rs.getInt(4); switch (module) { case Types.APPOINTMENT:
     * mapping.addAppointment(client_id, target_id); break; case Types.TASK: mapping.addTask(client_id, target_id); break; default:
     * LOG.warn("Unknown iCal object mapping module {}", module); } } } catch (final SQLException e) { throw new
     * OXException(EnumComponent.ICAL, Category.CODE_ERROR, 9999, e.getMessage(), e); } finally { DBUtils.closeSQLStuff(rs, ps);
     * DBPool.closeReaderSilent(context, readCon); } return mapping; }
     */

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
        final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx);
        return (uc.hasICal() && uc.hasCalendar() && uc.hasTask());
    }

    @Override
    protected void decrementRequests() {
        // TODO: MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.OUTLOOK);
    }

    @Override
    protected void incrementRequests() {
        // TODO: MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.OUTLOOK);
    }
}
