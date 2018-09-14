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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.impl.rmi.ShareRMIService;

/**
 * {@link ShareRMIServiceImpl}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ShareRMIServiceImpl implements ShareRMIService {

    private static final String EMPTY_STRING = "";
    private final DefaultShareService shareService;

    /**
     * Initialises a new {@link ShareRMIServiceImpl}.
     */
    public ShareRMIServiceImpl(DefaultShareService shareService) {
        super();
        this.shareService = shareService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.share.impl.rmi.ShareRMIService#listShares(int)
     */
    @Override
    public String listShares(int contextId) throws RemoteException {
        try {
            return formatForCLT(shareService.getAllShares(contextId));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.share.impl.rmi.ShareRMIService#listShares(int, int)
     */
    @Override
    public String listShares(int contextId, int guestId) throws RemoteException {
        try {
            return formatForCLT(shareService.getAllShares(contextId, guestId));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.share.impl.rmi.ShareRMIService#listShares(java.lang.String)
     */
    @Override
    public String listShares(String token) throws RemoteException {
        String path = null;
        if (Strings.isNotEmpty(token) && token.contains("/")) {
            String[] split = token.split("/");
            path = split[1];
            token = split[0];
        }
        try {
            return formatForCLT(shareService.getShares(token, path));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.share.impl.rmi.ShareRMIService#removeShare(java.lang.String, java.lang.String)
     */
    @Override
    public int removeShare(String token, String path) throws RemoteException {
        if (null != path && !path.isEmpty() && !EMPTY_STRING.equals(path)) {
            token = token + "/" + path;
        }
        try {
            return shareService.removeShares(Collections.singletonList(token));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.share.impl.rmi.ShareRMIService#removeShare(java.lang.String, java.lang.String, int)
     */
    @Override
    public int removeShare(String shareToken, String targetPath, int contextId) throws RemoteException {
        if (null != targetPath && !targetPath.isEmpty() && !EMPTY_STRING.equals(targetPath)) {
            shareToken = shareToken + "/" + targetPath;
        }
        try {
            return shareService.removeShares(Collections.singletonList(shareToken), contextId);
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.share.impl.rmi.ShareRMIService#removeShares(int)
     */
    @Override
    public int removeShares(int contextId) throws RemoteException {
        try {
            return shareService.removeShares(contextId);
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.share.impl.rmi.ShareRMIService#removeShares(int, int)
     */
    @Override
    public int removeShares(int contextId, int guestId) throws RemoteException {
        try {
            return shareService.removeShares(contextId, guestId);
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Formats the specified share information for the command line tools
     * 
     * @param shareInfos A {@link List} with the {@link ShareInfo}
     * @return A formatted string with the share information
     */
    private String formatForCLT(List<ShareInfo> shareInfos) {
        if (null == shareInfos || shareInfos.isEmpty()) {
            return "No shares found.";
        }
        StringBuilder sb = new StringBuilder();
        for (ShareInfo info : shareInfos) {
            sb.append(info).append('\n');
        }
        return sb.toString();
    }
}
