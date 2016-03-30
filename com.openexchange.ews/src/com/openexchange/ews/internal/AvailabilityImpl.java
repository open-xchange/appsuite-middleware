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

package com.openexchange.ews.internal;

import java.util.Date;
import java.util.List;
import javax.xml.ws.Holder;
import com.microsoft.schemas.exchange.services._2006.messages.ExchangeServicePortType;
import com.microsoft.schemas.exchange.services._2006.messages.FreeBusyResponseType;
import com.microsoft.schemas.exchange.services._2006.messages.GetUserAvailabilityRequestType;
import com.microsoft.schemas.exchange.services._2006.messages.GetUserAvailabilityResponseType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfMailboxData;
import com.microsoft.schemas.exchange.services._2006.types.DayOfWeekType;
import com.microsoft.schemas.exchange.services._2006.types.Duration;
import com.microsoft.schemas.exchange.services._2006.types.EmailAddress;
import com.microsoft.schemas.exchange.services._2006.types.FreeBusyViewOptionsType;
import com.microsoft.schemas.exchange.services._2006.types.MailboxData;
import com.microsoft.schemas.exchange.services._2006.types.MeetingAttendeeType;
import com.microsoft.schemas.exchange.services._2006.types.SerializableTimeZone;
import com.microsoft.schemas.exchange.services._2006.types.SerializableTimeZoneTime;
import com.openexchange.ews.Availability;
import com.openexchange.ews.DateConverter;
import com.openexchange.ews.EWSExceptionCodes;
import com.openexchange.ews.ExchangeWebService;
import com.openexchange.exception.OXException;

/**
 * {@link AvailabilityImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AvailabilityImpl extends Common implements Availability {

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

    public AvailabilityImpl(ExchangeWebService service, ExchangeServicePortType port) {
        super(service, port);
    }

    @Override
    public List<FreeBusyResponseType> getFreeBusy(List<String> emailAddresses, Date from, Date until, boolean detailed) throws OXException {
        GetUserAvailabilityResponseType userAvailibility = getUserAvailibility(emailAddresses, from, until, detailed);
        if (null == userAvailibility || null == userAvailibility.getFreeBusyResponseArray() ||
            null == userAvailibility.getFreeBusyResponseArray().getFreeBusyResponse()) {
            throw EWSExceptionCodes.NO_RESPONSE.create();
        }
        return userAvailibility.getFreeBusyResponseArray().getFreeBusyResponse();
    }

    private GetUserAvailabilityResponseType getUserAvailibility(List<String> emailAddresses, Date from, Date until, boolean detailed) {
        Holder<GetUserAvailabilityResponseType> responseHolder = new Holder<GetUserAvailabilityResponseType>();
        GetUserAvailabilityRequestType request = createAvailabilityRequest(emailAddresses, from, until, detailed);
        port.getUserAvailability(request, getRequestVersion(), responseHolder, getVersionHolder());
        return responseHolder.value;
    }

    private static GetUserAvailabilityRequestType createAvailabilityRequest(List<String> emailAddresses, Date from, Date until, boolean detailed) {
        GetUserAvailabilityRequestType getUserAvailabilityRequestType = new GetUserAvailabilityRequestType();
        getUserAvailabilityRequestType.setTimeZone(LEGACY_TIMEZONE);
        getUserAvailabilityRequestType.setFreeBusyViewOptions(getFreebusyViewOptions(from, until, detailed));
        getUserAvailabilityRequestType.setMailboxDataArray(getMailboxData(emailAddresses));
        return getUserAvailabilityRequestType;
    }

    private static ArrayOfMailboxData getMailboxData(List<String> emailAddresses) {
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

    private static FreeBusyViewOptionsType getFreebusyViewOptions(Date from, Date until, boolean detailed) {
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