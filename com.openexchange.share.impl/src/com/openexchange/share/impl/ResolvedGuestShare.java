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
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestShare;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;

/**
 * {@link ResolvedGuestShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ResolvedGuestShare implements GuestShare {

    private final int contextID;
    private final User guestUser;
    private final List<ShareTarget> targets;

    /**
     * Initializes a new {@link ResolvedGuestShare}.
     *
     * @param contextID The context ID
     * @param guestUser The guest user
     * @param shares The shares
     * @throws OXException
     */
    public ResolvedGuestShare(int contextID, User guestUser, List<Share> shares) throws OXException {
        super();
        this.contextID = contextID;
        this.guestUser = guestUser;
        this.targets = new ArrayList<ShareTarget>(shares.size());
        for (Share share : shares) {
            if (share.getGuest() != guestUser.getId()) {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Share " + share + " does not belong to guest " + guestUser);
            }
            targets.add(share.getTarget());
        }
    }

    @Override
    public int getGuestID() {
        return guestUser.getId();
    }

    @Override
    public int getContextID() {
        return contextID;
    }

    @Override
    public List<ShareTarget> getTargets() {
        return targets;
    }

    @Override
    public AuthenticationMode getAuthentication() {
        return ShareTool.getAuthenticationMode(guestUser);
    }

    @Override
    public String getToken() throws OXException {
        return new ShareToken(contextID, guestUser).getToken();
    }

    @Override
    public int getCommonModule() {
        if (null == targets || 0 == targets.size()) {
            return 0;
        } else {
            int module = targets.get(0).getModule();
            for (int i = 1; i < targets.size(); i++) {
                if (module != targets.get(i).getModule()) {
                    return 0;
                }
            }
            return module;
        }
    }

    @Override
    public String getCommonFolder() {
        if (null == targets || 0 == targets.size()) {
            return null;
        } else {
            String folder = targets.get(0).getFolder();
            if (null == folder) {
                return null;
            }
            for (int i = 1; i < targets.size(); i++) {
                if (false == folder.equals(targets.get(i).getFolder())) {
                    return null;
                }
            }
            return folder;
        }
    }

    @Override
    public ShareTarget resolveTarget(String path) {
        if (null != targets && 0 < targets.size() && null != path) {
            for (ShareTarget target : targets) {
                if (path.equals(target.getPath())) {
                    return target;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isMultiTarget() {
        return null != targets && 1 < targets.size();
    }

    @Override
    public ShareTarget getSingleTarget() {
        return null != targets && 1 == targets.size() ? targets.get(0) : null;
    }

    @Override
    public String toString() {
        return "ResolvedGuestShare [contextID=" + contextID + ", guestUser=" + guestUser + ", targets=" + targets + "]";
    }

}
