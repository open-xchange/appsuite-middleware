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
import com.openexchange.folderstorage.AltNameAwareFolder;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link AltNameLocalizedDatabaseFolder} - A locale-sensitive database folder with respect to alternative App Suite folder names.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AltNameLocalizedDatabaseFolder extends LocalizedDatabaseFolder implements AltNameAwareFolder {

    private static final long serialVersionUID = 3834568343115931304L;

    private final String altName;

    /**
     * Initializes a new cacheable {@link AltNameLocalizedDatabaseFolder} from given database folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor. Moreover passed database folder is considered to be
     * subscribed.
     *
     * @param folderObject The underlying database folder
     * @param altName The alternative App Suite folder name
     */
    public AltNameLocalizedDatabaseFolder(final FolderObject folderObject, final String altName) {
        this(folderObject, true, altName);
    }

    /**
     * Initializes a new {@link AltNameLocalizedDatabaseFolder} from given database folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor. Moreover passed database folder is considered to be
     * subscribed.
     *
     * @param folderObject The underlying database folder
     * @param cacheable <code>true</code> if this database folder is cacheable; otherwise <code>false</code>
     * @param altName The alternative App Suite folder name
     */
    public AltNameLocalizedDatabaseFolder(final FolderObject folderObject, final boolean cacheable, final String altName) {
        super(folderObject, cacheable);
        localizedNames = new NonBlockingHashMap<Locale, String>(8);
        this.altName = altName;
    }

    @Override
    public Object clone() {
        final AltNameLocalizedDatabaseFolder clone = (AltNameLocalizedDatabaseFolder) super.clone();
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
    public String getLocalizedName(Locale locale, boolean altName) {
        if (!altName) {
            return getLocalizedName(locale);
        }
        final String toTranslate = this.altName;
        if (null == toTranslate) {
            return getLocalizedName(locale);
        }
        return translationFor(toTranslate, locale);
    }

    @Override
    public boolean supportsAltName() {
        return true;
    }

}
