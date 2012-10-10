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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.xml.ws.Holder;
import org.apache.commons.logging.Log;
import com.microsoft.schemas.exchange.services._2006.messages.FreeBusyResponseType;
import com.microsoft.schemas.exchange.services._2006.messages.GetUserAvailabilityRequestType;
import com.microsoft.schemas.exchange.services._2006.messages.GetUserAvailabilityResponseType;
import com.microsoft.schemas.exchange.services._2006.messages.ResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfMailboxData;
import com.microsoft.schemas.exchange.services._2006.types.CalendarEvent;
import com.microsoft.schemas.exchange.services._2006.types.DayOfWeekType;
import com.microsoft.schemas.exchange.services._2006.types.Duration;
import com.microsoft.schemas.exchange.services._2006.types.EmailAddress;
import com.microsoft.schemas.exchange.services._2006.types.ExchangeVersionType;
import com.microsoft.schemas.exchange.services._2006.types.FreeBusyView;
import com.microsoft.schemas.exchange.services._2006.types.FreeBusyViewOptionsType;
import com.microsoft.schemas.exchange.services._2006.types.LegacyFreeBusyType;
import com.microsoft.schemas.exchange.services._2006.types.MailboxData;
import com.microsoft.schemas.exchange.services._2006.types.MeetingAttendeeType;
import com.microsoft.schemas.exchange.services._2006.types.RequestServerVersion;
import com.microsoft.schemas.exchange.services._2006.types.ResponseClassType;
import com.microsoft.schemas.exchange.services._2006.types.SerializableTimeZone;
import com.microsoft.schemas.exchange.services._2006.types.SerializableTimeZoneTime;
import com.microsoft.schemas.exchange.services._2006.types.ServerVersionInfo;
import com.openexchange.config.ConfigurationService;
import com.openexchange.ews.DateConverter;
import com.openexchange.ews.EWSException;
import com.openexchange.ews.ExchangeWebService;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.BusyStatus;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.FreeBusyInterval;
import com.openexchange.freebusy.provider.FreeBusyProvider;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.session.Session;

/**
 * {@link EWSFreeBusyProvider}
 * 
 * Provider of free/busy information.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EWSFreeBusyProvider implements FreeBusyProvider {
    
    /**
     * The maximum value for this time period is 42 days. Any requests for user availability information beyond the maximum value will 
     * return an error.
     * @see http://msdn.microsoft.com/en-us/library/exchangewebservices.freebusyviewoptionstype.timewindow.aspx
     */
    private static final int MAX_DAYS = 62;
    
    private static final DateConverter DATE_CONVERTER = new DateConverter();
    private static final Log LOG = com.openexchange.log.Log.loggerFor(EWSFreeBusyProvider.class);
    
    private final ExchangeWebService ews;
    private String[] emailSuffixes;
    private Boolean validEmailsOnly;
    
    /**
     * Initializes a new {@link EWSFreeBusyProvider}.
     * 
     * @throws OXException 
     */
    public EWSFreeBusyProvider() throws OXException {
        super();
        this.ews = createWebService();
    }
    
    private static ExchangeWebService createWebService() throws OXException {
        ConfigurationService configService = EWSFreeBusyProviderLookup.getService(ConfigurationService.class);
        ExchangeWebService ews = new ExchangeWebService(
            configService.getProperty("com.openexchange.freebusy.provider.ews.url"), 
            configService.getProperty("com.openexchange.freebusy.provider.ews.userName"), 
            configService.getProperty("com.openexchange.freebusy.provider.ews.password"));
        ews.getConfig().setExchangeVersion(ExchangeVersionType.valueOf(ExchangeVersionType.class, 
            configService.getProperty("com.openexchange.freebusy.provider.ews.exchangeVersion", "EXCHANGE_2010").toUpperCase()));
        ews.getConfig().setIgnoreHostnameValidation(configService.getBoolProperty(
            "com.openexchange.freebusy.provider.ews.skipHostVerification", false));
        ews.getConfig().setTrustAllCerts(configService.getBoolProperty("com.openexchange.freebusy.provider.ews.trustAllCerts", false));
        return ews;
    }
    
    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }
    
    @Override
    public List<FreeBusyData> getFreeBusy(Session session, List<String> participants, Date from, Date until) {
        /*
         * prepare participant's free/busy data
         */
        Map<String, FreeBusyData> freeBusyInformation = new HashMap<String, FreeBusyData>();
        List<String> filteredParticipants = new ArrayList<String>();
        for (String participant : participants) {
            FreeBusyData freeBusyData = new FreeBusyData(participant, from, until);
            freeBusyInformation.put(participant, freeBusyData);
            if (hasAllowedEmailSuffix(participant) && isValidEmail(participant)) {
                filteredParticipants.add(participant);                
            } else {
                freeBusyData.addWarning(FreeBusyExceptionCodes.DATA_NOT_AVAILABLE.create(participant));
            }
        }
        /*
         * query free/busy information in chunks
         */
        if (0 < filteredParticipants.size()) {
            for (Date currentFrom = from; currentFrom.before(until); currentFrom = addDays(currentFrom, MAX_DAYS)) {
                Date maxUntil = addDays(currentFrom, MAX_DAYS);
                Date currentUntil = until.before(maxUntil) ? until : maxUntil; 
                List<FreeBusyResponseType> freeBusyResponses = getFreeBusyResponses(filteredParticipants, currentFrom, currentUntil);
                if (null == freeBusyResponses) {
                    LOG.warn("Got no free/busy response from EWS");
                    continue;
                }
                if (freeBusyResponses.size() != filteredParticipants.size()) {
                    LOG.warn("Response array size different from requested participants, unable to map times to participants");
                    continue;
                }
                /*
                 * add data from all filtered participants
                 */
                for (int i = 0; i < filteredParticipants.size(); i++) {
                    String participant = filteredParticipants.get(i);
                    FreeBusyData freeBusyData = freeBusyInformation.get(participant);
                    FreeBusyResponseType freeBusyResponse = freeBusyResponses.get(i);
                    if (null != freeBusyResponse) {
                        freeBusyData.addWarning(getError(filteredParticipants.get(i), freeBusyResponse.getResponseMessage()));
                        freeBusyData.addAll(getFreeBusyIntervals(freeBusyResponse.getFreeBusyView()));
                    } else {
                        freeBusyData.addWarning(OXException.general("got no response"));
                    }
                }
            }
        }
        /*
         * create list result from free/busy information
         */
        List<FreeBusyData> freeBusyDataList = new ArrayList<FreeBusyData>(); 
        for (String participant : participants) {
            freeBusyDataList.add(freeBusyInformation.get(participant));
        }
        return freeBusyDataList;
    }

    @Override
    public FreeBusyData getFreeBusy(Session session, String participant, Date from, Date until) {
        List<FreeBusyData> freeBusyData = this.getFreeBusy(session, Arrays.asList(new String[] { participant }), from, until);
        return null != freeBusyData && 0 < freeBusyData.size() ? freeBusyData.get(0) : null; 
    }
    
    private static OXException getError(String participant, ResponseMessageType responseMessage) {
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
    
    private static Collection<FreeBusyInterval> getFreeBusyIntervals(FreeBusyView freeBusyView) {
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
        
    private static FreeBusyInterval getFreeBusyInterval(CalendarEvent calendarEvent) {
        FreeBusyInterval interval = new FreeBusyInterval(DATE_CONVERTER.getDate(calendarEvent.getStartTime()),
            DATE_CONVERTER.getDate(calendarEvent.getEndTime()), getStatus(calendarEvent.getBusyType())); 
        if (null != calendarEvent.getCalendarEventDetails()) {
            interval.setTitle(calendarEvent.getCalendarEventDetails().getSubject());
            interval.setLocation(calendarEvent.getCalendarEventDetails().getLocation());
        }
        return interval;
    }
        
    private static BusyStatus getStatus(LegacyFreeBusyType type) {
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
        
    private List<FreeBusyResponseType> getFreeBusyResponses(List<String> emailAddresses, Date from, Date until) {
        GetUserAvailabilityResponseType userAvailibility = getUserAvailibility(emailAddresses, from, until);
        if (null == userAvailibility || null == userAvailibility.getFreeBusyResponseArray() || 
                null == userAvailibility.getFreeBusyResponseArray().getFreeBusyResponse()) {
            return null;//TODO
//            throw new Exception("Got no user availibility response");
        }
        return userAvailibility.getFreeBusyResponseArray().getFreeBusyResponse();
    }
    
    private GetUserAvailabilityResponseType getUserAvailibility(List<String> emailAddresses, Date from, Date until) {
        Holder<GetUserAvailabilityResponseType> responseHolder = new Holder<GetUserAvailabilityResponseType>();
        Holder<ServerVersionInfo> versionHolder = new Holder<ServerVersionInfo>();
        GetUserAvailabilityRequestType request = createAvailabilityRequest(emailAddresses, from, until);
        this.ews.getServicePort().getUserAvailability(request, getRequestVersion(), responseHolder, versionHolder);
        return responseHolder.value;        
    }
    
    private static GetUserAvailabilityRequestType createAvailabilityRequest(List<String> emailAddresses, Date from, Date until) {
        GetUserAvailabilityRequestType getUserAvailabilityRequestType = new GetUserAvailabilityRequestType();
        getUserAvailabilityRequestType.setTimeZone(getLegacyTimeZone());
        getUserAvailabilityRequestType.setFreeBusyViewOptions(getFreebusyViewOptions(from, until));
        getUserAvailabilityRequestType.setMailboxDataArray(getMailboxData(emailAddresses));
        return getUserAvailabilityRequestType;
    }   
    
    private static RequestServerVersion getRequestVersion() {
        RequestServerVersion requestServerVersion = new RequestServerVersion();
        requestServerVersion.setVersion(ExchangeVersionType.EXCHANGE_2010);
        return requestServerVersion;
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
        
    private static FreeBusyViewOptionsType getFreebusyViewOptions(Date from, Date until) {
        FreeBusyViewOptionsType freeBusyViewOptionsType = new FreeBusyViewOptionsType();
        Duration duration = new Duration();
        duration.setStartTime(DateConverter.DEFAULT.getXMLCalendar(from));
        duration.setEndTime(DateConverter.DEFAULT.getXMLCalendar(until));
        freeBusyViewOptionsType.setTimeWindow(duration);
        // http://msdn.microsoft.com/en-us/library/exchange/exchangewebservices.freebusyviewtype(v=exchg.140)
        freeBusyViewOptionsType.getRequestedView().add("FreeBusy");
        freeBusyViewOptionsType.getRequestedView().add("Detailed");
        return freeBusyViewOptionsType;
    }
    
    private static SerializableTimeZone getLegacyTimeZone() {
        SerializableTimeZoneTime standardTime = new SerializableTimeZoneTime();
        standardTime.setBias(0);
        standardTime.setTime("00:00:00");
        standardTime.setDayOrder((short)1);
        standardTime.setDayOfWeek(DayOfWeekType.SUNDAY);
        SerializableTimeZone timezone = new SerializableTimeZone();
        timezone.setBias(0);
        timezone.setStandardTime(standardTime);
        timezone.setDaylightTime(standardTime);
        return timezone;
    }
    
    private boolean hasAllowedEmailSuffix(String participant) {
        String[] emailSuffixes = getEmailSuffixes();
        if (null != emailSuffixes && 0 < emailSuffixes.length) {
            for (String suffix : emailSuffixes) {
                if (participant.endsWith(suffix)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    private boolean isValidEmail(String participant) {
        if (isValidEmailsOnly()) {
            try {
                new QuotedInternetAddress(participant).validate();
            } catch (AddressException e) {
                return false;
            }          
        }
        return true;
    }
    
    private String[] getEmailSuffixes() {
        if (null == this.emailSuffixes) {
            String value = null;
            try {
                value = EWSFreeBusyProviderLookup.getService(ConfigurationService.class).getProperty(
                    "com.openexchange.freebusy.provider.ews.emailSuffixes");
            } catch (OXException e) {
                LOG.warn("error reading 'com.openexchange.freebusy.provider.ews.emailSuffixes'", e);
            }
            this.emailSuffixes = null == value || 0 == value.trim().length() ? new String[0] : value.trim().split(",");
        }    
        return this.emailSuffixes;
    }
    
    private boolean isValidEmailsOnly() {
        if (null == this.validEmailsOnly) {
            boolean value = true;
            try {
                value = EWSFreeBusyProviderLookup.getService(ConfigurationService.class).getBoolProperty(
                    "com.openexchange.freebusy.provider.ews.validEmailsOnly", true);
            } catch (OXException e) {
                LOG.warn("error reading 'com.openexchange.freebusy.provider.ews.validEmailsOnly'", e);
            }
            this.validEmailsOnly = Boolean.valueOf(value);
        }    
        return this.validEmailsOnly.booleanValue();
    }

}
