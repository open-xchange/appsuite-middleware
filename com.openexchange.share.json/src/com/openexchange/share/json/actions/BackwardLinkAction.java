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

package com.openexchange.share.json.actions;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.subscription.ShareSubscriptionRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BackwardLinkAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class BackwardLinkAction implements AJAXActionService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link BackwardLinkAction}.
     * 
     * @param services The service lookup
     */
    public BackwardLinkAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        boolean redirect = AJAXRequestDataTools.parseBoolParameter(request.getParameter("redirect"));
        try {
            /*
             * parse parameters & generate backward link
             */
            String link = request.checkParameter(AbstractShareSubscriptionAction.LINK);
            String folder = request.checkParameter("folder");
            String item = request.getParameter("item");
            String backwardLink = services.getServiceSafe(ShareSubscriptionRegistry.class).getBackwardLink(session, link, folder, item, null);
            /*
             * send redirect if requested & possible, otherwise return as api response
             */
            if (redirect) {
                HttpServletResponse response = request.optHttpServletResponse();
                try {
                    response.sendRedirect(backwardLink);
                    return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(ResultType.DIRECT);
                } catch (IOException e) {
                    throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
            return new AJAXRequestResult(new JSONObject().putSafe("link", backwardLink), "json");
        } catch (OXException e) {
            if (redirect) {
                HttpServletResponse response = request.optHttpServletResponse();
                if (null != response) {
                    try {
                        Tools.sendErrorPage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                        return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                    } catch (IOException i) {
                        throw AjaxExceptionCodes.IO_ERROR.create(i, i.getMessage());
                    }
                }
            }
            throw e;
        }
    }

}
