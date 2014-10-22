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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.servlet.handler.ShareHandler;
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
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Create a new HttpSession if it is missing
            request.getSession(true);

            // Extract share from path info
            Share share;
            ShareTarget target;
            {
                String pathInfo = request.getPathInfo();
                String[] paths = Strings.isEmpty(pathInfo) ? null : pathInfo.split("/");
                share = null == paths || 0 == paths.length ? null :
                    ShareServiceLookup.getService(ShareService.class, true).resolveToken(paths[0]);
                if (null == share) {
                    LOG.debug("No share found at '{}'", pathInfo);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                LOG.debug("Successfully resolved token at '{}' to {}", pathInfo, share);
                target = 1 < paths.length ? share.resolveTarget(paths[1]) : null;
            }

            // Determine appropriate ShareHandler and handle the share
            for (ShareHandler handler : shareHandlerRegistry.getServiceList()) {
                if (handler.handle(share, target, request, response)) {
                    return;
                }
            }

            // No appropriate ShareHandler available
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("No share handler found");
        } catch (RateLimitedException e) {
            response.setContentType("text/plain; charset=UTF-8");
            if(e.getRetryAfter() > 0) {
                response.setHeader("Retry-After", String.valueOf(e.getRetryAfter()));
            }
            response.sendError(429, "Too Many Requests - Your request is being rate limited.");
        } catch (OXException e) {
            LOG.error("Error processing share '{}': {}", request.getPathInfo(), e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
