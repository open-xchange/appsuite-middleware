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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.index.internal;

import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.index.ConfigIndexService;
import com.openexchange.index.IndexServer;
import com.openexchange.index.IndexUrl;


/**
 * {@link StaticConfigIndexService} - Only for testing purpose!!!
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StaticConfigIndexService implements ConfigIndexService {

    private final IndexUrl indexUrl;
    
    private final IndexServer server;
    
    /**
     * Initializes a new {@link StaticConfigIndexService}.
     */
    public StaticConfigIndexService() {
        super();
        final IndexServerImpl server = new IndexServerImpl();
        server.setId(1);
        server.setUrl("http://10.20.31.1:8580");
        server.setConnectionTimeout(1000);
        server.setMaxConnectionsPerHost(100);
        server.setSoTimeout(3000);
        this.server = server;
        this.indexUrl = new IndexUrlImpl(server, "solr/main");
    }

    @Override
    public IndexUrl getReadOnlyURL(final int contextId, final int userId, final int module) throws OXException {
        return indexUrl;
    }

    @Override
    public IndexUrl getWriteURL(final int contextId, final int userId, final int module) throws OXException {
        return indexUrl;
    }

    @Override
    public int registerIndexServer(final IndexServer server) throws OXException {
        return 0;        
    }

    @Override
    public void unregisterIndexServer(final int serverId, final boolean deleteMappings) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<IndexServer> getAllIndexServers() throws OXException {
        return Arrays.asList(new IndexServer[] { server });
    }

    @Override
    public void modifyIndexServer(final IndexServer server) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addIndexMapping(final int cid, final int uid, final int module, final String index) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeIndexMapping(final int cid, final int uid, final int module) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void modifiyIndexMapping(final int cid, final int uid, final int module, final int server, final String index) throws OXException {
        // TODO Auto-generated method stub
        
    }

}
