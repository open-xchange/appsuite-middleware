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

package com.openexchange.calendar.itip;

import java.util.Date;
import java.util.List;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.itip.analyzers.AbstractITipAnalyzer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;

import static com.openexchange.calendar.itip.ITipUtils.*;

/**
 * {@link ITipChange}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ITipChange {

    public static enum Type {
        CREATE, UPDATE, DELETE, CREATE_DELETE_EXCEPTION;
    }

    private Type type;

    private Appointment currentAppointment;

    private CalendarDataObject newAppointment;

    private List<Appointment> conflicts;

    private CalendarDataObject master;

    private Appointment deleted;

    private boolean isException = false;

    private ParticipantChange participantChange;

    private AppointmentDiff diff;

    private List<String> diffDescription;

	private String introduction;

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public CalendarDataObject getNewAppointment() {
        return newAppointment;
    }

    public void setNewAppointment(CalendarDataObject newAppointment) {
        this.newAppointment = newAppointment;
    }

    public Appointment getCurrentAppointment() {
        if (currentAppointment == null) {
            if (isException && master != null && newAppointment != null && newAppointment.getRecurrenceDatePosition() != null) {
                // TODO: Calculate original ocurrence time for diff
                CalendarDataObject originalOcurrence = master.clone();
                CalendarCollection calCol = new CalendarCollection();
                try {
                    if (!originalOcurrence.containsTimezone()) {
                        originalOcurrence.setTimezone("UTC");
                    }
                    RecurringResultsInterface recurring = calCol.calculateRecurring(originalOcurrence, startOfTheDay(newAppointment.getRecurrenceDatePosition()), endOfTheDay(newAppointment.getRecurrenceDatePosition()), 0);
                    if (recurring != null && recurring.size() > 0) {
                        RecurringResultInterface recurringResult = recurring.getRecurringResult(0);
                        originalOcurrence.setStartDate(new Date(recurringResult.getStart()));
                        originalOcurrence.setEndDate(new Date(recurringResult.getEnd()));
                    }
                } catch (OXException e) {
                    // IGNORE
                    originalOcurrence = master;
                }
                return originalOcurrence;
            }
        }
        return currentAppointment;
    }

    public void setCurrentAppointment(Appointment currentAppointment) {
        this.currentAppointment = currentAppointment;
    }

    public List<Appointment> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<Appointment> conflicts) {
        this.conflicts = conflicts;
    }

    public CalendarDataObject getMasterAppointment() {
        return master;
    }

    public void setMaster(CalendarDataObject master) {
        this.master = master;
    }

    public Appointment getDeletedAppointment() {
        return deleted;
    }

    public void setDeleted(Appointment deleted) {
        this.deleted = deleted;
    }

    public void setException(boolean b) {
        isException = b;
    }

    public boolean isException() {
        return isException;
    }

    public ParticipantChange getParticipantChange() {
        return participantChange;
    }

    public void setParticipantChange(ParticipantChange participantChange) {
        this.participantChange = participantChange;
    }

    public AppointmentDiff getDiff() {
        autodiff();
        return diff;
    }

    private void autodiff() {
        if (currentAppointment != null && newAppointment != null && type == Type.UPDATE) {
            diff = AppointmentDiff.compare(currentAppointment, newAppointment, AbstractITipAnalyzer.SKIP);
        }

        if (isException && master != null && newAppointment != null && type == Type.CREATE) {
            // TODO: Calculate original ocurrence time for diff
            CalendarDataObject originalOcurrence = master.clone();
            CalendarCollection calCol = new CalendarCollection();
            try {
                if (!originalOcurrence.containsTimezone()) {
                    originalOcurrence.setTimezone("UTC");
                }
                RecurringResultsInterface recurring = calCol.calculateRecurring(originalOcurrence, startOfTheDay(newAppointment.getRecurrenceDatePosition()), endOfTheDay(newAppointment.getRecurrenceDatePosition()), 0);
                if (recurring != null && recurring.size() > 0) {
                    RecurringResultInterface recurringResult = recurring.getRecurringResult(0);
                    originalOcurrence.setStartDate(new Date(recurringResult.getStart()));
                    originalOcurrence.setEndDate(new Date(recurringResult.getEnd()));
                }
            } catch (OXException e) {
                // IGNORE
                originalOcurrence = master;
            }
            diff = AppointmentDiff.compare(originalOcurrence, newAppointment, AbstractITipAnalyzer.SKIP);
        }
    }

    public void setDiffDescription(List<String> diffDescription) {
        this.diffDescription = diffDescription;
    }

    public List<String> getDiffDescription() {
        return diffDescription;
    }

	public void setIntroduction(String message) {
		this.introduction = message;
	}

	public String getIntroduction() {
		return introduction;
	}



}
