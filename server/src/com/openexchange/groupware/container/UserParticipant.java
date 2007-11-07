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
 * UserParticipant
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class UserParticipant implements Participant, Comparable {
	
	public static final int NO_PFID = -1;
	
	private int id = 0;
	
	private int pfid = NO_PFID;
	
	private int alarmMinutes = -1;
	
	private Date alarmDate = null;
	
	private int confirm = CalendarObject.NONE;
	
	private String confirmMessage = null;
	
	private String displayName = null;
	
	private String emailaddress = null;
	
	private boolean isModified = false;
	
	private boolean b_id = false;
	
	private boolean bAlarmMinutes = false;

	private boolean bAlarmDate = false;
	
	private boolean b_confirm = false;
	
	private boolean b_confirmMessage = false;
	
	private boolean b_displayName = false;
	
	private boolean b_emailaddress = false;
	
	/**
     * @deprecated User {@link #UserParticipant(int)}.
	 */
    public UserParticipant() {
	}

    /**
     * Default constructor.
     * @param id unique identifier of the user.
     */
    public UserParticipant(final int id) {
        super();
        this.id = id;
        b_id = true;
    }

	public void setPersonalFolderId(final int pfid) {
		this.pfid = pfid;
	}
	
	/**
     * @deprecated Use {@link #UserParticipant(int)}.
	 */
    public void setIdentifier(final int id) {
		this.id = id;
		b_id = true;
	}
	
	public int getIdentifier() {
		return id;
	}
	
	public boolean isModified() {
		return isModified;
	}
	
	public void setIsModified( final boolean isModified) {
		this.isModified = isModified;
	}
	
	public void setAlarmMinutes( final int alarmMinutes) {
		this.alarmMinutes = alarmMinutes;
		bAlarmMinutes = true;
	}
	
	public void setAlarmDate( final Date alarmDate) {
		this.alarmDate = alarmDate;
		bAlarmDate = true;
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
	
	public void setConfirm( final int confirm) {
		this.confirm = confirm;
		b_confirm = true;
	}
	
	public int getConfirm() {
		return confirm;
	}
	
	public void setConfirmMessage( final String confirmMessage) {
		this.confirmMessage = confirmMessage;
		b_confirmMessage = true;
	}
	
	public String getConfirmMessage() {
		return confirmMessage;
	}
	
	public void setDisplayName( final String displayName) {
		this.displayName = displayName;
		b_displayName = true;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getEmailAddress() {
		return emailaddress;
	}
	
	public void setEmailAddress( final String emailaddress) {
		this.emailaddress = emailaddress;
		b_emailaddress = true;
	}
	
	public boolean containsIdentifier() {
		return b_id;
	}
	
	public boolean containsAlarm() {
		return bAlarmMinutes;
	}
	
	public boolean containsAlarmDate() {
		return bAlarmDate;
	}
	
	public boolean containsConfirm() {
		return b_confirm;
	}
	
	public boolean containsConfirmMessage() {
		return b_confirmMessage;
	}
	
	public boolean containsDisplayName() {
		return b_displayName;
	}
	
	public boolean containsEmailAddress() {
		return b_emailaddress;
	}
	
	public void removeIdentifier() {
		id = 0;
		b_id = false;
	}
	
	public void removeAlarmMinutes() {
		alarmMinutes = 0;
		bAlarmMinutes = false;
	}
	
	public void removeAlarmDate() {
		alarmDate = null;
		bAlarmDate = false;
	}
	
	public void removeConfirm() {
		confirm = 0;
		b_confirm = false;
	}
	
	public void removeDisplayName() {
		displayName = null;
		b_displayName = false;
	}
	
	public void removeConfirmMessage() {
		confirmMessage = null;
		b_confirmMessage = false;
	}
	
	public void removeEmailAddress() {
		emailaddress = null;
		b_emailaddress = false;
	}
	
	public void reset() {
		id = 0;
		alarmMinutes = 0;
		confirm = 0;
		confirmMessage = null;
		displayName = null;
		emailaddress = null;
		
		b_id = false;
		bAlarmMinutes = false;
		b_confirm = false;
		b_confirmMessage = false;
		b_displayName = false;
		b_emailaddress = false;
	}
	
	public int getType() {
		return USER;
	}
	
	@Override
	public int hashCode() {
		return getHashString(getIdentifier(), getType(), getDisplayName(), getEmailAddress()).hashCode();
	}
	
	@Override
	public boolean equals(final Object o) {
		return (hashCode() == o.hashCode());
	}
	
	public int compareTo(final Object o) {
		final String s1 = getHashString(getIdentifier(), getType(), getDisplayName(), getEmailAddress());
		final String s2 = getHashString(((Participant)o).getIdentifier(), ((Participant)o).getType(), ((Participant)o).getDisplayName(), ((Participant)o).getEmailAddress());
		
		return s1.compareTo(s2);
	}
	
	private String getHashString(final int id, final int type, final String displayName, final String emailaddress) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append('I');
		stringBuilder.append(id);
		stringBuilder.append('T');
		stringBuilder.append(type);
		stringBuilder.append('D');
		stringBuilder.append(displayName);
		stringBuilder.append('E');
		stringBuilder.append(emailaddress);
		
		return stringBuilder.toString();
	}
}
