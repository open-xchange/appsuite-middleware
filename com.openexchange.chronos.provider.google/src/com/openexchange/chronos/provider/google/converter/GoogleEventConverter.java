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

package com.openexchange.chronos.provider.google.converter;

import java.util.HashMap;
import java.util.Map;
import org.dmfs.rfc5545.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.Mapping;

/**
 * {@link GoogleEventConverter}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleEventConverter {

    private static final GoogleEventConverter INSTANCE = new GoogleEventConverter();

    private static final EventMapper MAPPER = EventMapper.getInstance();

    private Map<EventField, GoogleMapping> mappings = null;

    public static GoogleEventConverter getInstance() {
        return INSTANCE;
    }


    /**
     * Initializes a new {@link GoogleEventConverter}.
     */
    public GoogleEventConverter() {
        super();
        mappings = createMappings();
    }

    public com.google.api.services.calendar.model.Event convertToGoogleEvent(Event from) throws OXException {
        com.google.api.services.calendar.model.Event to = new com.google.api.services.calendar.model.Event();
        for(GoogleMapping mapping: mappings.values()){
            mapping.deserialize(from, to);
        }
        return to;
    }

    public Event convertToEvent(com.google.api.services.calendar.model.Event from) throws OXException {
        Event to = new Event();
        for(GoogleMapping mapping: mappings.values()){
            mapping.serialize(to, from);
        }
        return to;
    }

    private Map<EventField, GoogleMapping> createMappings(){
        final EventMapper mapper = MAPPER;
        Map<EventField, GoogleMapping> result = new HashMap<>();

        result.put(EventField.ID, new GoogleMapping() {

            @SuppressWarnings("unchecked")
            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) throws OXException {
                ((Mapping<String, Event>)MAPPER.get(EventField.ID)).set(to, from.getId());
            }

            @Override
            public void deserialize(Event from, com.google.api.services.calendar.model.Event to) throws OXException {
                to.setId((String) MAPPER.get(EventField.ID).get(from));
            }
        });
        result.put(EventField.START_DATE, new GoogleMapping() {

            @SuppressWarnings("unchecked")
            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) throws OXException {
                ((Mapping<DateTime, Event>)MAPPER.get(EventField.START_DATE)).set(to, new DateTime(from.getStart().getDateTime().getValue()));
            }

            @Override
            public void deserialize(Event from, com.google.api.services.calendar.model.Event to) throws OXException {
                EventDateTime eventDateTime = new EventDateTime();
                eventDateTime.setDateTime(new com.google.api.client.util.DateTime(from.getStartDate().getTimestamp()));
                to.setStart(eventDateTime);
            }
        });
        result.put(EventField.END_DATE, new GoogleMapping() {

            @SuppressWarnings("unchecked")
            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) throws OXException {
                ((Mapping<DateTime, Event>)MAPPER.get(EventField.END_DATE)).set(to, new DateTime(from.getStart().getDateTime().getValue()));
            }

            @Override
            public void deserialize(Event from, com.google.api.services.calendar.model.Event to) throws OXException {
                EventDateTime eventDateTime = new EventDateTime();
                eventDateTime.setDateTime(new com.google.api.client.util.DateTime(from.getStartDate().getTimestamp()));
                to.setEnd(eventDateTime);
            }
        });

        return result;
    }

    private interface GoogleMapping {

        public void serialize(Event to, com.google.api.services.calendar.model.Event from) throws OXException;

        public void deserialize(Event from, com.google.api.services.calendar.model.Event to) throws OXException;

    }

}
