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
