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

package com.openexchange.share.impl;

import java.util.Iterator;
import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.CreateRequest;
import com.openexchange.share.DeleteRequest;
import com.openexchange.share.Share;
import com.openexchange.share.ShareService;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link DefaultShareService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class DefaultShareService implements ShareService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DefaultShareService}.
     *
     * @param storage The underlying share storage
     */
    public DefaultShareService(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public List<Share> create(CreateRequest shareRequest, Session session) throws OXException {
        return new CreateHandler(shareRequest, ServerSessionAdapter.valueOf(session), services).processRequest();
    }

    @Override
    public Share resolveToken(String token) throws OXException {
        int contextID = ShareTool.extractContextId(token);
        Share share = services.getService(ShareStorage.class).loadShare(contextID, token, StorageParameters.NO_PARAMETERS);
        if (share.isExpired()) {
            removeShare(share);
            return null;
        }
        return share;
    }

    @Override
    public List<Share> getAllShares(Session session) throws OXException {
        List<Share> shares = services.getService(ShareStorage.class).loadSharesCreatedBy(
            session.getContextId(), session.getUserId(), StorageParameters.NO_PARAMETERS);
        return removeExpired(shares);
    }

    @Override
    public List<Share> getSharesForFolder(int contextID, String folder) throws OXException {
        List<Share> shares = services.getService(ShareStorage.class).loadSharesForFolder(contextID, folder, StorageParameters.NO_PARAMETERS);
        return removeExpired(shares);
    }

    @Override
    public void delete(DeleteRequest deleteRequest, Session session) throws OXException {
        new DeleteHandler(deleteRequest, ServerSessionAdapter.valueOf(session), services).processRequest();
    }

    private List<Share> removeExpired(List<Share> shares) throws OXException {
        if (null != shares && 0 < shares.size()) {
            Iterator<Share> iterator = shares.iterator();
            while (iterator.hasNext()) {
                Share share = iterator.next();
                if (share.isExpired()) {
                    removeShare(share);
                    iterator.remove();
                }
            }
        }
        return shares;
    }

    private void removeShare(Share share) throws OXException {
        ContextService contextService = services.getService(ContextService.class);
        Context context = contextService.getContext(share.getContextID());
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setModule(share.getModule());
        deleteRequest.setFolder(share.getFolder());
        deleteRequest.setItem(share.getItem());
        deleteRequest.addGuestID(share.getGuest());
        new DeleteHandler(deleteRequest, context, services).processRequest();
    }

}
