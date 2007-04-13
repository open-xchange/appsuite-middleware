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

import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.tools.StringCollection;

/**
 * AppointmentObject
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class AppointmentObject extends CalendarObject implements Cloneable {
	
	public static final int LOCATION = 400;
	
	public static final int FULL_TIME = 401;
	
	public static final int SHOWN_AS = 402;
	
	public static final int TIMEZONE = 408;	
	
	public static final int RESERVED = 1;
	public static final int TEMPORARY = 2;
	public static final int ABSENT = 3;
	public static final int FREE = 4;
	
	protected int DEFAULTFOLDER = -1;
	
	protected String location = null;
	protected boolean fulltime = false;
	protected int shown_as = 0;
	protected int alarm = 0;
	protected long recurring_start;
        
	protected boolean ignoreConflicts = false;
	
	protected String timezone = null;
	
	protected boolean b_location = false;
	protected boolean b_fulltime = false;
	protected boolean b_shown_as = false;
	protected boolean bAlarm = false;
	protected boolean b_timezone = false;
        protected boolean b_recurring_start = false;
	
	public AppointmentObject() {
		
	}
	
	// GET METHODS
	public String getLocation( ) {
		return location;
	}
	
	public boolean getFullTime() {
		return fulltime;
	}
	
	public int getShownAs( ) {
		return shown_as;
	}
	
	public int getAlarm() {
		return alarm;
	}
	
	public boolean getIgnoreConflicts() {
		return ignoreConflicts;
	}
	
        public final long getRecurringStart() {
            return recurring_start;
        }           
        
	public String getTimezone() {
        if (timezone != null) {
            return timezone;
        }
        System.out.println("FIX ME AND PROVIDE A TIMEZONE :"+StringCollection.getStackAsString()); // TODO: Remove me
        return "UTC";
	}
	
        public final void setRecurringStart(long recurring_start) {
        	final long mod = recurring_start%CalendarRecurringCollection.MILLI_DAY;
            if (mod != 0) {
                recurring_start = recurring_start-mod;
            }
            this.recurring_start = recurring_start;
            b_recurring_start = true;
        }     
        
	// SET METHODS
	public void setLocation( final String location ) {
		this.location = location;
		b_location = true;
	}
	
	public void setFullTime( final boolean fulltime) {
		this.fulltime = fulltime;
		b_fulltime = true;
	}
	
	public void setShownAs( final int shown_as ) {
		this.shown_as = shown_as;
		b_shown_as = true;
	}
	
	public void setAlarm(final int alarm) {
		this.alarm = alarm;
		bAlarm = true;
	}
	
	public void setIgnoreConflicts(final boolean ignoreConflicts) {
		this.ignoreConflicts = ignoreConflicts;
	}
	
	public void setTimezone(final String timezone) {
		this.timezone = timezone;
	}
	
	// REMOVE METHODS
	public void removeLocation( ) {
		location = null;
		b_location = false;
	}
	
	public void removeFullTime( ) {
		fulltime = false;
		b_fulltime = false;
	}
	
	public void removeShownAs( ) {
		shown_as = 0;
		b_shown_as = false;
	}
	
	public void removeAlarm() {
		alarm = 0;
		bAlarm = false;
	}
	
	public void removeTimezone() {
		timezone = null;
		b_timezone = false;
	}
	
        public void removeRecurringStart() {
            recurring_start = 0;
            b_recurring_start = false;
        }
        
	// CONTAINS METHODS
        
        public boolean containsRecurringStart() {
            return b_recurring_start;
        }
        
	public boolean containsLocation() {
		return b_location;
	}
	
	public boolean containsFullTime() {
		return b_fulltime;
	}
	
	public boolean containsShownAs() {
		return b_shown_as;
	}
	
	public boolean containsAlarm()	{
		return bAlarm;
	}
	
	public boolean containsTimezone()	{
		return b_timezone;
	}
	
	@Override
	public void reset() {
		super.reset();
		
		location = null;
		fulltime = false;
		shown_as = 0;
		alarm = 0;
		timezone = null;
		
		b_location = false;
		b_fulltime = false;
		b_shown_as = false;
		bAlarm = false;
		b_timezone = false;
	}
	
	@Override
	public int hashCode() {
		return objectId;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o instanceof AppointmentObject) {
			if (((AppointmentObject)o).hashCode() == hashCode()) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	@Override
	public Object clone() {
		try {
			final AppointmentObject appointmentobject = (AppointmentObject) super.clone();/*new AppointmentObject();*/
			appointmentobject.setLabel(getLabel());
			appointmentobject.setFullTime(getFullTime());
			appointmentobject.setLocation(getLocation());
			appointmentobject.setShownAs(getShownAs());
			appointmentobject.setLabel(getLabel());
			appointmentobject.setOccurrence(getOccurrence());
			appointmentobject.setTimezone(getTimezone());
			return appointmentobject;
		} catch (CloneNotSupportedException e) {
			/*
			 * Cannot occur since we are cloneable
			 */
			throw new InternalError(e.getMessage());
		}
	}
}
