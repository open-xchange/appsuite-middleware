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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.scheduling.changes.impl.desc;

import static org.slf4j.LoggerFactory.getLogger;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.itip.HumanReadableRecurrences;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.impl.ChangeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.FormattableArgument;
import com.openexchange.chronos.scheduling.changes.impl.MessageContext;
import com.openexchange.chronos.scheduling.changes.impl.SentenceImpl;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.regional.RegionalSettingsUtil;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RRuleDescriber}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class RRuleDescriber implements ChangeDescriber {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link RRuleDescriber}.
     *
     * @param services A service lookup reference
     */
    public RRuleDescriber(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    @NonNull
    public EventField[] getFields() {
        return new EventField[] { EventField.RECURRENCE_RULE };
    }

    @Override
    public Description describe(EventUpdate eventUpdate) {
        if (false == eventUpdate.getUpdatedFields().contains(EventField.RECURRENCE_RULE)) {
            return null;
        }
        /*
         * check if only the series UNTIL data was updated
         */
        DateTime newLastOccurrence = optNewLastOccurrence(eventUpdate);
        if (null != newLastOccurrence) {
            FormattableArgument argument = new FormattableArgument() {

                @Override
                public Object format(MessageContext context) {
                    DateFormat dateFormat = RegionalSettingsUtil.getDateFormat(context.getRegionalSettings(), DateFormat.LONG, context.getLocale());
                    dateFormat.setTimeZone(newLastOccurrence.isAllDay() ? TimeZone.getTimeZone("UTC") : context.getTimeZone());
                    return dateFormat.format(new Date(newLastOccurrence.getTimestamp()));
                }
            };
            return new DefaultDescription(new SentenceImpl(Messages.HAS_CHANGED_RRULE_UNTIL).add(argument, ArgumentType.UPDATED), EventField.RECURRENCE_RULE);
        }
        /*
         * describe updated RRULE
         */
        FormattableArgument argument = new FormattableArgument() {

            @Override
            public Object format(MessageContext context) {
                DateFormat dateFormat = RegionalSettingsUtil.getDateFormat(context.getRegionalSettings(), DateFormat.FULL, context.getLocale());
                HumanReadableRecurrences readableRecurrences = new HumanReadableRecurrences(eventUpdate.getUpdate(), context.getLocale());
                String string = readableRecurrences.getString();
                StringBuilder stringBuilder = new StringBuilder();
                if (Strings.isNotEmpty(string)) {
                    stringBuilder.append(string);
                }
                String end = readableRecurrences.getEnd(dateFormat);
                if (Strings.isNotEmpty(end)) {
                    if (0 < stringBuilder.length()) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(end);
                }
                return stringBuilder.toString();
            }
        };
        return new DefaultDescription(new SentenceImpl(Messages.HAS_CHANGED_RRULE).add(argument, ArgumentType.NONE), EventField.RECURRENCE_RULE);
    }

    /**
     * Checks if only the <code>UNTIL</code> part of a recurring event series was adjusted so that updated series now ends with a
     * different last occurrence as before. If that's the case, the date of the new last occurrence is calculated and returned.
     *
     * @param eventUpdate The event update
     * @return The new last occurrence of the event series if the <code>UNTIL</code> part of the recurrence rule was updated, otherwise <code>null</code>
     */
    private DateTime optNewLastOccurrence(EventUpdate eventUpdate) {
        RecurrenceRule originalRule = null;
        RecurrenceRule updatedRule = null;
        try {
            if (null != eventUpdate.getOriginal().getRecurrenceRule()) {
                originalRule = CalendarUtils.initRecurrenceRule(eventUpdate.getOriginal().getRecurrenceRule());
            }
            if (null != eventUpdate.getUpdate().getRecurrenceRule()) {
                updatedRule = CalendarUtils.initRecurrenceRule(eventUpdate.getUpdate().getRecurrenceRule());
            }
        } catch (OXException e) {
            getLogger(RRuleDescriber.class).warn("Unexpected error initializing recurrence rule", e);
            return null;
        }
        if (null == originalRule || null == updatedRule || null == updatedRule.getUntil() || updatedRule.getUntil().equals(originalRule.getUntil())) {
            return null; // no or same UNTIL in rules
        }
        originalRule.setUntil(updatedRule.getUntil());
        if (false == originalRule.toString().equals(updatedRule.toString())) {
            return null; // further changes in rule
        }
        try {
            RecurrenceId lastOccurrence = services.getServiceSafe(RecurrenceService.class).getLastOccurrence(new DefaultRecurrenceData(eventUpdate.getUpdate()));
            return null != lastOccurrence ? lastOccurrence.getValue() : null;
        } catch (OXException e) {
            getLogger(RRuleDescriber.class).warn("Unexpected error determining new last occurrence", e);
            return null;
        }
    }

}
