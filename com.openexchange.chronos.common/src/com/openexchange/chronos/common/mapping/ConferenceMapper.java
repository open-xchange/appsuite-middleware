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

package com.openexchange.chronos.common.mapping;

import static com.openexchange.java.Autoboxing.B;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ConferenceField;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.DefaultMapping;
import com.openexchange.groupware.tools.mappings.Mapping;

/**
 * {@link ConferenceMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class ConferenceMapper extends DefaultMapper<Conference, ConferenceField> {

    private static final ConferenceMapper INSTANCE = new ConferenceMapper();

    /**
     * Gets the mapper instance.
     *
     * @return The instance.
     */
    public static ConferenceMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link ConferenceMapper}.
     */
    private ConferenceMapper() {
        super();
    }

	@Override
    public Conference newInstance() {
        return new Conference();
	}

	@Override
    public ConferenceField[] newArray(int size) {
        return new ConferenceField[size];
	}

    @Override
    protected EnumMap<ConferenceField, ? extends Mapping<? extends Object, Conference>> getMappings() {
        EnumMap<ConferenceField, Mapping<? extends Object, Conference>> mappings =
            new EnumMap<ConferenceField, Mapping<? extends Object, Conference>>(ConferenceField.class);

        mappings.put(ConferenceField.ID, new DefaultMapping<Integer, Conference>() {

            @Override
            public void set(Conference conference, Integer value) {
                conference.setId(null == value ? 0 : value.intValue());
            }

            @Override
            public boolean isSet(Conference conference) {
                return conference.containsId();
            }

            @Override
            public Integer get(Conference conference) {
                return Integer.valueOf(conference.getId());
            }

            @Override
            public void remove(Conference conference) {
                conference.removeId();
            }
        });
        mappings.put(ConferenceField.URI, new DefaultMapping<String, Conference>() {

            @Override
            public void set(Conference conference, String value) {
                conference.setUri(value);
            }

            @Override
            public boolean isSet(Conference conference) {
                return conference.containsUri();
            }

            @Override
            public String get(Conference conference) {
                return conference.getUri();
            }

            @Override
            public void remove(Conference conference) {
                conference.removeUri();
            }
        });
        mappings.put(ConferenceField.LABEL, new DefaultMapping<String, Conference>() {

            @Override
            public void set(Conference conference, String value) {
                conference.setLabel(value);
            }

            @Override
            public boolean isSet(Conference conference) {
                return conference.containsLabel();
            }

            @Override
            public String get(Conference conference) {
                return conference.getLabel();
            }

            @Override
            public void remove(Conference conference) {
                conference.removeLabel();
            }
        });
        mappings.put(ConferenceField.FEATURES, new DefaultMapping<List<String>, Conference>() {

            @Override
            public boolean isSet(Conference conference) {
                return conference.containsFeatures();
            }

            @Override
            public void set(Conference conference, List<String> value) throws OXException {
                conference.setFeatures(value);
            }

            @Override
            public List<String> get(Conference conference) {
                return conference.getFeatures();
            }

            @Override
            public void remove(Conference conference) {
                conference.removeFeatures();
            }
        });
        mappings.put(ConferenceField.EXTENDED_PARAMETERS, new DefaultMapping<List<ExtendedPropertyParameter>, Conference>() {

            @Override
            public void copy(Conference from, Conference to) throws OXException {
                List<ExtendedPropertyParameter> value = get(from);
                if (null == value) {
                    set(to, null);
                } else {
                    List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>(value.size());
                    for (ExtendedPropertyParameter parameter : value) {
                        parameters.add(new ExtendedPropertyParameter(parameter));
                    }
                    set(to, parameters);
                }
            }

            @Override
            public boolean isSet(Conference object) {
                return object.containsExtendedParameters();
            }

            @Override
            public void set(Conference object, List<ExtendedPropertyParameter> value) throws OXException {
                object.setExtendedParameters(value);
            }

            @Override
            public List<ExtendedPropertyParameter> get(Conference object) {
                return object.getExtendedParameters();
            }

            @Override
            public void remove(Conference object) {
                object.removeExtendedParameters();
            }

            @Override
            public boolean equals(Conference object1, Conference object2) {
                return CalendarUtils.matches(object1.getExtendedParameters(), object2.getExtendedParameters(),
                        (item1, item2) -> B(null == item1 ? null == item2 : item1.equals(item2)));
            }
        });

        return mappings;
	}

}
