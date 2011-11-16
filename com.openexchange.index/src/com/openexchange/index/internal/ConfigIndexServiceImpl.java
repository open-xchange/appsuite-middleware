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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.index.ConfigIndexService;
import com.openexchange.index.IndexServer;
import com.openexchange.index.IndexUrl;


/**
 * {@link ConfigIndexServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ConfigIndexServiceImpl implements ConfigIndexService {    

    public ConfigIndexServiceImpl() {
        super();
    }

    @Override
    public IndexUrl getReadOnlyURL(final int cid, final int uid, final int module) throws OXException {
        return ConfigIndexMysql.getInstance().getIndexUrl(cid, uid, module);
    }

    @Override
    public IndexUrl getWriteURL(final int cid, final int uid, final int module) throws OXException {
        // TODO: Until now there is now difference between read and write connection.
        // Change this here if it's going to be implemented.
        return ConfigIndexMysql.getInstance().getIndexUrl(cid, uid, module);
    }

    @Override
    public void unregisterIndexServer(int serverId, boolean deleteMappings) throws OXException {
        ConfigIndexMysql.getInstance().unregisterIndexServer(serverId, deleteMappings);     
    }

    @Override
    public int registerIndexServer(IndexServer server) throws OXException {
        return ConfigIndexMysql.getInstance().registerIndexServer(server);
    }

    @Override
    public List<IndexServer> getAllIndexServers() throws OXException {
        return ConfigIndexMysql.getInstance().getAllIndexServers();
    }

    @Override
    public void modifyIndexServer(IndexServer server) throws OXException {        
        ConfigIndexMysql.getInstance().modifyIndexServer(server);
    }

    @Override
    public void addIndexMapping(int cid, int uid, int module, String index) throws OXException {
        int serverId = ConfigIndexMysql.getInstance().createIndexMapping(cid, uid, module, index);
        
        // TODO: Create index / core here
    }
    
    @Override
    public void removeIndexMapping(int cid, int uid, int module) throws OXException {
        ConfigIndexMysql.getInstance().removeIndexMapping(cid, uid, module);
        
        // TODO: Remove index / core
    }

    @Override
    public void modifiyIndexMapping(int cid, int uid, int module, int server, String index) throws OXException {
        ConfigIndexMysql.getInstance().modifiyIndexMapping(cid, uid, module, server, index);
        
        // TODO: Apply any changes to the modified index / core
    }
}
