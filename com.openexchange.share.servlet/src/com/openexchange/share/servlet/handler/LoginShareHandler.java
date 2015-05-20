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
import org.apache.commons.httpclient.util.URIUtil;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.notification.FullNameBuilder;
import com.openexchange.share.servlet.ShareServletStrings;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.share.servlet.utils.ShareRedirectUtils;
import com.openexchange.user.UserService;

/**
 * {@link LoginShareHandler} - The share handler that redirects to standard login page.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class LoginShareHandler extends AbstractShareHandler {

    /**
     * Initializes a new {@link LoginShareHandler}.
     *
     * @param shareLoginConfiguration The login configuration for shares
     */
    public LoginShareHandler() {
        super();
    }

    @Override
    public int getRanking() {
        return 10;
    }

    @Override
    public ShareHandlerReply handle(GuestShare share, ShareTarget target, HttpServletRequest request, HttpServletResponse response) throws OXException {
        if (false == handles(share)) {
            // No password prompt required
            return ShareHandlerReply.NEUTRAL;
        }

        try {
            StringBuilder message = new StringBuilder();
            String messageType = null;
            String action = null;
            GuestInfo guestInfo = share.getGuest();
            User sharingUser = ShareServiceLookup.getService(UserService.class).getUser(guestInfo.getCreatedBy(), guestInfo.getContextID());
            TranslatorFactory factory = ShareServiceLookup.getService(TranslatorFactory.class);
            if (null == factory) {
                throw ServiceExceptionCode.absentService(TranslatorFactory.class);
            }
            Translator translator = factory.translatorFor(guestInfo.getLocale());
            String displayName = FullNameBuilder.buildFullName(sharingUser, translator);
            ModuleSupport moduleSupport = ShareServiceLookup.getService(ModuleSupport.class);
            TargetProxy proxy = moduleSupport.loadAsAdmin(target, guestInfo.getContextID());
            if (null != proxy) {
                String type = target.isFolder() ? translator.translate(ShareServletStrings.FOLDER) : translator.translate(ShareServletStrings.FILE);
                message.append(URIUtil.encodeQuery(String.format(translator.translate(ShareServletStrings.SHARE_WITH_TARGET), displayName, type, proxy.getTitle())));
            } else {
                message.append(URIUtil.encodeQuery(translator.translate(ShareServletStrings.SHARE_WITHOUT_TARGET)));
            }
            if (!guestInfo.isPasswordSet()) {
                User guest = ShareServiceLookup.getService(UserService.class).getUser(guestInfo.getGuestID(), guestInfo.getContextID());
                Set<String> loginsWithoutPassword = guest.getAttributes().get("guestLoginWithoutPassword");
                int loginCount = -1;
                if (null != loginsWithoutPassword && !loginsWithoutPassword.isEmpty()) {
                    try {
                        loginCount = Integer.parseInt(loginsWithoutPassword.iterator().next());
                    } catch (RuntimeException e) {
                        throw LoginExceptionCodes.UNKNOWN.create(e);
                    }
                }
                int emptyGuestPasswords = ShareServiceLookup.getService(ConfigurationService.class).getIntProperty("com.openexchange.share.emptyGuestPasswords", -1);
                if (emptyGuestPasswords < 0 || emptyGuestPasswords > loginCount) {
                    String count = (emptyGuestPasswords - loginCount) < 0 ? translator.translate(ShareServletStrings.UNLIMITED) : String.valueOf((emptyGuestPasswords - loginCount));
                    if (null != proxy) {
                        message.append(URIUtil.encodeQuery(String.format(translator.translate(ShareServletStrings.ASK_PASSWORD_WITH_TARGET), proxy.getTitle(), count)));
                    } else {
                        message.append(URIUtil.encodeQuery(String.format(translator.translate(ShareServletStrings.ASK_PASSWORD_WITHOUT_TARGET), count)));
                    }
                    messageType = "WARN";
                    action = "ask_password";
                } else {
                    if (null != proxy) {
                        message.append(URIUtil.encodeQuery(String.format(translator.translate(ShareServletStrings.REQUIRE_PASSWORD_WITH_TARGET), proxy.getTitle())));
                    } else {
                        message.append(URIUtil.encodeQuery(translator.translate(ShareServletStrings.REQUIRE_PASSWORD_WITHOUT_TARGET)));
                    }
                    messageType = "ERROR";
                    action = "require_password";
                }
            } else {
                messageType = "INFO";
                action = "login";
            }
            String redirectUrl = ShareRedirectUtils.getRedirectUrl(guestInfo, target, getShareLoginConfiguration().getLoginConfig(), message.toString(), messageType, action);

            // Do the redirect
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect(redirectUrl);

            return ShareHandlerReply.ACCEPT;
        } catch (IOException e) {
            throw ShareExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Checks if this redirecting share handler feels responsible for passed share
     *
     * @param share The associated share
     * @return <code>true</code> if share can be handled; otherwise <code>false</code>
     */
    private boolean handles(GuestShare share) {
        AuthenticationMode authentication = share.getGuest().getAuthentication();
        return null != authentication && (AuthenticationMode.ANONYMOUS_PASSWORD == authentication || AuthenticationMode.GUEST_PASSWORD == authentication);
    }
}
