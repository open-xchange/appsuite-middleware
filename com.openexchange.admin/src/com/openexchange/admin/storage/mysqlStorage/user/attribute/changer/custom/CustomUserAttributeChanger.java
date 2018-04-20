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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractMultiAttributeChanger;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;
import com.openexchange.database.Databases;
import com.openexchange.java.util.UUIDs;

/**
 * {@link CustomUserAttributeChanger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CustomUserAttributeChanger extends AbstractMultiAttributeChanger {

    /**
     * Initialises a new {@link CustomUserAttributeChanger}.
     */
    public CustomUserAttributeChanger() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.UserAttributeChanger#changeAttribute(int, int, com.openexchange.admin.rmi.dataobjects.User, java.sql.Connection)
     */
    @Override
    public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
        if (!userData.isUserAttributesset()) {
            return false;
        }
        PreparedStatement stmtInsertAttribute = null;
        PreparedStatement stmtDeleteAttribute = null;
        boolean changedAttributes = false;
        try {
            for (Map.Entry<String, Map<String, String>> ns : userData.getUserAttributes().entrySet()) {
                String namespace = ns.getKey();
                for (Map.Entry<String, String> pair : ns.getValue().entrySet()) {
                    String name = namespace + "/" + pair.getKey();
                    String value = pair.getValue();
                    if (value == null) {
                        if (null == stmtDeleteAttribute) {
                            stmtDeleteAttribute = connection.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND id=? AND name=?");
                            stmtDeleteAttribute.setInt(1, contextId);
                            stmtDeleteAttribute.setInt(2, userId);
                        }
                        stmtDeleteAttribute.setString(3, name);
                        stmtDeleteAttribute.addBatch();
                    } else {
                        if (null == stmtInsertAttribute) {
                            stmtInsertAttribute = connection.prepareStatement("INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE value=?");
                            stmtInsertAttribute.setInt(1, contextId);
                            stmtInsertAttribute.setInt(2, userId);
                        }
                        stmtInsertAttribute.setString(3, name);
                        stmtInsertAttribute.setString(4, value);
                        stmtInsertAttribute.setBytes(5, UUIDs.toByteArray(UUID.randomUUID()));
                        stmtInsertAttribute.setString(6, value);
                        stmtInsertAttribute.executeUpdate();
                        stmtInsertAttribute.addBatch();
                    }
                    changedAttributes = true;
                }
            }

            if (null != stmtDeleteAttribute) {
                stmtDeleteAttribute.executeBatch();
            }
            if (null != stmtInsertAttribute) {
                stmtInsertAttribute.executeBatch();
            }
        } finally {
            Databases.closeSQLStuff(stmtInsertAttribute);
            Databases.closeSQLStuff(stmtDeleteAttribute);
        }
        // TODO: notify callers of changed attributes, maybe use observer pattern?
        return changedAttributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChanger#prepareStatement(java.lang.String, java.util.Set, java.sql.Connection)
     */
    @Override
    protected PreparedStatement prepareStatement(String table, Set<Attribute> attributes, Connection connection) throws SQLException {
        // no-op
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChanger#prepareDefaultStatement(java.lang.String, java.util.Set, java.sql.Connection)
     */
    @Override
    protected PreparedStatement prepareDefaultStatement(String table, Set<Attribute> attributes, Connection connection) throws SQLException {
        // no-op
        return null;
    }

}
