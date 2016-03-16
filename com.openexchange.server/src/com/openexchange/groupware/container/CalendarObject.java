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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * CalendarObject
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public abstract class CalendarObject extends CommonObject {

    private static final long serialVersionUID = 8108851156436746900L;

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

	/**
	 * Attribute number indicating the times an object in a recurrence has to
	 * occur.
	 */
	public static final int RECURRENCE_COUNT = 222;

	public static final int ORGANIZER = 224;

	public static final int SEQUENCE = 225;

	public static final int CONFIRMATIONS = 226;

	public static final int ORGANIZER_ID = 227;

	public static final int PRINCIPAL = 228;

	public static final int PRINCIPAL_ID = 229;

    public static final int FULL_TIME = 401;

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

	protected Participant participants[];

	protected UserParticipant[] users;

	protected ConfirmableParticipant[] confirmations;

	protected String title;

	protected Date start_date;

	protected Date end_date;

	protected String note;

	protected int recurrence_id;

	protected int recurrence_position;

	protected Date recurrence_date_position;

	protected int recurrence_type = NO_RECURRENCE;

	protected Date[] change_exceptions;

	protected Date[] delete_exceptions;

	protected int days;

	protected int day_in_month;

	protected int month;

	protected int interval;

	protected Date until;

	protected boolean notification;

	protected int recurrence_calculator;

	protected boolean alarmFlag;

	protected int occurrence;

	/**
	 * The end of a recurrence can be defined either through the until date or
	 * through this count value. If this count value is used the object must
	 * occur count times until the recurrence ends.
	 */
	protected int recurrence_count;

	protected int confirm;

	protected String confirmMessage;

	protected String organizer;

	protected int sequence;

    protected int organizerId;

    protected String principal;

    protected int principalId;
    
    /**
     * An attachment link.
     */
    protected String attachmentLink;

    protected boolean fulltime;

	protected boolean b_title;

	protected boolean b_start_date;

	protected boolean b_end_date;

	protected boolean b_note;

	protected boolean b_recurrence_id;

	protected boolean b_recurrence_position;

	protected boolean b_recurrence_date_position;

	/**
	 * Indicates that the attribute <code>recurrence_count</code> has been set
	 * or is empty.
	 */
	protected boolean b_recurrence_count;

	protected boolean b_change_exceptions;

	protected boolean b_delete_exceptions;

	protected boolean b_days;

	protected boolean b_day_in_month;

	protected boolean b_month;

	protected boolean b_interval;

	protected boolean b_recurrence_type;

	protected boolean b_until;

	protected boolean b_notification;

	protected boolean b_participants;

	protected boolean b_users;

	protected boolean bConfirmations;

	protected boolean b_confirm;

	protected boolean b_confirmMessage;

	protected boolean b_occurrence;

	protected boolean b_organizer;

	protected boolean b_sequence;

    protected boolean b_organizerId;

    protected boolean b_principal;

    protected boolean b_principalId;

    protected boolean b_fulltime;

    protected  boolean bAttachmentLink;

	// GET METHODS
	public String getTitle() {
		return title;
	}

	public Date getStartDate() {
		return start_date;
	}

	public Date getEndDate() {
		return end_date;
	}

	public String getNote() {
		return note;
	}

	public int getRecurrenceID() {
		return recurrence_id;
	}

	public int getRecurrencePosition() {
		return recurrence_position;
	}

	public Date getRecurrenceDatePosition() {
		return recurrence_date_position;
	}

	public int getRecurrenceType() {
		return recurrence_type;
	}

	public Date[] getChangeException() {
		return change_exceptions;
	}

	public Date[] getDeleteException() {
		return delete_exceptions;
	}

	public int getDays() {
		return days;
	}

	public int getDayInMonth() {
		return day_in_month;
	}

	public int getMonth() {
		return month;
	}

	public int getInterval() {
		return interval;
	}

	public Date getUntil() {
		return until;
	}

	public boolean getNotification() {
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

	public String getOrganizer() {
		return organizer;
	}

	public int getSequence() {
		return this.sequence;
	}

    public int getOrganizerId() {
        return this.organizerId;
    }

    public String getPrincipal() {
        return this.principal;
    }

    public int getPrincipalId() {
        return this.principalId;
    }

    public boolean getFullTime() {
        return fulltime;
    }

	// SET METHODS
	public void setTitle(final String title) {
		this.title = title;
		b_title = true;
	}

	public void setStartDate(final Date start_date) {
		this.start_date = start_date;
		b_start_date = true;
	}

	public void setEndDate(final Date end_date) {
		this.end_date = end_date;
		b_end_date = true;
	}

	public void setNote(final String note) {
		this.note = note;
		b_note = true;
	}

	public void setRecurrenceID(final int recurrence_id) {
		this.recurrence_id = recurrence_id;
		b_recurrence_id = true;
	}

	public void setRecurrencePosition(final int recurrence_position) {
		this.recurrence_position = recurrence_position;
		b_recurrence_position = true;
	}

	public void setRecurrenceDatePosition(final Date recurrence_date_position) {
		this.recurrence_date_position = recurrence_date_position;
		b_recurrence_date_position = true;
	}

	public void setRecurrenceType(final int recurrence_type) {
		this.recurrence_type = recurrence_type;
		b_recurrence_type = true;
	}

	public void setChangeExceptions(final Date[] change_exceptions) {
		this.change_exceptions = change_exceptions;
		b_change_exceptions = true;
	}

	public void setChangeExceptions(List<? extends Date> change_exceptions) {
		this.change_exceptions = change_exceptions
				.toArray(new Date[change_exceptions.size()]);
		b_change_exceptions = true;
	}

	public void addChangeException(final Date change_exception) {
		if (change_exceptions == null) {
			setChangeExceptions(new Date[] { change_exception });
		} else {
			final Date[] tmp = change_exceptions;
			change_exceptions = new Date[tmp.length + 1];
			System.arraycopy(tmp, 0, change_exceptions, 0, tmp.length);
			change_exceptions[change_exceptions.length - 1] = change_exception;
		}
	}

	public void setDeleteExceptions(final Date[] delete_exceptions) {
		this.delete_exceptions = delete_exceptions;
		b_delete_exceptions = true;
	}

	public void setDeleteExceptions(List<? extends Date> delete_exceptions) {
		this.delete_exceptions = delete_exceptions
				.toArray(new Date[delete_exceptions.size()]);
		b_delete_exceptions = true;
	}

	public void addDeleteException(final Date delete_exception) {
		if (delete_exceptions == null) {
			setDeleteExceptions(new Date[] { delete_exception });
		} else {
			final Date[] tmp = delete_exceptions;
			delete_exceptions = new Date[tmp.length + 1];
			System.arraycopy(tmp, 0, delete_exceptions, 0, tmp.length);
			delete_exceptions[delete_exceptions.length - 1] = delete_exception;
		}
	}

	public void setDays(final int days) {
		this.days = days;
		b_days = true;
	}

	public void setDayInMonth(final int day_in_month) {
		this.day_in_month = day_in_month;
		b_day_in_month = true;
	}

	public void setMonth(final int month) {
		this.month = month;
		b_month = true;
	}

	public void setInterval(final int interval) {
		this.interval = interval;
		b_interval = true;
	}

	public void setUntil(final Date until) {
		this.until = until;
		b_until = true;
	}

	public void setNotification(final boolean notification) {
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

	public void setOrganizer(String organizer) {
		this.organizer = organizer;
		b_organizer = true;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
		b_sequence = true;
	}

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
        b_organizerId = true;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
        b_principal = true;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
        b_principalId = true;
    }

    public void setFullTime(final boolean fulltime) {
        this.fulltime = fulltime;
        b_fulltime = true;
    }

	// REMOVE METHODS
	public void removeTitle() {
		title = null;
		b_title = false;
	}

	public void removeStartDate() {
		start_date = null;
		b_start_date = false;
	}

	public void removeEndDate() {
		end_date = null;
		b_end_date = false;
	}

	public void removeNote() {
		note = null;
		b_note = false;
	}

	public void removeRecurrenceID() {
		recurrence_id = 0;
		b_recurrence_id = false;
	}

	public void removeRecurrencePosition() {
		recurrence_position = 0;
		b_recurrence_position = false;
	}

	public void removeRecurrenceDatePosition() {
		recurrence_date_position = null;
		b_recurrence_date_position = false;
	}

	public void removeRecurrenceType() {
		recurrence_type = 0;
		b_recurrence_type = false;
	}

	public void removeChangeExceptions() {
		change_exceptions = null;
		b_change_exceptions = false;
	}

	public void removeDeleteExceptions() {
		delete_exceptions = null;
		b_delete_exceptions = false;
	}

	public void removeDays() {
		days = 0;
		b_days = false;
	}

	public void removeDayInMonth() {
		day_in_month = 0;
		b_day_in_month = false;
	}

	public void removeMonth() {
		month = 0;
		b_month = false;
	}

	public void removeInterval() {
		interval = 0;
		b_interval = false;
	}

	public void removeUntil() {
		until = null;
		b_until = false;
	}

	public void removeNotification() {
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

	public void removeOrganizer() {
		organizer = null;
		b_organizer = false;
	}

	public void removeSequence() {
		sequence = 0;
		b_sequence = false;
	}

    public void removeOrganizerId() {
        organizerId = 0;
        b_organizerId = false;
    }

    public void removePrincipal() {
        principal = null;
        b_principal = false;
    }

    public void removePrincipalId() {
        principalId = 0;
        b_principalId = false;
    }

    public void removeFullTime() {
        fulltime = false;
        b_fulltime = false;
    }

	// CONTAINS METHODS
	public boolean containsTitle() {
		return b_title;
	}

	public boolean containsStartDate() {
		return b_start_date;
	}

	/** Checks if start date has been set and current value is not <code>null</code> */
	public boolean containsStartDateAndIsNotNull() {
        return b_start_date && null != start_date;
    }

	public boolean containsEndDate() {
		return b_end_date;
	}

	/** Checks if end date has been set and current value is not <code>null</code> */
	public boolean containsEndDateAndIsNotNull() {
        return b_end_date && null != end_date;
    }

	public boolean containsNote() {
		return b_note;
	}

	public boolean containsRecurrenceID() {
		return b_recurrence_id;
	}

	public boolean containsRecurrencePosition() {
		return b_recurrence_position;
	}

	public boolean containsRecurrenceDatePosition() {
		return b_recurrence_date_position;
	}

	public boolean containsRecurrenceType() {
		return b_recurrence_type;
	}

	public boolean containsChangeExceptions() {
		return b_change_exceptions;
	}

	public boolean containsDeleteExceptions() {
		return b_delete_exceptions;
	}

	public boolean containsDays() {
		return b_days;
	}

	public boolean containsDayInMonth() {
		return b_day_in_month;
	}

	public boolean containsMonth() {
		return b_month;
	}

	public boolean containsInterval() {
		return b_interval;
	}

	public boolean containsUntil() {
		return b_until;
	}

	public boolean containsNotification() {
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

	public boolean containsOrganizer() {
		return b_organizer;
	}

	public boolean containsSequence() {
		return b_sequence;
	}

    public boolean containsOrganizerId() {
        return b_organizerId;
    }

    public boolean containsPrincipal() {
        return b_principal;
    }

    public boolean containsPrincipalId() {
        return b_principalId;
    }

    public boolean containsFullTime() {
        return b_fulltime;
    }

	public void setParticipants(final List<? extends Participant> participants) {
		this.participants = participants.toArray(new Participant[participants
				.size()]);
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
		if (participants == null) {
			setParticipants(new Participant[] { p });
		} else {
			final int newLength = participants.length + 1;
			final Participant[] tmp = participants;
			participants = new Participant[newLength];
			System.arraycopy(tmp, 0, participants, 0, tmp.length);
			participants[newLength - 1] = p;
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

	public ConfirmableParticipant[] getConfirmations() {
		return confirmations;
	}

	public void setConfirmations(ConfirmableParticipant[] confirmations) {
		this.confirmations = confirmations;
		bConfirmations = true;
	}

	public void removeConfirmations() {
		this.confirmations = null;
		bConfirmations = false;
	}

	public void setConfirmations(List<ConfirmableParticipant> value) {
		setConfirmations(value
				.toArray(new ConfirmableParticipant[value.size()]));
	}

	public void removeConfigurations() {
		this.confirmations = null;
		bConfirmations = false;
	}

	public boolean containsConfirmations() {
		return bConfirmations;
	}

    public String getAttachmentLink() {
        return attachmentLink;
    }

    public void setAttachmentLink(String attachmentLink) {
        this.attachmentLink = attachmentLink;
        this.bAttachmentLink = true;
    }
    
    public void removeAttachmentLink() {
        this.attachmentLink = null;
        this.bAttachmentLink = false;
    }
    
    public boolean containsAttachmentLink() {
        return bAttachmentLink;
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
		confirmations = null;
		occurrence = 0;
		organizer = null;
		sequence = 0;
		organizerId = 0;
		principal = null;
		principalId = 0;
        fulltime = false;

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
		b_organizer = false;
		b_sequence = false;
		b_organizerId = false;
		b_principal = false;
		b_principalId = false;
        b_fulltime = false;
	}

	/**
	 * @return the recurrence count.
	 */
	public int getRecurrenceCount() {
		return recurrence_count;
	}

	/**
	 * @param recurrenceCount
	 *            the recurrence count to set.
	 */
	public void setRecurrenceCount(final int recurrenceCount) {
		recurrence_count = recurrenceCount;
		b_recurrence_count = true;
	}

	/**
	 * Removes the value of the recurrence count.
	 */
	public void removeRecurrenceCount() {
		recurrence_count = 0;
		b_recurrence_count = false;
	}

	/**
	 * @return if the attribute <code>recurrence_count</code> has been set or
	 *         not.
	 */
	public boolean containsRecurrenceCount() {
		return b_recurrence_count;
	}

	/*-
	 * ----------------- Recurrence Diagnosis -----------------
	 */

    /**
	 * Tests if this event is either a recurring event or a part of a recurring
	 * event (change exception).
	 * <p>
	 * This test checks if recurrence ID is different to zero.
	 *
	 * @return <code>true</code> if this event is either a recurring event or a
	 *         part of a recurring event (change exception); otherwise
	 *         <code>false</code>
	 */
	public boolean isPartOfSeries() {
		return (getRecurrenceID() != 0);
	}

	/**
	 * Tests if this event denotes a specific occurrence within a recurring
	 * event.
	 *
	 * @return <code>true</code> if this event denotes a specific occurrence
	 *         within a recurring event; otherwise <code>false</code>
	 */
	public boolean isSpecificOcurrence() {
		return isException();
	}

	/**
	 * Tests if this event denotes a change exception.
	 *
	 * @return <code>true</code> if this event denotes a change exception;
	 *         otherwise <code>false</code>
	 */
	public boolean isException() {
		return isPartOfSeries()
				&& ((getRecurrencePosition() != 0) || (getRecurrenceDatePosition() != null));
	}

	/**
	 * Tests if this event denotes a recurring event (and <b>not</b> a change
	 * exception).
	 *
	 * @return <code>true</code> if this event denotes a recurring event (and
	 *         <b>not</b> a change exception); otherwise <code>false</code>
	 */
	public boolean isMaster() {
		return (getRecurrenceID() == getObjectID()) && !isSpecificOcurrence();
	}

	/**
	 * Tests if this event is neither a recurring event nor a part of a
	 * recurring event (change exception).
	 * <p>
	 * This test checks if recurrence ID is equal to zero.
	 *
	 * @return <code>true</code> if this event is a single event; otherwise
	 *         <code>false</code>
	 */
	public boolean isSingle() {
		return !isPartOfSeries();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.openexchange.groupware.container.CommonObject#set(int,
	 * java.lang.Object)
	 */
	@Override
	public void set(final int field, final Object value) {
		switch (field) {
		case UNTIL:
			setUntil((Date) value);
			break;
		case USERS:
			if (List.class.isInstance(value)) {
				setUsers((List<UserParticipant>) value);
			} else {
				setUsers((UserParticipant[]) value);
			}
			break;
		case NOTE:
			setNote((String) value);
			break;
		case RECURRENCE_DATE_POSITION:
			setRecurrenceDatePosition((Date) value);
			break;
		case END_DATE:
			setEndDate((Date) value);
			break;
		case RECURRENCE_POSITION:
			setRecurrencePosition((Integer) value);
			break;
		case RECURRENCE_CALCULATOR:
			setRecurrenceCalculator((Integer) value);
			break;
		case DAYS:
			setDays((Integer) value);
			break;
		case NOTIFICATION:
			setNotification((Boolean) value);
			break;
		case MONTH:
			setMonth((Integer) value);
			break;
		case RECURRENCE_COUNT:
			setRecurrenceCount((Integer) value);
			setOccurrence(getRecurrenceCount());
			break;
		case DAY_IN_MONTH:
			setDayInMonth((Integer) value);
			break;
		case RECURRENCE_TYPE:
			setRecurrenceType((Integer) value);
			break;
		case START_DATE:
			setStartDate((Date) value);
			break;
		case INTERVAL:
			setInterval((Integer) value);
			break;
		case TITLE:
			setTitle((String) value);
			break;
		case RECURRENCE_ID:
			setRecurrenceID((Integer) value);
			break;
		case PARTICIPANTS:
			if (List.class.isInstance(value)) {
				setParticipants((List<Participant>) value);
			} else {
				setParticipants((Participant[]) value);
			}
			break;
		case CONFIRMATIONS:
			if (List.class.isInstance(value)) {
				setConfirmations((List<ConfirmableParticipant>) value);
			} else {
				setConfirmations((ConfirmableParticipant[]) value);
			}
			break;
		case CHANGE_EXCEPTIONS:
			if (List.class.isInstance(value)) {
				setChangeExceptions((List<Date>) value);
			} else {
				setChangeExceptions((Date[]) value);
			}
			break;
		case DELETE_EXCEPTIONS:
			if (List.class.isInstance(value)) {
				setDeleteExceptions((List<Date>) value);
			} else {
				setDeleteExceptions((Date[]) value);
			}
			break;
		case ORGANIZER:
			setOrganizer((String) value);
			break;
		case SEQUENCE:
			setSequence((Integer) value);
			break;
		case ORGANIZER_ID:
		    setOrganizerId((Integer) value);
		    break;
		case PRINCIPAL:
		    setPrincipal((String) value);
		    break;
		case PRINCIPAL_ID:
		    setPrincipalId((Integer) value);
		    break;
        case FULL_TIME:
            setFullTime((Boolean) value);
            break;
		default:
			super.set(field, value);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.openexchange.groupware.container.CommonObject#get(int)
	 */
	@Override
	public Object get(final int field) {
		switch (field) {
		case UNTIL:
			return getUntil();
		case USERS:
			return getUsers();
		case NOTE:
			return getNote();
		case RECURRENCE_DATE_POSITION:
			return getRecurrenceDatePosition();
		case END_DATE:
			return getEndDate();
		case RECURRENCE_POSITION:
			return getRecurrencePosition();
		case RECURRENCE_CALCULATOR:
			return getRecurrenceCalculator();
		case DAYS:
			return getDays();
		case NOTIFICATION:
			return getNotification();
		case MONTH:
			return getMonth();
		case RECURRENCE_COUNT:
			if (containsRecurrenceCount()) {
                return getRecurrenceCount();
            } else {
                return getOccurrence();
            }
		case DAY_IN_MONTH:
			return getDayInMonth();
		case RECURRENCE_TYPE:
			return getRecurrenceType();
		case START_DATE:
			return getStartDate();
		case INTERVAL:
			return getInterval();
		case TITLE:
			return getTitle();
		case RECURRENCE_ID:
			return getRecurrenceID();
		case PARTICIPANTS:
			return getParticipants();
		case CHANGE_EXCEPTIONS:
			return getChangeException();
		case DELETE_EXCEPTIONS:
			return getDeleteException();
		case ORGANIZER:
			return getOrganizer();
		case SEQUENCE:
			return getSequence();
		case CONFIRMATIONS:
			return getConfirmations();
		case ORGANIZER_ID:
		    return getOrganizerId();
		case PRINCIPAL:
		    return getPrincipal();
		case PRINCIPAL_ID:
		    return getPrincipalId();
        case FULL_TIME:
            return getFullTime();
		default:
			return super.get(field);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.openexchange.groupware.container.CommonObject#contains(int)
	 */
	@Override
	public boolean contains(final int field) {
		switch (field) {
		case UNTIL:
			return containsUntil();
		case USERS:
			return containsUserParticipants();
		case NOTE:
			return containsNote();
		case RECURRENCE_DATE_POSITION:
			return containsRecurrenceDatePosition();
		case END_DATE:
			return containsEndDate();
		case RECURRENCE_POSITION:
			return containsRecurrencePosition();
		case DAYS:
			return containsDays();
		case NOTIFICATION:
			return containsNotification();
		case MONTH:
			return containsMonth();
		case RECURRENCE_COUNT:
			return containsRecurrenceCount() || containsOccurrence();
		case DAY_IN_MONTH:
			return containsDayInMonth();
		case RECURRENCE_TYPE:
			return containsRecurrenceType();
		case START_DATE:
			return containsStartDate();
		case INTERVAL:
			return containsInterval();
		case TITLE:
			return containsTitle();
		case RECURRENCE_ID:
			return containsRecurrenceID();
		case PARTICIPANTS:
			return containsParticipants();
		case CONFIRMATIONS:
			return containsConfirmations();
		case CHANGE_EXCEPTIONS:
			return containsChangeExceptions();
		case DELETE_EXCEPTIONS:
			return containsDeleteExceptions();
		case RECURRENCE_CALCULATOR:
			return true;
		case ORGANIZER:
			return containsOrganizer();
		case SEQUENCE:
			return containsSequence();
		case ORGANIZER_ID:
		    return containsOrganizerId();
		case PRINCIPAL:
		    return containsPrincipal();
		case PRINCIPAL_ID:
		    return containsPrincipalId();
        case FULL_TIME:
            return containsFullTime();
		default:
			return super.contains(field);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.openexchange.groupware.container.CommonObject#remove(int)
	 */
	@Override
    public void remove(final int field) {
        switch (field) {
        case UNTIL:
            removeUntil();
            break;
        case USERS:
            removeUsers();
            break;
        case NOTE:
            removeNote();
            break;
        case RECURRENCE_DATE_POSITION:
            removeRecurrenceDatePosition();
            break;
        case END_DATE:
            removeEndDate();
            break;
        case RECURRENCE_POSITION:
            removeRecurrencePosition();
            break;
        case DAYS:
            removeDays();
            break;
        case NOTIFICATION:
            removeNotification();
            break;
        case MONTH:
            removeMonth();
            break;
        case RECURRENCE_COUNT:
            removeRecurrenceCount();
            removeOccurrence();
            break;
        case DAY_IN_MONTH:
            removeDayInMonth();
            break;
        case RECURRENCE_TYPE:
            removeRecurrenceType();
            break;
        case START_DATE:
            removeStartDate();
            break;
        case INTERVAL:
            removeInterval();
            break;
        case TITLE:
            removeTitle();
            break;
        case RECURRENCE_ID:
            removeRecurrenceID();
            break;
        case PARTICIPANTS:
            removeParticipants();
            break;
        case CONFIRMATIONS:
        	removeConfirmations();
        	break;
        case CHANGE_EXCEPTIONS:
            removeChangeExceptions();
        case DELETE_EXCEPTIONS:
            removeDeleteExceptions();
        case RECURRENCE_CALCULATOR:
            return;
        case ORGANIZER:
            removeOrganizer();
            break;
        case SEQUENCE:
            removeSequence();
            break;
        case ORGANIZER_ID:
            removeOrganizerId();
            break;
        case PRINCIPAL:
            removePrincipal();
            break;
        case PRINCIPAL_ID:
            removePrincipalId();
            break;
        case FULL_TIME:
            removeFullTime();
            break;
        default:
            super.remove(field);

        }
    }

	public static Set<Differ<? super CalendarObject>> differ = new HashSet<Differ<? super CalendarObject>>();

	static {
		differ.add(new ChangeExceptionsDiffer());
		differ.add(new DeleteExceptionsDiffer());
		differ.add(new ParticipantsDiffer());
		differ.add(new UsersDiffer());
		differ.add(new ConfirmationsDiffer());
	}

	@Override
	public String toString() {
		return "[" + this.getObjectID() + "] " + this.getTitle();
	}

	@Override
    public CalendarObject clone() {
	    CalendarObject retval;
        try {
            retval = (CalendarObject) super.clone();

            if (getParticipants() != null) {
                Participant[] clonedParticipants = new Participant[getParticipants().length];
                for (int i = 0; i < getParticipants().length; i++) {
                    clonedParticipants[i] = getParticipants()[i].getClone();
                }
                retval.setParticipants(clonedParticipants);
            }

            if (getConfirmations() != null) {
                ConfirmableParticipant[] clonedConfirmations = new ConfirmableParticipant[getConfirmations().length];
                for (int i = 0; i < getConfirmations().length; i++) {
                    clonedConfirmations[i] = getConfirmations()[i].getClone();
                }
                retval.setConfirmations(clonedConfirmations);
            }

            if (getUsers() != null) {
                UserParticipant[] clonedUsers = new UserParticipant[getUsers().length];
                for (int i = 0; i <  getUsers().length; i++) {
                    clonedUsers[i] = getUsers()[i].getClone();
                }
                retval.setUsers(clonedUsers);
            }

            if (containsFullTime()) {
                retval.setFullTime(getFullTime());
            }

        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
	    return retval;
	}
}
