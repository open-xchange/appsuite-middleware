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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.publish;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.openexchange.database.DBPoolingException;
import static com.openexchange.publish.Transaction.INT;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class PublicationSQL {

    private final static String SITE_TABLE = "publication_sites";

    public static void addSite(Site site) throws SQLException, DBPoolingException {
        if (siteExists(site)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(SITE_TABLE);
        sb.append(" (");
        sb.append("cid, user, name, folderId");
        sb.append(") ");
        sb.append("VALUES");
        sb.append(" (");
        sb.append("?, ?, ?, ?");
        sb.append(" )");

        Transaction.commitStatement(
            site.getContextId(),
            sb.toString(),
            site.getContextId(),
            site.getOwnerId(),
            site.getName(),
            site.getFolderId());
    }

    public static void removeSite(Site site) throws DBPoolingException, SQLException {
        if (!siteExists(site)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(SITE_TABLE);
        sb.append(" WHERE cid = ? AND folderId = ?");

        Transaction.commitStatement(site.getContextId(), sb.toString(), site.getContextId(), site.getFolderId());
    }

    public static Site getSite(Path path) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(SITE_TABLE);
        sb.append(" WHERE cid = ? AND user = ? AND name = ?");

        List<Map<String, Object>> sites = Transaction.commitQuery(
            path.getContextId(),
            sb.toString(),
            path.getContextId(),
            path.getOwnerId(),
            path.getSiteName());
        
        Map<String, Object> site = sites.get(0);

        Site siteObject = new Site();
        siteObject.setPath(path);
        siteObject.setFolderId(INT(site.get("folderId")));

        return siteObject;
    }

    public static Collection<Site> getSites(int contextId, int userId) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(SITE_TABLE);
        sb.append(" WHERE cid = ? AND user = ?");

        List<Site> retval = new ArrayList<Site>();
        List<Map<String, Object>> sites = Transaction.commitQuery(contextId, sb.toString(), contextId, userId);
        for (Map<String, Object> site : sites) {
            Site siteObject = new Site();
            Path pathObject = new Path();
            pathObject.setContextId(contextId);
            pathObject.setOwnerId(userId);
            pathObject.setSiteName((String) site.get("name"));
            siteObject.setPath(pathObject);
            siteObject.setFolderId(INT(site.get("folderId")));
            retval.add(siteObject);
        }

        return retval;
    }

    public static boolean siteExists(Site site) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM ");
        sb.append(SITE_TABLE);
        sb.append(" WHERE cid = ? AND folderId = ?");

        Transaction transaction = new Transaction(site.getContextId());
        List<Map<String, Object>> sites = transaction.executeQuery(sb.toString(), site.getContextId(), site.getFolderId());

        return sites.size() > 0;
    }
}
