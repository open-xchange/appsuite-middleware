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
import java.util.Date;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.alarm.message.AlarmNotificationService;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.ratelimit.Rate;
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

    private final SMSServiceSPI smsService;
    private final TranslatorFactory translatorFactory;
    private final UserService userService;
    private final LeanConfigurationService leanConfigurationService;

    /**
     * Initializes a new {@link SMSNotificationService}.
     */
    public SMSNotificationService(  SMSServiceSPI smsService,
                                    TranslatorFactory translatorFactory,
                                    UserService userService,
                                    LeanConfigurationService leanConfigurationService) {
        super();
        this.smsService = smsService;
        this.translatorFactory = translatorFactory;
        this.userService = userService;
        this.leanConfigurationService = leanConfigurationService;
    }

    @Override
    public void send(Event event, Alarm alarm, int contextId, int accountId, int userId, long trigger) throws OXException {
        String phoneNumber = getPhoneNumber(alarm);
        if(phoneNumber == null) {
            LOG.warn("Unable to send sms alarm for user {} in context {} because of a missing or invalid telephone number.", userId, contextId);
            return;
        } else {
            smsService.sendMessage(new String[] {phoneNumber}, generateSMS(event, userId, contextId), userId, contextId);
        }
    }

    /**
     * Retrieves the phone number for a sms alarm
     *
     * @param alarm The {@link Alarm}
     * @return The phone number or null
     */
    private String getPhoneNumber(Alarm alarm) {
        ExtendedProperties extendedProperties = alarm.getExtendedProperties();
        if(extendedProperties == null) {
            return null;
        }
        ExtendedProperty extendedProperty = extendedProperties.get("SMS-PHONE-NUMBER");
        if(extendedProperty == null) {
            return null;
        }
        return extendedProperty.getValue().toString();
    }

    private String generateSMS(Event event, int userId, int contextId) throws OXException {
        User user = userService.getUser(userId, contextId);
        Locale locale = user.getLocale();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        Translator translator = translatorFactory.translatorFor(locale);
        DateFormat df = CalendarUtils.isAllDay(event) ? DateFormat.getDateInstance(DateFormat.LONG, locale) : DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
        String formattedStartDate = df.format(new Date(event.getStartDate().getTimestamp()));

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
    public int getShift() throws OXException {
        return leanConfigurationService.getIntProperty(SMSAlarmConfig.SMS_SHIFT);
    }

    @Override
    public boolean isEnabled(int userId, int contextId) throws OXException {
        return leanConfigurationService.getBooleanProperty(userId, contextId, SMSAlarmConfig.SMS_ENABLED);
    }

    @Override
    public Rate getRate(int userId, int contextId) throws OXException {
        return Rate.create( leanConfigurationService.getIntProperty(userId, contextId, SMSAlarmConfig.SMS_LIMIT_AMOUNT), 
                            leanConfigurationService.getLongProperty(userId, contextId, SMSAlarmConfig.SMS_LIMIT_TIME_FRAME));
    }

}
