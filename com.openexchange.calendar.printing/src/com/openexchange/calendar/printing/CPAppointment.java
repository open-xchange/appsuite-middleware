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

package com.openexchange.calendar.printing;

import java.util.Date;
import com.openexchange.calendar.printing.days.CalendarTools;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.container.Appointment;

/**
 * The view of an appointment. A dumbed-down version without recurrence pattern, day spanning and so on.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPAppointment {

    private String title, description, location;

    private Date startDate, endDate;

    private Appointment original;

    private final CPCalendar cal;

    public CPAppointment() {
        super();
        this.cal = null;
    }

    public CPAppointment(Appointment mother) {
        this(mother, null);
    }

    public CPAppointment(Appointment mother, CPCalendar cal) {
        super();
        this.cal = cal;
        setTitle(mother.getTitle());
        setDescription(mother.getNote());
        setLocation(mother.getLocation());
        setStartDate(mother.getStartDate());
        setEndDate(mother.getEndDate());
        setOriginal(mother);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String format(String pattern, Date date) {
        return cal.format(pattern, date);
    }

    public long getStartMinutes() {
        return (startDate.getTime() - CalendarTools.getDayStart(cal, startDate).getTime()) / Constants.MILLI_MINUTE;
    }

    public Date getEndDate() {
        return endDate;
    }

    public long getDurationInMinutes() {
        return (endDate.getTime() - startDate.getTime()) / Constants.MILLI_MINUTE;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStartDate(Date start) {
        this.startDate = start;
    }

    public void setEndDate(Date end) {
        this.endDate = end;
    }
    
    public void setOriginal(Appointment original) {
        this.original = original;
    }
    
    public Appointment getOriginal() {
        return original;
    }

    @Override
    public String toString() {
        return getStartDate() + " - " + getEndDate() +": " + getTitle();
    }
    
    
}