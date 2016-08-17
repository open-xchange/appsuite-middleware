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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.property.Attendee;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.FreeBusyInformation;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link FreeBusyAttendees}
 *
 * Parses the attendees for a {@link FreeBusyInformation}s attendee.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusyAttendees extends AbstractVerifyingAttributeConverter<VFreeBusy, FreeBusyInformation> {

    /**
     * Initializes a new {@link FreeBusyAttendees}.
     */
    public FreeBusyAttendees() {
        super();
    }

    @Override
    public boolean isSet(FreeBusyInformation freeBusyInformation) {
        return null != freeBusyInformation.getAttendee();
    }

    @Override
    public void emit(Mode mode, int index, FreeBusyInformation freeBusyInformation, VFreeBusy vFreeBusy, List<ConversionWarning> warnings,
        Context ctx, Object... args) throws ConversionError {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean hasProperty(VFreeBusy vFreeBusy) {
        PropertyList properties = vFreeBusy.getProperties(Property.ATTENDEE);
        return null != properties && 0 < properties.size();
    }

    @Override
    public void parse(int index, VFreeBusy vFreeBusy, FreeBusyInformation freeBusyInformation, TimeZone timeZone, Context ctx,
        List<ConversionWarning> warnings) throws ConversionError {
        List<String> attendees = new ArrayList<String>();
        PropertyList properties = vFreeBusy.getProperties(Property.ATTENDEE);
        if (null != properties && 0 < properties.size()) {
            for (int i = 0; i < properties.size(); i++) {
                attendees.add(((Attendee) properties.get(i)).getValue());
            }
        }
        freeBusyInformation.setAttendees(attendees.toArray(new String[attendees.size()]));
    }

}
