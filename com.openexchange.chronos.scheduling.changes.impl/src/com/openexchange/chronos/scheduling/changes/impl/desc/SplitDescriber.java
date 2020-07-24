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

package com.openexchange.chronos.scheduling.changes.impl.desc;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.impl.ChangeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.FormattableArgument;
import com.openexchange.chronos.scheduling.changes.impl.MessageContext;
import com.openexchange.chronos.scheduling.changes.impl.SentenceImpl;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.regional.RegionalSettingsUtil;

/**
 * {@link SplitDescriber}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class SplitDescriber implements ChangeDescriber {

    @Override
    @NonNull
    public EventField[] getFields() {
        return new EventField[] { EventField.RELATED_TO };
    }

    @Override
    public Description describe(EventUpdate eventUpdate) {
        if (false == indicatesSplit(eventUpdate)) {
            return null;
        }
        DateTime startDate = eventUpdate.getUpdate().getStartDate();
        FormattableArgument argument = new FormattableArgument() {
            
            @Override
            public Object format(MessageContext context) {
                DateFormat dateFormat = RegionalSettingsUtil.getDateFormat(context.getRegionalSettings(), DateFormat.LONG, context.getLocale());
                dateFormat.setTimeZone(startDate.isAllDay() ? TimeZone.getTimeZone("UTC") : context.getTimeZone());
                return  dateFormat.format(new Date(startDate.getTimestamp()));
            }
        };
        SentenceImpl sentence = new SentenceImpl(Messages.HAS_SPLIT).add(argument, ArgumentType.UPDATED);
        return new DefaultDescription(Collections.singletonList(sentence), Arrays.asList(getFields()));
    }
    
    private static boolean indicatesSplit(EventUpdate eventUpdate) {
        RelatedTo originalRelatedTo = eventUpdate.getOriginal().getRelatedTo();
        RelatedTo updatedRelatedTo = eventUpdate.getUpdate().getRelatedTo();
        return CalendarUtils.isSeriesMaster(eventUpdate.getUpdate()) && 
            Objects.nonNull(updatedRelatedTo) &&
            false == Objects.equals(originalRelatedTo, updatedRelatedTo) &&
            "X-CALENDARSERVER-RECURRENCE-SET".equals(updatedRelatedTo.getRelType());
    }

}
