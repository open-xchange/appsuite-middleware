/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.alarm.sms;

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.ratelimit.Rate;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sms.SMSServiceSPI;
import com.openexchange.user.User;
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
        if (phoneNumber == null) {
            LOG.warn("Unable to send sms alarm for user {} in context {} because of a missing or invalid telephone number.", I(userId), I(contextId));
            return;
        }
        serviceLookup.getServiceSafe(SMSServiceSPI.class).sendMessage(new String[] { phoneNumber }, generateSMS(event, user, contextId), userId, contextId);
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

    private String generateSMS(Event event, User user, int contextId) {
        Locale locale = user.getLocale();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        RegionalSettingsService regionalSettingsService = serviceLookup.getService(RegionalSettingsService.class);
        Translator translator = translatorFactory.translatorFor(locale);
        DateFormat df;
        if (null == regionalSettingsService) {
            df = CalendarUtils.isAllDay(event) ? DateFormat.getDateInstance(DateFormat.LONG, locale) : DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
        } else {
            df = CalendarUtils.isAllDay(event) ?
                regionalSettingsService.getDateFormat(contextId, user.getId(), locale, DateFormat.LONG) : regionalSettingsService.getDateTimeFormat(contextId, user.getId(), locale, DateFormat.LONG, DateFormat.SHORT);
        }

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
