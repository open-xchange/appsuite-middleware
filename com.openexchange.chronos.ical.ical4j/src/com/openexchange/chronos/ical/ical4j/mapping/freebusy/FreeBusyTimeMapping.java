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

package com.openexchange.chronos.ical.ical4j.mapping.freebusy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.chronos.FreeBusyData;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.FreeBusy;

/**
 * {@link FreeBusyTimeMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class FreeBusyTimeMapping extends AbstractICalMapping<VFreeBusy, FreeBusyData> {

	@Override
    public void export(FreeBusyData object, VFreeBusy component, ICalParameters parameters, List<OXException> warnings) {
        List<FreeBusyTime> freeBusyTimes = object.getFreeBusyTimes();
        if (null == freeBusyTimes || 0 == freeBusyTimes.size()) {
            removeProperties(component, Property.FREEBUSY);
		} else {
            removeProperties(component, Property.FREEBUSY);
            for (FreeBusyTime freeBusyTime : freeBusyTimes) {
                component.getProperties().add(exportFreeBusyTime(freeBusyTime));
            }
		}
	}

    @Override
    public void importICal(VFreeBusy component, FreeBusyData object, ICalParameters parameters, List<OXException> warnings) {
        PropertyList properties = component.getProperties(Property.FREEBUSY);
        if (null == properties || 0 == properties.size()) {
            object.setFreeBusyTimes(null);
        } else {
            List<FreeBusyTime> freeBusyTimes = new ArrayList<FreeBusyTime>(properties.size());
            for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
                freeBusyTimes.addAll(importFreeBusy((FreeBusy) iterator.next()));
            }
            object.setFreeBusyTimes(freeBusyTimes);
        }
    }

    private static FreeBusy exportFreeBusyTime(FreeBusyTime freeBusyTime) {
        FreeBusy freeBusy = new FreeBusy();
        DateTime start = new DateTime(true);
        start.setTime(freeBusyTime.getStartTime().getTime());
        DateTime end = new DateTime(true);
        end.setTime(freeBusyTime.getEndTime().getTime());
        freeBusy.getPeriods().add(new Period(start, end));
        if (null != freeBusyTime.getFbType()) {
            freeBusy.getParameters().add(new FbType(freeBusyTime.getFbType().getValue()));
        }
        return freeBusy;
    }

    private static List<FreeBusyTime> importFreeBusy(FreeBusy freeBusy) {
        String value = optParameterValue(freeBusy, Parameter.FBTYPE);
        com.openexchange.chronos.FbType fbType = null == value ? com.openexchange.chronos.FbType.BUSY : new com.openexchange.chronos.FbType(value);
        PeriodList periods = freeBusy.getPeriods();
        List<FreeBusyTime> freeBusyTimes = new ArrayList<FreeBusyTime>(periods.size());
        for (Iterator<?> iterator = periods.iterator(); iterator.hasNext();) {
            Period period = (Period) iterator.next();
            freeBusyTimes.add(new FreeBusyTime(fbType, period.getRangeStart(), period.getRangeEnd()));
        }
        return freeBusyTimes;
    }

}
