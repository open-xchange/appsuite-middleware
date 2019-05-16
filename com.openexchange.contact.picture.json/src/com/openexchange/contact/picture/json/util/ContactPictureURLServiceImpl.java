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

import static com.openexchange.contact.picture.json.PictureRequestParameter.CONTACT;
import static com.openexchange.contact.picture.json.PictureRequestParameter.CONTACT_FOLDER;
import static com.openexchange.contact.picture.json.PictureRequestParameter.GUEST_CONTEXT;
import static com.openexchange.contact.picture.json.PictureRequestParameter.GUEST_USER;
import static com.openexchange.contact.picture.json.PictureRequestParameter.USER;
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.contact.picture.json.ContactPictureActionFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.RequestContextHolder;
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
    public String getContactPictureUrl(int contactId, int folderId, Session session, Long timestamp, boolean preferRelativeUrl) throws OXException {
        if (contactId < 0 || folderId < 0) {
            throw ContactExceptionCodes.UNEXPECTED_ERROR.create("The contactId and the folderId must be set!");
        }
        return getPictureUrl(contactId, folderId, -1, session, timestamp, preferRelativeUrl);
    }

    @Override
    public String getUserPictureUrl(int userId, Session session, Long timestamp, boolean preferRelativeUrl) throws OXException {
        if (userId < 0) {
            throw ContactExceptionCodes.UNEXPECTED_ERROR.create("The userId must be set!");
        }
        return getPictureUrl(-1, -1, userId, session, timestamp, preferRelativeUrl);
    }

    /**
     * Gets the url to the contact picture. Either contact and folder id or the user id must be provided. If both are provided only the contact and user id are used.
     *
     * @param contactId The optional contact id
     * @param folderId The folder id. Must be not null in case the contact id is not null.
     * @param userId The optional user id
     * @param session The users session
     * @param timestamp An optional timestamp value to add to the url
     * @param preferRelativeUrl Whether relative url is preferred
     * @return The url to the contact picture
     * @throws OXException
     */
    private String getPictureUrl(int contactId, int folderId, int userId, final Session session, Long timestamp, final boolean preferRelativeUrl) throws OXException {
        URIBuilder builder = new URIBuilder();
        if (false == preferRelativeUrl) {
            final HostData hostData;
            com.openexchange.framework.request.RequestContext requestContext = RequestContextHolder.get();
            if (null != requestContext) {
                hostData = requestContext.getHostData();
            } else {
                hostData = (HostData) session.getParameter(HostnameService.PARAM_HOST_DATA);
            }

            if (null != hostData) {
                // Set absolute path
                builder.setScheme(hostData.isSecure() ? "https://" : "http://");
                builder.setHost(hostData.getHost());
                final int port = hostData.getPort();
                if ((hostData.isSecure() && port != 443) || (!hostData.isSecure() && port != 80)) {
                    builder.setPort(hostData.getPort());
                }
            }
        }

        // Get path
        StringBuilder sb = new StringBuilder();
        sb.append(services.getServiceSafe(DispatcherPrefixService.class).getPrefix());
        if (Sessions.isOAuthSession(session)) {
            sb.append(OAuthConstants.OAUTH_SERVLET_SUBPREFIX);
        }
        sb.append(ContactPictureActionFactory.Module);

        builder.setPath(sb.toString());
        builder.setParameter("action", "get");

        if (contactId > -1 && folderId > -1) {
            builder.setParameter(CONTACT.getParameter(), String.valueOf(contactId));
            builder.setParameter(CONTACT_FOLDER.getParameter(), String.valueOf(folderId));
        } else {
            builder.setParameter(USER.getParameter(), String.valueOf(userId));
        }

        if (timestamp != null) {
            builder.addParameter("timestamp", String.valueOf(timestamp));
        }
        if (session.containsParameter(Session.PARAM_GUEST)) {
            /*
             * If we have a guest session, explicit set context and user.
             * Thus we can support multiple session/shares cross-context
             */
            builder.setParameter(GUEST_USER.getParameter(), String.valueOf(session.getUserId()));
            builder.setParameter(GUEST_CONTEXT.getParameter(), String.valueOf(session.getContextId()));
        }

        try {
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw ContactExceptionCodes.UNEXPECTED_ERROR.create("Unable to build URI for contact.");
        }
    }
}
