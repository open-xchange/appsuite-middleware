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

package com.openexchange.halo.xing;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.java.Strings;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.xing.Contacts;
import com.openexchange.xing.Path;
import com.openexchange.xing.UserField;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.access.XingOAuthAccessProvider;
import com.openexchange.xing.exception.XingApiException;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingUnlinkedException;
import com.openexchange.xing.session.WebAuthSession;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class XingUserDataSource implements HaloContactDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(XingUserDataSource.class);

    private final XingOAuthAccessProvider provider;

    public XingUserDataSource(final XingOAuthAccessProvider provider) {
        super();
        this.provider = provider;
    }

    @Override
    public String getId() {
        return "com.openexchange.halo.xing";
    }

    @Override
    public boolean isAvailable(ServerSession session) throws OXException {
        try {
            provider.getXingOAuthAccount(session);
        } catch (OXException e) {
            if (OAuthExceptionCodes.ACCOUNT_NOT_FOUND.equals(e)) {
                return false;
            }

            throw e;
        }

        return true;
    }

    @Override
    public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData req, ServerSession session) throws OXException {
        XingAPI<WebAuthSession> api = getAPI(session);
        com.openexchange.xing.User userInfo = loadXingUser(api, query);
        XingInvestigationResult result = new XingInvestigationResult(userInfo);
        if (userInfo != null) {
            try {
                com.openexchange.xing.User sessionUser = api.userInfo();
                String sessionUserId = sessionUser.getId();
                String otherId = userInfo.getId();
                if (!sessionUserId.equals(otherId)) {
                    EnumSet<UserField> fields = EnumSet.noneOf(UserField.class);
                    fields.add(UserField.DISPLAY_NAME);
                    fields.add(UserField.FIRST_NAME);
                    fields.add(UserField.LAST_NAME);
                    fields.add(UserField.PHOTO_URLS);
                    fields.add(UserField.PERMALINK);
                    try {
                        Path shortestPath = api.getShortestPath(sessionUserId, userInfo.getId(), fields);
                        result.setShortestPath(shortestPath);
                    } catch (XingApiException e) {
                        LOG.warn("Could not load shortest path from XING.", e);
                    }

                    try {
                        Contacts sharedContacts = api.getSharedContactsWith(otherId, 0, 0, UserField.LAST_NAME, fields);
                        result.setSharedContacts(sharedContacts);
                    } catch (XingApiException e) {
                        LOG.warn("Could not load shared contacts from XING.", e);
                    }
                }
            } catch (XingUnlinkedException e) {
                throw XingExceptionCodes.UNLINKED_ERROR.create();
            } catch (XingException e) {
                throw XingExceptionCodes.XING_ERROR.create(e, e.getMessage());
            }
        }

        return new AJAXRequestResult(result, XingInvestigationResult.class.getName());
    }

    private XingAPI<WebAuthSession> getAPI(ServerSession session) throws OXException {
        XingOAuthAccess access = provider.accessFor(provider.getXingOAuthAccount(session), session);
        return access.getXingAPI();
    }

    private static com.openexchange.xing.User loadXingUser(XingAPI<WebAuthSession> api, HaloContactQuery query) throws OXException {
        List<String> mailAddresses = prepareMailAddresses(query);
        if (mailAddresses.isEmpty()) {
            return null;
        }

        try {
            List<String> userIds = api.findByEmails(mailAddresses);
            if (!userIds.isEmpty()) {
                return api.userInfo(userIds.get(0));
            }
        } catch (XingUnlinkedException e) {
            throw XingExceptionCodes.UNLINKED_ERROR.create();
        } catch (XingException e) {
            throw XingExceptionCodes.XING_ERROR.create(e, e.getMessage());
        }

        return null;
    }

    private static List<String> prepareMailAddresses(HaloContactQuery query) {
        List<String> mailAddresses = new LinkedList<String>();
        User user = query.getUser();
        if (user != null) {
            addMailAddress(user.getMail(), mailAddresses);
        }

        Contact contact = query.getContact();
        if (contact != null) {
            addMailAddress(contact.getEmail1(), mailAddresses);
            addMailAddress(contact.getEmail2(), mailAddresses);
            addMailAddress(contact.getEmail3(), mailAddresses);
        }

        return mailAddresses;
    }

    private static void addMailAddress(String address, List<String> mailAddresses) {
        if (Strings.isNotEmpty(address)) {
            mailAddresses.add(address);
        }
    }

}
