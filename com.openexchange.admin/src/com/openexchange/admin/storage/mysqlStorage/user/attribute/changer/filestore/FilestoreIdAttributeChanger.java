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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user.AbstractUserAttributeChanger;

/**
 * {@link FilestoreIdAttributeChanger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class FilestoreIdAttributeChanger extends AbstractUserAttributeChanger {

    /**
     * Initialises a new {@link FilestoreIdAttributeChanger}.
     */
    public FilestoreIdAttributeChanger() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.UserAttributeChanger#changeAttribute(int, int, com.openexchange.admin.rmi.dataobjects.User, java.sql.Connection)
     */
    @Override
    public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
        Integer filestoreId = userData.getFilestoreId();
        if (filestoreId == null) {
            return false;
        }

        Filestore filestore;
        try {
            OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
            filestore = oxutil.getFilestore(filestoreId.intValue(), false);
        } catch (StorageException e) {
            // TODO: Throw as SQL?
            return false;
        }

        if (filestore.getId() != null && -1 != filestore.getId().intValue()) {
            try (PreparedStatement prep = connection.prepareStatement("UPDATE user SET filestore_id = ? WHERE cid = ? AND id = ? AND filestore_id <> ?")) {
                prep.setInt(1, filestore.getId().intValue());
                prep.setInt(2, contextId);
                prep.setInt(3, userId);
                prep.setInt(4, filestore.getId().intValue());
                return prep.executeUpdate() > 0;
            }
        }
        return false;
    }
}
