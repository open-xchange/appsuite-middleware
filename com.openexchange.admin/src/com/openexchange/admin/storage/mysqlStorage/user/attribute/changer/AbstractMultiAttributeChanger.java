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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link AbstractMultiAttributeChanger} - Base stub class for providing the logic of
 * changing multiple attributes for a user in a context.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractMultiAttributeChanger extends AbstractAttributeChanger implements UserAttributeChanger {

    /**
     * Initialises a new {@link AbstractMultiAttributeChanger}.
     */
    public AbstractMultiAttributeChanger() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChanger#fillSetStatement(java.sql.PreparedStatement, java.util.Map, java.util.Map, int, int)
     */
    @Override
    protected int fillSetStatement(PreparedStatement stmt, Map<Attribute, Setter> setters, Map<Attribute, Object> attributes, int userId, int contextId) throws SQLException {
        int parameterIndex = 1;
        for (Entry<Attribute, Object> entry : attributes.entrySet()) {
            Setter setter = setters.get(entry.getKey());
            if (setter == null) {
                continue;
            }
            setter.set(stmt, entry.getValue(), parameterIndex++);
        }
        return appendContextUser(contextId, userId, stmt, parameterIndex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChanger#fillUnsetStatement(java.sql.PreparedStatement, java.util.Map, java.util.Set, int, int)
     */
    @Override
    protected int fillUnsetStatement(PreparedStatement stmt, Map<Attribute, Unsetter> unsetters, Set<Attribute> attributes, int userId, int contextId) throws SQLException {
        int parameterIndex = 1;
        for (Attribute attribute : attributes) {
            Unsetter unsetter = unsetters.get(attribute);
            if (unsetter == null) {
                continue;
            }
            unsetter.unset(stmt, parameterIndex++);
        }
        return appendContextUser(contextId, userId, stmt, parameterIndex);
    }
}
