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

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tools.mappings.Factory#newInstance()
     */
    @Override
    public MultifactorProvider newInstance() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tools.mappings.ArrayFactory#newArray(int)
     */
    @Override
    public MultifactorProviderField[] newArray(int size) {
        return new MultifactorProviderField[size];
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper#createMappings()
     */
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
                return object.isBackupProvider();
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
                return object.isBackupOnlyProvider();
            }

            @Override
            public void remove(MultifactorProvider object) {
                throw new UnsupportedOperationException();
            }
        });


        return mappings;
    }

}
