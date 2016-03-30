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

package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import java.net.URISyntaxException;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link RequestParticipants}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class RequestParticipants<T extends CalendarComponent, U extends CalendarObject> extends Participants<T, U> {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RequestParticipants.class);

    @Override
    protected void addUserAttendee(final int index, final UserParticipant userParticipant, final Context ctx, final T component, final U cObj) throws ConversionError {
        if (userParticipant.getIdentifier() == cObj.getCreatedBy()) {
            super.addUserAttendee(index, userParticipant, ctx, component, cObj);
        } else {
            final Attendee attendee = new Attendee();
            try {
                attendee.setValue("mailto:" + resolveUserMail(index, userParticipant, ctx));
                final ParameterList parameters = attendee.getParameters();
                parameters.add(CuType.INDIVIDUAL);
                parameters.add(PartStat.NEEDS_ACTION);
                parameters.add(Role.REQ_PARTICIPANT);
                parameters.add(Rsvp.TRUE);
                component.getProperties().add(attendee);
            } catch (final URISyntaxException e) {
                LOG.error("", e);
            }
        }
    }

}
