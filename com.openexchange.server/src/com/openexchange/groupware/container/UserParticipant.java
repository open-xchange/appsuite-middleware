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
            retval = Integer.valueOf(id).compareTo(Integer.valueOf(part.getIdentifier()));
        } else {
            retval = Integer.valueOf(USER).compareTo(Integer.valueOf(part.getType()));
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
