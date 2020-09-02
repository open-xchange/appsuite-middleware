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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.xctx.subscription;

import static com.openexchange.share.AuthenticationMode.ANONYMOUS;
import static com.openexchange.share.AuthenticationMode.ANONYMOUS_PASSWORD;
import static com.openexchange.share.AuthenticationMode.GUEST;
import static com.openexchange.share.AuthenticationMode.GUEST_PASSWORD;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE;
import static com.openexchange.share.subscription.ShareLinkState.ADDABLE_WITH_PASSWORD;
import static com.openexchange.share.subscription.ShareLinkState.INACCESSIBLE;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.xctx.XctxFileStorageService;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.subscription.AbstractFileStorageSubscriptionProvider;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult;
import com.openexchange.share.subscription.ShareLinkState;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link XctxShareSubscriptionProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class XctxShareSubscriptionProvider extends AbstractFileStorageSubscriptionProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link XctxShareSubscriptionProvider}.
     * 
     * @param services The services
     * @param fileStorageService The filestorage to operate with
     */
    public XctxShareSubscriptionProvider(ServiceLookup services, XctxFileStorageService fileStorageService) {
        super(fileStorageService, services.getService(UserPermissionService.class));
        this.services = services;
    }

    @Override
    public boolean isSupported(Session session, String shareLink) {
        if (false == ShareTool.isShare(shareLink)) {
            return false;
        }
        ShareTargetPath targetPath = ShareTool.getShareTarget(shareLink);
        if (Module.INFOSTORE.getFolderConstant() != targetPath.getModule()) {
            return false;
        }
        String baseToken = ShareTool.extractBaseToken(shareLink);
        if (Strings.isEmpty(baseToken)) {
            return false;
        }
        try {
            GuestInfo guestInfo = services.getServiceSafe(ShareService.class).resolveGuest(baseToken);
            if (null == guestInfo) {
                return false;
            }
            /*
             * Skip checking if context or user are enabled. This provider is responsible for handling the link
             */
            return hasCapability(session);
        } catch (OXException e) {
            logExcpetionError(e);
        }
        return false;
    }

    @Override
    public int getRanking() {
        return 60;
    }

    @Override
    public ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException {
        requireAccess(session);
        FileStorageAccountAccess accountAccess = getStorageAccountAccess(session, shareLink);
        if (null != accountAccess) {
            ShareLinkState state = checkAccessible(accountAccess, shareLink);
            return new ShareLinkAnalyzeResult(state, generateInfos(accountAccess, shareLink));
        }
        /*
         * Try to resolve the token to a guest and if found announce that it can be added
         */
        GuestInfo guestInfo = services.getServiceSafe(ShareService.class).resolveGuest(ShareLinks.extractBaseToken(shareLink));
        ShareLinkState state = INACCESSIBLE;
        if (null != guestInfo && null != guestInfo.getAuthentication()) {
            AuthenticationMode mode = guestInfo.getAuthentication();
            if (GUEST_PASSWORD.equals(mode) || ANONYMOUS_PASSWORD.equals(mode)) {
                state = ADDABLE_WITH_PASSWORD;
            } else if (GUEST.equals(mode) || ANONYMOUS.equals(mode)) {
                state = ADDABLE;
            }
        }
        return new ShareLinkAnalyzeResult(state, new ShareSubscriptionInformation(getId(), null, String.valueOf(Module.INFOSTORE.getFolderConstant()), null));
    }

    @Override
    public boolean isPasswordMissing(OXException e) {
        if (LoginExceptionCodes.prefix().equals(e.getPrefix())) {
            if (LoginExceptionCodes.INVALID_CREDENTIALS.getNumber() == e.getCode() || LoginExceptionCodes.INVALID_GUEST_PASSWORD.getNumber() == e.getCode()) {
                return true;
            }
        }
        return false;
    }
}
