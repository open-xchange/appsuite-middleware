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

package com.openexchange.folderstorage.contact;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link ContactsStorageFolder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsStorageFolder extends AbstractFolder implements ParameterizedFolder {

    private static final long serialVersionUID = 8228162262139020282L;
    private final Map<FolderField, FolderProperty> properties;
    private final boolean localizable;

    /**
     * Initializes a new contacts folder as used by the internal folder storage.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The content type to take over
     */
    public ContactsStorageFolder(String treeId, ContentType contentType) {
        this(treeId, contentType, false);
    }

    /**
     * Initializes a new contacts folder as used by the internal folder storage.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The content type to take over
     * @param localizable <code>true</code> to be localizable; otherwise <code>false</code>
     */
    public ContactsStorageFolder(String treeId, ContentType contentType, boolean localizable) {
        super();
        this.localizable = localizable;
        this.properties = new HashMap<FolderField, FolderProperty>();
        setTreeID(treeId);
        setSubscribed(true);
        setContentType(contentType);
        setDefaultType(contentType.getModule());
        setMeta(null);
    }

    @Override
    public boolean isGlobalID() {
        return false;
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public void setProperty(FolderField name, Object value) {
        if (null == value) {
            properties.remove(name);
        } else {
            properties.put(name, new FolderProperty(name.getName(), value));
        }
    }

    @Override
    public Map<FolderField, FolderProperty> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public String getLocalizedName(final Locale locale) {
        return localizable ? translationFor(getName(), locale) : super.getLocalizedName(locale);
    }

    /**
     * Gets the translation for specified name.
     *
     * @param toTranslate The name to translate
     * @param locale The locale
     * @return The translation or specified name
     */
    protected String translationFor(final String toTranslate, final Locale locale) {
        return StringHelper.valueOf(null == locale ? LocaleTools.DEFAULT_LOCALE : locale).getString(toTranslate);
    }

    @Override
    public String toString() {
        return "ContactsStorageFolder [account=" + accountId + ", id=" + id + ", name=" + name + "]";
    }
}
