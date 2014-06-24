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

package com.openexchange.admin.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.Callable;
import com.openexchange.databaseold.Database;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FilestoreLocationUpdater}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class FilestoreLocationUpdater implements Callable<Void> {

    private Map<String, String> fileMapping;

    private int ctxId;

    /**
     * Initializes a new {@link FilestoreLocationUpdater}.
     */
    public FilestoreLocationUpdater(Map<String, String> fileMapping, int ctxId) {
        super();
        this.fileMapping = fileMapping;
        this.ctxId = ctxId;
    }

    @Override
    public Void call() throws Exception {
        Connection con = null;
        PreparedStatement infostore = null;
        PreparedStatement attachment = null;
        PreparedStatement snippet = null;
        PreparedStatement preview = null;
        try {
            con = Database.getNoTimeout(ctxId, true);
            infostore = con.prepareStatement("UPDATE infostore_document SET file_store_location = ? WHERE cid = ? AND file_store_location = ?");
            attachment = con.prepareStatement("UPDATE prg_attachment SET file_id = ? WHERE cid = ? AND file_id = ?");
            snippet = con.prepareStatement("UPDATE snippet SET refId = ? WHERE cid = ? AND refId = ?");
            preview = con.prepareStatement("UPDATE preview SET refId = ? WHERE cid = ? AND refId = ?");
            for (String old : fileMapping.keySet()) {
                infostore.setString(1, fileMapping.get(old));
                infostore.setInt(2, ctxId);
                infostore.setString(3, old);
                infostore.addBatch();
                attachment.setString(1, fileMapping.get(old));
                attachment.setInt(2, ctxId);
                attachment.setString(3, old);
                attachment.addBatch();
                snippet.setString(1, fileMapping.get(old));
                snippet.setInt(2, ctxId);
                snippet.setString(3, old);
                snippet.addBatch();
                preview.setString(1, fileMapping.get(old));
                preview.setInt(2, ctxId);
                preview.setString(3, old);
                preview.addBatch();
            }
            infostore.executeBatch();
            attachment.executeBatch();
            snippet.executeBatch();
            preview.executeBatch();
        } finally {
            DBUtils.closeSQLStuff(infostore);
            DBUtils.closeSQLStuff(attachment);
            DBUtils.closeSQLStuff(snippet);
            DBUtils.closeSQLStuff(preview);
            Database.backNoTimeout(ctxId, true, con);
        }

        return null;
    }

}
