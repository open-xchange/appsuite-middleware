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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.filestore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.UserAttributeChanger;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user.AbstractUserAttributeChanger;
import com.openexchange.filestore.FileStorages;

/**
 * {@link FilestoreUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class FilestoreUserAttributeChangers extends AbstractUserAttributeChangers {

    private static final String TABLE = "user";
    private final UserAttributeChanger filestoreIdAttributeChanger;

    /**
     * Initialises a new {@link FilestoreUserAttributeChangers}.
     */
    public FilestoreUserAttributeChangers() {
        super(TABLE, EnumSet.allOf(FilestoreAttribute.class));
        filestoreIdAttributeChanger = new FilestoreIdAttributeChanger();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractUserAttributeChangers#initialiseChangers()
     */
    @Override
    protected Map<Attribute, UserAttributeChanger> initialiseChangers() {
        Map<Attribute, UserAttributeChanger> changers = new HashMap<>();
        changers.put(FilestoreAttribute.OWNER, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                Integer filestoreOwner = userData.getFilestoreOwner();
                int ownerId = (filestoreOwner != null && -1 != filestoreOwner.intValue()) ? filestoreOwner.intValue() : 0;
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(FilestoreAttribute.OWNER, ownerId), connection);
            }
        });

        changers.put(FilestoreAttribute.OWNER, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String filestore_name = userData.getFilestore_name();
                String filestoreName = (filestore_name != null) ? filestore_name : FileStorages.getNameForUser(userData.getId().intValue(), contextId);
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(FilestoreAttribute.NAME, filestoreName), connection);
            }
        });
        return Collections.unmodifiableMap(changers);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AttributeChangers#change(java.util.Set, com.openexchange.admin.rmi.dataobjects.User, int, int, java.sql.Connection)
     */
    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        try {
            if (filestoreIdAttributeChanger.changeAttribute(userId, contextId, userData, connection)) {
                return super.change(userData, userId, contextId, connection, pendingInvocations);
            }
            return EMPTY_SET;
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }
}
