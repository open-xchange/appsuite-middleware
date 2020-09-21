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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.file.storage.xctx;

import static com.openexchange.java.Autoboxing.I;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.List;
import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.EntityInfo.Type;
import com.openexchange.session.Session;
import com.openexchange.tools.id.IDMangler;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link EntityHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class EntityHelper {

    private final XctxAccountAccess accountAccess;
    private final Session guestSession;
    private final Session localSession;

    /**
     * Initializes a new {@link EntityHelper}.
     * 
     * @param accountAccess The parent account access
     * @param localSession The user's <i>local</i> session associated with the file storage account
     * @param guestSession The <i>remote</i> session of the guest user used to access the contents of the foreign context
     */
    public EntityHelper(XctxAccountAccess accountAccess, Session localSession, Session guestSession) {
        super();
        this.accountAccess = accountAccess;
        this.guestSession = guestSession;
        this.localSession = localSession;
    }

    String mangleRemoteEntity(int entity) {
        if (0 >= entity) {
            return null;
        }
        return IDMangler.mangle(accountAccess.getService().getId(), accountAccess.getAccountId(), String.valueOf(entity));
    }

    EntityInfo lookupUserEntity(Session session, int userId) {
        User user;
        try {
            user = accountAccess.getServiceSafe(UserService.class).getUser(userId, session.getContextId());
        } catch (OXException e) {
            getLogger(EntityHelper.class).warn("Error looking up user {} in context {}", I(userId), I(session.getContextId()), e);
            return null;
        }
        //TODO anonymization required although it's still a "peer" permission?
        EntityInfo.Type type = user.isGuest() ? Type.GUEST : Type.USER;
        return new EntityInfo(String.valueOf(user.getId()), user.getDisplayName(), null, user.getGivenName(), user.getSurname(), user.getMail(), user.getId(), null, type);
    }

    int unmangleLocalEntity(String identifier) {
        if (null != identifier) {
            List<String> components = IDMangler.unmangle(identifier);
            if (matchesAccount(components)) {
                try {
                    return Integer.parseInt(components.get(2));
                } catch (NumberFormatException e) {
                    getLogger(EntityHelper.class).warn("Unexpected error extracting entity identifier from {}", identifier, e);
                }
            }
        }
        return -1;
    }

    EntityInfo mangleRemoteUserEntity(EntityInfo entityInfo, int userId) {
        EntityInfo remoteEntityInfo = null != entityInfo ? entityInfo : 0 < userId ? lookupUserEntity(guestSession, userId) : null;
        return mangleRemoteEntity(remoteEntityInfo);
    }

    EntityInfo mangleRemoteEntity(EntityInfo entityInfo) {
        if (null == entityInfo) {
            return null;
        }
        String identifier = entityInfo.getIdentifier();
        if (null != identifier) {
            identifier = IDMangler.mangle(accountAccess.getService().getId(), accountAccess.getAccountId(), identifier);
        }
        String imageUrl = entityInfo.getImageUrl();
        if (null != imageUrl) {
            // TODO: encode service/account?
            imageUrl = null;
        }
        return new EntityInfo(identifier, entityInfo.getDisplayName(), entityInfo.getTitle(), entityInfo.getFirstName(), entityInfo.getLastName(), entityInfo.getEmail1(), 0, imageUrl, entityInfo.getType());
    }

    EntityInfo unmangleLocalEntity(EntityInfo entityInfo) {
        if (null == entityInfo) {
            return null;
        }
        String identifier = entityInfo.getIdentifier();
        if (null != identifier) {
            List<String> components = IDMangler.unmangle(identifier);
            if (matchesAccount(components)) {
                identifier = components.get(2);
            }
        }
        String imageUrl = entityInfo.getImageUrl();
        if (null != imageUrl) {
            // TODO: decode service/account?
            imageUrl = null;
        }
        //TODO: re-extract entity?

        return new EntityInfo(identifier, entityInfo.getDisplayName(), entityInfo.getTitle(), entityInfo.getFirstName(), entityInfo.getLastName(), entityInfo.getEmail1(), 0, imageUrl, entityInfo.getType());
    }
    
    private boolean matchesAccount(List<String> unmangledComponents) {
        return null != unmangledComponents && 3 == unmangledComponents.size() && 
            Objects.equals(unmangledComponents.get(0), accountAccess.getService().getId()) && 
            Objects.equals(unmangledComponents.get(1), accountAccess.getAccountId());
    }

}
