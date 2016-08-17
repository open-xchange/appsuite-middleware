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

package com.openexchange.groupware.calendar;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Differ;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * CalendarDataObject
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class CalendarDataObject extends Appointment {

    private static final long serialVersionUID = -1333032651882959872L;

    private String rec_string;

    private Context context;
    private int folder_type = -1;
    private int pfid, action_folder;
    private int shared_folder_owner;
    private int folder_move_action;

    private boolean is_hard_conflict;

    private boolean contains_resources;
    private boolean folder_move;
    private boolean b_recurrencestring;

    private boolean fill_participants;
    private boolean fill_user_participants;
    private boolean fill_folder_id;
    private boolean fillConfirmations;
    private boolean fillLastModifiedOfNewestAttachment;

    private boolean externalOrganizer;


    private CalendarCollectionService getCalendarCollectionService(){
    	return ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
    }

    @Override
    public final void setUntil(final Date until) {
        if (until != null) {
            final long mod = until.getTime() % Constants.MILLI_DAY;
            if (mod != 0) {
                if (getCalendarCollectionService().exceedsHourOfDay(until.getTime(), getTimezoneFallbackUTC())) {
                    until.setTime((((until.getTime() - mod) + Constants.MILLI_DAY)));
                } else {
                    until.setTime(until.getTime() - mod);
                }
//                until = new Date(((until.getTime()-mod)+CalendarRecurringCollection.MILLI_DAY));
            }
            super.setUntil(until);
        } else {
            super.setUntil(until);
        }
    }

    @Override
    public void setOccurrence(final int occurrence) {
        if (occurrence == 0) {
            setUntil(null);
        }
        super.setOccurrence(occurrence);
    }

    public final void setSharedFolderOwner(final int shared_folder_owner) {
        this.shared_folder_owner = shared_folder_owner;
    }

    public final int getSharedFolderOwner() {
        return shared_folder_owner;
    }

    public final void setFolderType(final int folder_type) {
        this.folder_type = folder_type;
    }

    public final int getFolderType() {
        return folder_type;
    }

    public final void setActionFolder(final int action_folder) {
        this.action_folder = action_folder;
    }

    public final int getActionFolder() {
        return action_folder;
    }

    public final void setGlobalFolderID(final int fid) {
        super.setParentFolderID(fid);
    }

    public final int getGlobalFolderID() {
        return getParentFolderID();
    }

    public final void setPrivateFolderID(final int pfid) {
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
            /*System.out.println("FIX ME AND PROVIDE A FOLDER  :"+StringCollection.getStackAsString());*/ // TODO: Remove me
            return 0;
        }
    }

    public final void setContext(final Context context) {
        this.context = context;
    }

    public final Context getContext() {
        return context;
    }

    public final int getContextID() {
        if (context != null) {
            return context.getContextId();
        }
        return 0;
    }

    public final void setRecurrence(final String rec_string) {
        this.rec_string = rec_string;
        b_recurrencestring = true;
    }

    public final boolean containsRecurrenceString() {
        return b_recurrencestring;
    }

    public void removeRecurrenceString() {
        this.rec_string = null;
        b_recurrencestring = false;
    }

    public final String getRecurrence() {
        return rec_string;
    }

    public final void setDelExceptions(final String delete_execptions) {
        if (delete_execptions != null) {
            super.setDeleteExceptions(getCalendarCollectionService().convertString2Dates(delete_execptions));
        } else {
            setDeleteExceptions((Date[])null);
        }
    }

    public final String getDelExceptions() {
        if (containsDeleteExceptions()) {
            return getCalendarCollectionService().convertDates2String(getDeleteException());
        }
        return null;
    }

    public final void setExceptions(final String changeExceptions) {
        if (changeExceptions != null) {
            final CalendarCollectionService collectionService = getCalendarCollectionService();
            if (null != collectionService) {
                super.setChangeExceptions(collectionService.convertString2Dates(changeExceptions));
            }
        } else {
            setChangeExceptions((Date[])null);
        }
    }

    public final String getExceptions() {
        if (containsChangeExceptions()) {
            return getCalendarCollectionService().convertDates2String(getChangeException());
        }
        return null;
    }

    public final boolean calculateRecurrence() throws OXException {
        if (isSequence()) {
            return getCalendarCollectionService().fillDAO(this);
        }
        return false;
    }

    @Override
    public final Date getUntil() {
        if (!containsUntil()) {
            /*
             * Determine max. end date
             */
            return getCalendarCollectionService().getMaxUntilDate(this);
        }
        return super.getUntil();
    }

    /**
     * Checks if this calendar data object denotes a recurring appointment
     *
     * @param what <code>true</code> to check by recurrence pattern or recurrence type; otherwise <code>false</code> to check by recurrence ID and recurrence type
     * @return <code>true</code> if this calendar data object denotes a recurring appointment; otherwise <code>false</code>
     */
    public final boolean isSequence(final boolean what) {
        if (what) {
            return ((containsRecurrenceString() && getRecurrence() != null) || (containsRecurrenceType() && getRecurrenceType() > 0 && getInterval() > 0));
        }
        return (containsRecurrenceID() && containsRecurrenceType() && getRecurrenceType() > 0 && getInterval() > 0);
    }

    public final boolean isSequence() {
        return isSequence(true);
    }

    public void setFolderMove(final boolean folder_move) {
        this.folder_move = folder_move;
    }

    public boolean getFolderMove() {
        return folder_move;
    }

    public void setFolderMoveAction(final int folder_move_action) {
        this.folder_move_action = folder_move_action;
    }

    public int getFolderMoveAction() {
        return folder_move_action;
    }

    public void setContainsResources(final boolean contains_resources) {
        this.contains_resources = contains_resources;
    }

    public boolean containsResources() {
        return contains_resources;
    }

    public boolean isHardConflict() {
        return is_hard_conflict;
    }

    public void setHardConflict() {
        is_hard_conflict = true;
    }

     public void setFillParticipants() {
        fill_participants = true;
    }

     public boolean fillParticipants() {
        return fill_participants;
    }

    public void setFillConfirmations() {
        fillConfirmations = true;
    }

    public boolean fillConfirmations() {
        return fillConfirmations;
    }

     public void setFillUserParticipants() {
        fill_user_participants = true;
    }

     public boolean fillUserParticipants() {
        return fill_user_participants;
    }
     public void setFillFolderID() {
        fill_folder_id = true;
    }

     public boolean fillFolderID() {
        return fill_folder_id;
    }

    public boolean isFillLastModifiedOfNewestAttachment() {
        return fillLastModifiedOfNewestAttachment;
    }

    public void setFillLastModifiedOfNewestAttachment(boolean fillLastModifiedOfNewestAttachment) {
        this.fillLastModifiedOfNewestAttachment = fillLastModifiedOfNewestAttachment;
    }

    @Override
    public CalendarDataObject clone() {
        final CalendarDataObject clone = (CalendarDataObject) super.clone();
        clone.setContext(getContext());
        if (containsObjectID()) {
            clone.setObjectID(getObjectID());
        }
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
        if (containsCreatedBy()) {
            clone.setCreatedBy(getCreatedBy());
        }
        if (containsCreationDate()) {
            clone.setCreationDate((Date) getCreationDate().clone());
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
        if (containsRecurrencePosition()) {
            clone.setRecurrencePosition(getRecurrencePosition());
        }
        if (containsRecurrenceType()) {
            clone.setRecurrenceType(getRecurrenceType());
        }
        if (containsRecurringStart()) {
            clone.setRecurringStart(getRecurringStart());
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
        if (containsChangeExceptions()) {
            clone.setChangeExceptions(copy(getChangeException()));
        }
        if (containsDeleteExceptions()) {
            clone.setDeleteExceptions(copy(getDeleteException()));
        }
        if (containsNotification()) {
            clone.setNotification(getNotification());
        }
        clone.setIgnoreConflicts(ignoreConflicts);
        clone.setRecurrenceCalculator(getRecurrenceCalculator());
        if (containsRecurrenceString()) {
            clone.setRecurrence(getRecurrence());
        }
        clone.setFolderType(getFolderType());
        if (containsTimezone()) {
            clone.setTimezone(getTimezone());
        }
        clone.setParentFolderID(getParentFolderID());
        clone.setPrivateFolderID(getPrivateFolderID());
        clone.setActionFolder(getActionFolder());
        clone.setFolderType(getFolderType());
        clone.setContainsResources(containsResources());
        if (isHardConflict()) {
            clone.setHardConflict();
        }
        clone.setFolderMoveAction(getFolderMoveAction());
        clone.setFolderMove(getFolderMove());
        clone.setSharedFolderOwner(getSharedFolderOwner());


        return clone;
    }

    public boolean isExternalOrganizer() {
		return externalOrganizer;
	}

	public void setExternalOrganizer(boolean externalOrganizer) {
		this.externalOrganizer = externalOrganizer;
	}


    private static final Date[] copy(final Date[] copyMe) {
        if (copyMe == null) {
            return null;
        }
        final Date[] clone = new Date[copyMe.length];
        for (int i = 0; i < clone.length; i++) {
            final Date cur = copyMe[i];
            clone[i] = (Date) (cur == null ? null : cur.clone());
        }
        return clone;
    }

    private static final String STR_DELIM = " - ";

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append('@');
        sb.append('\n');
        sb.append("object_id - title - start - end : ");
        sb.append(getObjectID());
        sb.append(STR_DELIM);
        sb.append(getTitle());
        sb.append(STR_DELIM);
        sb.append(getStartDate());
        sb.append(STR_DELIM);
        sb.append(getEndDate());
        sb.append('\n');
        sb.append("context_id: ");
        sb.append(getContextID());
        sb.append('\n');
        sb.append("folder_information (parent:effective:action:type:move:move_action): ");
        sb.append(getParentFolderID());
        sb.append(':');
        sb.append(getEffectiveFolderId());
        sb.append(':');
        sb.append(getActionFolder());
        sb.append(':');
        sb.append(getFolderType());
        sb.append(':');
        sb.append(getFolderMove());
        sb.append(':');
        sb.append(getFolderMoveAction());
        sb.append(')');
        sb.append('\n');
        sb.append("recurrence: ");
        sb.append(getRecurrence());
        sb.append(" -- ");
        sb.append(getRecurrenceID());
        sb.append(STR_DELIM);
        sb.append(getRecurrenceType());
        sb.append(STR_DELIM);
        sb.append(getInterval());
        sb.append(STR_DELIM);
        sb.append(getDays());
        sb.append(STR_DELIM);
        sb.append(getMonth());
        sb.append(STR_DELIM);
        sb.append(getDayInMonth());
        return sb.toString();
    }

    public static Set<Differ<? super CalendarDataObject>> differ = new HashSet<Differ<? super CalendarDataObject>>();

    static {
        differ.addAll(Appointment.differ);
    }
}
