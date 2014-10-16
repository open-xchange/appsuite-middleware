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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.share.Share;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.tools.Collections.Filter;


/**
 * {@link SimShareStorage}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.x.x
 */
public class SimShareStorage implements ShareStorage {

    private final Map<String, Share> shares = new HashMap<String, Share>();

    @Override
    public Share loadShare(int contextID, String token, StorageParameters parameters) throws OXException {
        return shares.get(token);
    }

    @Override
    public void storeShare(Share share, StorageParameters parameters) throws OXException {
        shares.put(share.getToken(), share);
    }

    @Override
    public void storeShares(int contextID, List<Share> shares, StorageParameters parameters) throws OXException {
        for (Share share : shares) {
            storeShare(share, parameters);
        }
    }

    @Override
    public void updateShare(Share share, StorageParameters parameters) throws OXException {
        shares.put(share.getToken(), share);
    }

    @Override
    public void updateShares(int contextID, List<Share> shares, StorageParameters parameters) throws OXException {
        for (Share share : shares) {
            updateShare(share, parameters);
        }
    }

    @Override
    public void deleteShare(int contextID, String token, StorageParameters parameters) throws OXException {
        shares.remove(token);
    }

    @Override
    public void deleteShares(int contextID, List<String> tokens, StorageParameters parameters) throws OXException {
        for (String token : tokens) {
            deleteShare(contextID, token, parameters);
        }
    }

    @Override
    public List<Share> loadSharesCreatedBy(int contextID, int createdBy, StorageParameters parameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Share> loadShares(int contextID, List<String> tokens, StorageParameters parameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Share> loadSharesForFolder(int contextID, String folder, StorageParameters parameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Share> loadSharesForItem(int contextID, String folder, String item, StorageParameters parameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Share> loadSharesExpiredAfter(int contextID, Date expires, StorageParameters parameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Share> loadSharesForGuest(final int contextID, final int guestID, StorageParameters parameters) throws OXException {
        return filter(new Filter<Share>() {
            @Override
            public boolean accept(Share share) {
                return share.getContextID() == contextID && share.getGuest() == guestID;
            }
        });
    }

    @Override
    public List<Share> loadSharesForContext(int contextID, StorageParameters parameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    private List<Share> filter(Filter<Share> filter) {
        List<Share> output = new ArrayList<Share>();
        com.openexchange.tools.Collections.collect(shares.values(), filter, output);
        return output;
    }

}
