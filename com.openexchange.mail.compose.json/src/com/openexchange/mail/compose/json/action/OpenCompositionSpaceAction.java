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

package com.openexchange.mail.compose.json.action;

import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.Message.ContentType;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.Type;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link OpenCompositionSpaceAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class OpenCompositionSpaceAction extends AbstractMailComposeAction {

    /**
     * Initializes a new {@link OpenCompositionSpaceAction}.
     *
     * @param services The service look-up
     */
    public OpenCompositionSpaceAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        // Acquire needed service
        CompositionSpaceService compositionSpaceService = getCompositionSpaceService();

        // Determine type
        Type type = Type.NEW;
        {
            String sType = requestData.getParameter("type");
            if (Strings.isNotEmpty(sType)) {
                type = Type.typeFor(sType.trim());
                if (null == type) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", type);
                }
            }
        }

        UserSettingMail usm = session.getUserSettingMail();
        usm.setNoSave(true);

        // Build parameters
        OpenCompositionSpaceParameters.Builder parameters;
        switch (type) {
            case COPY:
                {
                    MailPath copyFor = requireReferencedMail(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForCopy(copyFor, usm);
                }
                break;
            case EDIT:
                {
                    MailPath editFor = requireReferencedMail(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForEdit(editFor, usm);
                }
                break;
            case FORWARD:
                {
                    List<MailPath> forwardsFor = requireReferencedMails(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForForward(forwardsFor, usm);
                }
                break;
            case NEW:
                parameters = OpenCompositionSpaceParameters.builderForNew(usm);
                break;
            case REPLY:
                // fall-through
            case REPLY_ALL:
                {
                    MailPath replyFor = requireReferencedMail(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForReply(type == Type.REPLY_ALL, replyFor, usm);
                }
                break;
            case RESEND:
                {
                    MailPath resendtFor = requireReferencedMail(requestData);
                    parameters = OpenCompositionSpaceParameters.builderForResend(resendtFor, usm);
                }
                break;
            default:
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", type.getId());
        }

        if (AJAXRequestDataTools.parseBoolParameter("vcard", requestData)) {
            parameters.withAppendVCard(true);
        }

        if (AJAXRequestDataTools.parseBoolParameter("originalAttachments", requestData)) {
            parameters.withAppendOriginalAttachments(true);
        }

        if (AJAXRequestDataTools.parseBoolParameter("requestReadReceipt", requestData)) {
            parameters.withRequestReadReceipt(true);
        }

        // Check for priority
        {
            String priority = requestData.getParameter("priority");
            if (Strings.isNotEmpty(priority)) {
                Priority p = Priority.priorityFor(priority);
                if (null != p) {
                    parameters.withPriority(p);
                }
            }
        }

        // Check for Content-Type
        {
            String contentType = requestData.getParameter("contentType");
            if (Strings.isNotEmpty(contentType)) {
                ContentType ct = ContentType.contentTypeFor(contentType);
                if (null != ct) {
                    parameters.withContentType(ct);
                }
            } else if (Type.NEW == type) {
                ContentType ct = usm.isDisplayHtmlInlineContent() ? ContentType.TEXT_HTML : ContentType.TEXT_PLAIN;
                parameters.withContentType(ct);
            }
        }

        // Does not hold since a JSON array is expected for the mail paths...
        //        JSONObject jMessage = (JSONObject) requestData.getData();
        //        if (null != jMessage) {
        //            // Security settings
        //            {
        //                JSONObject jSecurity = jMessage.optJSONObject("security");
        //                if (null != jSecurity) {
        //                    parameters.withSecurity(toSecurity(jSecurity));
        //                }
        //            }
        //
        //            // Shared attachments information
        //            {
        //                JSONObject jSharedAttachments = jMessage.optJSONObject("sharedAttachments");
        //                if (null != jSharedAttachments) {
        //                    parameters.withSharedAttachmentsInfo(toSharedAttachmentsInfo(jSharedAttachments));
        //                }
        //            }
        //        }

        CompositionSpace compositionSpace = compositionSpaceService.openCompositionSpace(parameters.build(), session);
        return new AJAXRequestResult(compositionSpace, "compositionSpace");
    }

}
