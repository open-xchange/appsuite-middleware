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

package com.openexchange.contact.picture.json.util;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.contact.picture.json.ContactPictureActionFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.ContactPictureURLService;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.Sessions;

/**
 * {@link ContactPictureURLServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class ContactPictureURLServiceImpl implements ContactPictureURLService {

    private final AtomicReference<DispatcherPrefixService> DPS_REF = new AtomicReference<DispatcherPrefixService>();
    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContactPictureURLServiceImpl}.
     * 
     * @param serviceLookup The {@link ServiceLookup} 
     */
    public ContactPictureURLServiceImpl(ServiceLookup serviceLookup) {
        this.services = serviceLookup;
    }

    @Override
    public String getContactPictureUrl(Integer contactId, Integer folderId, Session session, boolean preferRelativeUrl) throws OXException {
        if (contactId == null || folderId == null) {
            throw ContactExceptionCodes.UNEXPECTED_ERROR.create("The contactId and the folderId must not be null!");
        }
        return getPictureUrl(contactId, folderId, null, session, preferRelativeUrl);
    }

    @Override
    public String getUserPictureUrl(Integer userId, Session session, boolean preferRelativeUrl) throws OXException {
        if (userId == null) {
            throw ContactExceptionCodes.UNEXPECTED_ERROR.create("The userId must not be null!");
        }
        return getPictureUrl(null, null, userId, session, preferRelativeUrl);
    }

    private String getPictureUrl(Integer contactId, Integer folderId, Integer userId, final Session session, final boolean preferRelativeUrl) throws OXException {
        StringBuilder sb = new StringBuilder();
        final String prefix;
        final HostData hostData = (HostData) session.getParameter(HostnameService.PARAM_HOST_DATA);
        if (hostData == null) {
            /*
             * Compose relative URL
             */
            prefix = "";
        } else {
            /*
             * Compose absolute URL if a relative one is not preferred
             */
            if (preferRelativeUrl) {
                prefix = "";
            } else {
                sb.append(hostData.isSecure() ? "https://" : "http://");
                sb.append(hostData.getHost());
                final int port = hostData.getPort();
                if ((hostData.isSecure() && port != 443) || (!hostData.isSecure() && port != 80)) {
                    sb.append(':').append(port);
                }
                prefix = sb.toString();
                sb.setLength(0);
            }
        }
        /*
         * Compose URL parameters
         */
        sb.append(prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix);
        sb.append(getDispatcherPrefix());
        boolean isOAuthSession = Sessions.isOAuthSession(session);
        if (isOAuthSession) {
            sb.append(OAuthConstants.OAUTH_SERVLET_SUBPREFIX);
        }

        sb.append(ContactPictureActionFactory.Module);
        sb.append("?action=get");

        if (contactId != null && folderId != null) {
            sb.append("&contactId=").append(contactId);
            sb.append("&folderId=").append(folderId);
        } else {
            sb.append("&userId=").append(userId);
        }
        return sb.toString();
    }

    String getDispatcherPrefix() throws OXException {
        DispatcherPrefixService dispatcherPrefixService = DPS_REF.get();
        if (dispatcherPrefixService == null) {
            dispatcherPrefixService = services.getServiceSafe(DispatcherPrefixService.class);
            DPS_REF.set(dispatcherPrefixService);
        }

        return dispatcherPrefixService.getPrefix();
    }

}
