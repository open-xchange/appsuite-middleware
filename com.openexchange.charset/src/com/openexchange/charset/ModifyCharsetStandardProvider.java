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
    public static CharsetProvider[] modifyCharsetExtendedProvider() throws NoSuchFieldException, IllegalAccessException {
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
        } catch (final UnsupportedCharsetException e) {
            /*
             * Leave unchanged since fall-back charset "CP50220" is not support by JVM
             */
            org.slf4j.LoggerFactory.getLogger(ModifyCharsetStandardProvider.class).warn(
                new StringBuilder("Charset \"CP50220\" is not supported by JVM \"").append(System.getProperty("java.vm.vendor")).append(" v").append(
                    System.getProperty("java.vm.version")).append("\". Japanese encoding \"ISO-2022-JP\" not supported ! ! !").toString());
        }
        try {
            charsetProvider = new ISOReplacementCharsetProvider(null == charsetProvider ? backupCharsetProvider : charsetProvider);
        } catch (final UnsupportedCharsetException e) {
            /*
             * Leave unchanged since fall-back charset "WINDOWS-1252" is not support by JVM
             */
            org.slf4j.LoggerFactory.getLogger(ModifyCharsetStandardProvider.class).warn(
                new StringBuilder("Charset \"WINDOWS-1252\" is not supported by JVM \"").append(System.getProperty("java.vm.vendor")).append(" v").append(
                    System.getProperty("java.vm.version")).append("\".").toString());
        }
        try {
            charsetProvider = new ASCIIReplacementCharsetProvider(null == charsetProvider ? backupCharsetProvider : charsetProvider);
        } catch (final UnsupportedCharsetException e) {
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
