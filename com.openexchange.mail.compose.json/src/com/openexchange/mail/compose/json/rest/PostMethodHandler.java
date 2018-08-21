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

package com.openexchange.mail.compose.json.rest;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link PostMethodHandler} - Serves the REST-like <code>POST</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PostMethodHandler extends AbstractMailComposeMethodHandler {

    /**
     * Initializes a new {@link PostMethodHandler}.
     */
    public PostMethodHandler() {
        super();
    }

    // POST /mail/compose

    @Override
    protected void modifyByPathInfo(AJAXRequestData requestData, String[] restPathElements, HttpServletRequest req) throws IOException, OXException {
        if (hasNoPathInfo(restPathElements)) {
            // Create a new composition space.
            requestData.setAction("open");
            return;
        }

        int length = restPathElements.length;
        if (0 == length) {
            // Create a new composition space.
            requestData.setAction("open");
            return;
        }

        if (2 == length) {
            requestData.putParameter("id", restPathElements[0]);

            String actionId = restPathElements[1];
            if (actionId == null) {
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(req.getPathInfo());
            }
            switch (actionId) {
                case "attachments":
                    /*-
                     *  POST /mail/compose/123/attachments
                     */
                    requestData.setAction("addAttachment");
                    break;
                case "send":
                    /*-
                     *  POST /mail/compose/123/send
                     */
                    requestData.setAction("send");
                    break;
                case "save":
                    /*-
                     *  POST /mail/compose/123/save
                     */
                    requestData.setAction("save");
                    break;
                default:
                    throw AjaxExceptionCodes.UNKNOWN_ACTION.create(req.getPathInfo());
            }
        } else if (3 == length) {
            if (!"attachments".equals(restPathElements[1])) {
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(req.getPathInfo());
            }

            requestData.putParameter("id", restPathElements[0]);

            String actionId = restPathElements[2];
            switch (actionId) {
                case "original":
                    /*-
                     *  POST /mail/compose/123/attachments/original
                     */
                    requestData.setAction("addOriginalAttachments");
                    break;
                case "vcard":
                    /*-
                     *  POST /mail/compose/123/attachments/vcard
                     */
                    requestData.setAction("addVCardAttachment");
                    break;
                default:
                    /*-
                     * Assume an attachment identifier
                     * POST /mail/compose/123/attachments/456
                     */
                    requestData.putParameter("attachmentId", actionId);
                    requestData.setAction("replaceAttachment");
                    break;
            }
        } else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(req.getPathInfo());
        }
    }

}
