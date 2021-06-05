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
import com.openexchange.groupware.tools.mappings.json.BooleanMapping;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;
import com.openexchange.multifactor.MultifactorProvider;

/**
 * {@link MultifactorProviderMapper}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorProviderMapper extends DefaultJsonMapper<MultifactorProvider, MultifactorProviderField>{

    private static final MultifactorProviderMapper INSTANCE = new MultifactorProviderMapper();

    /**
     *
     * Initializes a new {@link MultifactorProviderMapper}.
     */
    private MultifactorProviderMapper() { }

    public static MultifactorProviderMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public MultifactorProvider newInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultifactorProviderField[] newArray(int size) {
        return new MultifactorProviderField[size];
    }

    @Override
    protected EnumMap<MultifactorProviderField, ? extends JsonMapping<? extends Object, MultifactorProvider>> createMappings() {
        EnumMap<MultifactorProviderField, JsonMapping<? extends Object, MultifactorProvider>> mappings =
            new EnumMap<MultifactorProviderField, JsonMapping<? extends Object, MultifactorProvider>>(MultifactorProviderField.class);

        mappings.put(MultifactorProviderField.NAME,new StringMapping<MultifactorProvider>(MultifactorProviderField.NAME.getJsonName(), null) {

            @Override
            public boolean isSet(MultifactorProvider object) {
                return true;
            }

            @Override
            public void set(MultifactorProvider object, String value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String get(MultifactorProvider object) {
                return object.getName();
            }

            @Override
            public void remove(MultifactorProvider object) {
                throw new UnsupportedOperationException();

            }
        });
        mappings.put(MultifactorProviderField.IS_BACKUP,new BooleanMapping<MultifactorProvider>(MultifactorProviderField.IS_BACKUP.getJsonName(), null) {

            @Override
            public boolean isSet(MultifactorProvider object) {
                return true;
            }

            @Override
            public void set(MultifactorProvider object, Boolean value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Boolean get(MultifactorProvider object) {
                return (object.isBackupProvider() ? Boolean.TRUE : Boolean.FALSE);
            }

            @Override
            public void remove(MultifactorProvider object) {
                throw new UnsupportedOperationException();
            }

        });
        mappings.put(MultifactorProviderField.IS_BACKUP_ONLY,new BooleanMapping<MultifactorProvider>(MultifactorProviderField.IS_BACKUP_ONLY.getJsonName(), null) {

            @Override
            public boolean isSet(MultifactorProvider object) {
                return true;
            }

            @Override
            public void set(MultifactorProvider object, Boolean value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Boolean get(MultifactorProvider object) {
                return object.isBackupOnlyProvider() ? Boolean.TRUE : Boolean.FALSE;
            }

            @Override
            public void remove(MultifactorProvider object) {
                throw new UnsupportedOperationException();
            }
        });


        return mappings;
    }

}
