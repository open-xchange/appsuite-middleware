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

package com.openexchange.chronos.json.converter;

import java.util.EnumMap;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;

/**
 * {@link EventMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventMapper extends DefaultJsonMapper<Event, EventField> {

    private static final EventMapper INSTANCE = new EventMapper();

    private EventField[] mappedFields;

    /**
     * Gets the EventMapper instance.
     *
     * @return The EventMapper instance.
     */
    public static EventMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link EventMapper}.
     */
    private EventMapper() {
        super();
        this.mappedFields = mappings.keySet().toArray(newArray(mappings.keySet().size()));
    }

    public EventField[] getMappedFields() {
        return mappedFields;
    }

    @Override
    public Event newInstance() {
        return new Event();
    }

    @Override
    public EventField[] newArray(int size) {
        return new EventField[size];
    }

    @Override
    protected EnumMap<EventField, ? extends JsonMapping<? extends Object, Event>> createMappings() {
        EnumMap<EventField, JsonMapping<? extends Object, Event>> mappings = new
            EnumMap<EventField, JsonMapping<? extends Object, Event>>(EventField.class);
        mappings.put(EventField.ID, new StringMapping<Event>("id", DataObject.OBJECT_ID) {

            @Override
            public boolean isSet(Event object) {
                return object.containsId();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setId(value);
            }

            @Override
            public String get(Event object) {
                return object.getId();
            }

            @Override
            public void remove(Event object) {
                object.removeId();
            }
        });
        mappings.put(EventField.FOLDER_ID, new StringMapping<Event>("folder", FolderChildObject.FOLDER_ID) {

            @Override
            public boolean isSet(Event object) {
                return object.containsFolderId();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setFolderId(value);
            }

            @Override
            public String get(Event object) {
                return object.getFolderId();
            }

            @Override
            public void remove(Event object) {
                object.removeFolderId();
            }
        });
        //...
        mappings.put(EventField.SUMMARY, new StringMapping<Event>("summary", 901) {

            @Override
            public boolean isSet(Event object) {
                return object.containsSummary();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setSummary(value);
            }

            @Override
            public String get(Event object) {
                return object.getSummary();
            }

            @Override
            public void remove(Event object) {
                object.removeSummary();
            }
        });

        return mappings;
    }

}
