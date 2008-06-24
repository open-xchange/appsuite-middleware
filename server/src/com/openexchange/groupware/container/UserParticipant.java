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

package com.openexchange.groupware.container;

import java.util.Date;

/**
 * Internal user participant.
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class UserParticipant implements Participant, Comparable<Participant> {

    public static final int NO_PFID = -1;

    private int id = NO_ID;

    private int pfid = NO_PFID;

    private int alarmMinutes = -1;

    private Date alarmDate = null;

    private int confirm = CalendarObject.NONE;

    private String confirmMessage = null;

    private String displayName = null;

    private String emailaddress = null;

    private boolean isModified = false;

    private boolean bAlarmMinutes = false;

    private boolean b_confirm = false;

    private boolean b_confirmMessage = false;

    /**
     * @deprecated User {@link #UserParticipant(int)}.
     */
    @Deprecated
    public UserParticipant() {
        super();
    }

    /**
     * Default constructor.
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
    @Deprecated
    public void setIdentifier(final int id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
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

    public void setConfirmMessage(final String confirmMessage) {
        this.confirmMessage = confirmMessage;
        b_confirmMessage = true;
    }

    public String getConfirmMessage() {
        return confirmMessage;
    }

    /**
     * {@inheritDoc}
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * {@inheritDoc}
     */
    public String getEmailAddress() {
        return emailaddress;
    }

    /**
     * {@inheritDoc}
     */
    public void setEmailAddress(final String emailaddress) {
        this.emailaddress = emailaddress;
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
    public int compareTo(final Participant part) {
        final int retval;
        if (USER == part.getType()) {
            retval = Integer.valueOf(id).compareTo(Integer.valueOf(part.getIdentifier()));
        } else {
            retval = Integer.valueOf(USER).compareTo(Integer.valueOf(part.getType()));
        }
        return retval;
    }
}
