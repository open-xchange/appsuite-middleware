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

package com.openexchange.halo.xing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.container.IFileHolder;
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
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.access.XingOAuthAccessProvider;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.session.WebAuthSession;


/**
 * {@link XingUserDataSource}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class XingUserDataSource implements HaloContactDataSource, HaloContactImageSource {

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
        com.openexchange.xing.User userInfo = loadXingUser(getAPI(session), query, session);
        if (userInfo == null) {
            return AJAXRequestResult.EMPTY_REQUEST_RESULT;
        }

        return new AJAXRequestResult(userInfo, com.openexchange.xing.User.class.getName());
    }

    @Override
    public Picture getPicture(HaloContactQuery query, ServerSession session) throws OXException {
        XingAPI<WebAuthSession> api = getAPI(session);
        com.openexchange.xing.User userInfo = loadXingUser(api, query, session);
        if (userInfo == null) {
            return null;
        }

        // TODO: make API nicer
        Map<String, Object> photoUrls = userInfo.getPhotoUrls();
        if (photoUrls == null || photoUrls.isEmpty()) {
            return null;
        }

        Object object = photoUrls.get("maxi_thumb");
        if (object == null) {
            return null;
        }

        try {
            String url = (String) object;
            IFileHolder photo = api.getPhoto(url);
            if (photo == null) {
                return null;
            }

            return new Picture(Base64.encode(url), photo);
        } catch (XingException e) {
            // TODO mach ma richtich
            throw new OXException(e);
        }
    }

    private XingAPI<WebAuthSession> getAPI(ServerSession session) throws OXException {
        XingOAuthAccessProvider provider = getAccessProvider();
        if (provider == null) {
            // TODO mach ma richtich
            throw new OXException();
        }

        XingOAuthAccess access = provider.accessFor(provider.getXingOAuthAccount(session), session);
        return access.getXingAPI();
    }

    private XingOAuthAccessProvider getAccessProvider() {
        return provider;
    }

    private static com.openexchange.xing.User loadXingUser(XingAPI<WebAuthSession> api, HaloContactQuery query, ServerSession session) throws OXException {
        String mailAddresses = prepareMailAddresses(query);
        if (Strings.isEmpty(mailAddresses)) {
            return null;
        }

        try {
            // TODO: change api call to take a list of addresses
            // TODO: fails with exception of user not found...
            String userId = api.findByEmails(mailAddresses);
            if (!Strings.isEmpty(userId)) {
                return api.userInfo(userId);
            }
        } catch (XingException e) {
            // TODO mach ma richtich
            throw new OXException(e);
        }

        return null;
    }

    private static String prepareMailAddresses(HaloContactQuery query) {
        Set<String> mailAddresses = new HashSet<String>();
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

        if (mailAddresses.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String address : mailAddresses) {
            sb.append(address).append(',');
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private static void addMailAddress(String address, Set<String> mailAddresses) {
        if (!Strings.isEmpty(address)) {
            mailAddresses.add(address);
        }
    }

}
