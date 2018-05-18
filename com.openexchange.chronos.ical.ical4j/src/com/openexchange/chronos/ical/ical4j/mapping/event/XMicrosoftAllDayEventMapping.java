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

package com.openexchange.chronos.ical.ical4j.mapping.event;

import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.ParserTools;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.extensions.outlook.AllDayEvent;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * {@link XMicrosoftAllDayEventMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class XMicrosoftAllDayEventMapping extends AbstractICalMapping<VEvent, Event> {

    /** Constant for the property with value <code>TRUE</code> */
    private static final AllDayEvent TRUE = new AllDayEvent(new ParameterList(true), AllDayEvent.FACTORY, "TRUE");

    /**
     * Initializes a new {@link XMicrosoftAllDayEventMapping}.
     */
	public XMicrosoftAllDayEventMapping() {
        super();
	}

    @Override
    public void export(Event object, VEvent component, ICalParameters parameters, List<OXException> warnings) {
        removeProperties(component, AllDayEvent.PROPERTY_NAME);
        if (null != object.getStartDate() && object.getStartDate().isAllDay() && isNotIgnored(parameters, AllDayEvent.PROPERTY_NAME)) {
            component.getProperties().add(TRUE);
        }
    }

    @Override
    public void importICal(VEvent component, Event object, ICalParameters parameters, List<OXException> warnings) {
        Property property = component.getProperty(AllDayEvent.PROPERTY_NAME);
        if (null != property && Boolean.valueOf(property.getValue()) && isNotIgnored(parameters, AllDayEvent.PROPERTY_NAME) &&
            ParserTools.isDateOrMidnight(component.getStartDate()) && ParserTools.isDateOrMidnight(component.getEndDate())) {
            if (null != object.getStartDate() && false == object.getStartDate().isAllDay()) {
                object.setStartDate(object.getStartDate().toAllDay());
            }
            if (null != object.getEndDate() && false == object.getEndDate().isAllDay()) {
                object.setEndDate(object.getEndDate().toAllDay());
            }
        }
    }

}
