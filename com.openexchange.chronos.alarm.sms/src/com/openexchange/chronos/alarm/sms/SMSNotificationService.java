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

package com.openexchange.chronos.alarm.sms;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.alarm.message.AlarmNotificationService;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.ratelimit.Rate;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sms.SMSServiceSPI;
import com.openexchange.user.UserService;

/**
 * {@link SMSNotificationService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class SMSNotificationService implements AlarmNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(SMSNotificationService.class);

    private final TranslatorFactory translatorFactory;
    private final UserService userService;
    private final LeanConfigurationService leanConfigurationService;
    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link SMSNotificationService}.
     */
    public SMSNotificationService(  ServiceLookup serviceLookup,
                                    TranslatorFactory translatorFactory,
                                    UserService userService,
                                    LeanConfigurationService leanConfigurationService) {
        super();
        this.serviceLookup = serviceLookup;
        this.translatorFactory = translatorFactory;
        this.userService = userService;
        this.leanConfigurationService = leanConfigurationService;
    }

    @Override
    public void send(Event event, Alarm alarm, int contextId, int accountId, int userId, long trigger) throws OXException {
        User user = userService.getUser(userId, contextId);
        String phoneNumber = getPhoneNumber(alarm, user.getLocale());
        if(phoneNumber == null) {
            LOG.warn("Unable to send sms alarm for user {} in context {} because of a missing or invalid telephone number.", userId, contextId);
            return;
        }
        serviceLookup.getServiceSafe(SMSServiceSPI.class).sendMessage(new String[] { phoneNumber }, generateSMS(event, user), userId, contextId);
    }

    /**
     * Retrieves the phone number for a sms alarm
     *
     * @param alarm The {@link Alarm}
     * @param locale The locale to use to parse the number or null.
     * @return The phone number or null
     */
    private static String getPhoneNumber(Alarm alarm, Locale locale) {
        if (alarm.containsAttendees() && alarm.getAttendees().size() > 0) {
            String uri = alarm.getAttendees().get(0).getUri();
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                PhoneNumber phoneNumber = phoneUtil.parse(uri, locale.getCountry());
                return phoneUtil.format(phoneNumber, PhoneNumberFormat.E164);
            } catch (NumberParseException e) {
                LOG.debug("Unable to parse phone number: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    private String generateSMS(Event event, User user) {
        Locale locale = user.getLocale();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        Translator translator = translatorFactory.translatorFor(locale);
        DateFormat df = CalendarUtils.isAllDay(event) ? DateFormat.getDateInstance(DateFormat.LONG, locale) : DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);

        String timezone = null;
        if (!TimeZone.getTimeZone(user.getTimeZone()).equals(event.getStartDate().getTimeZone())) {
            timezone = event.getStartDate().getTimeZone().getDisplayName(true, TimeZone.SHORT);
        }

        Calendar instance = Calendar.getInstance(event.getStartDate().getTimeZone());
        instance.setTimeInMillis(event.getStartDate().getTimestamp());
        String formattedStartDate = df.format(instance.getTime());
        if (timezone != null) {
            formattedStartDate = formattedStartDate.concat(" ").concat(timezone);
        }

        String summary = event.getSummary();
        if (summary.length() > (100 - formattedStartDate.length())) {
            summary = summary.substring(0, (100 - formattedStartDate.length() - 3)).concat("...");
        }

        return translator.translate(SMSAlarmStrings.REMINDER).concat(": ").concat(summary).concat(" - ").concat(formattedStartDate);
    }

    @Override
    public AlarmAction getAction() {
        return AlarmAction.SMS;
    }

    @Override
    public int getShift() {
        return leanConfigurationService.getIntProperty(SMSAlarmConfig.SMS_SHIFT);
    }

    @Override
    public boolean isEnabled(int userId, int contextId) {
        return leanConfigurationService.getBooleanProperty(userId, contextId, SMSAlarmConfig.SMS_ENABLED);
    }

    @Override
    public Rate getRate(int userId, int contextId) {
        return Rate.create( leanConfigurationService.getIntProperty(userId, contextId, SMSAlarmConfig.SMS_LIMIT_AMOUNT),
                            leanConfigurationService.getLongProperty(userId, contextId, SMSAlarmConfig.SMS_LIMIT_TIME_FRAME));
    }

}
