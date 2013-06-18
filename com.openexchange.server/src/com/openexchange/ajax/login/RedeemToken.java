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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.AJAXServlet.CONTENTTYPE_HTML;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tokenlogin.TokenLoginSecret;
import com.openexchange.tokenlogin.TokenLoginService;


/**
 * {@link RedeemToken}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class RedeemToken implements LoginRequestHandler {
    
    private final LoginConfiguration conf;

    /**
     * Initializes a new {@link RedeemToken}.
     */
    public RedeemToken(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajax.login.LoginRequestHandler#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doRedeemToken(req, resp);
        } catch (OXException e) {
            String errorPage = conf.getErrorPageTemplate().replace("ERROR_MESSAGE", e.getMessage());
            resp.setContentType(CONTENTTYPE_HTML);
            resp.getWriter().write(errorPage);
        }
    }
    
    private void doRedeemToken(HttpServletRequest req, HttpServletResponse resp) throws OXException {
        String authId = LoginTools.parseAuthId(req, true);
        String client = LoginTools.parseClient(req, true, "");
        String userAgent = LoginTools.parseUserAgent(req);
        String hash = HashCalculator.getInstance().getHash(req, userAgent, client);
        String token = LoginTools.parseToken(req);
        String appSecret = LoginTools.parseAppSecret(req);
        TokenLoginService service = ServerServiceRegistry.getInstance().getService(TokenLoginService.class);
        Session session = service.redeemToken(token, appSecret, client, authId, hash);
        TokenLoginSecret tokenLoginSecret = service.getTokenLoginSecret(appSecret);
        boolean writePassword = (Boolean) tokenLoginSecret.getParameters().get("accessPassword");
        if (!writePassword) {
            
        }
        
    }

}
