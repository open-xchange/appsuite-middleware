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

package com.openexchange.index.solr.internal;

import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacade;
import com.openexchange.session.Session;

/**
 * {@link SolrIndexFacade} - The Solr {@link IndexFacade}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SolrIndexFacade implements IndexFacade {

    private final ConfigurationService config;

    private final ConcurrentHashMap<SolrIndexIdentifier, SolrIndexAccess> accessMap;

    /**
     * Initializes a new {@link SolrIndexFacade}.
     * 
     * @param config The configuration service
     */
    public SolrIndexFacade(final ConfigurationService config) {
        super();
        this.config = config;
        accessMap = new ConcurrentHashMap<SolrIndexIdentifier, SolrIndexAccess>();
    }

    @Override
    public IndexAccess acquireIndexAccess(final int module, final int userId, final int contextId) throws OXException {
        final SolrIndexIdentifier identifier = new SolrIndexIdentifier(contextId, userId, module);
        SolrIndexAccess cachedIndexAccess = accessMap.get(identifier);
        if (null == cachedIndexAccess) {
            final SolrIndexAccess newAccess = new SolrIndexAccess(identifier, config);
            cachedIndexAccess = accessMap.putIfAbsent(identifier, newAccess);
            if (null == cachedIndexAccess) {
                cachedIndexAccess = newAccess;
            }
        }
        cachedIndexAccess.incrementRetainCount();
        return cachedIndexAccess;
    }

    @Override
    public void releaseIndexAccess(final IndexAccess indexAccess) throws OXException {
        final SolrIndexAccess cachedIndexAccess = accessMap.get(((SolrIndexAccess) indexAccess).getIdentifier());
        if (null != cachedIndexAccess) {
            final int retainCount = cachedIndexAccess.decrementRetainCount();
            if (retainCount == 0) {
                cachedIndexAccess.release();
            }
        }
    }

    @Override
    public IndexAccess acquireIndexAccess(final int module, final Session session) throws OXException {
        return acquireIndexAccess(session.getContextId(), session.getUserId(), module);
    }

}
