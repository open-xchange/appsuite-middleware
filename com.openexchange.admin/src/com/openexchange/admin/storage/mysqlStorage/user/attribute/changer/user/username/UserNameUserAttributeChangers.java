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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user.username;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChangers;
import com.openexchange.admin.tools.AdminCache;

/**
 * {@link UserNameUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UserNameUserAttributeChangers extends AbstractAttributeChangers {

    private AdminCache adminCache;

    /**
     * Initialises a new {@link UserNameUserAttributeChangers}.
     */
    public UserNameUserAttributeChangers(AdminCache adminCache) {
        super();
        this.adminCache = adminCache;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AttributeChangers#change(java.util.Set, com.openexchange.admin.rmi.dataobjects.User, int, int, java.sql.Connection)
     */
    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection) throws StorageException {
        // Updates the username in 'login2user' table only if the if 'USERNAME_CHANGEABLE' is set to 'true'
        if (!adminCache.getProperties().getUserProp(AdminProperties.User.USERNAME_CHANGEABLE, false)) {
            return EMPTY_SET;
        }
        if (userData.getName() == null) {
            return EMPTY_SET;
        }
        if (userData.getName().trim().length() <= 0) {
            return EMPTY_SET;
        }
        if (adminCache.getProperties().getUserProp(AdminProperties.User.CHECK_NOT_ALLOWED_CHARS, true)) {
            try {
                OXToolStorageInterface.getInstance().validateUserName(userData.getName());
            } catch (InvalidDataException e) {
                throw new StorageException(e);
            }
        }

        if (adminCache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            userData.setName(userData.getName().toLowerCase());
        }
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE login2user SET uid=? WHERE cid=? AND id=?")) {
            stmt.setString(1, userData.getName().trim());
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() == 1 ? Collections.singleton("username") : EMPTY_SET;
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }
}
