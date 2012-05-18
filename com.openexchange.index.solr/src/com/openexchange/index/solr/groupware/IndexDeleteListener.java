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

package com.openexchange.index.solr.groupware;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.ldap.User;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.solr.SolrCoreConfigService;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.user.UserService;


/**
 * {@link IndexDeleteListener}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexDeleteListener implements DeleteListener {
    
    private final int[] coreTypes;
    

    public IndexDeleteListener() {
        super();
        // TODO: extend!
        coreTypes = new int[] { Types.EMAIL };
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (event.getType() == DeleteEvent.TYPE_USER) {
            final int cid = event.getContext().getContextId();
            final int uid = event.getId();
            
            deleteAllCores(cid, uid);
        } else if (event.getType() == DeleteEvent.TYPE_CONTEXT) {
            final int cid = event.getContext().getContextId();
            final UserService userService = Services.getService(UserService.class);
            final User[] users = userService.getUser(event.getContext());
            
            for (final User user : users) {
                deleteAllCores(cid, user.getId());
            }
        }
    }
    
    private void deleteAllCores(final int cid, final int uid) throws OXException {
        final SolrCoreConfigService indexService = Services.getService(SolrCoreConfigService.class);
        for (final int type : coreTypes) {
            indexService.removeCoreEnvironment(new SolrCoreIdentifier(cid, uid, type)); 
        }
    }
}
