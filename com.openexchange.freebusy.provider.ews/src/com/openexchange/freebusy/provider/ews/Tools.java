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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.freebusy.provider.ews;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.microsoft.schemas.exchange.services._2006.messages.GetUserAvailabilityRequestType;
import com.microsoft.schemas.exchange.services._2006.messages.ResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfMailboxData;
import com.microsoft.schemas.exchange.services._2006.types.CalendarEvent;
import com.microsoft.schemas.exchange.services._2006.types.DayOfWeekType;
import com.microsoft.schemas.exchange.services._2006.types.Duration;
import com.microsoft.schemas.exchange.services._2006.types.EmailAddress;
import com.microsoft.schemas.exchange.services._2006.types.FreeBusyView;
import com.microsoft.schemas.exchange.services._2006.types.FreeBusyViewOptionsType;
import com.microsoft.schemas.exchange.services._2006.types.LegacyFreeBusyType;
import com.microsoft.schemas.exchange.services._2006.types.MailboxData;
import com.microsoft.schemas.exchange.services._2006.types.MeetingAttendeeType;
import com.microsoft.schemas.exchange.services._2006.types.ResponseClassType;
import com.microsoft.schemas.exchange.services._2006.types.SerializableTimeZone;
import com.microsoft.schemas.exchange.services._2006.types.SerializableTimeZoneTime;
import com.openexchange.ews.DateConverter;
import com.openexchange.ews.EWSException;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.BusyStatus;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.FreeBusyInterval;

/**
 * {@link Tools}
 * 
 * Utilities for the EWS free/busy provider
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools {
    
    private static final SerializableTimeZone LEGACY_TIMEZONE;
    static {
        SerializableTimeZoneTime standardTime = new SerializableTimeZoneTime();
        standardTime.setBias(0);
        standardTime.setTime("00:00:00");
        standardTime.setDayOrder((short)1);
        standardTime.setDayOfWeek(DayOfWeekType.SUNDAY);
        LEGACY_TIMEZONE = new SerializableTimeZone();
        LEGACY_TIMEZONE.setBias(0);
        LEGACY_TIMEZONE.setStandardTime(standardTime);
        LEGACY_TIMEZONE.setDaylightTime(standardTime);
    }
    
    public static OXException getError(String participant, ResponseMessageType responseMessage) {
        if (null != responseMessage && false == ResponseClassType.SUCCESS.equals(responseMessage.getResponseClass())) {
            EWSException ewsException = new EWSException(responseMessage);
            if ("ErrorMailRecipientNotFound".equals(responseMessage.getResponseCode())) {
                return FreeBusyExceptionCodes.PARTICIPANT_NOT_FOUND.create(ewsException, participant);               
            } else {
                return OXException.general("Unknown error: " + responseMessage.getMessageText());             
            }
        }
        return null;
    }
    
    public static Collection<FreeBusyInterval> getFreeBusyIntervals(FreeBusyView freeBusyView) {
        Collection<FreeBusyInterval> freeBusyIntervals = new ArrayList<FreeBusyInterval>();
        if (null != freeBusyView && null != freeBusyView.getCalendarEventArray()) { 
            List<CalendarEvent> calendarEvents = freeBusyView.getCalendarEventArray().getCalendarEvent();
            if (null != calendarEvents) {
                for (CalendarEvent calendarEvent : calendarEvents) {
                    freeBusyIntervals.add(getFreeBusyInterval(calendarEvent));
                }
            }
        }
        return freeBusyIntervals;
    }
        
    public static FreeBusyInterval getFreeBusyInterval(CalendarEvent calendarEvent) {
        FreeBusyInterval interval = new FreeBusyInterval(DateConverter.DEFAULT.getDate(calendarEvent.getStartTime()),
            DateConverter.DEFAULT.getDate(calendarEvent.getEndTime()), getStatus(calendarEvent.getBusyType())); 
        if (null != calendarEvent.getCalendarEventDetails()) {
            interval.setTitle(calendarEvent.getCalendarEventDetails().getSubject());
            interval.setLocation(calendarEvent.getCalendarEventDetails().getLocation());
        }
        return interval;
    }
        
    public static BusyStatus getStatus(LegacyFreeBusyType type) {
        switch (type) {
        case FREE:
            return BusyStatus.FREE;            
        case OOF:
            return BusyStatus.ABSENT;          
        case TENTATIVE:
            return BusyStatus.TEMPORARY;           
        case BUSY:
            return BusyStatus.RESERVED;            
        default:
            return BusyStatus.UNKNOWN;
        }
    }
        
    public static GetUserAvailabilityRequestType createAvailabilityRequest(List<String> emailAddresses, Date from, Date until, boolean detailed) {
        GetUserAvailabilityRequestType getUserAvailabilityRequestType = new GetUserAvailabilityRequestType();
        getUserAvailabilityRequestType.setTimeZone(LEGACY_TIMEZONE);
        getUserAvailabilityRequestType.setFreeBusyViewOptions(getFreebusyViewOptions(from, until, detailed));
        getUserAvailabilityRequestType.setMailboxDataArray(getMailboxData(emailAddresses));
        return getUserAvailabilityRequestType;
    }   
    
    public static ArrayOfMailboxData getMailboxData(List<String> emailAddresses) {
        ArrayOfMailboxData mailboxData = new ArrayOfMailboxData();
        for (String address : emailAddresses) {
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.setAddress(address);
            MailboxData mailbox = new MailboxData();
            mailbox.setEmail(emailAddress);
            mailbox.setAttendeeType(MeetingAttendeeType.REQUIRED);
            mailboxData.getMailboxData().add(mailbox);
        }
        return mailboxData;
    }
        
    public static FreeBusyViewOptionsType getFreebusyViewOptions(Date from, Date until, boolean detailed) {
        FreeBusyViewOptionsType freeBusyViewOptionsType = new FreeBusyViewOptionsType();
        Duration duration = new Duration();
        duration.setStartTime(DateConverter.DEFAULT.getXMLCalendar(from));
        duration.setEndTime(DateConverter.DEFAULT.getXMLCalendar(until));
        freeBusyViewOptionsType.setTimeWindow(duration);
        // http://msdn.microsoft.com/en-us/library/exchange/exchangewebservices.freebusyviewtype(v=exchg.140)
        freeBusyViewOptionsType.getRequestedView().add(detailed ? "Detailed" : "FreeBusy");
        return freeBusyViewOptionsType;
    }
 
}
