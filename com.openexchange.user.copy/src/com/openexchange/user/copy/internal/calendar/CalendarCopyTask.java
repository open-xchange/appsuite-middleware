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

package com.openexchange.user.copy.internal.calendar;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.user.copy.internal.CopyTools.getIntOrNegative;
import static com.openexchange.user.copy.internal.CopyTools.replaceIdsInQuery;
import static com.openexchange.user.copy.internal.CopyTools.setIntOrNull;
import static com.openexchange.user.copy.internal.CopyTools.setStringOrNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.sql.tools.SQLTools;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.IntegerMapping;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link CalendarCopyTask} - Copies all private appointments for the user to move.
 * Only the user itself and external participants are kept.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CalendarCopyTask implements CopyUserTaskService {

    private static final String SELECT_APP_IDS =
        "SELECT " +
            "m.object_id " +
        "FROM " +
            "prg_dates_members AS m " +
        "JOIN " +
            "prg_date_rights AS r " +
        "ON " +
            "m.object_id = r.object_id " +
        "AND " +
            "m.cid = r.cid " +
        "AND " +
            "m.member_uid = r.id " +
        "WHERE " +
            "m.member_uid = ? " +
        "AND " +
            "m.cid = ? " +
        "AND " +
            "m.pfid IN (#IDS#)";

    private static final String SELECT_APPOINTMENTS =
        "SELECT " +
            "creating_date, changing_date, fid, pflag, timestampfield01, timestampfield02, " +
            "timezone, intfield01, intfield02, intfield03, intfield04, intfield05, intfield06, " +
            "intfield07, intfield08, field01, field02, field04, field06, field07, field08, " +
            "field09, uid, organizer, sequence, organizerId, principal, principalId, filename " +
        "FROM " +
            "prg_dates " +
        "WHERE " +
            "cid = ? " +
        "AND " +
            "intfield01 IN (#IDS#)";

    private static final String SELECT_PARTICIPANTS =
        "SELECT " +
            "r.id, r.type, r.ma, r.dn, m.confirm, m.reason, m.pfid, m.reminder " +
        "FROM " +
            "prg_date_rights AS r " +
        "LEFT JOIN " +
            "prg_dates_members AS m " +
        "ON " +
            "r.cid = m.cid " +
        "AND " +
            "r.object_id = m.object_id " +
        "AND " +
            "r.id = m.member_uid " +
        "WHERE " +
            "r.cid = ? " +
        "AND " +
            "r.object_id = ? " +
        "AND " +
            "(r.id = ? OR r.type = 5);";

    private static final String INSERT_APPOINTMENT =
        "INSERT INTO " +
            "prg_dates " +
            "(creating_date, created_from, changing_date, changed_from, " +
            "fid, pflag, cid, timestampfield01, timestampfield02, " +
            "timezone, intfield01, intfield02, intfield03, intfield04, intfield05, intfield06, " +
            "intfield07, intfield08, field01, field02, field04, field06, field07, field08, " +
            "field09, uid, organizer, sequence, organizerId, principal, principalId, filename) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_MEMBER =
        "INSERT INTO " +
            "prg_dates_members " +
            "(object_id, member_uid, confirm, reason, pfid, reminder, cid) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_RIGHT =
        "INSERT INTO " +
    		"prg_date_rights " +
    		"(object_id, cid, id, type, ma, dn) " +
    	"VALUES " +
    	    "(?, ?, ?, ?, ?, ?)";

    private static final String SELECT_DATE_EXTERNAL =
        "SELECT " +
            "objectId, mailAddress, displayName, confirm, reason " +
        "FROM " +
            "dateExternal " +
        "WHERE " +
            "cid = ? " +
        "AND " +
            "objectId IN (#IDS#)";

    private static final String INSERT_DATE_EXTERNAL =
        "INSERT INTO " +
            "dateExternal " +
            "(cid, objectId, mailAddress, displayName, confirm, reason) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?)";


    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return com.openexchange.groupware.container.Appointment.class.getName();
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public IntegerMapping copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Integer srcCtxId = copyTools.getSourceContextId();
        final Integer dstCtxId = copyTools.getDestinationContextId();
        final Integer srcUsrId = copyTools.getSourceUserId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();
        final ObjectMapping<FolderObject> folderMapping = copyTools.getFolderMapping();
        final Set<Integer> sourceFolderIds = folderMapping.getSourceKeys();

        final List<Integer> appointmentIds = loadAppointmentIdsFromDB(sourceFolderIds, i(srcUsrId), i(srcCtxId), srcCon);
        final Map<Integer, CalendarDataObject> appointments = loadAppointmentsFromDB(appointmentIds, i(srcCtxId), srcCon);
        checkAppointmentsForMissingRecurrenceMasters(appointments);
        addParticipants(appointments, srcCon, i(srcCtxId), i(srcUsrId));
        exchangeIds(appointments, folderMapping, i(dstUsrId), i(dstCtxId), dstCon, i(srcUsrId));
        writeAppointmentsToDB(dstCon, appointments, i(dstUsrId), i(dstCtxId));
        writeParticipantsToDB(dstCon, appointments, i(dstCtxId));

        final IntegerMapping mapping = new IntegerMapping();
        for (final Entry<Integer, CalendarDataObject> appointmentEntry : appointments.entrySet()) {
            final CalendarDataObject appointment = appointmentEntry.getValue();
            mapping.addMapping(appointmentEntry.getKey(), I(appointment.getObjectID()));
        }

        final Map<Integer, ExternalDate> externalDates = loadExternalDatesFromDB(srcCon, i(srcCtxId), appointmentIds);
        writeExternalDatesToDB(dstCon, i(dstCtxId), externalDates, mapping);

        return mapping;
    }

    void writeExternalDatesToDB(final Connection con, final int cid, final Map<Integer, ExternalDate> dates, final IntegerMapping mapping) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_DATE_EXTERNAL);
            for (final Entry<Integer, ExternalDate> dateEntry : dates.entrySet()) {
                final Integer destination = mapping.getDestination(dateEntry.getKey());
                if (destination != null) {
                    int i = 1;
                    final ExternalDate date = dateEntry.getValue();
                    stmt.setInt(i++, cid);
                    stmt.setInt(i++, i(destination));
                    stmt.setString(i++, date.getMailAddress());
                    setStringOrNull(i++, stmt, date.getDisplayName());
                    stmt.setInt(i++, date.getConfirm());
                    setStringOrNull(i++, stmt, date.getReason());

                    stmt.addBatch();
                }

                stmt.executeBatch();
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    Map<Integer, ExternalDate> loadExternalDatesFromDB(final Connection con, final int cid, final Collection<Integer> appointmentIds) throws OXException {
        if (null == appointmentIds || appointmentIds.isEmpty()) {
            return Collections.<Integer, ExternalDate> emptyMap();
        }
        final Map<Integer, ExternalDate> dates = new HashMap<Integer,ExternalDate>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final String sql = replaceIdsInQuery("#IDS#", SELECT_DATE_EXTERNAL, appointmentIds);
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, cid);

            rs = stmt.executeQuery();
            while (rs.next()) {
                int i = 1;
                final ExternalDate date = new ExternalDate();
                final int dateId = rs.getInt(i++);
                date.setMailAddress(rs.getString(i++));
                date.setDisplayName(rs.getString(i++));
                date.setConfirm(rs.getInt(i++));
                date.setReason(rs.getString(i++));

                dates.put(dateId, date);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return dates;
    }

    private void writeParticipantsToDB(final Connection dstCon, final Map<Integer, CalendarDataObject> appointments, final int dstCtxId) throws OXException {
        PreparedStatement mstmt = null;
        PreparedStatement rstmt = null;
        try {
            mstmt = dstCon.prepareStatement(INSERT_MEMBER);
            rstmt = dstCon.prepareStatement(INSERT_RIGHT);
            for (final CalendarDataObject appointment : appointments.values()) {
                final Participant[] participants = appointment.getParticipants();
                if (participants != null) {
                    for (final Participant participant : participants) {
                        int j = 1;
                        if (participant.getType() == Participant.USER) {
                            final UserParticipant userParticipant = (UserParticipant) participant;
                            int i = 1;
                            mstmt.setInt(i++, appointment.getObjectID());
                            mstmt.setInt(i++, userParticipant.getIdentifier());
                            mstmt.setInt(i++, userParticipant.getConfirm());
                            setStringOrNull(i++, mstmt, userParticipant.getConfirmMessage());
                            setIntOrNull(i++, mstmt, userParticipant.getPersonalFolderId());
                            setIntOrNull(i++, mstmt, userParticipant.getAlarmMinutes());
                            mstmt.setInt(i++, dstCtxId);

                            mstmt.addBatch();
                        }

                        rstmt.setInt(j++, appointment.getObjectID());
                        rstmt.setInt(j++, dstCtxId);
                        rstmt.setInt(j++, participant.getIdentifier());
                        rstmt.setInt(j++, participant.getType());
                        setStringOrNull(j++, rstmt, participant.getEmailAddress());
                        setStringOrNull(j++, rstmt, participant.getDisplayName());

                        rstmt.addBatch();
                    }
                }
            }

            mstmt.executeBatch();
            rstmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(mstmt);
            DBUtils.closeSQLStuff(rstmt);
        }
    }

    private void writeAppointmentsToDB(final Connection dstCon, final Map<Integer, CalendarDataObject> appointments, final int dstUsrId, final int dstCtxId) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = dstCon.prepareStatement(INSERT_APPOINTMENT);
            for (final CalendarDataObject appointment : appointments.values()) {
                int i = 1;
                stmt.setTimestamp(i++, SQLTools.toTimestamp(appointment.getCreationDate()));
                stmt.setInt(i++, dstUsrId);
                stmt.setLong(i++, appointment.getLastModified().getTime());
                stmt.setInt(i++, dstUsrId);
                stmt.setInt(i++, appointment.getParentFolderID());
                stmt.setInt(i++, appointment.getPrivateFlag() ? 1 : 0);
                stmt.setInt(i++, dstCtxId);
                stmt.setTimestamp(i++, new Timestamp(appointment.getStartDate().getTime()));
                stmt.setTimestamp(i++, new Timestamp(appointment.getEndDate().getTime()));
                stmt.setString(i++, appointment.getTimezone());
                stmt.setInt(i++, appointment.getObjectID());
                setIntOrNull(i++, stmt, appointment.getRecurrenceID());
                setIntOrNull(i++, stmt, appointment.getLabel());
                setIntOrNull(i++, stmt, appointment.getDays());
                setIntOrNull(i++, stmt, appointment.getRecurrencePosition());
                stmt.setInt(i++, appointment.getShownAs());
                setIntOrNull(i++, stmt, appointment.getFullTime() ? 1 : 0);
                setIntOrNull(i++, stmt, appointment.getNumberOfAttachments());
                setStringOrNull(i++, stmt, appointment.getTitle());
                setStringOrNull(i++, stmt, appointment.getLocation());
                setStringOrNull(i++, stmt, appointment.getNote());
                setStringOrNull(i++, stmt, appointment.getRecurrence());
                setStringOrNull(i++, stmt, appointment.getDelExceptions());
                setStringOrNull(i++, stmt, appointment.getExceptions());
                setStringOrNull(i++, stmt, appointment.getCategories());
                // TODO: If the participants of this appointment have been modified because of the context move,
                // keeping the appointments uid may cause problems.
                setStringOrNull(i++, stmt, appointment.getUid());
                setStringOrNull(i++, stmt, appointment.getOrganizer());
                setIntOrNull(i++, stmt, appointment.getSequence());
                stmt.setInt(i++, appointment.getOrganizerId());
                setStringOrNull(i++, stmt, appointment.getPrincipal());
                stmt.setInt(i++, appointment.getPrincipalId());
                setStringOrNull(i++, stmt, appointment.getFilename());

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private void exchangeIds(final Map<Integer, CalendarDataObject> appointments, final ObjectMapping<FolderObject> folderMapping, final int dstUsrId, final int dstCtxId, final Connection dstCon, final int srcUsrId) throws OXException {
        try {
            final Map<Integer, CalendarDataObject> seriesAppointments = new HashMap<Integer, CalendarDataObject>();
            for (final Entry<Integer, CalendarDataObject> appointmentEntry : appointments.entrySet()) {
                final int newAppointmentId = IDGenerator.getId(dstCtxId, com.openexchange.groupware.Types.APPOINTMENT, dstCon);
                final CalendarDataObject appointment = appointmentEntry.getValue();
                appointment.setObjectID(newAppointmentId);
                appointment.setCreatedBy(dstUsrId);
                appointment.setModifiedBy(dstUsrId);
                if (appointment.getRecurrenceID() != -1) {
                    seriesAppointments.put(appointmentEntry.getKey(), appointment);
                }

                final FolderObject sourceFolder = folderMapping.getSource(appointment.getParentFolderID());
                int newParentFolderId = appointment.getParentFolderID();
                if (sourceFolder != null) {
                    final FolderObject destinationFolder = folderMapping.getDestination(sourceFolder);
                    newParentFolderId = destinationFolder.getObjectID();
                }
                appointment.setParentFolderID(newParentFolderId);

                if (appointment.getOrganizerId() == srcUsrId) {
                    appointment.setOrganizerId(dstUsrId);
                } else {
                    appointment.setOrganizerId(0);
                }
                if (appointment.getPrincipalId() == srcUsrId) {
                    appointment.setPrincipalId(dstUsrId);
                } else {
                    appointment.setPrincipalId(0);
                }

                final Participant[] participants = appointment.getParticipants();
                if (participants != null) {
                    for (final Participant participant : participants) {
                        if (participant.getType() == Participant.USER) {
                            final UserParticipant userParticipant = (UserParticipant) participant;
                            userParticipant.setIdentifier(dstUsrId);

                            final int personalFolderId = userParticipant.getPersonalFolderId();
                            final FolderObject sourcePersonalFolder = folderMapping.getSource(personalFolderId);
                            if (sourcePersonalFolder != null) {
                                final FolderObject destinationPersonalFolder = folderMapping.getDestination(sourcePersonalFolder);
                                userParticipant.setPersonalFolderId(destinationPersonalFolder.getObjectID());
                            }
                        }
                    }
                }
            }

            for (final Integer appointmentId : seriesAppointments.keySet()) {
                final CalendarDataObject appointment = appointments.get(appointmentId);
                final int recurrenceId = appointment.getRecurrenceID();
                final CalendarDataObject master = appointments.get(recurrenceId);
                appointment.setRecurrenceID(master.getObjectID());
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        }
    }

    void addParticipants(final Map<Integer, CalendarDataObject> appointments, final Connection srcCon, final Integer srcCtxId, final Integer srcUsrId) throws OXException {
        for (final CalendarDataObject appointment : appointments.values()) {
            final List<Participant> participants = fetchMoveableParticipants(appointment, srcCon, srcCtxId, srcUsrId);
            appointment.setParticipants(participants);
        }
    }

    private void checkAppointmentsForMissingRecurrenceMasters(final Map<Integer, CalendarDataObject> appointments) {
        final List<Integer> toRemove = new ArrayList<Integer>();
        for (final Entry<Integer, CalendarDataObject> appointmentEntry : appointments.entrySet()) {
            final CalendarDataObject appointment = appointmentEntry.getValue();
            final int recurrenceId = appointment.getRecurrenceID();
            Integer appointmentId = appointmentEntry.getKey();
            if (recurrenceId != -1 && recurrenceId != appointmentId.intValue()) {
                /*
                 * This is a change exception.
                 * We have to check if there is an existing master appointment.
                 * Otherwise we remove this one.
                 */
                final CalendarDataObject master = appointments.get(recurrenceId);
                if (master == null) {
                    toRemove.add(appointmentId);
                }
            }
        }

        for (final int id : toRemove) {
            appointments.remove(id);
        }
    }

    private List<Participant> fetchMoveableParticipants(final CalendarDataObject appointment, final Connection srcCon, final Integer srcCtxId, final Integer srcUsrId) throws OXException {
        final List<Participant> participants = new ArrayList<Participant>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = srcCon.prepareStatement(SELECT_PARTICIPANTS);
            stmt.setInt(1, srcCtxId);
            stmt.setInt(2, appointment.getObjectID());
            stmt.setInt(3, srcUsrId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                int i = 1;
                final int id = rs.getInt(i++);
                final int type = rs.getInt(i++);
                final Participant participant;
                if (type == Participant.USER) {
                    final UserParticipant userParticipant = new UserParticipant(id);
                    userParticipant.setEmailAddress(rs.getString(i++));
                    userParticipant.setDisplayName(rs.getString(i++));
                    userParticipant.setConfirm(rs.getInt(i++));
                    userParticipant.setConfirmMessage(rs.getString(i++));
                    userParticipant.setPersonalFolderId(getIntOrNegative(i++, rs));
                    userParticipant.setAlarmMinutes(getIntOrNegative(i++, rs));

                    participant = userParticipant;
                } else if (type == Participant.EXTERNAL_USER) {
                    final ExternalUserParticipant externalParticipant = new ExternalUserParticipant(rs.getString(i++));
                    externalParticipant.setDisplayName(rs.getString(i++));
                    externalParticipant.setIdentifier(id);

                    participant = externalParticipant;
                } else {
                    continue;
                }

                participants.add(participant);
            }

            return participants;
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    Map<Integer, CalendarDataObject> loadAppointmentsFromDB(final List<Integer> appointmentIds, final Integer srcCtxId, final Connection srcCon) throws OXException {
        final Map<Integer, CalendarDataObject> appointments = new HashMap<Integer, CalendarDataObject>();
        if (!appointmentIds.isEmpty()) {
            final String selectStatement = CopyTools.replaceIdsInQuery("#IDS#", SELECT_APPOINTMENTS, appointmentIds);
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = srcCon.prepareStatement(selectStatement);
                stmt.setInt(1, srcCtxId);

                rs = stmt.executeQuery();
                while (rs.next()) {
                    int i = 1;
                    final CalendarDataObject appointment = new CalendarDataObject();
                    appointment.setCreationDate(rs.getTimestamp(i++));
                    appointment.setLastModified(new Date(rs.getLong(i++)));
                    appointment.setGlobalFolderID(rs.getInt(i++));
                    appointment.setPrivateFlag(rs.getInt(i++) == 0 ? false : true);
                    appointment.setStartDate(rs.getTimestamp(i++)); // timestampfield01
                    appointment.setEndDate(rs.getTimestamp(i++)); // timestampfield02
                    appointment.setTimezone(rs.getString(i++));
                    appointment.setObjectID(rs.getInt(i++)); // intfield01
                    appointment.setRecurrenceID(getIntOrNegative(i++, rs)); // intfield02
                    appointment.setLabel(getIntOrNegative(i++, rs)); // intfield03 Outlook Farbe.
                    appointment.setDays(getIntOrNegative(i++, rs)); // intfield04
                    appointment.setRecurrencePosition(getIntOrNegative(i++, rs)); // intfield05
                    appointment.setShownAs(rs.getInt(i++)); // intfield06
                    appointment.setFullTime(rs.getInt(i++) == 0 ? false : true); // intfield07
                    appointment.setNumberOfAttachments(getIntOrNegative(i++, rs)); // intfield08
                    appointment.setTitle(rs.getString(i++)); // field01
                    appointment.setLocation(rs.getString(i++)); // field02
                    appointment.setNote(rs.getString(i++)); // field04
                    appointment.setRecurrence(rs.getString(i++)); // field06
                    appointment.setDelExceptions(rs.getString(i++)); // field07
                    appointment.setExceptions(rs.getString(i++)); // field08
                    appointment.setCategories(rs.getString(i++)); // field09 (Tags)
                    appointment.setUid(rs.getString(i++));
                    appointment.setOrganizer(rs.getString(i++));
                    appointment.setSequence(getIntOrNegative(i++, rs));
                    appointment.setOrganizerId(getIntOrNegative(i++, rs));
                    appointment.setPrincipal(rs.getString(i++));
                    appointment.setPrincipalId(getIntOrNegative(i++, rs));
                    appointment.setFilename(rs.getString(i++));

                    appointments.put(appointment.getObjectID(), appointment);
                }
            } catch (final SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
        }
        return appointments;
    }

    List<Integer> loadAppointmentIdsFromDB(final Set<Integer> sourceFolderIds, final Integer srcUsrId, final Integer srcCtxId, final Connection srcCon) throws OXException {
        final List<Integer> appointmentIds = new ArrayList<Integer>();
        if (!sourceFolderIds.isEmpty()) {
            final String selectStatement = CopyTools.replaceIdsInQuery("#IDS#", SELECT_APP_IDS, sourceFolderIds);
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = srcCon.prepareStatement(selectStatement);
                stmt.setInt(1, srcUsrId);
                stmt.setInt(2, srcCtxId);

                rs = stmt.executeQuery();
                while (rs.next()) {
                    appointmentIds.add(rs.getInt(1));
                }
            } catch (final SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
        }
        return appointmentIds;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
