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
import java.util.Collections;
import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Autoboxing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.PersonalizedShareTarget;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.user.UserService;

/**
 * {@link DefaultShareInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class DefaultShareInfo extends ResolvedGuestShare implements ShareInfo {

    /**
     * Creates a list of extended share info objects for the supplied shares.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param shares The shares
     * @return The share infos
     */
//    public static List<ShareInfo> createShareInfos(ServiceLookup services, int contextID, List<Share> shares) throws OXException {
//        if (null == shares || 0 == shares.size()) {
//            return Collections.emptyList();
//        }
//        /*
//         * retrieve referenced guest users
//         */
//        Context context = services.getService(ContextService.class).getContext(contextID);
//        Set<Integer> guestIDs = ShareTool.getGuestIDs(shares);
//        User[] users = services.getService(UserService.class).getUser(context, I2i(guestIDs));
//        Map<Integer, User> guestUsers = new HashMap<Integer, User>(users.length);
//        for (User user : users) {
//            if (false == user.isGuest()) {
//                throw ShareExceptionCodes.UNKNOWN_GUEST.create(I(user.getId()));
//            }
//            guestUsers.put(Integer.valueOf(user.getId()), user);
//        }
//        /*
//         * build & return share infos
//         */
//        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
//        List<ShareInfo> shareInfos = new ArrayList<ShareInfo>(shares.size());
//        for (Share share : shares) {
//            User user = guestUsers.get(I(share.getGuest()));
//            PersonalizedShareTarget personalizedTarget = moduleSupport.personalizeTarget(share.getTarget(), contextID, user.getId());
//            shareInfos.add(new DefaultShareInfo(services, contextID, user, share, personalizedTarget));
//        }
//        return shareInfos;
//    }

    /**
     * Creates a list of extended share info objects for the supplied shares.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestID The guest ID
     * @param targets The share targets
     * @return The share infos
     */
    public static List<ShareInfo> createShareInfos(ServiceLookup services, int contextID, int guestID, List<ShareTarget> targets) throws OXException {
        if (null == targets || 0 == targets.size()) {
            return Collections.emptyList();
        }
        /*
         * retrieve referenced guest users
         */
        Context context = services.getService(ContextService.class).getContext(contextID);
        User user = services.getService(UserService.class).getUser(guestID, context);
        if (false == user.isGuest()) {
            throw ShareExceptionCodes.UNKNOWN_GUEST.create(Autoboxing.I(user.getId()));
        }
        /*
         * build & return share infos
         */
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        List<ShareInfo> shareInfos = new ArrayList<ShareInfo>(targets.size());
        for (ShareTarget target : targets) {
            PersonalizedShareTarget personalizedTarget = moduleSupport.personalizeTarget(target, contextID, user.getId());
            shareInfos.add(new DefaultShareInfo(services, contextID, user, target, personalizedTarget));
        }
        return shareInfos;
    }

    private final ShareTarget target;
    private final PersonalizedShareTarget personalizedTarget;

    /**
     * Initializes a new {@link DefaultShareInfo}.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestUser The guest user
     * @param target The target
     * @throws OXException
     */
    public DefaultShareInfo(ServiceLookup services, int contextID, User guestUser, ShareTarget target, PersonalizedShareTarget personalizedTarget) throws OXException {
        super(services, contextID, guestUser, Collections.singletonList(target), Collections.singletonList(personalizedTarget));
        this.target = target;
        this.personalizedTarget = personalizedTarget;
    }

    @Override
    public ShareTarget getTarget() {
        return target;
    }

    @Override
    public String getShareURL(HostData hostData) {
        ShareTarget target = getSingleTarget();
        if (target == null) {
            return ShareLinks.generateExternal(hostData, super.getGuest().getBaseToken(), null);
        }

        return ShareLinks.generateExternal(hostData, super.getGuest().getBaseToken(), personalizedTarget);
    }

}
