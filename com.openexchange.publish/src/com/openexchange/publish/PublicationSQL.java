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
import com.openexchange.server.impl.DBPoolingException;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class PublicationSQL {

    private final static String PUBLICATION_TABLE = "publications";

    private final static String SITE_TABLE = "publication_sites";

    public static void addSite(Site site) throws SQLException, DBPoolingException {
        //if (siteExists(site)) {
        //    return;
        //}

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(SITE_TABLE);
        sb.append(" (");
        sb.append("cid, user, name");
        sb.append(") ");
        sb.append("VALUES");
        sb.append(" (");
        sb.append("?, ?, ?");
        sb.append(" )");

        Transaction.commitStatement(site.getContextId(), sb.toString(), site.getContextId(), site.getOwnerId(), site.getName());
        // TODO: Publications speichern.
    }

   /* public static void addPublicatione(Publication publication) throws SQLException, DBPoolingException {
        if (publicationExists(publication)) {
            return;
        }

        Transaction transaction = new Transaction(publication.getContextID());
        StringBuilder sb;
        Site site = publication.getSite();
        int siteId;
        if (!siteExists(site)) {
            sb = new StringBuilder();
            sb.append("INSERT INTO ");
            sb.append(SITE_TABLE);
            sb.append(" (");
            sb.append("cid, user, name");
            sb.append(") ");
            sb.append("VALUES");
            sb.append(" (");
            sb.append("?, ?, ?");
            sb.append(" )");

            List<Integer> keys = transaction.executeStatement(sb.toString(), site.getContextId(), site.getOwnerId(), site.getName());
            siteId = keys.get(0);
        } else {
            sb = new StringBuilder();
            sb.append("SELECT * FROM ");
            sb.append(SITE_TABLE);
            sb.append(" WHERE cid = ? AND user = ? AND name = ?");

            List<Map<String, Object>> sites = Transaction.commitQuery(
                publication.getContextID(),
                sb.toString(),
                site.getContextId(),
                site.getOwnerId(),
                site.getName());
            siteId = ((Long) sites.get(0).get("id")).intValue();
        }

        sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(PUBLICATION_TABLE);
        sb.append(" (");
        sb.append("cid, user, site_id, type, object_id, folder_id");
        sb.append(") ");
        sb.append("VALUES");
        sb.append(" (");
        sb.append("?, ?, ?, ?, ?, ?");
        sb.append(" )");

        try {
            transaction.executeStatement(
                sb.toString(),
                publication.getContextID(),
                publication.getOwnerId(),
                siteId,
                publication.getType(),
                publication.getObjectID(),
                publication.getFolderId());
        } catch (DBPoolingException e) {
            transaction.rollback();
            throw e;
        } catch (SQLException e) {
            transaction.rollback();
            throw e;
        }
        transaction.commit();
    }

    public static void removeSite(Site site) throws DBPoolingException, SQLException {
        if (!siteExists(site)) {
            return;
        }

        Transaction transaction = new Transaction(site.getContextId());

        for (Publication publication : site) {
            removePublication(publication, transaction);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(SITE_TABLE);
        sb.append(" WHERE cid = ? AND user = ? AND name = ?");
        transaction.executeStatement(sb.toString(), site.getContextId(), site.getOwnerId(), site.getName());
    }

    public static void removePublication(Publication publication) throws DBPoolingException, SQLException {
        Transaction transaction = new Transaction(publication.getContextID());
        removePublication(publication, transaction);
        transaction.commit();
    }

    private static void removePublication(Publication publication, Transaction transaction) throws DBPoolingException, SQLException {
        if (!publicationExists(publication)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT id FROM ");
        sb.append(SITE_TABLE);
        sb.append(" WHERE cid = ? AND user = ? AND name = ?");
        List<Map<String, Object>> sites = transaction.executeQuery(
            sb.toString(),
            publication.getContextID(),
            publication.getOwnerId(),
            publication.getSite().getName());
        Map<String, Object> site = sites.get(0);
        int siteId = ((Long) site.get("id")).intValue();

        sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(PUBLICATION_TABLE);
        sb.append(" WHERE cid = ? AND site_id = ? AND type = ? AND object_id = ? AND folder_id = ?");
        transaction.executeStatement(
            sb.toString(),
            publication.getContextID(),
            siteId,
            publication.getType(),
            publication.getObjectID(),
            publication.getFolderId());
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
        List<Publication> publications = getPublications(path.getContextId(), ((Long) site.get("id")).intValue());
        for (Publication publication : publications) {
            siteObject.addPublication(publication);
        }
        return siteObject;
    }

    public static Collection<Site> getSites(int contextId, int userId) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(SITE_TABLE);
        sb.append("WHERE cid = ? AND user = ?");

        List<Site> retval = new ArrayList<Site>();

        List<Map<String, Object>> sites = Transaction.commitQuery(contextId, sb.toString(), contextId, userId);
        for (Map<String, Object> site : sites) {
            List<Publication> publications = getPublications(contextId, (Integer) site.get("id"));
            Site siteObject = new Site();
            for (Publication publication : publications) {
                siteObject.addPublication(publication);
            }
            Path pathObject = new Path();
            pathObject.setContextId(contextId);
            pathObject.setOwnerId(userId);
            pathObject.setSiteName((String) site.get("name"));

            siteObject.setPath(pathObject);
            retval.add(siteObject);
        }

        return retval;
    }

    private static List<Publication> getPublications(int contextId, int siteId) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(PUBLICATION_TABLE);
        sb.append(" WHERE site_id = ?");

        List<Map<String, Object>> publications = Transaction.commitQuery(contextId, sb.toString(), siteId);
        List<Publication> retval = new ArrayList<Publication>();
        for (Map<String, Object> publication : publications) {
            Publication pubObject = new Publication();
            pubObject.setObjectID(((Long) publication.get("object_id")).intValue());
            pubObject.setFolderId(((Long) publication.get("folder_id")).intValue());
            pubObject.setType(((Long) publication.get("type")).intValue());
            retval.add(pubObject);
        }

        return retval;
    }

    public static boolean siteExists(Site site) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM ");
        sb.append(SITE_TABLE);
        sb.append(" WHERE cid = ? AND user = ? AND name = ?");

        Transaction transaction = new Transaction(site.getContextId());
        List<Map<String, Object>> sites = transaction.executeQuery(sb.toString(), site.getContextId(), site.getOwnerId(), site.getName());

        return sites.size() > 0;
    }

    public static boolean publicationExists(Publication publication) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM ");
        sb.append(PUBLICATION_TABLE + " pub");
        sb.append(" JOIN ");
        sb.append(SITE_TABLE + " site");
        sb.append(" WHERE pub.cid = ? AND pub.user = ? AND pub.object_id = ? AND pub.folder_id = ? AND site.name = ?");

        Transaction transaction = new Transaction(publication.getContextID());
        List<Map<String, Object>> publications = transaction.executeQuery(
            sb.toString(),
            publication.getContextID(),
            publication.getOwnerId(),
            publication.getObjectID(),
            publication.getFolderId(),
            publication.getSite().getName());

        return publications.size() > 0;
    } */

}
