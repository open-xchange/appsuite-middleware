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

package com.openexchange.share.json.actions;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;


/**
 * {@link TokenParser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class TokenParser {
//
//    /**
//     * Takes a token as provided by a client and parses it to determine the plain
//     * share token and the optional target path.
//     *
//     * @param token The token
//     * @return A pair containing the share token as first and the target path as second
//     * element. If no target path is set, the according element is <code>null</code>.
//     */
//    public static Pair<String, String> parseToken(String token) throws OXException {
//        String shareToken = null;
//        String targetPath = null;
//        String[] split = token.split("/");
//        if (split.length == 1) {
//            shareToken = token;
//            targetPath = null;
//        } else if (split.length == 2) {
//            shareToken = split[0];
//            targetPath = split[1];
//        } else {
//            throw ShareExceptionCodes.INVALID_TOKEN.create(token);
//        }
//
//        return new Pair<String, String>(shareToken, targetPath);
//    }

    /**
     * Obtains the share identified by the given token from the given {@link ShareService} instance.
     * This method takes care about general and specific tokens. I.e. if the token has a postfix for
     * a specific {@link ShareTarget}, the resolve-method is called with the general token.
     *
     * @param token The token
     * @param shareService The {@link ShareService}
     * @return The guest share, containing all shares the user has access to, or null if no valid share could be looked up
     * @throws OXException
     */
    public static GuestShare resolveShare(String token, ShareService shareService) throws OXException {
        String shareToken = null;
        String[] split = token.split("/");
        if (split.length == 1) {
            shareToken = token;
        } else if (split.length == 2) {
            shareToken = split[0];
        } else {
            throw ShareExceptionCodes.INVALID_TOKEN.create(token);
        }

        return shareService.resolveToken(shareToken);
    }

    /**
     * Gets the list of {@link ShareTarget}s identified by the given token. This method takes care about
     * general and specific tokens. I.e. if the token has a postfix for a specific {@link ShareTarget},
     * the returned list contains only this single target. Otherwise it contains all targets of the share.
     *
     * @param share The share
     * @param token The token
     * @return A list of {@link ShareTarget}s.
     * @throws OXException
     */
    public static List<ShareTarget> resolveTargets(GuestShare share, String token) throws OXException {
        if (share == null) {
            throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
        }

        String sharePath = null;
        String[] split = token.split("/");
        if (split.length == 2) {
            sharePath = split[1];
        } else if (split.length != 1) {
            throw ShareExceptionCodes.INVALID_TOKEN.create(token);
        }

        if (sharePath == null) {
            return share.getTargets();
        } else {
            ShareTarget target = share.resolveTarget(sharePath);
            if (target == null) {
                throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
            }

            return Collections.singletonList(target);
        }
    }

}
