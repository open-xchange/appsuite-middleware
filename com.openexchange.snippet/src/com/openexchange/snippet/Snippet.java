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

package com.openexchange.snippet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link Snippet} - Represents arbitrary (textual) content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Snippet {

    /**
     * The property name for the identifier.
     */
    public static final String PROP_ID = Property.ID.getPropName();

    /**
     * The property name for the account identifier.
     */
    public static final String PROP_ACCOUNT_ID = Property.ACCOUNT_ID.getPropName();

    /**
     * The property name for the type; e.g. <code>"signature"</code>.
     */
    public static final String PROP_TYPE = Property.TYPE.getPropName();

    /**
     * The property name for the display name.
     */
    public static final String PROP_DISPLAY_NAME = Property.DISPLAY_NAME.getPropName();

    /**
     * The property name for the module identifier; e.g. <code>"com.openexchange.mail"</code>.
     */
    public static final String PROP_MODULE = Property.MODULE.getPropName();

    /**
     * The property name for the creator.
     */
    public static final String PROP_CREATED_BY = Property.CREATED_BY.getPropName();

    /**
     * The property name for the shared flag.
     */
    public static final String PROP_SHARED = Property.SHARED.getPropName();

    /**
     * The property name for the optional miscellaneous JSON data.
     */
    public static final String PROP_MISC = Property.MISC.getPropName();

    /**
     * The set of named properties.
     */
    public static final Set<String> NAMED_PROPERTIES = ImmutableSet.of(
        PROP_ACCOUNT_ID,
        PROP_CREATED_BY,
        PROP_DISPLAY_NAME,
        PROP_ID,
        PROP_MISC,
        PROP_MODULE,
        PROP_SHARED,
        PROP_TYPE);

    /**
     * Gets the identifier.
     *
     * @return The identifier.
     */
    String getId();

    /**
     * Gets the account identifier (if applicable for associated module).
     *
     * @return The account identifier or <code>-1</code>.
     */
    int getAccountId();

    /**
     * Gets the module identifier; e.g. <code>"com.openexchange.mail"</code>.
     *
     * @return The module identifier
     */
    String getModule();

    /**
     * Gets the type; e.g. <code>"signature"</code>.
     *
     * @return The type
     */
    String getType();

    /**
     * Gets the display name.
     *
     * @return The display name
     */
    String getDisplayName();

    /**
     * Gets the textual content.
     *
     * @return The content
     */
    String getContent();

    /**
     * Gets the collection of attachments.
     *
     * @return The attachments or an empty collection if none attached
     */
    List<Attachment> getAttachments();

    /**
     * Gets miscellaneous JSON data.
     * <p>
     * Allowed is any object returned by:
     *
     * <pre>
     * String sJson = &quot;...&quot; // Any JSON representation
     * new org.json.JSONTokener(sJson).nextValue();
     * </pre>
     *
     * @return The JSON data or <code>null</code> if absent
     */
    Object getMisc();

    /**
     * Signals whether this snippet is shared to others.
     *
     * @return <code>true</code> if shared; otherwise <code>false</code>
     */
    boolean isShared();

    /**
     * Gets the identifier of this snippet's creator.
     *
     * @return The creator identifier or <code>-1</code> if absent
     */
    int getCreatedBy();

    /**
     * Gets this snippet's properties.
     *
     * @return The properties as an unmodifiable {@link Map map}
     */
    Map<String, Object> getProperties();

    /**
     * Gets this snippet's unnamed properties.
     *
     * @return The unnamed properties as an unmodifiable {@link Map map}
     */
    Map<String, Object> getUnnamedProperties();

    /**
     * Gets the error message for this snippet in case an error occurred while loading this snippet.
     *
     * @return the optional error message
     */
    public Optional<String> getError();
}
