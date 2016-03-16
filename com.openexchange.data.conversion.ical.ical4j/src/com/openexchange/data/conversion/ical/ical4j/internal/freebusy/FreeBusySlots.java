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

package com.openexchange.data.conversion.ical.ical4j.internal.freebusy;

import java.util.List;
import java.util.TimeZone;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.FreeBusy;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.FreeBusyInformation;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.freebusy.FreeBusyInterval;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link FreeBusyIntervals}
 *
 * Emits free-busy intervals.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class FreeBusySlots extends AbstractVerifyingAttributeConverter<VFreeBusy, FreeBusyInformation> {

    public FreeBusySlots() {
        super();
    }

	@Override
	public boolean isSet(FreeBusyInformation u) {
		return null != u.getFreeBusyIntervals() && 0 < u.getFreeBusyIntervals().size();
	}

	@Override
	public void emit(Mode mode, int index, FreeBusyInformation u, VFreeBusy t, List<ConversionWarning> warnings, Context ctx, Object... args) throws ConversionError {
		for (FreeBusyInterval interval : u.getFreeBusyIntervals()) {
			FreeBusy freeBusy = new FreeBusy();
			freeBusy.getParameters().add(getFBType(interval));
			freeBusy.getPeriods().add(new Period(new DateTime(interval.getStartTime()), new DateTime(interval.getEndTime())));
			t.getProperties().add(freeBusy);
		}
	}

	/**
	 * Gets the suitable free/busy-type from the supplied free/busy-interval.
	 *
	 * @param interval The interval to get the free/busy type from
	 * @return The free/busy-type
	 */
	private static FbType getFBType(FreeBusyInterval interval) {
	    switch (interval.getStatus()) {
        case ABSENT:
            return FbType.BUSY_UNAVAILABLE;
        case FREE:
            return FbType.FREE;
        case TEMPORARY:
            return FbType.BUSY_TENTATIVE;
        default:
            return FbType.BUSY;

	    }
	}

	@Override
	public boolean hasProperty(VFreeBusy t) {
        return false;
	}

	@Override
	public void parse(int index, VFreeBusy t, FreeBusyInformation u, TimeZone timeZone, Context ctx, List<ConversionWarning> warnings) throws ConversionError {
		throw new UnsupportedOperationException("not implemented");
	}

}
