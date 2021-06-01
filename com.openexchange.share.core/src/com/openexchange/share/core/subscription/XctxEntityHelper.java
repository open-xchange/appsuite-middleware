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

package com.openexchange.share.core.subscription;

import static com.openexchange.java.Autoboxing.I;
import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import java.net.URISyntaxException;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.EntityInfo.Type;
import com.openexchange.groupware.LinkEntityInfo;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.subscription.XctxHostData;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link XctxEntityHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public abstract class XctxEntityHelper extends EntityMangler {

    protected final String shareUrl;

    /**
     * Initializes a new {@link XctxEntityHelper}.
     * 
     * @param serviceId The service identifier to mangle into qualified identifiers
     * @param accountId The account identifier to mangle into qualified identifiers
     * @param shareUrl The share URL that is used to access the cross-context share
     */
    public XctxEntityHelper(String serviceId, String accountId, String shareUrl) {
        super(serviceId, accountId);
        this.shareUrl = shareUrl;
    }

    protected abstract UserService getUserService() throws OXException;

    protected abstract GroupService getGroupService() throws OXException;

    protected abstract ShareService getShareService() throws OXException;

    protected abstract DispatcherPrefixService getDispatcherPrefixService() throws OXException;

    /**
     * Resolves and builds additional entity info for a certain user or group under perspective of the passed session's user.
     * <p/>
     * If no entity could be found in the session's context, a placeholder entity is returned.
     * 
     * @param session The session to use to resolve the entity
     * @param entity The identifier of the entity to resolve
     * @param isGroup <code>true</code> if the entity refers to a group, <code>false</code>, otherwise
     * @return The entity info, or <code>null</code> if the referenced entity could not be resolved
     */
    protected EntityInfo lookupEntity(Session session, int entity, boolean isGroup) {
        if (0 > entity || 0 == entity && false == isGroup) {
            getLogger(XctxEntityHelper.class).warn("Unable to lookup entity info for {}", I(entity));
            return null;
        }
        if (isGroup) {
            /*
             * lookup group and build entity info
             */
            Group group = lookupGroup(session, entity);
            return null != group ? getEntityInfo(group) : null;
        }
        /*
         * lookup user and build entity info
         */
        User user = lookupUser(session, entity);
        if (null == user) {
            return null;
        }
        EntityInfo entityInfo = getEntityInfo(user);
        if (ShareTool.isAnonymousGuest(user)) {
            /*
             * derive additional link information for anonymous guests
             */
            GuestInfo guest = lookupGuest(session, entity);
            if (null != guest) {
                String shareUrl = generateShareLink(session, guest);
                return new LinkEntityInfo(entityInfo, shareUrl, guest.getPassword(), guest.getExpiryDate(), false);
            }
        }
        return entityInfo;
    }

    private User lookupUser(Session session, int userId) {
        try {
            return getUserService().getUser(userId, session.getContextId());
        } catch (OXException e) {
            getLogger(XctxEntityHelper.class).warn("Error looking up user {} in context {}", I(userId), I(session.getContextId()), e);
            return null;
        }
    }

    private Group lookupGroup(Session session, int groupId) {
        try {
            return getGroupService().getGroup(ServerSessionAdapter.valueOf(session).getContext(), groupId);
        } catch (OXException e) {
            getLogger(XctxEntityHelper.class).warn("Error looking up group {} in context {}", I(groupId), I(session.getContextId()), e);
            return null;
        }
    }

    private GuestInfo lookupGuest(Session session, int guestId) {
        try {
            return getShareService().getGuestInfo(session, guestId);
        } catch (OXException e) {
            getLogger(XctxEntityHelper.class).warn("Error looking up guest {} in context {}", I(guestId), I(session.getContextId()), e);
            return null;
        }
    }
    
    private EntityInfo getEntityInfo(User user) {
        EntityInfo.Type type = user.isGuest() ? (user.isAnonymousGuest() ? Type.ANONYMOUS : Type.GUEST) : Type.USER;
        return new EntityInfo(String.valueOf(user.getId()), user.getDisplayName(), null, user.getGivenName(), user.getSurname(), user.getMail(), user.getId(), null, type);
    }
    
    private EntityInfo getEntityInfo(Group group) {
        return new EntityInfo(String.valueOf(group.getIdentifier()), group.getDisplayName(), null, null, null, null, group.getIdentifier(), null, Type.GROUP);
    }

    private String generateShareLink(Session session, GuestInfo guest) {
        try {
            if (null != guest.getLinkTarget()) {
                ShareTargetPath targetPath = new ShareTargetPath(guest.getLinkTarget().getModule(), guest.getLinkTarget().getFolder(), guest.getLinkTarget().getItem());
                return guest.generateLink(getGuestHostData(session), targetPath);
            }
            return guest.generateLink(getGuestHostData(session), null);
        } catch (OXException e) {
            getLogger(XctxEntityHelper.class).warn("Error generating share link for {}", guest, e);
            return null;
        }
    }

    /**
     * Gets a {@link HostData} implementation under the perspective of the guest share.
     *
     * @param session The guest session in the remote context
     * @return The host data for the guest
     * @throws OXException In case URL is missing or invalid
     */
    private HostData getGuestHostData(Session session) throws OXException {
        if (Strings.isEmpty(shareUrl)) {
            throw ShareExceptionCodes.INVALID_LINK.create(shareUrl);
        }
        URI uri;
        try {
            uri = new URI(shareUrl);
        } catch (URISyntaxException e) {
            throw ShareExceptionCodes.INVALID_LINK.create(shareUrl, e);
        }
        return new XctxHostData(uri, session) {

            @Override
            protected DispatcherPrefixService getDispatcherPrefixService() throws OXException {
                return XctxEntityHelper.this.getDispatcherPrefixService();
            }
        };
    }

}
