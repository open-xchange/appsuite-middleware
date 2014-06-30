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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.groupware.attach;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.groupware.filestore.FilestoreLocationUpdater;


/**
 * {@link AttachmentFilestoreLocationUpdater}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class AttachmentFilestoreLocationUpdater implements FilestoreLocationUpdater {

    /**
     * Initializes a new {@link AttachmentFilestoreLocationUpdater}.
     */
    public AttachmentFilestoreLocationUpdater() {
        super();
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.filestore.FilestoreLocationUpdater#updateFilestoreLocation(java.util.Map, int, java.sql.Connection)
     */
    @Override
    public void updateFilestoreLocation(Map<String, String> fileMapping, int ctxId, Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("UPDATE prg_attachment SET file_id = ? WHERE cid = ? AND file_id = ?");
        for (String old : fileMapping.keySet()) {
            stmt.setString(1, fileMapping.get(old));
            stmt.setInt(2, ctxId);
            stmt.setString(3, old);
            stmt.addBatch();
        }
        stmt.executeBatch();
    }

}
