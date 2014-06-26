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

package com.openexchange.solr;

import java.net.URI;
import java.util.List;
import javax.management.MBeanException;
import javax.management.ObjectName;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.exception.OXException;


/**
 * {@link SolrMBean} - A MBean containing some administrative functions as well as some functions to debug solr.<br>
 * The {@link ObjectName} under that the instance of this MBean is registered is created like this:<br>
 * <code>new ObjectName(SolrMBean.DOMAIN, SolrMBean.KEY, SolrMBean.VALUE);</code>. Any JMX client should instantiate it in
 * the same way to prevent naming errors.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface SolrMBean {

    public static final String DOMAIN = "com.openexchange.solr";

    public static final String KEY = "type";

    public static final String VALUE = "solrControl";

    /**
     * Lists all solr cores that are active on this server.
     *
     * @return The list of active cores.
     * @throws MBeanException If this is not a solr node.
     */
    public List<String> getActiveCores() throws MBeanException;

    /**
     * This removes a whole solr core environment for a users module.
     *
     * @throws MBeanException If an error occurrs.
     */
    public void removeCoreEnvironment(int contextId, int userId, int module) throws MBeanException;

    /**
     * Runs a query against an index.
     *
     * @param contextId The context id of the index.
     * @param userId The user id of the index.
     * @param module The module of the index.
     * @param queryString The query String.
     * @param limit A positive number to limit the results. Or a negative one to use Integer.MAX_MAX_VALUE.
     * @return A printable String that lists the results.
     * @throws MBeanException
     */
    public String search(int contextId, int userId, int module, String queryString, int limit) throws MBeanException;

    /**
     * Deletes documents by the given query.
     *
     * @param contextId The context id of the index.
     * @param userId The user id of the index.
     * @param module The module of the index.
     * @param queryString The query String.
     * @return The number of deleted documents.
     * @throws MBeanException
     */
    public long delete(int contextId, int userId, int module, String queryString) throws MBeanException;

    /**
     * Counts the number of documents found by the given query.
     *
     * @param contextId The context id of the index.
     * @param userId The user id of the index.
     * @param module The module of the index.
     * @param queryString The query String.
     * @throws MBeanException
     */
    public long count(int contextId, int userId, int module, String queryString) throws MBeanException;

    /**
     * Gets a core store by its id.
     *
     * @param id The core store id.
     * @return The core store.
     * @throws MBeanException
     */
    public SolrCoreStore getCoreStore(int id) throws MBeanException;

    /**
     * Gets a list of all available core stores.
     *
     * @param credentials The credentials of the OX master admin
     * @return The store list.
     * @throws OXException
     */
    public List<SolrCoreStore> getAllStores(Credentials credentials) throws MBeanException;

    /**
     * Registers a new solr core store.
     *
     * @param credentials The credentials of the OX master admin
     * @param uri The uri that points to the stores mount point.
     * @param maxCores The maximal number of cores handled by this core store.
     * @return The stores id.
     * @throws OXException
     */
    public int registerCoreStore(Credentials credentials, URI uri, int maxCores) throws MBeanException;

    /**
     * Modifies an existing core.
     *
     * @param credentials The credentials of the OX master admin
     * @param id The core stores id.
     * @param uri The uri that points to the stores mount point.
     * @param maxCores The maximal number of cores handled by this core store.
     * @throws OXException
     */
    public void modifyCoreStore(Credentials credentials, int id, URI uri, int maxCores) throws MBeanException;

    /**
     * Unregisters a core store.
     *
     * @param credentials The credentials of the OX master admin
     * @param id The id of the store to unregister.
     * @throws OXException
     */
    public void unregisterCoreStore(Credentials credentials, int id) throws MBeanException;
}
