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

package com.openexchange.halo.xing;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactImageSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.halo.Picture;
import com.openexchange.java.Strings;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.xing.Contacts;
import com.openexchange.xing.Path;
import com.openexchange.xing.PhotoUrls;
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
public class XingUserDataSource implements HaloContactDataSource, HaloContactImageSource {

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
        } catch(OXException e) {
            if (OAuthExceptionCodes.ACCOUNT_NOT_FOUND.equals(e)) {
                return false;
            }

            throw e;
        }

        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData req, ServerSession session) throws OXException {
        XingAPI<WebAuthSession> api = getAPI(session);
        com.openexchange.xing.User userInfo = loadXingUser(api, query, session);
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
            } catch (final XingUnlinkedException e) {
                throw XingExceptionCodes.UNLINKED_ERROR.create();
            } catch (final XingException e) {
                throw XingExceptionCodes.XING_ERROR.create(e, e.getMessage());
            }
        }

        return new AJAXRequestResult(result, XingInvestigationResult.class.getName());
    }

    @Override
    public String getPictureETag(HaloContactQuery query, ServerSession session) throws OXException {
        XingAPI<WebAuthSession> api = getAPI(session);
        com.openexchange.xing.User userInfo = loadXingUser(api, query, session);
        if (userInfo == null) {
            return null;
        }

        PhotoUrls photoUrls = userInfo.getPhotoUrls();
        if (photoUrls == null) {
            return null;
        }

        String url = photoUrls.getMaxiThumbUrl();
        if (url == null) {
            url = photoUrls.getLargestAvailableUrl();
        }

        if (url != null) {
            return Base64.encode(url);
        }

        return null;
    }

    @Override
    public Picture getPicture(HaloContactQuery query, ServerSession session) throws OXException {
        XingAPI<WebAuthSession> api = getAPI(session);
        com.openexchange.xing.User userInfo = loadXingUser(api, query, session);
        if (userInfo == null) {
            return null;
        }

        PhotoUrls photoUrls = userInfo.getPhotoUrls();
        if (photoUrls == null) {
            return null;
        }

        String url = photoUrls.getMaxiThumbUrl();
        if (url == null) {
            url = photoUrls.getLargestAvailableUrl();
        }

        if (url != null) {
            try {
                IFileHolder photo = api.getPhoto(url);
                if (photo == null) {
                    return null;
                }

                return new Picture(Base64.encode(url), photo);
            } catch (XingException e) {
                LOG.error("Could not load photo '{}' from XING. Returning 'null' instead.", url, e);
            }
        }

        return null;
    }

    private XingAPI<WebAuthSession> getAPI(ServerSession session) throws OXException {
        XingOAuthAccessProvider provider = getAccessProvider();
        XingOAuthAccess access = provider.accessFor(provider.getXingOAuthAccount(session), session);
        return access.getXingAPI();
    }

    private XingOAuthAccessProvider getAccessProvider() {
        return provider;
    }

    private static com.openexchange.xing.User loadXingUser(XingAPI<WebAuthSession> api, HaloContactQuery query, ServerSession session) throws OXException {
        List<String> mailAddresses = prepareMailAddresses(query);
        if (mailAddresses.isEmpty()) {
            return null;
        }

        try {
            List<String> userIds = api.findByEmails(mailAddresses);
            if (!userIds.isEmpty()) {
                return api.userInfo(userIds.get(0));
            }
        } catch (final XingUnlinkedException e) {
            throw XingExceptionCodes.UNLINKED_ERROR.create();
        } catch (final XingException e) {
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
        if (!Strings.isEmpty(address)) {
            mailAddresses.add(address);
        }
    }

}
