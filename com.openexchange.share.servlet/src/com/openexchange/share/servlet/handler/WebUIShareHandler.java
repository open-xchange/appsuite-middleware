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

package com.openexchange.share.servlet.handler;

import java.io.IOException;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.notification.FullNameBuilder;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.servlet.ShareServletStrings;
import com.openexchange.share.servlet.auth.ShareLoginMethod;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.share.servlet.utils.LoginLocationBuilder;
import com.openexchange.share.servlet.utils.MessageType;
import com.openexchange.share.servlet.utils.ShareServletUtils;
import com.openexchange.user.UserService;

/**
 * This handler either logs in the guest user (if no password/PIN is set) and redirects him into
 * a web session or redirects him to the login page.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class WebUIShareHandler extends AbstractShareHandler {

    /**
     * Initializes a new {@link WebUIShareHandler}.
     *
     * @param shareLoginConfiguration The login configuration for shares
     */
    public WebUIShareHandler() {
        super();
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public ShareHandlerReply handle(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) throws OXException {
        GuestInfo guest = shareRequest.getGuest();
        AuthenticationMode authMode = guest.getAuthentication();
        switch (authMode) {
            case ANONYMOUS:
            case GUEST:
            {
                if (shareRequest.isInvalidTarget()) {
                    return redirectToLoginPage(shareRequest, request, response);
                }

                ShareLoginMethod shareLoginMethod = getShareLoginMethod(guest);
                if (ShareServletUtils.createSessionAndRedirect(guest, shareRequest.getTarget(), request, response, shareLoginMethod)) {
                    return ShareHandlerReply.ACCEPT;
                }

                return ShareHandlerReply.DENY;
            }
            case ANONYMOUS_PASSWORD:
            case GUEST_PASSWORD:
                return redirectToLoginPage(shareRequest, request, response);
            default:
                return ShareHandlerReply.NEUTRAL;
        }
    }

    private ShareHandlerReply redirectToLoginPage(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) throws OXException {
        try {
            GuestInfo guestInfo = shareRequest.getGuest();
            User sharingUser = ShareServiceLookup.getService(UserService.class, true).getUser(guestInfo.getCreatedBy(), guestInfo.getContextID());
            TranslatorFactory factory = ShareServiceLookup.getService(TranslatorFactory.class, true);
            Translator translator = factory.translatorFor(guestInfo.getLocale());

            ShareTargetPath targetPath = shareRequest.getTargetPath();
            if (shareRequest.isInvalidTarget()) {
                LoginLocationBuilder location;
                if (guestInfo.getAuthentication() == AuthenticationMode.GUEST) {
                    location = new LoginLocationBuilder()
                        .status("not_found_continue")
                        .share(guestInfo.getBaseToken())
                        .loginType(guestInfo.getAuthentication())
                        .target(targetPath)
                        .message(MessageType.INFO, translator.translate(ShareServletStrings.NO_ACCESS_TO_SHARE_CONTACT_OWNER_CONTINUE))
                        .loginName(guestInfo.getEmailAddress());
                } else {
                    location = new LoginLocationBuilder()
                        .status("not_found_continue")
                        .share(guestInfo.getBaseToken())
                        .loginType(guestInfo.getAuthentication())
                        .target(targetPath)
                        .message(MessageType.INFO, translator.translate(ShareServletStrings.NO_ACCESS_TO_SHARE_CONTACT_OWNER_LOG_IN));
                    if (guestInfo.getAuthentication() == AuthenticationMode.GUEST_PASSWORD) {
                        location.loginName(guestInfo.getEmailAddress());
                    }
                }

                response.sendRedirect(location.build());
                return ShareHandlerReply.ACCEPT;
            }

            String displayName = FullNameBuilder.buildFullName(sharingUser, translator);
            TargetProxy proxy = shareRequest.getTargetProxy();
            String type = targetPath.isFolder() ? translator.translate(ShareServletStrings.FOLDER) : translator.translate(ShareServletStrings.FILE);
            String message = String.format(translator.translate(ShareServletStrings.SHARE_WITH_TARGET), displayName, type, proxy.getTitle());

            LoginLocationBuilder location = new LoginLocationBuilder()
                .share(guestInfo.getBaseToken())
                .loginType(guestInfo.getAuthentication())
                .message(MessageType.INFO, message);
            if (guestInfo.getAuthentication() == AuthenticationMode.GUEST_PASSWORD) {
                location.loginName(guestInfo.getEmailAddress());
            }
            if (targetPath != null) {
                location.target(targetPath);
            }

            response.sendRedirect(location.build());
            return ShareHandlerReply.ACCEPT;
        } catch (IOException e) {
            throw ShareExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private String displayName(GuestInfo guest) throws OXException {
        ShareService service = ShareServiceLookup.getService(ShareService.class, true);
        Set<Integer> users = service.getSharingUsersFor(guest.getContextID(), guest.getGuestID());
        if (users.size() != 1) {
            return null;
        }
        User sharingUser = ShareServiceLookup.getService(UserService.class).getUser(users.iterator().next(), guest.getContextID());
        return FullNameBuilder.buildFullName(sharingUser, ShareServiceLookup.getService(TranslatorFactory.class).translatorFor(guest.getLocale()));
    }
}
