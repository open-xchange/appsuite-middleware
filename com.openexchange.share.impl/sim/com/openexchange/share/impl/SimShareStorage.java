///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the Open-Xchange, Inc. group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2004-2014 Open-Xchange, Inc.
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//
//package com.openexchange.share.impl;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import com.openexchange.exception.OXException;
//import com.openexchange.share.ShareList;
//import com.openexchange.share.Share;
//import com.openexchange.share.storage.ShareStorage;
//import com.openexchange.share.storage.StorageParameters;
//import com.openexchange.tools.Collections.Filter;
//
//
///**
// * {@link SimShareStorage}
// *
// * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
// * @since v7.x.x
// */
//public class SimShareStorage implements ShareStorage {
//
//    private final Map<String, ShareList> shares = new HashMap<String, ShareList>();
//
//    @Override
//    public ShareList loadShare(int contextID, String token, StorageParameters parameters) throws OXException {
//        return shares.get(token);
//    }
//
//    @Override
//    public void storeShare(ShareList share, StorageParameters parameters) throws OXException {
//        shares.put(share.getToken(), share);
//    }
//
//    @Override
//    public void storeShares(int contextID, List<ShareList> shares, StorageParameters parameters) throws OXException {
//        for (ShareList share : shares) {
//            storeShare(share, parameters);
//        }
//    }
//
//    @Override
//    public void updateShare(ShareList share, StorageParameters parameters) throws OXException {
//        shares.put(share.getToken(), share);
//    }
//
//    @Override
//    public void updateShares(int contextID, List<ShareList> shares, StorageParameters parameters) throws OXException {
//        for (ShareList share : shares) {
//            updateShare(share, parameters);
//        }
//    }
//
//    @Override
//    public void deleteShare(int contextID, String token, StorageParameters parameters) throws OXException {
//        shares.remove(token);
//    }
//
//    @Override
//    public void deleteShares(int contextID, List<String> tokens, StorageParameters parameters) throws OXException {
//        for (String token : tokens) {
//            deleteShare(contextID, token, parameters);
//        }
//    }
//
//    @Override
//    public List<ShareList> loadSharesCreatedBy(int contextID, int createdBy, StorageParameters parameters) throws OXException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public List<ShareList> loadShares(int contextID, List<String> tokens, StorageParameters parameters) throws OXException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public List<ShareList> loadSharesForTarget(final int contextID, final Share target, StorageParameters parameters) throws OXException {
//        return filter(new Filter<ShareList>() {
//            @Override
//            public boolean accept(ShareList share) {
//                if (share.getContextID() == contextID) {
//                    for (Share t : share.getTargets()) {
//                        if (target.equals(t)) {
//                            return true;
//                        }
//                    }
//                }
//                return false;
//            }
//        });
//    }
//
//    @Override
//    public List<ShareList> loadSharesExpiredAfter(int contextID, Date expires, StorageParameters parameters) throws OXException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public List<ShareList> loadSharesForGuest(final int contextID, final int guestID, StorageParameters parameters) throws OXException {
//        return filter(new Filter<ShareList>() {
//            @Override
//            public boolean accept(ShareList share) {
//                return share.getContextID() == contextID && share.getGuest() == guestID;
//            }
//        });
//    }
//
//    @Override
//    public List<ShareList> loadSharesForGuests(final int contextID, final int[] guestIDs, StorageParameters parameters) throws OXException {
//        return filter(new Filter<ShareList>() {
//            @Override
//            public boolean accept(ShareList share) {
//                return share.getContextID() == contextID && com.openexchange.tools.arrays.Arrays.contains(guestIDs, share.getGuest());
//            }
//        });
//    }
//
//    @Override
//    public List<ShareList> loadSharesForContext(final int contextID, StorageParameters parameters) throws OXException {
//        return filter(new Filter<ShareList>() {
//            @Override
//            public boolean accept(ShareList share) {
//                return share.getContextID() == contextID;
//            }
//        });
//    }
//
//    private List<ShareList> filter(Filter<ShareList> filter) {
//        List<ShareList> output = new ArrayList<ShareList>();
//        com.openexchange.tools.Collections.collect(shares.values(), filter, output);
//        return output;
//    }
//
//    @Override
//    public List<ShareList> loadSharesForTarget(int contextID, Share target, int[] guestIDs, StorageParameters parameters) throws OXException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//}
