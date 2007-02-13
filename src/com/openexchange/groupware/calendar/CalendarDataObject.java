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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.StringCollection;

import java.sql.Timestamp;
import java.util.Date;

/**
 * CalendarDataObject
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class CalendarDataObject extends AppointmentObject {
    
    private Timestamp creating_date, changing_date;
    private String rec_string;
    
    private Context context;
    private int folder_type = -1;
    private int pfid, action_folder;
    private int shared_folder_owner;
    private int folder_move_action;
    
    private boolean is_hard_conflict = false;
    
    private boolean contains_resources = false;
    private boolean folder_move = false;
    private boolean b_recurrencestring = false;
    
    
    public final void setUntil(Date until) {
        if (until != null) {
            long mod = until.getTime()%CalendarRecurringCollection.MILLI_DAY;
            if (mod != 0) {
                until = new Date(((until.getTime()-mod)+CalendarRecurringCollection.MILLI_DAY));
            }
            super.setUntil(until);
        }
    }
    
    public final void setSharedFolderOwner(int shared_folder_owner) {
        this.shared_folder_owner = shared_folder_owner;
    }
    
    public final int getSharedFolderOwner() {
        return shared_folder_owner;
    }
    
    public final void setFolderType(int folder_type) {
        this.folder_type = folder_type;
    }
    
    public final int getFolderType() {
        return folder_type;
    }
    
    public final void setActionFolder(int action_folder) {
        this.action_folder = action_folder;
    }
    
    public final int getActionFolder() {
        return action_folder;
    }
    
    public final int getFulltime() {
        if (!super.getFullTime()) {
            return 0;
        }
        return 1;
    }
    
    public final int getPrivateflag() {
        if (!super.getPrivateFlag()) {
            return 0;
        }
        return 1;
    }
    
    public void setCreatingDate(Timestamp creating_date) {
        this.creating_date = creating_date;
        super.setCreationDate(new Date(creating_date.getTime()));
    }
    
    public final Timestamp getCreatingDate() {
        if (creating_date != null) {
            return creating_date;
        }
        if (getLastModified() != null) {
            return new Timestamp(getLastModified().getTime());
        }
        return new Timestamp(System.currentTimeMillis());
    }
    
    public final void setChangingDate(Timestamp changing_date) {
        if (changing_date != null) {
            this.changing_date = changing_date;
            super.setLastModified(new Date(changing_date.getTime()));
        }
    }
    
    public final Timestamp getChangingDate() {
        if (changing_date != null) {
            return changing_date;
        }
        return new Timestamp(System.currentTimeMillis());
    }
    
    public final void setGlobalFolderID(int fid) {
        super.setParentFolderID(fid);
    }
    
    public final int getGlobalFolderID() {
        return getParentFolderID();
    }
    
    public final void setPrivateFolderID(int pfid) {
        this.pfid = pfid;
    }
    
    public final int getPrivateFolderID() {
        return pfid;
    }
    
    public int getEffectiveFolderId() {
        if (getParentFolderID() != 0) {
            return getParentFolderID();
        } else if (getPrivateFolderID() != 0) {
            return getPrivateFolderID();
        } else if (getActionFolder() != 0) {
            return getActionFolder();
        } else {
            System.out.println("FIX ME AND PROVIDE A FOLDER  :"+StringCollection.getStackAsString()); // TODO: Remove me
            return 0;
        }
    }
    
    public final void setContext(Context context) {
        this.context = context;
    }
    
    public final Context getContext() {
        return context;
    }
    
    public final int getContextID() {
        if (context != null) {
            return context.getContextId();
        } else {
            return 0;
        }
    }
    
    public final void setRecurrence(String rec_string) {
        this.rec_string = rec_string;
        if (rec_string != null) {
            b_recurrencestring = true;
        }
    }
    
    public final boolean containsRecurrenceString() {
        return b_recurrencestring;
    }
    
    public final String getRecurrence() {
        return rec_string;
    }
    
    public final void setDelExceptions(String delete_execptions) {
        if (delete_execptions != null) {
            super.setDeleteExceptions(CalendarCommonCollection.convertString2Dates(delete_execptions));
        } else {
            setDeleteExceptions(null);
        }
    }
    
    public final String getDelExceptions() {
        if (containsDeleteExceptions()) {
            return CalendarCommonCollection.convertDates2String(getDeleteException());
        } else {
            return null;
        }
    }
    
    public final void setExceptions(String change_exceptions) {
        if (change_exceptions != null) {
            super.setChangeExceptions(CalendarCommonCollection.convertString2Dates(change_exceptions));
        } else {
            setChangeExceptions(null);
        }
    }
    
    public final String getExceptions() {
        if (containsChangeExceptions()) {
            return CalendarCommonCollection.convertDates2String(getChangeException());
        } else {
            return null;
        }
    }
    
    public final boolean calculateRecurrence() throws OXException {
        if (isSequence(true)) {
            return CalendarRecurringCollection.fillDAO(this);
        }
        return false;
    }
    
    public final Date getUntil() {
        if (!containsUntil()) {
            if (getRecurrenceType() != CalendarObject.YEARLY) {
                return new Date(CalendarRecurringCollection.normalizeLong(getStartDate().getTime() + (CalendarRecurringCollection.MILLI_YEAR * CalendarRecurringCollection.getMAX_END_YEARS())));
            } else {
                return new Date(CalendarRecurringCollection.normalizeLong(getStartDate().getTime() + (CalendarRecurringCollection.MILLI_YEAR * 99)));
            }
        }
        return super.getUntil();
    }
    
    public final boolean isSequence(boolean what) {
        if (what) {
            if (containsRecurrenceString() || (containsRecurrenceType() && getRecurrenceType() > 0 && getInterval() > 0)) {
                return true;
            }
        } else {
            if (containsRecurrenceID() && containsRecurrenceType() && getRecurrenceType() > 0 && getInterval() > 0) {
                return true;
            }
        }
        return false;
    }
    
    public final boolean isSequence() {
        return isSequence(true);
    }
    
    public void setFolderMove(boolean folder_move) {
        this.folder_move = folder_move;
    }
    
    public boolean getFolderMove() {
        return folder_move;
    }
    
    public void setFolderMoveAction(int folder_move_action) {
        this.folder_move_action = folder_move_action;
    }
    
    public int getFolderMoveAction() {
        return folder_move_action;
    }
    
    public void setContainsResources(boolean contains_resources) {
        this.contains_resources = contains_resources;
    }
    
    public boolean containsResources() {
        return contains_resources;
    }
    
    public boolean isHardConflict() {
        return is_hard_conflict;
    }
    
    void setHardConflict() {
        is_hard_conflict = true;
    }
    
    
    
    public Object clone() {
        final CalendarDataObject clone = new CalendarDataObject();
        clone.setContext(getContext());
        if (containsTitle()) {
            clone.setTitle(getTitle());
        }
        if (containsLocation()) {
            clone.setLocation(getLocation());
        }
        if (containsStartDate()) {
            clone.setStartDate(getStartDate());
        }
        if (containsEndDate()) {
            clone.setEndDate(getEndDate());
        }
        if (containsCreationDate()) {
            clone.setCreatingDate(getCreatingDate());
        }
        if (containsCreatedBy()) {
            clone.setCreatedBy(getCreatedBy());
        }
        if (containsLastModified()) {
            clone.setLastModified(getLastModified());
        }
        if (containsModifiedBy()) {
            clone.setModifiedBy(getModifiedBy());
        }
        if (containsPrivateFlag()) {
            clone.setPrivateFlag(getPrivateFlag());
        }
        if (containsLabel()) {
            clone.setLabel(getLabel());
        }
        if (containsShownAs()) {
            clone.setShownAs(getShownAs());
        }
        if (containsNumberOfAttachments()) {
            clone.setNumberOfAttachments(getNumberOfAttachments());
        }
        if (containsNote()) {
            clone.setNote(getNote());
        }
        if (containsFullTime()) {
            clone.setFullTime(getFullTime());
        }
        if (containsCategories()) {
            clone.setCategories(getCategories());
        }
        if (containsRecurrenceID()) {
            clone.setRecurrenceID(getRecurrenceID());
        }
        if (containsRecurrenceType()) {
            clone.setRecurrenceType(getRecurrenceType());
        }
        if (containsInterval()) {
            clone.setInterval(getInterval());
        } 
        if (containsMonth()) {
            clone.setMonth(getMonth());
        } 
        if (containsDays()) {
            clone.setDays(getDays());
        }
        if (containsDayInMonth()) {
            clone.setDayInMonth(getDayInMonth());
        }
        if (containsUntil()) {
            clone.setUntil(getUntil());
        }
        if (containsOccurrence()) {
            clone.setOccurrence(getOccurrence());
        }        
        clone.setIgnoreConflicts(ignoreConflicts);
        clone.setUsers(getUsers());
        clone.setParticipants(getParticipants());        
        clone.setRecurrenceCalculator(getRecurrenceCalculator());
        clone.setRecurrence(getRecurrence());
        clone.setFolderType(getFolderType());
        clone.setTimezone(getTimezone());
        clone.setParentFolderID(getParentFolderID());
        clone.setPrivateFolderID(getPrivateFolderID());
        clone.setActionFolder(getActionFolder());
        clone.setContainsResources(containsResources());
        if (isHardConflict()) {
            clone.setHardConflict();
        }
        clone.setFolderMoveAction(getFolderMoveAction());
        clone.setFolderMove(getFolderMove());
        clone.setSharedFolderOwner(getSharedFolderOwner());
        
        
        return clone;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
	sb.append(getClass().getName());
        sb.append("@");
        sb.append("\n");
        sb.append("object_id - title - start - end : ");
        sb.append(getObjectID());
        sb.append(" - ");
        sb.append(getTitle());
        sb.append(" - ");
        sb.append(getStartDate());
        sb.append(" - ");
        sb.append(getEndDate());
        sb.append("\n");
        sb.append("context_id: ");
        sb.append(getContextID());
        sb.append("\n");
        sb.append("folder_information (parent:effective:action:type:move:move_action): ");
        sb.append(getParentFolderID());
        sb.append(":");
        sb.append(getEffectiveFolderId());
        sb.append(":");
        sb.append(getActionFolder());
        sb.append(":");
        sb.append(getFolderType());
        sb.append(":");
        sb.append(getFolderMove());
        sb.append(":");
        sb.append(getFolderMoveAction());
        sb.append(")");
        sb.append("\n");
        sb.append("recurrence: ");
        sb.append(getRecurrence());
        sb.append(" -- ");
        sb.append(getRecurrenceID());
        sb.append(" - ");
        sb.append(getRecurrenceType());
        sb.append(" - ");
        sb.append(getInterval());
        sb.append(" - ");
        sb.append(getDays());
        sb.append(" - ");
        sb.append(getMonth());
        sb.append(" - ");
        sb.append(getDayInMonth());
        return sb.toString();
    }    
    
}
