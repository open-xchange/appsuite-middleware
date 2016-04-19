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

package com.openexchange.share.servlet.internal;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.servlet.ShareServletStrings;
import com.openexchange.share.servlet.handler.AccessShareRequest;
import com.openexchange.share.servlet.handler.ShareHandler;
import com.openexchange.share.servlet.handler.ShareHandlerReply;
import com.openexchange.share.servlet.utils.LoginLocation;
import com.openexchange.share.servlet.utils.LoginLocationRegistry;
import com.openexchange.share.servlet.utils.LoginType;
import com.openexchange.share.servlet.utils.MessageType;
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
public class ShareServlet extends AbstractShareServlet {

    private static final long serialVersionUID = -598653369873570676L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareServlet.class);

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
        try {
            TranslatorFactory translatorFactory = ShareServiceLookup.getService(TranslatorFactory.class, true);
            translator = translatorFactory.translatorFor(determineLocale(request, null));

            request.getSession(true);

            // Extract share from path info
            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                sendNotFound(response, translator);
                return;
            }

            List<String> paths = ShareServletUtils.splitPath(pathInfo);
            if (paths.isEmpty()) {
                LOG.debug("No share found at '{}'", pathInfo);
                sendNotFound(response, translator);
                return;
            }

            GuestInfo guest = ShareServiceLookup.getService(ShareService.class, true).resolveGuest(paths.get(0));
            if (guest == null) {
                LOG.debug("No guest with token '{}' found at '{}'", paths.get(0), pathInfo);
                sendNotFound(response, translator);
                return;
            }

            LOG.debug("Successfully resolved guest at '{}' to {}", pathInfo, guest);

            // Switch language for errors if appropriate
            translator = translatorFactory.translatorFor(determineLocale(request, guest));

            TargetProxy targetProxy = null;
            ShareTargetPath targetPath = null;
            boolean invalidTarget = false;
            int contextId = guest.getContextID();
            int guestId = guest.getGuestID();
            if (paths.size() > 1) {
                ModuleSupport moduleSupport = ShareServiceLookup.getService(ModuleSupport.class, true);
                targetPath = ShareTargetPath.parse(paths.subList(1, paths.size()));
                if (targetPath == null) {
                    invalidTarget = true;
                } else {
                    int m = targetPath.getModule();
                    String f = targetPath.getFolder();
                    String i = targetPath.getItem();
                    if (moduleSupport.exists(m, f, i, contextId, guestId) && moduleSupport.isVisible(m, f, i, contextId, guestId)) {
                        targetProxy = moduleSupport.resolveTarget(targetPath, contextId, guestId);
                    } else {
                        invalidTarget = true;
                    }
                }

                if (invalidTarget) {
                    List<TargetProxy> otherTargets = moduleSupport.listTargets(contextId, guestId);
                    if (otherTargets.isEmpty()) {
                        sendNotFound(response, translator);
                        return;
                    }

                    targetProxy = applyFallbackPathMatching(otherTargets, paths.get(1));
                    if (targetProxy == null) {
                        targetProxy = otherTargets.iterator().next();
                    } else {
                        invalidTarget = false;
                    }
                    targetPath = targetProxy.getTargetPath();
                }
            }

            /*
             * Determine appropriate ShareHandler and handle the share
             */
            if (false == handle(new AccessShareRequest(guest, targetPath, targetProxy, invalidTarget), request, response)) {
                // No appropriate ShareHandler available
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("No share handler found");
            }
        } catch (RateLimitedException e) {
            e.send(response);
        } catch (OXException e) {
            if (ShareExceptionCodes.INVALID_TOKEN.equals(e) || ShareExceptionCodes.UNKNOWN_SHARE.equals(e)) {
                sendNotFound(response, translator);
            } else {
                LOG.error("Error processing share '{}': {}", request.getPathInfo(), e.getMessage(), e);
                LoginLocation location = new LoginLocation()
                    .loginType(LoginType.MESSAGE)
                    .message(MessageType.ERROR, translator.translate(OXExceptionStrings.MESSAGE_RETRY));
                LoginLocationRegistry.getInstance().putAndRedirect(location, response);
            }
        }
    }

    /**
     * Passes the resolved share to the most appropriate handler and lets him serve the request.
     *
     * @param shareRequest The share request
     * isn't existing or accessible.
     * @param request The associated HTTP request
     * @param response The associated HTTP response
     * @return <code>true</code> if the share request was handled, <code>false</code>, otherwise
     */
    private boolean handle(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) throws OXException {
        for (ShareHandler handler : shareHandlerRegistry.getServiceList()) {
            ShareHandlerReply reply = handler.handle(shareRequest, request, response);
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
    private static void sendNotFound(HttpServletResponse response, Translator translator) throws IOException {
        LoginLocation location = new LoginLocation()
            .loginType(LoginType.MESSAGE)
            .message(MessageType.ERROR, translator.translate(ShareServletStrings.SHARE_NOT_FOUND));
        LoginLocationRegistry.getInstance().putAndRedirect(location, response);
        return;
    }

    private TargetProxy applyFallbackPathMatching(List<TargetProxy> targets, String path) {
        final int prime = 31;
        for (TargetProxy proxy : targets) {
            ShareTarget target = proxy.getTarget();
            int hashCode = 1;
            hashCode = prime * hashCode + ((target.getFolder() == null) ? 0 : target.getFolder().hashCode());
            hashCode = prime * hashCode + ((target.getItem() == null) ? 0 : target.getItem().hashCode());
            hashCode = prime * hashCode + target.getModule();
            if (String.format("%08x", Integer.valueOf(hashCode)).equals(path)) {
                return proxy;
            }
        }

        return null;
    }

}
