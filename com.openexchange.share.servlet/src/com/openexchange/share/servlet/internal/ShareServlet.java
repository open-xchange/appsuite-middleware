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

package com.openexchange.share.servlet.internal;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.servlet.ShareServletStrings;
import com.openexchange.share.servlet.handler.ShareHandler;
import com.openexchange.share.servlet.handler.ShareHandlerReply;
import com.openexchange.share.servlet.utils.MessageType;
import com.openexchange.share.servlet.utils.LoginLocationBuilder;
import com.openexchange.share.servlet.utils.ShareServletUtils;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;

/**
 * {@link ShareServlet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ShareServlet extends HttpServlet {

    private static final long serialVersionUID = -598653369873570676L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareServlet.class);

    // --------------------------------------------------------------------------------------------------------------------------------- //

    private final RankingAwareNearRegistryServiceTracker<ShareHandler> shareHandlerRegistry;

    /**
     * Initializes a new {@link ShareServlet}.
     *
     * @param shareLoginConfig The share login configuration to use
     * @param shareHandlerRegistry The handler registry
     */
    public ShareServlet(RankingAwareNearRegistryServiceTracker<ShareHandler> shareHandlerRegistry) {
        super();
        this.shareHandlerRegistry = shareHandlerRegistry;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Tools.disableCaching(response);
        Translator translator = Translator.EMPTY;
        GuestShare share = null;
        try {
            translator = ShareServiceLookup.getService(TranslatorFactory.class, true).translatorFor(request.getLocale());

            request.getSession(true);

            // Extract share from path info
            ShareTarget target;
            {
                String pathInfo = request.getPathInfo();
                String[] paths = ShareServletUtils.splitPath(pathInfo);
                if (paths == null || paths.length == 0) {
                    LOG.debug("No share found at '{}'", pathInfo);
                    sendNotFound(response, translator);
                    return;
                }
                share = ShareServiceLookup.getService(ShareService.class, true).resolveToken(paths[0]);
                if (null == share) {
                    LOG.debug("No share with token '{}' found at '{}'", paths[0], pathInfo);
                    sendNotFound(response, translator);
                    return;
                }

                LOG.debug("Successfully resolved token at '{}' to {}", pathInfo, share);
                if (1 < paths.length) {
                    target = share.resolveTarget(paths[1]);
                    if (null == target) {
                        //TODO: fallback to share without target?
                        LOG.debug("Share target '{}' not found in share '{}' at '{}'", paths[1], paths[0], pathInfo);
                        sendNotFound(response, translator);
                        return;
                    }
                } else {
                    target = null;
                }
            }

            /*
             * Determine appropriate ShareHandler and handle the share
             */
            if (false == handle(share, target, request, response)) {
                // No appropriate ShareHandler available
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("No share handler found");
            }
        } catch (RateLimitedException e) {
            e.send(response);
        } catch (OXException e) {
            LOG.error("Error processing share '{}': {}", request.getPathInfo(), e.getMessage(), e);
            LoginLocationBuilder location = new LoginLocationBuilder().message(MessageType.ERROR, translator.translate(OXExceptionStrings.MESSAGE_RETRY), "internal_error");
            if (share != null) {
                AuthenticationMode authMode = share.getGuest().getAuthentication();
                if (authMode == AuthenticationMode.ANONYMOUS_PASSWORD || authMode == AuthenticationMode.GUEST_PASSWORD) {
                    location.loginType(authMode);
                }
            }
            response.sendRedirect(location.build());
        }
    }

    /**
     * Passes the resolved share to the most appropriate handler and lets him serve the request.
     *
     * @param share The guest share
     * @param target The share target within the share, or <code>null</code> if not addressed
     * @param request The associated HTTP request
     * @param response The associated HTTP response
     * @return <code>true</code> if the share request was handled, <code>false</code>, otherwise
     */
    private boolean handle(GuestShare share, ShareTarget target, HttpServletRequest request, HttpServletResponse response) throws OXException {
        for (ShareHandler handler : shareHandlerRegistry.getServiceList()) {
            ShareHandlerReply reply = handler.handle(share, target, request, response);
            if (ShareHandlerReply.NEUTRAL != reply) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sends a redirect with an appropriate error message for a not found share.
     *
     * @param response The HTTP servlet response to redirect
     * @param translator The translator
     */
    private static void sendNotFound(HttpServletResponse response, Translator translator) throws IOException, OXException {
        String redirectUrl = new LoginLocationBuilder()
            .message(MessageType.ERROR, translator.translate(ShareServletStrings.SHARE_NOT_FOUND), "not_found")
            .build();
        response.sendRedirect(redirectUrl);
        return;
    }

}
