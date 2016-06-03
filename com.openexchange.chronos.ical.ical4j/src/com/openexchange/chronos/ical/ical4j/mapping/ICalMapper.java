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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;

import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.alarm.AlarmMappings;
import com.openexchange.chronos.ical.ical4j.mapping.event.EventMappings;
import com.openexchange.chronos.ical.impl.ICalParametersImpl;
import com.openexchange.exception.OXException;

/**
 * {@link ICalMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICalMapper {
	
    /**
     * Initializes a new {@link ICalMapper}.
     */
	public ICalMapper() {
		super();
	}
	
    /**
     * Exports an event to a vEvent, optionally merging with an existing vEvent.
     *
     * @param event The contact to export
     * @param vEvent The vEvent to merge the event into, or <code>null</code> to export to a new vEvent
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The exported event as vEvent
     */
    public VEvent exportEvent(Event event, VEvent vEvent, ICalParameters parameters, List<OXException> warnings) {
        if (null == vEvent) {
            vEvent = new VEvent();
        }
        if (null == parameters) {
        	parameters = getDefaultParameters();
        }
        if (null == warnings) {
        	warnings = new ArrayList<OXException>();
        }
        for (ICalMapping<VEvent, Event> mapping : EventMappings.ALL) {
            mapping.export(event, vEvent, parameters, warnings);
        }
        return vEvent;
    }

    public VAlarm exportAlarm(Alarm alarm, VAlarm vAlarm, ICalParameters parameters, List<OXException> warnings) {
        if (null == vAlarm) {
            vAlarm = new VAlarm();
        }
        if (null == parameters) {
        	parameters = getDefaultParameters();
        }
        if (null == warnings) {
        	warnings = new ArrayList<OXException>();
        }
        for (ICalMapping<VAlarm, Alarm> mapping : AlarmMappings.ALL) {
            mapping.export(alarm, vAlarm, parameters, warnings);
        }
        return vAlarm;
    }

    /**
     * Imports a vEvent, optionally merging with an existing contact.
     *
     * @param vEvent The vEvent to import
     * @param event The contact to merge the vEvent into, or <code>null</code> to import as a new contact
     * @param parameters Further options to use, or <code>null</code> to stick with the defaults
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The imported vEvent as contact
     */
    public Event importVEvent(VEvent vEvent, Event event, ICalParameters parameters, List<OXException> warnings) {
        if (null == event) {
            event = new Event();
        }
        if (null == parameters) {
        	parameters = getDefaultParameters();
        }
        if (null == warnings) {
        	warnings = new ArrayList<OXException>();
        }
        for (ICalMapping<VEvent, Event> mapping : EventMappings.ALL) {
            mapping.importICal(vEvent, event, parameters, warnings);
        }
        return event;
    }
    
    public Alarm importVAlarm(VAlarm vAlarm, Alarm alarm, ICalParameters parameters, List<OXException> warnings) {
        if (null == alarm) {
            alarm = new Alarm();
        }
        if (null == parameters) {
        	parameters = getDefaultParameters();
        }
        if (null == warnings) {
        	warnings = new ArrayList<OXException>();
        }
        for (ICalMapping<VAlarm, Alarm> mapping : AlarmMappings.ALL) {
            mapping.importICal(vAlarm, alarm, parameters, warnings);
        }
        return alarm;
    }
    
    private ICalParameters getDefaultParameters() {
    	ICalParameters parameters = new ICalParametersImpl();
    	TimeZoneRegistry timeZoneRegistry = TimeZoneRegistryFactory.getInstance().createRegistry();
    	parameters.set(ICalParameters.TIMEZONE_REGISTRY, timeZoneRegistry);
		return parameters;    	
    }
	
}
