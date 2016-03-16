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

package com.openexchange.charset;

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
        } catch (final java.lang.NoSuchFieldException e) {
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
                } catch (final NoSuchFieldException e) {
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
