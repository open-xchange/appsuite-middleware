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

package com.openexchange.multifactor.json.converter.mapper;

import java.util.EnumMap;
import java.util.Map;
import com.openexchange.groupware.tools.mappings.json.BooleanMapping;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.MapMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;
import com.openexchange.multifactor.DefaultMultifactorDevice;
import com.openexchange.multifactor.MultifactorDevice;

/**
 * {@link MultifactorDeviceMapper}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorDeviceMapper extends DefaultJsonMapper<MultifactorDevice, MultifactorDeviceField> {

    private static final MultifactorDeviceMapper INSTANCE = new MultifactorDeviceMapper();

    /**
     *
     * Initializes a new {@link MultifactorDeviceMapper}.
     */
    private MultifactorDeviceMapper() { }

    public static MultifactorDeviceMapper getInstance() {
       return INSTANCE;
    }

    @Override
    public MultifactorDevice newInstance() {
        return new DefaultMultifactorDevice();
    }

    @Override
    public MultifactorDeviceField[] newArray(int size) {
        return new MultifactorDeviceField[size];
    }

    @Override
    protected EnumMap<MultifactorDeviceField, ? extends JsonMapping<? extends Object, MultifactorDevice>> createMappings() {
        EnumMap<MultifactorDeviceField, JsonMapping<? extends Object, MultifactorDevice>> mappings =
            new EnumMap<MultifactorDeviceField, JsonMapping<? extends Object, MultifactorDevice>>(MultifactorDeviceField.class);

        mappings.put(MultifactorDeviceField.ID, new StringMapping<MultifactorDevice>(MultifactorDeviceField.ID.getJsonName(), null) {

            @Override
            public void set(MultifactorDevice object, String value) {
               object.setId(value);
            }

            @Override
            public void remove(MultifactorDevice object) {
                object.setId(null);
            }

            @Override
            public boolean isSet(MultifactorDevice object) {
                return object.getId() != null;
            }

            @Override
            public String get(MultifactorDevice object) {
                return object.getId();
            }
        });
        mappings.put(MultifactorDeviceField.PROVIDER_NAME,new StringMapping<MultifactorDevice>(MultifactorDeviceField.PROVIDER_NAME.getJsonName(), null) {

            @Override
            public boolean isSet(MultifactorDevice object) {
                return object.getProviderName() != null;
            }

            @Override
            public void set(MultifactorDevice object, String value) {
                object.setProviderName(value);
            }

            @Override
            public String get(MultifactorDevice object) {
                return object.getProviderName();
            }

            @Override
            public void remove(MultifactorDevice object) {
                object.setProviderName(null);
            }
        });
        mappings.put(MultifactorDeviceField.NAME,new StringMapping<MultifactorDevice>(MultifactorDeviceField.NAME.getJsonName(), null) {

            @Override
            public boolean isSet(MultifactorDevice object) {
                return object.getName() != null;
            }

            @Override
            public void set(MultifactorDevice object, String value) {
                object.setName(value);
            }

            @Override
            public String get(MultifactorDevice object) {
                return object.getName();
            }

            @Override
            public void remove(MultifactorDevice object) {
                object.setName(null);
            }
        });
        mappings.put(MultifactorDeviceField.ENABLED, new BooleanMapping<MultifactorDevice>(MultifactorDeviceField.ENABLED.getJsonName(), null) {

            @Override
            public boolean isSet(MultifactorDevice object) {
                return object.isEnabled() != null;
            }

            @Override
            public void set(MultifactorDevice object, Boolean value) {
                object.enable(value);
            }

            @Override
            public Boolean get(MultifactorDevice object) {
                return object.isEnabled();
            }

            @Override
            public void remove(MultifactorDevice object) {
                object.enable(null);
            }
        });
        mappings.put(MultifactorDeviceField.IS_BACKUP, new BooleanMapping<MultifactorDevice>(MultifactorDeviceField.IS_BACKUP.getJsonName(),null) {

            @Override
            public boolean isSet(MultifactorDevice object) {
                return object.containsBackup();
            }

            @Override
            public void set(MultifactorDevice object, Boolean value) {
                if (value != null) {
                    object.setBackup(value.booleanValue());
                } else {
                    remove(object);
                }
            }

            @Override
            public Boolean get(MultifactorDevice object) {
                return (object.isBackup() ? Boolean.TRUE : Boolean.FALSE);
            }

            @Override
            public void remove(MultifactorDevice object) {
                object.removeBackup();
            }
        });
        mappings.put(MultifactorDeviceField.PARAMETERS, new MapMapping<MultifactorDevice>(MultifactorDeviceField.PARAMETERS.getJsonName(), null) {

            @Override
            public boolean isSet(MultifactorDevice object) {
                return object.containsParameters();
            }

            @Override
            public void set(MultifactorDevice object, Map<String, Object> value) {
                object.setParameters(value);
            }

            @Override
            public Map<String, Object> get(MultifactorDevice object) {
                // Never return device parameters
                return null;
            }

            @Override
            public void remove(MultifactorDevice object) {
                object.setParameters(null);
            }
        });
        return mappings;
    }
}
