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
