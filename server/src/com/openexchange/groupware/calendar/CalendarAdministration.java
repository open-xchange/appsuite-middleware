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

package com.openexchange.groupware.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.impl.SessionObject;

/**
 *  CalendarAdministration
 *  @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class CalendarAdministration implements DeleteListener {
    
    private StringBuilder u1 = null;
    private static final Log LOG = LogFactory.getLog(CalendarAdministration.class);
    
    public CalendarAdministration() {
        
    }
    
    public void deletePerformed(DeleteEvent deleteEvent, Connection readcon, Connection writecon) throws DeleteFailedException {
        try {
	    	if (deleteEvent.getType() == DeleteEvent.TYPE_USER) {
	            deleteUser(deleteEvent, readcon, writecon);
	        } else if (deleteEvent.getType() == DeleteEvent.TYPE_GROUP) {
	            deleteGroup(deleteEvent, readcon, writecon);
	        } else if (deleteEvent.getType() == DeleteEvent.TYPE_RESOURCE) {
	            deleteResource(deleteEvent, readcon, writecon);
	        } else if (deleteEvent.getType() == DeleteEvent.TYPE_RESOURCE_GROUP) {
	            deleteResourceGroup(deleteEvent, readcon, writecon);
	        } else {
	        	throw new DeleteFailedException(DeleteFailedException.Code.UNKNOWN_TYPE, Integer.valueOf(deleteEvent.getType()));
	        }
        } catch (final SQLException e) {
        	throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getLocalizedMessage());
        } catch (final LdapException e) {
        	throw new DeleteFailedException(e);
        }
    }
    
    private final void deleteUser(DeleteEvent deleteEvent, Connection readcon, Connection writecon) throws DeleteFailedException, LdapException, SQLException {
        try {
            //  Delete all appointments where the user is the only participant (and where the app is private) !! NO MOVE TO del_* !!
            //  Delete the user from the participant list and update the appointment
            //  Update all created_by and changed_from and changing_dates WHERE the user is the creator
            //  Update all changed_from and changing_dates WHERE the user is the editor
            deleteUserFromAppointments(deleteEvent, readcon, writecon);
        } catch (DBPoolingException ex) {
            throw new DeleteFailedException(ex);
        } catch (OXException ex) {
            throw new DeleteFailedException(ex);
        }
    }
    
    
    private final void deleteGroup(DeleteEvent deleteEvent, Connection readcon, Connection writecon) throws DeleteFailedException, LdapException, SQLException {
        deleteObjects(deleteEvent, readcon, writecon, CalendarSql.VIEW_TABLE_NAME, Participant.GROUP);
    }
    
    private final void deleteResource(DeleteEvent deleteEvent, Connection readcon, Connection writecon) throws DeleteFailedException, LdapException, SQLException {
        deleteObjects(deleteEvent, readcon, writecon, CalendarSql.VIEW_TABLE_NAME, Participant.RESOURCE);
    }
    
    private final void deleteResourceGroup(DeleteEvent deleteEvent, Connection readcon, Connection writecon) throws DeleteFailedException, LdapException, SQLException {
        deleteObjects(deleteEvent, readcon, writecon, CalendarSql.VIEW_TABLE_NAME, Participant.RESOURCEGROUP);
    }
    
    private final void deleteObjects(DeleteEvent deleteEvent, Connection readcon, Connection writecon, String table, int type) throws DeleteFailedException, LdapException, SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            StringBuilder sb = new StringBuilder(128);
            sb.append("SELECT object_id, cid, id, type from ");
            sb.append(table);
            sb.append(" WHERE cid = ");
            sb.append(deleteEvent.getContext().getContextId());
            sb.append(" AND type = ");
            sb.append(type);
            sb.append(" AND id = ");
            sb.append(deleteEvent.getId());
            pst = writecon.prepareStatement(sb.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            rs = CalendarSql.getCalendarSqlImplementation().getResultSet(pst);
            PreparedStatement update = getUpdatePreparedStatement(writecon);
            while (rs.next()) {
                int object_id = rs.getInt(1);
                eventHandling(object_id, deleteEvent.getContext(), deleteEvent.getSession(), CalendarOperation.UPDATE, readcon);
                rs.deleteRow();
                addUpdateMasterObjectBatch(update, deleteEvent.getContext().getMailadmin(), deleteEvent.getContext().getContextId(), object_id);
            }
            update.executeBatch();
            update.close();
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (rs != null) {
                pst.close();
            }
        }
    }
    
    private void deleteUserFromAppointments(DeleteEvent deleteEvent, Connection readcon, Connection writecon) throws DeleteFailedException, LdapException, SQLException, DBPoolingException, OXException {
        PreparedStatement pst = null;
        PreparedStatement del_rights = null;
        PreparedStatement del_members = null;
        PreparedStatement del_dates = null;
        ResultSet rs = null;
        PreparedStatement pst2 = null;
        ResultSet rs2 = null;
        PreparedStatement pst3 = null;
        PreparedStatement pst4 = null;
        PreparedStatement pst5 = null;
        PreparedStatement pst6 = null;
        try {
            StringBuilder sb = new StringBuilder(128);
            sb.append("SELECT pdr.object_id FROM ");
            sb.append(CalendarSql.VIEW_TABLE_NAME);
            sb.append(" pdr JOIN ");
            sb.append(CalendarSql.VIEW_TABLE_NAME);
            sb.append(" pdr2 ON pdr.cid = ");
            sb.append(deleteEvent.getContext().getContextId());
            sb.append(" AND pdr2.cid = ");
            sb.append(deleteEvent.getContext().getContextId());
            sb.append(" AND pdr.object_id = pdr2.object_id");
            sb.append(" WHERE pdr2.id = ");
            sb.append(deleteEvent.getId());
            sb.append(" group by pdr.object_id having count(pdr.object_id ) = 1");
            pst = readcon.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = CalendarSql.getCalendarSqlImplementation().getResultSet(pst);
            while (rs.next()) {
                if (del_rights == null) {
                    del_rights = writecon.prepareStatement("delete from prg_date_rights WHERE cid = ? AND object_id = ?");
                }
                if (del_members == null) {
                    del_members = writecon.prepareStatement("delete from prg_dates_members WHERE cid = ? AND object_id = ?");
                }
                if (del_dates == null) {
                    del_dates = writecon.prepareStatement("delete FROM prg_dates WHERE cid = ? AND intfield01 = ?");
                }
                int object_id = rs.getInt(1);
                del_dates.setInt(1, deleteEvent.getContext().getContextId());
                del_dates.setInt(2, object_id);
                del_dates.addBatch();
                del_members.setInt(1, deleteEvent.getContext().getContextId());
                del_members.setInt(2, object_id);
                del_members.addBatch();
                del_rights.setInt(1, deleteEvent.getContext().getContextId());
                del_rights.setInt(2, object_id);
                del_rights.addBatch();
                
                eventHandling(object_id, deleteEvent.getContext(), deleteEvent.getSession(), CalendarOperation.DELETE, readcon);
                
            }
            if (del_dates != null) {
                del_dates.executeBatch();
            }
            if (del_members != null) {
                del_members.executeBatch();
            }
            if (del_rights != null) {
                del_rights.executeBatch();
            }
            
            StringBuilder sb2 = new StringBuilder(128);
            sb2.append("SELECT pdm.object_id, pdm.pfid FROM ");
            sb2.append(CalendarSql.PARTICIPANT_TABLE_NAME);
            sb2.append(" pdm JOIN ");
            sb2.append(CalendarSql.DATES_TABLE_NAME);
            sb2.append(" pd ON pdm.cid = ");
            sb2.append(deleteEvent.getContext().getContextId());
            sb2.append(" AND pd.cid = ");
            sb2.append(deleteEvent.getContext().getContextId());
            sb2.append(" AND pd.intfield01 = pdm.object_id");
            sb2.append(" WHERE pdm.member_uid = ");
            sb2.append(deleteEvent.getId());
            pst2 = readcon.prepareStatement(sb2.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs2 = CalendarSql.getCalendarSqlImplementation().getResultSet(pst2);
            PreparedStatement update = getUpdatePreparedStatement(writecon);
            while (rs2.next()) {
                int object_id = rs2.getInt(1);
                int fid = rs2.getInt(2);
                addUpdateMasterObjectBatch(update, deleteEvent.getContext().getMailadmin(), deleteEvent.getContext().getContextId(), object_id);
                eventHandling(object_id, deleteEvent.getContext(), deleteEvent.getSession(), CalendarOperation.UPDATE, readcon);
            }
            update.executeBatch();
            update.close();
            
            StringBuilder replace = new StringBuilder(128);
            replace.append("UPDATE ");
            replace.append(CalendarSql.DATES_TABLE_NAME);
            replace.append(" pd SET ");
            replace.append(CalendarCommonCollection.getFieldName(AppointmentObject.CREATED_BY));
            replace.append(" = ");
            replace.append(deleteEvent.getContext().getMailadmin());
            replace.append(", ");
            replace.append(CalendarCommonCollection.getFieldName(AppointmentObject.LAST_MODIFIED));
            replace.append(" = ");
            replace.append(System.currentTimeMillis());
            replace.append(" WHERE cid = ");
            replace.append(deleteEvent.getContext().getContextId());
            replace.append(" AND ");
            replace.append(CalendarCommonCollection.getFieldName(AppointmentObject.CREATED_BY));
            replace.append(" = ");
            replace.append(deleteEvent.getId());
            pst3 = writecon.prepareStatement(replace.toString());
            pst3.addBatch();
            pst3.executeBatch();
            
            StringBuilder replace_modified_by = new StringBuilder(128);
            replace_modified_by.append("UPDATE ");
            replace_modified_by.append(CalendarSql.DATES_TABLE_NAME);
            replace_modified_by.append(" pd SET ");
            replace_modified_by.append(CalendarCommonCollection.getFieldName(AppointmentObject.MODIFIED_BY));
            replace_modified_by.append(" = ");
            replace_modified_by.append(deleteEvent.getContext().getMailadmin());
            replace_modified_by.append(", ");
            replace_modified_by.append(CalendarCommonCollection.getFieldName(AppointmentObject.LAST_MODIFIED));
            replace_modified_by.append(" = ");
            replace_modified_by.append(System.currentTimeMillis());
            replace_modified_by.append(" WHERE cid = ");
            replace_modified_by.append(deleteEvent.getContext().getContextId());
            replace_modified_by.append(" AND ");
            replace_modified_by.append(CalendarCommonCollection.getFieldName(AppointmentObject.MODIFIED_BY));
            replace_modified_by.append(" = ");
            replace_modified_by.append(deleteEvent.getId());
            pst4 = writecon.prepareStatement(replace_modified_by.toString());
            pst4.addBatch();
            pst4.executeBatch();
            
            StringBuilder delete_participant_members = new StringBuilder(128);
            delete_participant_members.append("DELETE FROM prg_dates_members WHERE cid = ");
            delete_participant_members.append(deleteEvent.getContext().getContextId());
            delete_participant_members.append(" AND member_uid = ");
            delete_participant_members.append(deleteEvent.getId());
            pst5 = writecon.prepareStatement(delete_participant_members.toString());
            pst5.addBatch();
            pst5.executeBatch();
            
            StringBuilder delete_participant_rights = new StringBuilder(128);
            delete_participant_rights.append("delete from prg_date_rights WHERE cid = ");
            delete_participant_rights.append(deleteEvent.getContext().getContextId());
            delete_participant_rights.append(" AND id = ");
            delete_participant_rights.append(deleteEvent.getId());
            delete_participant_rights.append(" AND type = ");
            delete_participant_rights.append(Participant.USER);
            pst6 = writecon.prepareStatement(delete_participant_rights.toString());
            pst6.addBatch();
            pst6.executeBatch();
            
        } finally {
            if (rs != null) {
                CalendarCommonCollection.closeResultSet(rs);
            }
            if (pst != null) {
                CalendarCommonCollection.closePreparedStatement(pst);
            }
            if (del_dates != null) {
                CalendarCommonCollection.closePreparedStatement(del_dates);
            }
            if (del_rights != null) {
                CalendarCommonCollection.closePreparedStatement(del_rights);
            }
            if (del_members != null) {
                CalendarCommonCollection.closePreparedStatement(del_members);
            }
            if (rs2 != null) {
                CalendarCommonCollection.closeResultSet(rs2);
            }
            if (pst2 != null) {
                CalendarCommonCollection.closePreparedStatement(pst2);
            }
            if (pst3 != null) {
                CalendarCommonCollection.closePreparedStatement(pst3);
            }
            if (pst4 != null) {
                CalendarCommonCollection.closePreparedStatement(pst4);
            }
            if (pst5 != null) {
                CalendarCommonCollection.closePreparedStatement(pst5);
            }
            if (pst6 != null) {
                CalendarCommonCollection.closePreparedStatement(pst6);
            }
        }
    }
    
    private final void addUpdateMasterObjectBatch(PreparedStatement update, int mailadmin, int cid, int oid) throws SQLException {
        update.setInt(1, mailadmin);
        update.setLong(2, System.currentTimeMillis());
        update.setInt(3, cid);
        update.setInt(4, oid);
        update.addBatch();
    }
    
    private final PreparedStatement getUpdatePreparedStatement(Connection writecon) throws SQLException {
        if (u1 == null) {
            initializeUpdateString();
        }
        return writecon.prepareStatement(u1.toString());
    }
    
    public final void initializeUpdateString() {
        u1 = new StringBuilder(128);
        u1.append("UPDATE prg_dates pd SET ");
        u1.append(CalendarCommonCollection.getFieldName(AppointmentObject.MODIFIED_BY));
        u1.append(" = ? ,");
        u1.append(CalendarCommonCollection.getFieldName(AppointmentObject.LAST_MODIFIED));
        u1.append(" = ? ");
        u1.append(" WHERE cid = ? AND ");
        u1.append(CalendarCommonCollection.getFieldName(AppointmentObject.OBJECT_ID));
        u1.append(" = ?");
    }
    
    private final void eventHandling(int object_id, Context context, SessionObject so, int type, Connection readcon) throws SQLException, DeleteFailedException {
        CalendarOperation co = new CalendarOperation();
        CalendarSql csql = new CalendarSql(so);
        CalendarSqlImp cimp = csql.getCalendarSqlImplementation();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep =  cimp.getPreparedStatement(readcon, cimp.loadAppointment(object_id, context));
            rs = cimp.getResultSet(prep);
            CalendarDataObject cdao = null;
            try {
                cdao = co.loadAppointment(rs, object_id, 0, cimp, readcon, so, CalendarOperation.READ, 0, false);
                CalendarCommonCollection.triggerEvent(so, type, cdao);
            } catch (OXPermissionException ex) {
                throw new DeleteFailedException(ex);
            } catch (OXObjectNotFoundException ex) {
                LOG.warn("While deleting an object (type:"+type+") the master object with id "+object_id+" in context "+context.getContextId()+" was not found!");
            } catch (OXException ex) {
                throw new DeleteFailedException(ex);
            }
        } finally {
            CalendarCommonCollection.closeResultSet(rs);
            CalendarCommonCollection.closePreparedStatement(prep);
        }
    }
    
}
