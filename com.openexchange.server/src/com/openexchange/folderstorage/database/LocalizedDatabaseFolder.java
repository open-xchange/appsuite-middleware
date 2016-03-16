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

package com.openexchange.folderstorage.database;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link LocalizedDatabaseFolder} - A locale-sensitive database folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LocalizedDatabaseFolder extends DatabaseFolder {

    private static final long serialVersionUID = 3830248343115931304L;

    /** Cache for already translated names */
    protected ConcurrentMap<Locale, String> localizedNames;

    /**
     * Initializes a new cacheable {@link LocalizedDatabaseFolder} from given database folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor. Moreover passed database folder is considered to be
     * subscribed.
     *
     * @param folderObject The underlying database folder
     */
    public LocalizedDatabaseFolder(final FolderObject folderObject) {
        this(folderObject, true);
    }

    /**
     * Initializes a new {@link LocalizedDatabaseFolder} from given database folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor. Moreover passed database folder is considered to be
     * subscribed.
     *
     * @param folderObject The underlying database folder
     * @param cacheable <code>true</code> if this database folder is cacheable; otherwise <code>false</code>
     */
    public LocalizedDatabaseFolder(final FolderObject folderObject, final boolean cacheable) {
        super(folderObject, cacheable);
        localizedNames = new NonBlockingHashMap<Locale, String>(8);
    }

    @Override
    public Object clone() {
        final LocalizedDatabaseFolder clone = (LocalizedDatabaseFolder) super.clone();
        // Locale-sensitive names
        final ConcurrentMap<Locale, String> thisMap = localizedNames;
        if (null == localizedNames) {
            clone.localizedNames = null;
        } else {
            final ConcurrentMap<Locale, String> cloneMap = new NonBlockingHashMap<Locale, String>(thisMap.size());
            for (final Map.Entry<Locale, String> entry : thisMap.entrySet()) {
                cloneMap.put(entry.getKey(), entry.getValue());
            }
            clone.localizedNames = cloneMap;
        }
        // Return
        return clone;
    }

    @Override
    public String getLocalizedName(final Locale locale) {
        return translationFor(getName(), locale);
    }

    /**
     * Gets the translation for specified name.
     *
     * @param toTranslate The name to translate
     * @param locale The locale
     * @return The translation or specified name
     */
    protected String translationFor(final String toTranslate, final Locale locale) {
        final Locale loc = null == locale ? LocaleTools.DEFAULT_LOCALE : locale;
        String translation = localizedNames.get(loc);
        if (null == translation) {
            if (null == toTranslate) {
                return null;
            }
            final String ntranslation = StringHelper.valueOf(loc).getString(toTranslate);
            translation = localizedNames.putIfAbsent(loc, ntranslation);
            if (null == translation) {
                translation = ntranslation;
            }
        }
        return translation;
    }

}
