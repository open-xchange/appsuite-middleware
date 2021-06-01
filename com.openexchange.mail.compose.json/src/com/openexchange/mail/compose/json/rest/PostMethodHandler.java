/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.compose.json.rest;

import java.io.IOException;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link PostMethodHandler} - Serves the REST-like <code>POST</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PostMethodHandler extends AbstractMailComposeMethodHandler {

    private static final Set<String> ACTIONS_WITH_LENGTH_2 = ImmutableSet.of("addAttachment", "send", "save");
    private static final Set<String> ACTIONS_WITH_LENGTH_3 = ImmutableSet.of("addOriginalAttachments", "addVCardAttachment");

    /**
     * Initializes a new {@link PostMethodHandler}.
     */
    public PostMethodHandler() {
        super();
    }

    // POST /mail/compose

    @Override
    protected void modifyByPathInfo(AJAXRequestData requestData, String[] restPathElements, HttpServletRequest req) throws IOException, OXException {
        String action = getAction(restPathElements, req);
        if (action == null) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        requestData.setAction(action);

        if ("open".equals(action)) {
            return;
        }

        if (ACTIONS_WITH_LENGTH_2.contains(action)) {
            requestData.putParameter("id", restPathElements[0]);
        } else if (ACTIONS_WITH_LENGTH_3.contains(action)) {
            if (!"attachments".equals(restPathElements[1])) {
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(req.getPathInfo());
            }

            requestData.putParameter("id", restPathElements[0]);
        } else if ("replaceAttachment".equals(action)) {
            /*-
             * Assume an attachment identifier
             * POST /mail/compose/123/attachments/456
             */
            if (!"attachments".equals(restPathElements[1])) {
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(req.getPathInfo());
            }

            requestData.putParameter("id", restPathElements[0]);
            String actionId = restPathElements[2];
            requestData.putParameter("attachmentId", actionId);
        } else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(req.getPathInfo());
        }
    }

    @Override
    protected String doGetAction(String[] restPathElements, HttpServletRequest restRequest) {
        if (hasNoPathInfo(restPathElements)) {
            // Create a new composition space.
            return "open";
        }

        int length = restPathElements.length;
        if (0 == length) {
            // Create a new composition space.
            return "open";
        }

        if (2 == length) {
            String actionId = restPathElements[1];
            if (actionId != null) {
                switch (actionId) {
                    case "attachments":
                        /*-
                         *  POST /mail/compose/123/attachments
                         */
                        return "addAttachment";
                    case "send":
                        /*-
                         *  POST /mail/compose/123/send
                         */
                        return "send";
                    case "save":
                        /*-
                         *  POST /mail/compose/123/save
                         */
                        return "save";
                }
            }
        } else if (3 == length) {
            String actionId = restPathElements[2];
            switch (actionId) {
                case "original":
                    /*-
                     *  POST /mail/compose/123/attachments/original
                     */
                    return "addOriginalAttachments";
                case "vcard":
                    /*-
                     *  POST /mail/compose/123/attachments/vcard
                     */
                    return "addVCardAttachment";
                default:
                    /*-
                     * Assume an attachment identifier
                     * POST /mail/compose/123/attachments/456
                     */
                    return "replaceAttachment";
            }
        }
        return null;
    }

}
