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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.ical.ical4j.mapping.event;

import java.net.URISyntaxException;
import java.util.List;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.SentBy;

/**
 * {@link OrganizerMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class OrganizerMapping extends AbstractICalMapping<VEvent, Event> {

    @Override
    public void export(Event event, VEvent component, ICalParameters parameters, List<OXException> warnings) {
        Organizer organizer = event.getOrganizer();
        if (null == organizer) {
            removeProperties(component, Property.ORGANIZER);
        } else {
            net.fortuna.ical4j.model.property.Organizer property = component.getOrganizer();
            if (null == property) {
                property = new net.fortuna.ical4j.model.property.Organizer();
                component.getProperties().add(property);
            }
            try {
                exportOrganizer(organizer, property);
            } catch (URISyntaxException e) {
                addConversionWarning(warnings, e, Property.ORGANIZER, e.getMessage());
            }
        }
    }

    @Override
    public void importICal(VEvent vEvent, Event event, ICalParameters parameters, List<OXException> warnings) {
        net.fortuna.ical4j.model.property.Organizer property = vEvent.getOrganizer();
        if (null == property) {
            event.setOrganizer(null);
        } else {
            event.setOrganizer(importOrganizer(property));
        }
    }

    private static net.fortuna.ical4j.model.property.Organizer exportOrganizer(Organizer organizer, net.fortuna.ical4j.model.property.Organizer property) throws URISyntaxException {
        property.setValue(organizer.getUri());
        if (Strings.isNotEmpty(organizer.getCn())) {
            property.getParameters().replace(new Cn(organizer.getCn()));
        } else {
            property.getParameters().removeAll(Parameter.CN);
        }
        if (null != organizer.getSentBy() && Strings.isNotEmpty(organizer.getSentBy().getUri())) {
            property.getParameters().replace(new SentBy(organizer.getSentBy().getUri()));
        } else {
            property.getParameters().removeAll(Parameter.SENT_BY);
        }
        return property;
    }

    private Organizer importOrganizer(net.fortuna.ical4j.model.property.Organizer property) {
        Organizer organizer = new Organizer();
        if (null != property.getCalAddress()) {
            organizer.setUri(property.getCalAddress().toString());
        } else if (Strings.isNotEmpty(property.getValue())) {
            if (property.getValue().startsWith("mailto:")) {
                organizer.setUri(property.getValue());
            } else {
                organizer.setUri("mailto:" + property.getValue());
            }
        }
        Parameter cnParameter = property.getParameter(Parameter.CN);
        organizer.setCn(null != cnParameter ? cnParameter.getValue() : null);

        Parameter sentByParameter = property.getParameter(Parameter.SENT_BY);
        if (null != sentByParameter && Strings.isNotEmpty(sentByParameter.getValue())) {
            CalendarUser sentByUser = new CalendarUser();
            sentByUser.setUri(property.getValue());
            organizer.setSentBy(sentByUser);
        }
        return organizer;
    }

}
