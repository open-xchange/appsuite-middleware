/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderPermissionType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.java.Autoboxing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.SubfolderAwareTargetPermission;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link TokenCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class TokenCollection {

    private final Set<ShareToken> baseTokensOnly;
    private final Map<ShareToken, Set<String>> pathsPerBaseToken;
    private final ServiceLookup services;
    private final int contextID;
    private final Set<Integer> guestIDs;

    /**
     * Initializes a new {@link TokenCollection} based on the supplied tokens. The tokens might either be in their absolute format (i.e.
     * base token plus path), as well as in their base format only, superseding any absolute tokens with the same base token.
     *
     * @param services A service lookup reference
     * @param contextID The context identifier
     * @param tokens The tokens
     * @throws OXException
     */
    public TokenCollection(ServiceLookup services, int contextID, List<String> tokens) throws OXException {
        super();
        this.services = services;
        this.contextID = contextID;
        this.baseTokensOnly = new HashSet<ShareToken>();
        this.pathsPerBaseToken = new HashMap<ShareToken, Set<String>>();
        this.guestIDs = new HashSet<Integer>();
        /*
         * map tokens
         */
        for (String token : tokens) {
            ShareToken shareToken = new ShareToken(token);
            if (contextID != shareToken.getContextID()) {
                throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
            }
            guestIDs.add(Integer.valueOf(shareToken.getUserID()));
            String baseToken = shareToken.getToken();
            if (token.length() > baseToken.length() + 1 && '/' == token.charAt(baseToken.length())) {
                /*
                 * base token with path
                 */
                Set<String> paths = pathsPerBaseToken.get(shareToken);
                if (null == paths) {
                    paths = new HashSet<String>();
                    pathsPerBaseToken.put(shareToken, paths);
                }
                paths.add(token.substring(baseToken.length() + 1));
            } else {
                /*
                 * base token only
                 */
                baseTokensOnly.add(shareToken);
            }
        }
        /*
         * remove redundant tokens with path
         */
        if (0 < baseTokensOnly.size() && 0 < pathsPerBaseToken.size()) {
            for (ShareToken baseToken : baseTokensOnly) {
                pathsPerBaseToken.remove(baseToken);
            }
        }
    }

    /**
     * Loads all shares referenced by the tokens, i.e. all guest shares for the defined base tokens, as well as shares to specific
     * targets as defined by the tokens with path.
     *
     * @param parameters The storage parameters
     * @return The shares
     * @throws OXException
     */
    public List<ShareInfo> loadShares() throws OXException {
        List<ShareInfo> shares = new ArrayList<ShareInfo>();
        UserService userService = services.getService(UserService.class);
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        /*
         * gather all shares for guest users with base token only
         */
        for (ShareToken baseToken : baseTokensOnly) {
            User guestUser = userService.getUser(baseToken.getUserID(), contextID);
            List<TargetProxy> targetProxies = moduleSupport.listTargets(contextID, guestUser.getId());
            for (TargetProxy proxy : targetProxies) {
                ShareTargetPath targetPath = proxy.getTargetPath();
                ShareTarget srcTarget = new ShareTarget(targetPath.getModule(), targetPath.getFolder(), targetPath.getItem());
                ShareTarget dstTarget = proxy.getTarget();
                shares.add(new DefaultShareInfo(services, contextID, guestUser, srcTarget, dstTarget, targetPath, checkForLegatorPermission(proxy.getPermissions(), guestUser.getId())));
            }
        }
        /*
         * pick specific shares for guest users with base tokens and paths
         */
        for (Map.Entry<ShareToken, Set<String>> entry : pathsPerBaseToken.entrySet()) {
            User guestUser = userService.getUser(entry.getKey().getUserID(), contextID);
            for (String path : entry.getValue()) {
                ShareTargetPath targetPath = ShareTargetPath.parse(path);
                if (targetPath != null) {
                    TargetProxy proxy = moduleSupport.resolveTarget(targetPath, contextID, guestUser.getId());
                    if (proxy != null) {
                        ShareTarget srcTarget = new ShareTarget(targetPath.getModule(), targetPath.getFolder(), targetPath.getItem());
                        ShareTarget dstTarget = proxy.getTarget();
                        shares.add(new DefaultShareInfo(services, contextID, guestUser, srcTarget, dstTarget, targetPath, checkForLegatorPermission(proxy.getPermissions(), guestUser.getId())));
                    }
                }
            }
        }
        return shares;
    }

    /** The default permission bits to use for anonymous link shares */
    private static final int LINK_PERMISSION_BITS = Permissions.createPermissionBits(
        Permission.READ_FOLDER, Permission.READ_ALL_OBJECTS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, false);

    private boolean checkForLegatorPermission(List<TargetPermission> permissions, int guestId){
        for(TargetPermission perm: permissions) {
            if (perm.isGroup() == false && perm.getBits() == LINK_PERMISSION_BITS && perm.getEntity() == guestId) {
                if (perm instanceof SubfolderAwareTargetPermission) {
                    return ((SubfolderAwareTargetPermission) perm).getType() == FolderPermissionType.LEGATOR.getTypeNumber();
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Gets a set containing all share tokens that were supplied in their base token format.
     *
     * @return The base tokens, or an empty set if there were none
     */
    public Set<ShareToken> getBaseTokensOnly() {
        return baseTokensOnly;
    }

    /**
     * Gets a map holding those tokens that were supplied along with a specific path.
     *
     * @return The paths mapped to their share token, or an empty map if there were none
     */
    public Map<ShareToken, Set<String>> getPathsPerBaseToken() {
        return pathsPerBaseToken;
    }

    /**
     * Gets the identifiers of all guest users referenced by any of the tokens.
     *
     * @return The identifiers of the guest users
     */
    public int[] getGuestUserIDs() {
        return Autoboxing.I2i(guestIDs);
    }

}
