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
import java.util.List;

/**
 * CalendarObject
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public abstract class CalendarObject extends CommonObject
{
	
	public static final int TITLE = 200;
	
	public static final int START_DATE = 201;
	
	public static final int END_DATE = 202;
	
	public static final int NOTE = 203;
	
	public static final int ALARM = 204;
	
	public static final int RECURRENCE_ID = 206;
		
	public static final int RECURRENCE_POSITION = 207;
	
	public static final int RECURRENCE_DATE_POSITION = 208;
	
	public static final int RECURRENCE_TYPE = 209;
	
	public static final int CHANGE_EXCEPTIONS = 210;
	
	public static final int DELETE_EXCEPTIONS = 211;
	
	public static final int DAYS = 212;
	
	public static final int DAY_IN_MONTH = 213;
	
	public static final int MONTH = 214;
	
	public static final int INTERVAL = 215;
	
	public static final int UNTIL = 216;
	
	public static final int NOTIFICATION = 217;
	
	public static final int RECURRENCE_CALCULATOR = 218;
	
	public static final int PARTICIPANTS = 220;
	
	public static final int USERS = 221;

    public static final int RECURRING_OCCURRENCE = 403;

    /**
     * Attribute number indicating the times an object in a recurrence has to
     * occur.
     */
    public static final int RECURRENCE_COUNT = 222;

	public static final int NONE = 0;
	public static final int ACCEPT = 1;
	public static final int DECLINE = 2;
	public static final int TENTATIVE = 3;
	
	public static final int SUNDAY = 1;
	public static final int MONDAY = 2;
	public static final int TUESDAY = 4;
	public static final int WEDNESDAY = 8;
	public static final int THURSDAY = 16;
	public static final int FRIDAY = 32;
	public static final int SATURDAY = 64;
	
	public static final int DAY = 127;
	public static final int WEEKDAY = 62;
	public static final int WEEKENDDAY = 65;
	
	public static final int NO_RECURRENCE = 0;
    public static final int DAILY = 1;
	public static final int WEEKLY = 2;
	public static final int MONTHLY = 3;
	public static final int YEARLY = 4;
		
	protected Participant participants[] = null;
	protected UserParticipant[] users = null;
	
	protected String title = null;
	protected Date start_date = null;
	protected Date end_date = null;
	protected String note = null;
	protected int recurrence_id = 0;
	protected int recurrence_position = 0;
	protected Date recurrence_date_position = null;
	protected int recurrence_type = NO_RECURRENCE;
	protected Date[] change_exceptions = null;
	protected Date[] delete_exceptions = null;
	protected int days = 0;
	protected int day_in_month = 0;
	protected int month = 0;
	protected int interval = 0;
	protected Date until = null;
	protected boolean notification = false;
	protected int recurrence_calculator = 0;
	protected boolean alarmFlag = false;
	protected int occurrence = 0;
    
    /**
     * The end of a recurrence can be defined either through the until date or
     * through this count value. If this count value is used the object must
     * occur count times until the recurrence ends.
     */
    protected int recurrence_count = 0;
	
	protected int confirm = 0;
	protected String confirmMessage = null;
	
	protected boolean b_title = false;
	protected boolean b_start_date = false;
	protected boolean b_end_date = false;
	protected boolean b_note = false;
	protected boolean b_recurrence_id = false;
	protected boolean b_recurrence_position = false;
	protected boolean b_recurrence_date_position = false;

    /**
     * Indicates that the attribute <code>recurrence_count</code> has been set
     * or is empty.
     */
    protected boolean b_recurrence_count = false;

	protected boolean b_change_exceptions = false;
	protected boolean b_delete_exceptions = false;
	protected boolean b_days = false;
	protected boolean b_day_in_month = false;
	protected boolean b_month = false;
	protected boolean b_interval = false;
	protected boolean b_recurrence_type = false;
	protected boolean b_until = false;
	protected boolean b_notification = false;
	
	protected boolean b_participants = false;
	protected boolean b_users = false;
	
	protected boolean b_confirm = false;
	protected boolean b_confirmMessage = false;
	protected boolean b_occurrence = false;

	// GET METHODS
	public String getTitle ( ) 
	{
		return title;
	}
	
	public Date getStartDate( ) {
		return start_date;
	}
	
	public Date getEndDate( ) {
		return end_date;
	}
	
	public String getNote ( ) {
		return note;
	}
	
	public int getRecurrenceID( ) {
		return recurrence_id;
	}
	
	public int getRecurrencePosition( ) {
		return recurrence_position;
	}
	
	public Date getRecurrenceDatePosition( ) {
		return recurrence_date_position;
	}
	
	public int getRecurrenceType( ) {
		return recurrence_type;
	}
	
	public Date[] getChangeException( ) {
		return change_exceptions;
	}
	
	public Date[] getDeleteException( ) {
		return delete_exceptions;
	}
	
	public int getDays( ) {
		return days;
	}
	
	public int getDayInMonth( ) {
		return day_in_month;
	}
	
	public int getMonth( ) {
		return month;
	}

	public int getInterval( ) {
		return interval;
	}	
	
	public Date getUntil( ) {
		return until;
	}
	
	public boolean getNotification( ) {
		return notification;
	}
	
	public int getRecurrenceCalculator() {
		return recurrence_calculator;
	}
	
	public void setRecurrenceCalculator(final int recurrence_calculator) {
		this.recurrence_calculator = recurrence_calculator;
	}
	
	public int getConfirm() {
		return confirm;
	}
	
	public String getConfirmMessage() {
		return confirmMessage;
	}
	
	public boolean getAlarmFlag() {
		return alarmFlag;
	}
	
	public int getOccurrence() {
		return occurrence;
	}
	
	// SET METHODS
	public void setTitle( final String title ) {
		this.title = title;
		b_title = true;
	}
	
	public void setStartDate( final Date start_date ) {
		this.start_date = start_date;
		b_start_date = true;
	}
	
	public void setEndDate( final Date end_date ) {
		this.end_date = end_date;
		b_end_date = true;
	}
	
	public void setNote ( final String note ) {
		this.note = note;
		b_note = true;
	}
	
	public void setRecurrenceID( final int recurrence_id) {
		this.recurrence_id = recurrence_id;
		b_recurrence_id = true;
	}
	
	public void setRecurrencePosition( final int recurrence_position ) {
		this.recurrence_position = recurrence_position;
		b_recurrence_position = true;
	}
	
	public void setRecurrenceDatePosition( final Date recurrence_date_position ) {
		this.recurrence_date_position = recurrence_date_position;
		b_recurrence_date_position = true;
	}
	
	public void setRecurrenceType( final int recurrence_type ) {
		this.recurrence_type = recurrence_type;
		b_recurrence_type = true;
	}
	
	public void setChangeExceptions( final Date[] change_exceptions ) {
		this.change_exceptions = change_exceptions;
		b_change_exceptions = true;
	}
	
	public void addChangeException( final Date change_exception ) {
		if (this.change_exceptions == null) {
			setChangeExceptions(new Date[] { change_exception });
		} else {
			Date[] tmp = change_exceptions;
			change_exceptions = new Date[tmp.length + 1];
			System.arraycopy(tmp, 0, change_exceptions, 0, tmp.length);
			tmp = null;
			change_exceptions[change_exceptions.length - 1] = change_exception;
		}
	}
	
	public void setDeleteExceptions( final Date[] delete_exceptions ) {
		this.delete_exceptions = delete_exceptions;
		b_delete_exceptions = true;
	}
	
	public void addDeleteException( final Date delete_exception ) {
		if (this.delete_exceptions == null) {
			setDeleteExceptions(new Date[] { delete_exception });
		} else {
			Date[] tmp = delete_exceptions;
			delete_exceptions = new Date[tmp.length + 1];
			System.arraycopy(tmp, 0, delete_exceptions, 0, tmp.length);
			tmp = null;
			delete_exceptions[delete_exceptions.length - 1] = delete_exception;
		}
	}
	
	public void setDays( final int days ) {
		this.days = days;
		b_days = true;
	}
	
	public void setDayInMonth( final int day_in_month) {
		this.day_in_month = day_in_month;
		b_day_in_month = true;
	}
	
	public void setMonth(final int month ) {
		this.month = month;
		b_month = true;
	}

	public void setInterval(final int interval ) {
		this.interval = interval;
		b_interval = true;
	}
	
	public void setUntil(final Date until ) {
		this.until = until;
		b_until = true;
	}
	
	public void setNotification(final boolean notification ) {
		this.notification = notification;
		b_notification = true;
	}
	
	public void setConfirm(final int confirm) {
		this.confirm = confirm;
		b_confirm = true;
	}
	
	public void setConfirmMessage(final String confirmMessage) {
		this.confirmMessage = confirmMessage;
		b_confirmMessage = true;
	}
	
	public void setAlarmFlag(final boolean alarmFlag) {
		this.alarmFlag = alarmFlag;
	}
	
	public void setOccurrence(final int occurrence) {
		this.occurrence = occurrence;
                b_occurrence = true;
	}
	
	// REMOVE METHODS
	public void removeTitle( ) {
		title = null;
		b_title = false;
	}
	
	public void removeStartDate( ) {
		start_date = null;
		b_start_date = false;
	}
	
	public void removeEndDate( ) {
		end_date = null;
		b_end_date = false;
	}
	
	public void removeNote ( ) {
		note = null;
		b_note = false;
	}
	
	public void removeRecurrenceID( ) {
		recurrence_id = 0;
		b_recurrence_id = false;
	}
	
	public void removeRecurrencePosition( ) {
		recurrence_position = 0;
		b_recurrence_position = false;
	}
	
	public void removeRecurrenceDatePosition( ) {
		recurrence_date_position = null;
		b_recurrence_date_position = false;
	}
	
	public void removeRecurrenceType( ) {
		recurrence_type = 0;
		b_recurrence_type = false;
	}
	
	public void removeChangeExceptions( ) {
		this.change_exceptions = null;
		b_change_exceptions = false;
	}
	
	public void removeDeleteExceptions( ) {
		delete_exceptions = null;
		b_delete_exceptions = false;
	}
	
	public void removeDays( ) {
		days = 0;
		b_days = false;
	}
	
	public void removeDayInMonth( ) {
		day_in_month = 0;
		b_day_in_month = false;
	}
	
	public void removeMonth( ) {
		this.month = 0;
		b_month = false;
	}
	
	public void removeInterval( ) {
		interval = 0;
		b_interval = false;
	}

	public void removeUntil( ) {
		until = null;
		b_until = false;
	}
	
	public void removeNotification( ) {
		notification = false;
		b_notification = false;
	}
	
	public void removeConfirm() {
		confirm = 0;
		b_confirm = false;
	}
	
	public void removeConfirmMessage() {
		confirmMessage = null;
		b_confirmMessage = false;
	}
	
	public void removeOccurrence() {
		occurrence = 0;
		b_occurrence = false;
	}
	
	// CONTAINS METHODS
	public boolean containsTitle( ) {
		return b_title;
	}
	
	public boolean containsStartDate( ) {
		return b_start_date;
	}
	
	public boolean containsEndDate( ) {
		return b_end_date;
	}
	
	public boolean containsNote ( ) {
		return b_note;
	}
	
	public boolean containsRecurrenceID( ) {
		return b_recurrence_id;
	}
	
	public boolean containsRecurrencePosition( ) {
		return b_recurrence_position;
	}
	
	public boolean containsRecurrenceDatePosition( ) {
		return b_recurrence_date_position;
	}
	
	public boolean containsRecurrenceType( ) {
		return b_recurrence_type;
	}
	
	public boolean containsChangeExceptions( ) {
		return b_change_exceptions;
	}
	
	public boolean containsDeleteExceptions( ) {
		return b_delete_exceptions;
	}
	
	public boolean containsDays( ) {
		return b_days;
	}
	
	public boolean containsDayInMonth( ) {
		return b_day_in_month;
	}
	
	public boolean containsMonth( ) {
		return b_month;
	}
	
	public boolean containsInterval( ) {
		return b_interval;
	}
	
	public boolean containsUntil( ) {
		return b_until;
	}
	
	public boolean containsNotification( ) {
		return b_notification;
	}
	
	public boolean containsConfirm() {
		return b_confirm;
	}
	
	public boolean containsConfirmMessage() {
		return b_confirmMessage;
	}
	
	public boolean containsOccurrence() {
		return b_occurrence;
	}
	
	public void setParticipants(final Participant[] participants) {
		this.participants = participants;
        b_participants = true;
	}
	
	public void setParticipants(final List<? extends Participant> participants) {
		this.participants = participants.toArray(new Participant[participants.size()]);
        b_participants = true;
	}
	
	public Participant[] getParticipants() {
		return participants;
	}
	
	public void removeParticipants() {
		participants = null;
		b_participants = false;		
	}
	
	public boolean containsParticipants() {
		return b_participants;
	}
	
	public void addParticipant(final Participant p) {
		if (this.participants == null) {
			setParticipants(new Participant[] { p });
		} else {
			final int newLength = this.participants.length + 1;
			Participant[] tmp = this.participants;
			this.participants = new Participant[newLength];
			System.arraycopy(tmp, 0, this.participants, 0, tmp.length);
			tmp = null;
			this.participants[newLength - 1] = p;
		}
	}
	
	public void setUsers(final UserParticipant[] users) {
		this.users = users;
		b_users = true;
	}
	
	public void setUsers(final List<UserParticipant> users) {
		this.users = users.toArray(new UserParticipant[users.size()]);
		b_users = true;
	}

	
	public UserParticipant[] getUsers() {
		return users;
	}
	
	public void removeUsers() {
		users = null;
		b_users = false;		
	}
	
	public boolean containsUserParticipants() {
		return b_users;
	}
	
	@Override
	public void reset() {
		super.reset();
		
		title = null;
		start_date = null;
		end_date = null;
		recurrence_id = 0;
		recurrence_position = 0;
		recurrence_date_position = null;
		change_exceptions = null;
		delete_exceptions = null;
		days = 0;
		day_in_month = 0;
		month = 0;
		until = null;
        recurrence_count = 0;
		notification = false;
		participants = null;
		users = null;
		occurrence = 0;

		b_title = false;
		b_start_date = false;
		b_end_date = false;
		b_recurrence_id = false;
		b_recurrence_position = false;
		b_recurrence_date_position = false;
		b_change_exceptions = false;
		b_delete_exceptions = false;
		b_days = false;
		b_day_in_month = false;
		b_month = false;
		b_until = false;
        b_recurrence_count = false;
		b_notification = false;
		b_participants = false;
		b_users = false;
		b_occurrence = false;
	}

    /**
     * @return the recurrence count.
     */
    public int getRecurrenceCount() {
        return recurrence_count;
    }

    /**
     * @param recurrenceCount the recurrence count to set.
     */
    public void setRecurrenceCount(final int recurrenceCount) {
        this.recurrence_count = recurrenceCount;
        b_recurrence_count = true;
    }

    /**
     * Removes the value of the recurrence count.
     */
    public void removeRecurrenceCount() {
        this.recurrence_count = 0;
        b_recurrence_count = false;
    }

    /**
     * @return if the attribute <code>recurrence_count</code> has been set or
     * not.
     */
    public boolean containsRecurrenceCount() {
        return b_recurrence_count;
    }
}
