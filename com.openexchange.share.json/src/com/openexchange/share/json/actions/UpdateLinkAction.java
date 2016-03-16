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

package com.openexchange.share.json.actions;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.LinkUpdate;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareTarget;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateLinkAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class UpdateLinkAction extends AbstractShareAction {

    /**
     * Initializes a new {@link UpdateLinkAction}.
     *
     * @param services The service lookup
     */
    public UpdateLinkAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        /*
         * parse parameters & target
         */
        Date clientTimestamp = new Date(requestData.getParameter("timestamp", Long.class).longValue());
        JSONObject json = (JSONObject) requestData.requireData();
        ShareTarget target = getParser().parseTarget(json);
        /*
         * update share based on present data in update request
         */
        ShareLink shareLink;
        try {
            LinkUpdate linkUpdate = new LinkUpdate();
            if (json.has("expiry_date")) {
                Date newExpiry = json.isNull("expiry_date") ? null : new Date(getParser().removeTimeZoneOffset(json.getLong("expiry_date"), getTimeZone(requestData, session)));
                linkUpdate.setExpiryDate(newExpiry);
            }
            if (json.has("password")) {
                String newPassword = json.isNull("password") ? null : json.getString("password");
                linkUpdate.setPassword(newPassword);
            }
            shareLink = getShareService().updateLink(session, target, linkUpdate, clientTimestamp);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
        /*
         * return empty result in case of success
         */
        return new AJAXRequestResult(new JSONObject(), shareLink.getTimestamp(), "json");
    }

}
