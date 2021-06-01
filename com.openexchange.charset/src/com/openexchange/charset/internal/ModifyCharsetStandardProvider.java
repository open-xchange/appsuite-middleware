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

package com.openexchange.charset.internal;

import java.lang.reflect.Field;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;

/**
 * {@link ModifyCharsetStandardProvider} - Modifies the <code>charsetExtendedProvider</code> field in {@link java.nio.charset.Charset}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ModifyCharsetStandardProvider {

    private static volatile Field standardProviderField;

    /**
     * Initializes a new {@link ModifyCharsetStandardProvider}.
     */
    private ModifyCharsetStandardProvider() {
        super();
    }

    /**
     * Modifies field <code>java.nio.charset.Charset.standardProvider</code>
     *
     * @throws NoSuchFieldException If field "standardProvider" does not exist
     * @throws IllegalAccessException If field "standardProvider" is not accessible
     * @return An array of {@link CharsetProvider} of length <code>2</code>; the first index is occupied by replaced {@link CharsetProvider}
     *         instance, the second with new instance
     */
    public static CharsetProvider[] modifyCharsetStandardProvider() throws NoSuchFieldException, IllegalAccessException {
        /*
         * Modify java.nio.charset.Charset class
         */
        final Field standardProviderField = java.nio.charset.Charset.class.getDeclaredField("standardProvider");
        standardProviderField.setAccessible(true);
        ModifyCharsetStandardProvider.standardProviderField = standardProviderField;
        /*
         * Backup old charset provider
         */
        final CharsetProvider backupCharsetProvider = (CharsetProvider) standardProviderField.get(null);
        /*
         * Initialize new standard charset provider
         */
        CharsetProvider charsetProvider = null;
        try {
            charsetProvider = new AsianReplacementCharsetProvider(backupCharsetProvider);
        } catch (UnsupportedCharsetException e) {
            /*
             * Leave unchanged since fall-back charset "CP50220" is not support by JVM
             */
            org.slf4j.LoggerFactory.getLogger(ModifyCharsetStandardProvider.class).warn(
                new StringBuilder("Charset \"CP50220\" is not supported by JVM \"").append(System.getProperty("java.vm.vendor")).append(" v").append(
                    System.getProperty("java.vm.version")).append("\". Japanese encoding \"ISO-2022-JP\" not supported ! ! !").toString());
        }
        try {
            charsetProvider = new ASCIIReplacementCharsetProvider(null == charsetProvider ? backupCharsetProvider : charsetProvider);
        } catch (UnsupportedCharsetException e) {
            /*
             * Leave unchanged since fall-back charset "WINDOWS-1252" is not support by JVM
             */
            org.slf4j.LoggerFactory.getLogger(ModifyCharsetStandardProvider.class).warn(
                new StringBuilder("Charset \"WINDOWS-1252\" is not supported by JVM \"").append(System.getProperty("java.vm.vendor")).append(" v").append(
                    System.getProperty("java.vm.version")).append("\".").toString());
        }
        if (null == charsetProvider) {
            return null;
        }
        charsetProvider = new CachingCharsetProvider(charsetProvider);
        /*
         * Reinitialize field
         */
        standardProviderField.set(null, charsetProvider);
        return new CharsetProvider[] { backupCharsetProvider, charsetProvider };
    }

    /**
     * Restores field <code>java.nio.charset.Charset.standardProvider</code>
     *
     * @param provider The {@link CharsetProvider} instance to restore to
     * @throws IllegalAccessException If field "standardProvider" is not accessible
     */
    public static void restoreCharsetExtendedProvider(final CharsetProvider provider) throws IllegalAccessException {
        /*
         * Restore java.nio.charset.Charset class
         */
        final Field standardProviderField = ModifyCharsetStandardProvider.standardProviderField;
        if (null != standardProviderField) {
            /*
             * Assign previously remembered charset provider
             */
            standardProviderField.set(null, provider);
            ModifyCharsetStandardProvider.standardProviderField = null;
        }
    }

}
