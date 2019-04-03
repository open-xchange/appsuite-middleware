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

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tools.mappings.Factory#newInstance()
     */
    @Override
    public MultifactorDevice newInstance() {
        return new DefaultMultifactorDevice();
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tools.mappings.ArrayFactory#newArray(int)
     */
    @Override
    public MultifactorDeviceField[] newArray(int size) {
        return new MultifactorDeviceField[size];
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper#createMappings()
     */
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
                object.setBackup(value);
            }

            @Override
            public Boolean get(MultifactorDevice object) {
                return object.isBackup() ;
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
