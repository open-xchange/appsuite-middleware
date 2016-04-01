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

package com.openexchange.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.sql.DBUtils;

/**
 * FreeBusyResults
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class FreeBusyResults implements SearchIterator<CalendarDataObject> {

    public final static int MAX_SHOW_USER_PARTICIPANTS = 5;

    private final List<OXException> warnings;
    private final ResultSet rs;
    private final Connection con;
    private final Context c;
    private final int uid;
    private int seq = -1;
    private int sa;
    private int ft;
    private int oid;
    private int pflag;
    private int owner;
    private int fid;
    private int colorLabel;
    private String title;
    private RecurringResultsInterface rrs;
    private boolean has_next;
    private final boolean show_details;
    private final PreparedStatement prep;
    private CalendarFolderObject cfo;
    private final Participant conflict_objects[];
    private final long range_start;
    private final long range_end;

    private int last_up_oid;
    private UserParticipant last_up[];
    private int last_p_oid;
    private Participant last_p[];

    private List<CalendarDataObject> al = new ArrayList<CalendarDataObject>(16);
    private int counter;

    private PrivateFolderInformationObject[] private_folder_array;
    private final SearchIterator<List<Integer>> private_folder_information;

    private final CalendarSqlImp calendarsqlimp;

    private final CalendarCollection recColl;

    private int readFolderId;

    private String categories;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FreeBusyResults.class);

    /*public FreeBusyResults(final ResultSet rs, final PreparedStatement prep, final Context c, final Connection con, final long range_start, final long range_end) throws OXException {
    	this.warnings =  new ArrayList<AbstractOXException>(2);
    	this.rs = rs;
        this.prep = prep;
        this.con = con;
        this.c = c;
        this.range_start = range_start;
        this.range_end = range_end;
        rsNext();
        preFill();
    }*/

    public FreeBusyResults(final ResultSet rs, final PreparedStatement prep, final Context c, final int uid, final int groups[], final UserConfiguration uc, final Connection con, final boolean show_details, final Participant conflict_objects[], final SearchIterator<List<Integer>> private_folder_information, final CalendarSqlImp calendarsqlimp) throws OXException {
    	this(rs, prep, c, uid, groups, uc, con, show_details, conflict_objects, private_folder_information, calendarsqlimp, 0, 0);
    }

    public FreeBusyResults(final ResultSet rs, final PreparedStatement prep, final Context c, final int uid, final int groups[], final UserConfiguration uc, final Connection con, final boolean show_details, final Participant conflict_objects[], final SearchIterator<List<Integer>> private_folder_information, final CalendarSqlImp calendarsqlimp, final long range_start, final long range_end) throws OXException {
    	this.warnings =  new ArrayList<OXException>(2);
    	this.rs = rs;
        this.prep = prep;
        this.con = con;
        this.c = c;
        this.uid = uid;
        this.show_details = show_details;
        this.conflict_objects = conflict_objects;
        this.calendarsqlimp = calendarsqlimp;
        this.private_folder_information = private_folder_information;
        this.range_start = range_start;
        this.range_end = range_end;
        this.recColl = new CalendarCollection();
        if (show_details) {
            preFillPermissionArray(groups, uc);
            preFillPrivateFolderInformation();
        }
        rsNext();
        preFill();
    }

    private final void preFill() throws OXException {
        try {
            while (myhasNext()) {
                final CalendarDataObject o = mynext();
                if (o != null) {
                    al.add(o);
                }
            }
        } finally {
            myclose();
        }
    }

    @Override
    public final CalendarDataObject next() throws OXException {
        return al.get(counter++);
    }

    public final CalendarDataObject mynext() throws SearchIteratorException, OXException {
        CalendarDataObject cdao = null;
        if (seq >= 0 && rrs != null) {
            final RecurringResultInterface rr = rrs.getRecurringResult(seq);
            return recurringDAO(rr);
        }
        try {
            if (has_next) {
                cdao = new CalendarDataObject();
                oid = rs.getInt(1);
                cdao.setObjectID(oid);
                final java.util.Date s = rs.getTimestamp(2);
                cdao.setStartDate(s);
                final java.util.Date e = rs.getTimestamp(3);
                cdao.setEndDate(e);
                ft = rs.getInt(4);
                cdao.setFullTime(ft == 1);
                sa = rs.getInt(5);
                cdao.setShownAs(sa);
                title = rs.getString(6);
                fid = rs.getInt(7);
                pflag = rs.getInt(8);
                if (!rs.wasNull()) {
                    cdao.setPrivateFlag(pflag == 1);
                }
                owner = rs.getInt(9);
                colorLabel = rs.getInt(17); // SQL NULL would return zero which is no color label
                categories = rs.getString(18); // SQL NULL would return null
                cdao.setCreatedBy(owner);
                final int recid = rs.getInt(10);
                if (recid != 0) {
                    cdao.setRecurrenceID(recid);
                }
                if (!rs.wasNull()) {
                    if (recid == oid) {
                        // Main series
                        cdao.setRecurrenceCalculator(rs.getInt(11));
                        cdao.setRecurrence(rs.getString(12));
                        recColl.fillDAO(cdao);
                        cdao.setDelExceptions(rs.getString(13));
                        cdao.setExceptions(rs.getString(14));
                        cdao.setTimezone(rs.getString(15));
                        if (recColl.fillDAO(cdao)) {
                            try {
                                rrs = recColl.calculateRecurring(cdao, range_start, range_end, 0);
                            } catch (final OXException x) {
                                LOG.error("Can not load appointment '{}' with id {}:{} due to invalid recurrence pattern", cdao.getTitle(), cdao.getObjectID(), cdao.getContextID(), x);
                                recColl.recoverForInvalidPattern(cdao);
                                seq = -1;
                                rsNext();
                                return cdao;
                            }
                            seq = rrs.size()-1;
                            if (seq >= 0) {
                                final RecurringResultInterface rr = rrs.getRecurringResult(seq);
                                rsNext();
                                return recurringDAO(rr);
                            }
                            rsNext();
                            return null;
                        }
                    } else if (recid != 0) {
                        // Change exception
                        cdao.setRecurrencePosition(rs.getInt(16));
                    }
                }
            }
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } catch(final Exception e) {
            LOG.error("FreeBusyResults calculation problem with oid {} / {}", oid, cdao == null ? "" : cdao.toString() , e);
            throw SearchIteratorExceptionCodes.CALCULATION_ERROR.create(e, oid).setPrefix("APP");
        }
        if (ft != 0 && cdao != null) {
            cdao.setFullTime(true);
        }
        fillDetails(cdao);
        rsNext();
        return cdao;
    }

    @Override
    public int size() {
        return -1;
    }

    public boolean hasSize() {
        return false;
    }

    @Override
    public void addWarning(final OXException warning) {
		warnings.add(warning);
	}

	@Override
    public OXException[] getWarnings() {
		return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
	}

	@Override
    public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

    private final CalendarDataObject recurringDAO(final RecurringResultInterface rr) throws OXException {
        if (rr != null) {
            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setShownAs(sa);
            cdao.setStartDate(new Date(rr.getStart()));
            cdao.setEndDate(new Date(rr.getEnd()));
            if (ft != 0) {
                cdao.setFullTime(true);
            }
            cdao.setRecurrencePosition(rr.getPosition());
            cdao.setObjectID(oid);
            cdao.setCreatedBy(owner);
            fillDetails(cdao);
            seq--;
            return cdao;
        }
        rsNext();
        return null;
    }

    private void fillDetails(final CalendarDataObject cdao) throws OXException {
        if (show_details) {
            if (checkPermissions()) {
                cdao.setTitle(title);
                cdao.setParentFolderID(readFolderId);
                cdao.setLabel(colorLabel);
                cdao.setCategories(categories);
            }
            final Participants ret = resolveConflictingUserParticipants();
            cdao.setParticipants(ret.getList());
        }
    }

    @Override
    public boolean hasNext() throws OXException {
        if (!al.isEmpty() && counter < al.size()) {
            return true;
        }
        return false;
    }

    public final boolean myhasNext() {
        if (seq >= 0) {
            return true;
        }
        return has_next;
    }

    private void rsNext() {
        if (rs != null) {
            try {
                has_next = rs.next();
            } catch (final SQLException sqle) {
                has_next = false;
                LOG.error("Error while getting next result set", sqle);
            }
        }
    }

    @Override
    public void close() {
        al = null;
        title = null;
        rrs = null;
    }

    public final void myclose() {
        recColl.closeResultSet(rs);
        recColl.closePreparedStatement(prep);
        SearchIterators.close(private_folder_information);
        if (con != null) {
            DBPool.push(c, con);
        }
    }

    private final boolean checkPermissions() {
        boolean visible = checkVisibilityAnddetermineFolder();
        return pflag == 1 && owner == uid || pflag == 0 && visible;
    }

    /**
     * Checks, if the object is visible to the user and sets the appropriate folder identifier.
     * @return
     */
    private final boolean checkVisibilityAnddetermineFolder() {
        if (cfo == null) {
            return false;
        }
        /*
         * public folder?
         */
        if (fid > 0) {
            if ( cfo.canReadAllInPublicFolder(fid) ) {
                readFolderId = fid;
                return true;
            } else if (owner == uid && cfo.canReadOwnInPublicFolder(fid)) {
                readFolderId = fid;
                return true;
            }
        } else {
            /*
             * lookup parent folder information, try known private folders first
             */
            PrivateFolderInformationObject parentFolder = null;
            for (PrivateFolderInformationObject pfio : private_folder_array) {
                if (pfio.compareObjectId(oid) && canReadFrom(pfio)) {
                    readFolderId = pfio.getPrivateFolder();
                    return true;
                }
            }
            /*
             * lookup (shared) parent folder information based on current appointment's object id if no known private folder
             */
            if (null == parentFolder && 0 < cfo.getSharedFolderList().size()) {
                ResultSet result = null;
                PreparedStatement sharedFolderQuery = null;
                try {
                    sharedFolderQuery = calendarsqlimp.getSharedAppointmentFolderQuery(c, oid, cfo, con);
                    result = sharedFolderQuery.executeQuery();
                    while (result.next()) {
                        PrivateFolderInformationObject pfio = new PrivateFolderInformationObject(result.getInt(1), result.getInt(2), result.getInt(3));
                        if (canReadFrom(pfio)) {
                            readFolderId = pfio.getPrivateFolder();
                            return true;
                        }
                    }
                } catch (SQLException e) {
                    LOG.error("", e);
                } finally {
                    DBUtils.closeSQLStuff(result, sharedFolderQuery);
                }
            }
        }
        /*
         * not visible
         */
        return false;
    }

    /**
     * Gets a value indicating whether the user may read objects in the private folder identified by the supplied folder information
     * structure.
     *
     * @param parentFolder The parent folder information to check read-permissions for
     * @return <code>true</code> if the user is allowed to read appointments in that folder, <code>false</code>, otherwise.
     */
    private boolean canReadFrom(PrivateFolderInformationObject parentFolder) {
        int folderID = parentFolder.getPrivateFolder();
        if (cfo.canReadAllInPrivateFolder(folderID)) {
            return true;
        } else if (cfo.canReadAllInSharedFolder(folderID)) {
            return true;
        } else if (parentFolder.getParticipant() == uid) {
            if (cfo.canReadOwnInPrivateFolder(folderID)) {
                return true;
            } else if (cfo.canReadOwnInSharedFolder(folderID)) {
                return true;
            }
        }
        return false;
    }

    private final void preFillPermissionArray(final int groups[], final UserConfiguration uc) throws OXException {
        try {
            cfo = recColl.getAllVisibleAndReadableFolderObject(uid, groups, c, uc, con);
        } catch (final SQLException ex) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(ex);
        }
    }

    private final Participants resolveConflictingUserParticipants() throws OXException {
        final Participants p = new Participants();
        int counter = 0;
        if (conflict_objects instanceof UserParticipant[]) {
            try {
                final CalendarDataObject temp = new CalendarDataObject();
                temp.setContext(c);
                temp.setObjectID(oid);
                UserParticipant op[] = null;
                if (oid == last_up_oid) {
                    op = last_up;
                } else {
                    op = CalendarSql.getCalendarSqlImplementation().getUserParticipants(temp, con, uid).getUsers();
                    last_up_oid = oid;
                    last_up = op;
                }
                if (op != null && op.length > 0) {
                    final  UserParticipant up[] = (UserParticipant[])conflict_objects;
                    final Set<Integer> upIds = new HashSet<Integer>(up.length);
                    for(int a = 0, size = up.length; a < size; a++) { upIds.add(up[a].getIdentifier()); }

                    for (int b = 0; b < op.length; b++) {
                        if(upIds.contains(op[b].getIdentifier())) {
                            p.add(op[b]);
                            counter++;
                            if (counter >= MAX_SHOW_USER_PARTICIPANTS) {
                                break;
                            }
                        }
                    }

                }
            } catch(final SQLException sqle) {
                throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle, new Object[0]);
            }
        } else {
            try {
                final CalendarDataObject temp = new CalendarDataObject();
                temp.setContext(c);
                temp.setObjectID(oid);
                Participant op[] = null;
                if (oid == last_p_oid) {
                    op = last_p;
                } else {
                    op = CalendarSql.getCalendarSqlImplementation().getParticipants(temp, con).getList();
                    last_p_oid = oid;
                    last_p = op;
                }
                if (op != null && op.length > 1) {
                    for (int a = 0; a < conflict_objects.length; a++) {
                        for (int b = 0; b < op.length; b++) {
                            if (conflict_objects[a].getType() == Participant.RESOURCE && conflict_objects[a].getIdentifier() == op[b].getIdentifier()) {
                                p.add(op[b]);
                                counter++;
                                if (counter >= MAX_SHOW_USER_PARTICIPANTS) {
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch(final SQLException sqle) {
                throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle, new Object[0]);
            }
        }
        return p;
    }

    private final void preFillPrivateFolderInformation() {

        final List<PrivateFolderInformationObject> list = new ArrayList<PrivateFolderInformationObject>(16);

        int object_id = 0;
        int pfid = 0;
        int uid = 0;
        PreparedStatement shared_folder_info = null;
        try {
            for (final SearchIterator<List<Integer>> iter = private_folder_information; iter.hasNext();) {
                final List<Integer> vals = iter.next();
                object_id = vals.get(0).intValue();
                pfid = vals.get(1).intValue();
                uid = vals.get(2).intValue();
                final PrivateFolderInformationObject pfio = new PrivateFolderInformationObject(object_id, pfid, uid);
                list.add(pfio);
            }
        } catch (final OXException e) {
            LOG.error("", e);
        } finally {
            if(shared_folder_info != null) {
                try {
                    shared_folder_info.close();
                } catch (final SQLException e) {
                	LOG.error("", e);
                }
            }
        }
        private_folder_array = list.toArray(new PrivateFolderInformationObject[list.size()]);
    }

}
