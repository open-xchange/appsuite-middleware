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
