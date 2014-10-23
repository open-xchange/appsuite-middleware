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

package com.openexchange.share.json.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.share.Share;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link NewRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class NewRequest {

    private final AJAXRequestData requestData;

    private final List<Share> targets;

    private final List<ShareRecipient> recipients;

    private final List<ShareRecipient> internalRecipients;

    private final List<ShareRecipient> externalRecipients;

    private final String message;


    private NewRequest(AJAXRequestData requestData, List<ShareRecipient> recipients, List<Share> targets, String message) {
        super();
        this.requestData = requestData;
        this.recipients = recipients;
        internalRecipients = filterRecipients(recipients, RecipientType.USER, RecipientType.GROUP);
        externalRecipients = filterRecipients(recipients, RecipientType.ANONYMOUS, RecipientType.GUEST);
        this.targets = targets;
        this.message = message;
    }

    public static NewRequest parse(AJAXRequestData requestData) throws OXException {
        try {
            JSONObject data = (JSONObject) requestData.requireData();
            List<ShareRecipient> recipients = ShareJSONParser.parseRecipients(data.getJSONArray("recipients"));
            List<Share> targets = ShareJSONParser.parseTargets(data.getJSONArray("targets"));
            String message = data.optString("message", null);
            return new NewRequest(requestData, recipients, targets, message);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    public AJAXRequestData getRequestData() {
        return requestData;
    }

    public List<ShareRecipient> getRecipients() {
        return recipients;
    }

    public List<ShareRecipient> getInternalRecipients() {
        return internalRecipients;
    }

    public List<ShareRecipient> getExternalRecipients() {
        return externalRecipients;
    }

    public List<Share> getTargets() {
        return targets;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Gets a filtered list only containing the share recipients of the specified type.
     *
     * @param recipients The recipients to filter
     * @param types The allowed type
     * @return The filtered recipients
     */
    private static List<ShareRecipient> filterRecipients(List<ShareRecipient> recipients, RecipientType...types) {
        List<ShareRecipient> filteredRecipients = new ArrayList<ShareRecipient>();
        for (ShareRecipient recipient : recipients) {
            RecipientType type = RecipientType.of(recipient);
            for (RecipientType allowedType : types) {
                if (allowedType == type) {
                    filteredRecipients.add(recipient);
                    break;
                }
            }
        }
        return filteredRecipients;
    }

    @Override
    public String toString() {
        return "NewRequest [recipients=" + recipients + ", targets=" + targets + ", message=" + message + "]";
    }

}
