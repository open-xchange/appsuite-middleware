/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.calendar;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Differ;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;

/**
 * CalendarDataObject
 *
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


    /**
     * Checks if specified UTC date increases day in month if adding given time
     * zone's offset.
     *
     * @param millis
     *            The time millis
     * @param timeZoneID
     *            The time zone ID
     * @return <code>true</code> if specified date in increases day in month if
     *         adding given time zone's offset; otherwise <code>false</code>
     */
    private static boolean exceedsHourOfDay(final long millis, final String timeZoneID) {
        return exceedsHourOfDay(millis, getTimeZone(timeZoneID));
    }

    private static final Map<String, TimeZone> zoneCache = new ConcurrentHashMap<String, TimeZone>();

    private static TimeZone getTimeZone(final String ID) {
        TimeZone zone = zoneCache.get(ID);
        if (zone == null) {
            zone = TimeZone.getTimeZone(ID);
            zoneCache.put(ID, zone);
        }
        return zone;
    }

    /**
     * Checks if specified UTC date increases day in month if adding given time
     * zone's offset.
     *
     * @param millis
     *            The time millis
     * @param zone
     *            The time zone
     * @return <code>true</code> if specified date in increases day in month if
     *         adding given time zone's offset; otherwise <code>false</code>
     */
    private static boolean exceedsHourOfDay(final long millis, final TimeZone zone) {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(millis);
        final long hours = cal.get(Calendar.HOUR_OF_DAY) + (zone.getOffset(millis) / Constants.MILLI_HOUR);
        return hours >= 24 || hours < 0;
    }

    @Override
    public final void setUntil(final Date until) {
        if (until != null) {
            final long mod = until.getTime() % Constants.MILLI_DAY;
            if (mod != 0) {
                String tzn = getTimezone();
                if (tzn == null) {
                    tzn = "UTC";
                }
                if (exceedsHourOfDay(until.getTime(), tzn)) {
                    until.setTime((((until.getTime() - mod) + Constants.MILLI_DAY)));
                } else {
                    until.setTime(until.getTime() - mod);
                }
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
            /* System.out.println("FIX ME AND PROVIDE A FOLDER  :"+StringCollection.getStackAsString()); */ // TODO: Remove me
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
            super.setDeleteExceptions(convertString2Dates(delete_execptions));
        } else {
            setDeleteExceptions((Date[]) null);
        }
    }

    public final String getDelExceptions() {
        if (containsDeleteExceptions()) {
            return convertDates2String(getDeleteException());
        }
        return null;
    }

    private Date[] convertString2Dates(final String s) {
        if (s == null) {
            return null;
        } else if (s.length() == 0) {
            return new Date[0];
        }
        final String[] sa = Strings.splitByComma(s);
        final Date dates[] = new Date[sa.length];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = new Date(Long.parseLong(sa[i]));
        }
        return dates;
    }

    private String convertDates2String(final Date[] d) {
        if (d == null || d.length == 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(d.length << 4);
        Arrays.sort(d);
        sb.append(d[0].getTime());
        for (int i = 1; i < d.length; i++) {
            sb.append(',').append(d[i].getTime());
        }
        return sb.toString();
    }

    public final void setExceptions(final String changeExceptions) {
        if (changeExceptions != null) {
            super.setChangeExceptions(convertString2Dates(changeExceptions));
        } else {
            setChangeExceptions((Date[]) null);
        }
    }

    public final String getExceptions() {
        if (containsChangeExceptions()) {
            return convertDates2String(getChangeException());
        }
        return null;
    }

    public final boolean calculateRecurrence() throws OXException {
        if (isSequence()) {
            return CalendarCollectionUtils.fillDAO(this);
        }
        return false;
    }

    @Override
    public final Date getUntil() {
        if (!containsUntil()) {
            /*
             * Determine max. end date
             */
            return CalendarCollectionUtils.getMaxUntilDate(this);
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
