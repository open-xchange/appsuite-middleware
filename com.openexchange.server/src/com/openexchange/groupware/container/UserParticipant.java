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

package com.openexchange.groupware.container;

import java.util.Date;

/**
 * {@link UserParticipant} - Represents an internal user participant.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class UserParticipant implements Participant, Comparable<Participant> {

    private static final long serialVersionUID = -1585185796529773961L;

    /**
     * The constant to indicate no available participant identifier: <code>-1</code>.
     */
    public static final int NO_PFID = -1;

    private int id = NO_ID;

    private int pfid = NO_PFID;

    private int alarmMinutes = -1;

    private Date alarmDate;

    private int confirm = CalendarObject.NONE;

    private String confirmMessage;

    private String displayName;

    private String emailaddress;

    private boolean isModified;

    private boolean bAlarmMinutes;

    private boolean b_confirm;

    private boolean b_confirmMessage;

    private boolean ignoreNotification;

    /**
     * @deprecated User {@link #UserParticipant(int)}.
     */
    @Deprecated
    public UserParticipant() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param id unique identifier of the user.
     */
    public UserParticipant(final int id) {
        super();
        this.id = id;
    }

    public void setPersonalFolderId(final int pfid) {
        this.pfid = pfid;
    }

    /**
     * @deprecated Use {@link #UserParticipant(int)}.
     */
    @Override
    @Deprecated
    public void setIdentifier(final int id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentifier() {
        return id;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setIsModified(final boolean isModified) {
        this.isModified = isModified;
    }

    public void setAlarmMinutes(final int alarmMinutes) {
        this.alarmMinutes = alarmMinutes;
        bAlarmMinutes = true;
    }

    public void setAlarmDate(final Date alarmDate) {
        this.alarmDate = alarmDate;
    }

    public int getPersonalFolderId() {
        return pfid;
    }

    public int getAlarmMinutes() {
        return alarmMinutes;
    }

    public Date getAlarmDate() {
        return alarmDate;
    }

    public void setConfirm(final int confirm) {
        this.confirm = confirm;
        b_confirm = true;
    }

    public int getConfirm() {
        return confirm;
    }

    public void removeConfirm() {
        this.confirm = CalendarObject.NONE;
        b_confirm = false;
    }

    public void setConfirmMessage(final String confirmMessage) {
        this.confirmMessage = confirmMessage;
        b_confirmMessage = true;
    }

    public String getConfirmMessage() {
        return confirmMessage;
    }

    public void removeConfirmMessage() {
        this.confirmMessage = null;
        b_confirmMessage = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailAddress() {
        return emailaddress == null ? null : emailaddress.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    public void setEmailAddress(final String emailaddress) {
        this.emailaddress = emailaddress == null ? null : emailaddress.toLowerCase();
    }

    public boolean containsAlarm() {
        return bAlarmMinutes;
    }

    public boolean containsConfirm() {
        return b_confirm;
    }

    public boolean containsConfirmMessage() {
        return b_confirmMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() {
        return USER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + USER;
        result = prime * result + id;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof UserParticipant)) {
            return false;
        }
        final UserParticipant other = (UserParticipant) obj;
        return id == other.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Participant part) {
        final int retval;
        if (USER == part.getType()) {
            retval = Integer.compare(id, part.getIdentifier());
        } else {
            retval = Integer.compare(USER, part.getType());
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("user id:");
        sb.append(getIdentifier());
        return sb.toString();
    }

    @Override
    public UserParticipant clone() throws CloneNotSupportedException {
        final UserParticipant clone = (UserParticipant) super.clone();
        clone.alarmDate = (Date) (alarmDate == null ? null : alarmDate.clone());
        clone.alarmMinutes = alarmMinutes;
        clone.b_confirm = b_confirm;
        clone.b_confirmMessage = b_confirmMessage;
        clone.bAlarmMinutes = bAlarmMinutes;
        clone.confirm = confirm;
        clone.confirmMessage = confirmMessage;
        clone.displayName = displayName;
        clone.emailaddress = emailaddress;
        clone.id = id;
        clone.ignoreNotification = ignoreNotification;
        clone.isModified = isModified;
        clone.pfid = pfid;
        return clone;
    }

    @Override
    public UserParticipant getClone() throws CloneNotSupportedException {
        return clone();
    }

    @Override
    public boolean isIgnoreNotification() {
        return ignoreNotification;
    }

    @Override
    public void setIgnoreNotification(final boolean ignoreNotification) {
        this.ignoreNotification = ignoreNotification;
    }
}
