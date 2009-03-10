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
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.server.impl.DBPoolingException;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class PublicationServiceImpl implements PublicationService {

    private static final Log LOG = LogFactory.getLog(PublicationServiceImpl.class);

    public void create(Site site) {
        try {
            PublicationSQL.addSite(site);
        } catch (DBPoolingException e) {
            LOG.error("Error during creation of a site", e);
        } catch (SQLException e) {
            LOG.error("Error during creation of a site", e);
        }
    }

    public void delete(Site site) {
        try {
            PublicationSQL.removeSite(site);
        } catch (DBPoolingException e) {
            LOG.error("Error during delete of a site", e);
        } catch (SQLException e) {
            LOG.error("Error during delete of a site", e);
        }
    }

    public Site getSite(Path path) {
        try {
            return PublicationSQL.getSite(path);
        } catch (DBPoolingException e) {
            LOG.error("Error during loading of a site", e);
            return null;
        } catch (SQLException e) {
            LOG.error("Error during loading of a site", e);
            return null;
        }
    }

    public Site getSite(String path) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    public Collection<Site> getSites(int contextId, int userId) {
        try {
            return PublicationSQL.getSites(contextId, userId);
        } catch (DBPoolingException e) {
            LOG.error("Error during loading of a site", e);
            return null;
        } catch (SQLException e) {
            LOG.error("Error during loading of a site", e);
            return null;
        }
    }

}
