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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.Collection;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import com.openexchange.exception.OXException;


/**
 * {@link SolrAccessService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface SolrAccessService {
    /**
     * See {@link SolrServer#add(SolrInputDocument)} for details.
     * 
     * @param identifier The cores name.
     * @param commit <code>true</code> If the document should be committed. Otherwise <code>false</code>.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse add(SolrCoreIdentifier identifier, SolrInputDocument document, boolean commit) throws OXException;
    
    /**
     * See {@link SolrServer#add(Collection)} for details.
     * 
     * @param identifier The cores name.
     * @param commit <code>true</code> If the documents should be committed. Otherwise <code>false</code>.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse add(SolrCoreIdentifier identifier, Collection<SolrInputDocument> documents, boolean commit) throws OXException;
        
    /**
     * See {@link SolrServer#deleteById(String)} for details.
     * 
     * @param identifier The cores name.
     * @param commit <code>true</code> If the deletion should be committed. Otherwise <code>false</code>.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse deleteById(SolrCoreIdentifier identifier, String id, boolean commit) throws OXException;
    
    /**
     * See {@link SolrServer#deleteByQuery(String)} for details.
     * 
     * @param identifier The cores name.
     * @param commit <code>true</code> If the deletion should be committed. Otherwise <code>false</code>.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse deleteByQuery(SolrCoreIdentifier identifier, String query, boolean commit) throws OXException;
    
    /**
     * See {@link SolrServer#commit()} for details.
     * 
     * @param identifier The cores name.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse commit(SolrCoreIdentifier identifier) throws OXException;
    
    /**
     * See {@link SolrServer#commit(boolean, boolean)} for details.
     * 
     * @param identifier The cores name.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse commit(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher) throws OXException;
    
    /**
     * See {@link SolrServer#rollback()} for details.
     * 
     * @param identifier The cores name.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse rollback(SolrCoreIdentifier identifier) throws OXException;
    
    /**
     * See {@link SolrServer#optimize()} for details.
     * 
     * @param identifier The cores name.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse optimize(SolrCoreIdentifier identifier) throws OXException;
    
    /**
     * See {@link SolrServer#optimize(boolean, boolean)} for details.
     * 
     * @param identifier The cores name.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse optimize(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher) throws OXException;
    
    /**
     * See {@link SolrServer#optimize(boolean, boolean, int)} for details.
     * 
     * @param identifier The cores name.
     * @throws OXException in case of solr errors.
     */
    public UpdateResponse optimize(SolrCoreIdentifier identifier, boolean waitFlush, boolean waitSearcher, int maxSegments) throws OXException;
    
    /**
     * See {@link SolrServer#query(SolrParams)} for details.
     * 
     * @param identifier The cores name.
     * @throws OXException in case of solr errors.
     */
    public QueryResponse query(SolrCoreIdentifier identifier, SolrParams params) throws OXException;
    
    /**
     * Releases all resources hold for a core.
     * 
     * @param identifier The cores identifier.
     */
    public void freeResources(SolrCoreIdentifier identifier);
    
}
