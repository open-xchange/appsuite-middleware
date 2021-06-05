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
import java.nio.charset.spi.CharsetProvider;

/**
 * {@link ModifyCharsetExtendedProvider} - Modifies the <code>charsetExtendedProvider</code> field in {@link java.nio.charset.Charset}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ModifyCharsetExtendedProvider {

    private static volatile Field extendedProviderField;
    private static volatile Boolean isFinal;

    /**
     * Initializes a new {@link ModifyCharsetExtendedProvider}.
     */
    private ModifyCharsetExtendedProvider() {
        super();
    }

    /**
     * Modifies field <code>java.nio.charset.Charset.extendedProvider</code>
     *
     * @throws NoSuchFieldException If field "extendedProvider" does not exist
     * @throws IllegalAccessException If field "extendedProvider" is not accessible
     * @return An array of {@link CharsetProvider} of length <code>2</code>; the first index is occupied by replaced {@link CharsetProvider}
     *         instance, the second with new instance
     */
    public static CharsetProvider[] modifyCharsetExtendedProvider() throws NoSuchFieldException, IllegalAccessException {
        /*
         * Force initialization of Charset.extendedProvider. Otherwise target field "extendedProvider" is not initialized.
         */
        java.nio.charset.Charset.isSupported("X-Unknown-Charset");
        /*
         * Modify java.nio.charset.Charset class
         */
        Field extendedProviderField = null;
        boolean isFinal = false;
        try {
            extendedProviderField = java.nio.charset.Charset.class.getDeclaredField("extendedProvider");
        } catch (java.lang.NoSuchFieldException e) {
            // Java v8 ?
            Class<?> extendedProviderHolderClass = null;
            final Class<?>[] declaredClasses = java.nio.charset.Charset.class.getDeclaredClasses();
            for (int i = 0; null == extendedProviderHolderClass && i < declaredClasses.length; i++) {
                final Class<?> subclass = declaredClasses[i];
                if (subclass.getCanonicalName().endsWith("ExtendedProviderHolder")) {
                    extendedProviderHolderClass = subclass;
                }
            }
            if (null == extendedProviderHolderClass) {
                throw e;
            }
            extendedProviderField = extendedProviderHolderClass.getDeclaredField("extendedProvider");
            isFinal = true;
        }
        extendedProviderField.setAccessible(true);
        ModifyCharsetExtendedProvider.extendedProviderField = extendedProviderField;
        ModifyCharsetExtendedProvider.isFinal = Boolean.valueOf(isFinal);
        /*
         * Backup old charset provider
         */
        final CharsetProvider backupCharsetProvider = (CharsetProvider) extendedProviderField.get(null);
        /*
         * Add previous charset provider
         */
        final CharsetProvider collectionCharsetProvider;
        if (null == backupCharsetProvider) {
            collectionCharsetProvider = new CollectionCharsetProvider();
        } else {
            collectionCharsetProvider = new CollectionCharsetProvider((CharsetProvider) extendedProviderField.get(null));
        }
        /*
         * Reinitialize field
         */
        if (isFinal) {
            ReflectionHelper.setStaticFinalField(extendedProviderField, collectionCharsetProvider);
        } else {
            extendedProviderField.set(null, collectionCharsetProvider);
        }
        return new CharsetProvider[] { backupCharsetProvider, collectionCharsetProvider };
    }

    /**
     * Restores field <code>java.nio.charset.Charset.extendedProvider</code>
     *
     * @param provider The {@link CharsetProvider} instance to restore to
     * @throws IllegalAccessException If field "extendedProvider" is not accessible
     */
    public static void restoreCharsetExtendedProvider(final CharsetProvider provider) throws IllegalAccessException {
        /*
         * Restore java.nio.charset.Charset class
         */
        final Field extendedProviderField = ModifyCharsetExtendedProvider.extendedProviderField;
        if (null != extendedProviderField) {
            /*
             * Assign previously remembered charset provider
             */
            if (ModifyCharsetExtendedProvider.isFinal.booleanValue()) {
                try {
                    ReflectionHelper.setStaticFinalField(extendedProviderField, provider);
                } catch (NoSuchFieldException e) {
                    // Cannot occur
                }
            } else {
                extendedProviderField.set(null, provider);
            }
            ModifyCharsetExtendedProvider.extendedProviderField = null;
            ModifyCharsetExtendedProvider.isFinal = null;
        }
    }

}
