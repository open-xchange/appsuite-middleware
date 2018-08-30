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

package com.openexchange.halo.xing.picture;

import static com.openexchange.java.Autoboxing.I;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.UnmodifiableContactPictureRequestData;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.finder.FinderUtil;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.user.UserService;
import com.openexchange.xing.PhotoUrls;
import com.openexchange.xing.User;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.access.XingOAuthAccessProvider;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.session.WebAuthSession;

/**
 * {@link XingContactPictureFinder}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a> Logic from "com.openexchange.halo.xing.XingUserDataSource"
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> MW-926
 * @since v7.10.1
 */
public class XingContactPictureFinder implements ContactPictureFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(XingContactPictureFinder.class);

    private final XingOAuthAccessProvider provider;

    private final ServiceLookup services;

    /**
     * Initializes a new {@link XingContactPictureFinder}.
     *
     * @param services The {@link ServiceLookup}
     * @param provider The {@link XingOAuthAccessProvider}
     */
    public XingContactPictureFinder(ServiceLookup services, final XingOAuthAccessProvider provider) {
        this.provider = provider;
        this.services = services;
    }

    @Override
    public ContactPicture getPicture(Session session, UnmodifiableContactPictureRequestData original, ContactPictureRequestData modified, boolean onlyETag) throws OXException {
        XingOAuthAccess access = provider.accessFor(provider.getXingOAuthAccount(session), session);
        XingAPI<WebAuthSession> xingAPI = access.getXingAPI();

        try {
            List<String> userIds = xingAPI.findByEmails(new LinkedList<>(original.getEmails()));
            if (null == userIds || userIds.isEmpty()) {
                return null;
            }

            try {
                // XING only offers one mail address. This must not be a mail address we know about. So we use the first result, trusting XING to deliver best result first
                User user = xingAPI.userInfo(userIds.get(0));
                ContactPicture picture = getPictuerFromXing(xingAPI, user, onlyETag);
                if (null != picture && picture.containsContactPicture() && (onlyETag || FinderUtil.checkImage(picture.getFileHolder(), I(session.getContextId()), modified))) {
                    return picture;
                }
            } catch (XingException e) {
                LOGGER.debug("Unable to find user for identifier {}.", userIds.get(0), e);
            }
        } catch (XingException e) {
            LOGGER.debug("Unable to retrive contact picutre.", e);

        }
        return null;
    }

    @SuppressWarnings("resource")
    private ContactPicture getPictuerFromXing(XingAPI<WebAuthSession> xingAPI, User user, boolean onlyETag) {
        PhotoUrls photoUrls = user.getPhotoUrls();
        if (photoUrls == null) {
            return null;
        }

        String url = photoUrls.getMaxiThumbUrl();
        if (url == null) {
            url = photoUrls.getLargestAvailableUrl();
        }

        if (url != null) {
            IFileHolder photo = null;
            if (false == onlyETag) {
                try {
                    photo = xingAPI.getPhoto(url);
                    if (photo == null) {
                        return null;
                    }
                } catch (XingException e) {
                    LOGGER.error("Could not load photo '{}' from XING.", url, e);
                }
            }
            return new ContactPicture(Base64.encode(url), photo);
        }
        return null;
    }

    @Override
    public boolean isApplicable(Session session, ContactPictureRequestData original, ContactPictureRequestData modified) {
        if (null == session || modified.isEmpty() || false == original.hasEmail() || false == isEnabledForUser(session)) {
            return false;
        }
        ;
        try {
            provider.getXingOAuthAccount(session);
        } catch (OXException e) {
            if (OAuthExceptionCodes.ACCOUNT_NOT_FOUND.equals(e)) {
                LOGGER.error("Xing account does not exists.", e);
            }
            LOGGER.debug("Unable verify that XING accoutn exists.", e);
            return false;
        }

        return true;
    }

    private boolean isEnabledForUser(Session session) {
        try {
            com.openexchange.groupware.ldap.User user = services.getServiceSafe(UserService.class).getUser(session.getUserId(), session.getContextId());
            if (user.isGuest()) {
                return false;
            }
            ConfigViewFactory viewFactory = services.getServiceSafe(ConfigViewFactory.class);
            if (null == viewFactory) {
                throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
            }

            ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
            return ConfigViews.getDefinedBoolPropertyFrom(KnownApi.XING.getFullName(), true, view);
        } catch (Exception e) {
            LOGGER.warn("Unale to verify if XING is enabeld.", e);
        }
        return false;
    }

}
