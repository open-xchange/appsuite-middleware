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
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link ShareUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareUtils {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ShareUtils}.
     *
     * @param services The service lookup reference
     */
    public ShareUtils(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the context where the session's user is located in.
     *
     * @param session The session
     * @return The context
     */
    public Context getContext(Session session) throws OXException {
        if (ServerSession.class.isInstance(session)) {
            return ((ServerSession) session).getContext();
        }
        return requireService(ContextService.class).getContext(session.getContextId());
    }

    /**
     * Gets the session's user.
     *
     * @param session The session
     * @return The user
     */
    public User getUser(Session session) throws OXException {
        if (ServerSession.class.isInstance(session)) {
            return ((ServerSession) session).getUser();
        }
        return requireService(UserService.class).getUser(session.getUserId(), session.getContextId());
    }

    /**
     * Filters a list of share infos to only contain those shares that belong to external entities, i.e. those of type
     * {@link RecipientType#ANONYMOUS} or {@link RecipientType#GUEST}.
     *
     * @param shareInfos The shares to filter
     * @return The filtered shares
     */
    public List<ShareInfo> removeInternal(List<ShareInfo> shareInfos) {
        List<ShareInfo> externalShares = new ArrayList<ShareInfo>();
        if (null != shareInfos && 0 < shareInfos.size()) {
            for (ShareInfo shareInfo : externalShares) {
                RecipientType type = shareInfo.getGuest().getRecipientType();
                if (RecipientType.ANONYMOUS.equals(type) || RecipientType.GUEST.equals(type)) {
                    externalShares.add(shareInfo);
                }
            }
        }
        return externalShares;
    }

    /**
     * Gets the service of specified type, throwing an appropriate excpetion if it's missing.
     *
     * @param clazz The service's class
     * @return The service
     */
    public <S extends Object> S requireService(Class<? extends S> clazz) throws OXException {
        S service = services.getService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.absentService(clazz);
        }
        return service;
    }

}
