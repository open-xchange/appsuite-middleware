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

package com.openexchange.snippet.rdb;

import java.util.EnumSet;
import java.util.Set;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.PropertySwitch;

/**
 * {@link UpdateSnippetBuilder}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdateSnippetBuilder implements PropertySwitch {

    private StringBuilder snippetBuilder;
    private Set<Property> modifiableProperties;

    /**
     * Initializes a new {@link UpdateSnippetBuilder}.
     */
    public UpdateSnippetBuilder() {
        super();
    }

    /**
     * Gets the update statement.
     *
     * @return The update statement or <code>null</code>
     * @see #getModifiableProperties()
     */
    public String getUpdateStatement() {
        final StringBuilder sb = this.snippetBuilder;
        if (null == sb) {
            return null;
        }
        final int mlen = sb.length() - 1;
        if (',' == sb.charAt(mlen)) {
            sb.setLength(mlen); // discard last comma
            sb.append(" WHERE cid=? AND user=? AND id=?");
        }
        return sb.toString();
    }

    /**
     * Gets the modifiable properties.
     *
     * @return The modifiable properties or <code>null</code>
     * @see #getUpdateStatement()
     */
    public Set<Property> getModifiableProperties() {
        return modifiableProperties;
    }

    private StringBuilder getSnippetBuilder() {
        StringBuilder sb = this.snippetBuilder;
        if (null == sb) {
            sb = new StringBuilder(96).append("UPDATE snippet SET ");
            this.snippetBuilder = sb;
        }
        return sb;
    }

    private Set<Property> getModifiableProps() {
        Set<Property> set = modifiableProperties;
        if (null == set) {
            set = EnumSet.noneOf(Property.class);
            modifiableProperties = set;
        }
        return set;
    }

    @Override
    public Object id() {
        return null;
    }

    @Override
    public Object properties() {
        return null;
    }

    @Override
    public Object content() {
        return null;
    }

    @Override
    public Object attachments() {
        return null;
    }

    @Override
    public Object accountId() {
        getSnippetBuilder().append("accountId = ?,");
        getModifiableProps().add(Property.ACCOUNT_ID);
        return null;
    }

    @Override
    public Object type() {
        getSnippetBuilder().append("type = ?,");
        getModifiableProps().add(Property.TYPE);
        return null;
    }

    @Override
    public Object displayName() {
        getSnippetBuilder().append("displayName = ?,");
        getModifiableProps().add(Property.DISPLAY_NAME);
        return null;
    }

    @Override
    public Object module() {
        getSnippetBuilder().append("module = ?,");
        getModifiableProps().add(Property.MODULE);
        return null;
    }

    @Override
    public Object createdBy() {
        return null;
    }

    @Override
    public Object shared() {
        getSnippetBuilder().append("shared = ?,");
        getModifiableProps().add(Property.SHARED);
        return null;
    }

    @Override
    public Object misc() {
        return null;
    }

}
