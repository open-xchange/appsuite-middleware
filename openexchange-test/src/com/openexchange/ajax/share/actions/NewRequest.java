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

package com.openexchange.ajax.share.actions;

import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.groupware.modules.Module;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link NewRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class NewRequest implements AJAXRequest<NewResponse> {

    private final boolean failOnError;

    private final List<ShareTarget> targets;

    private final List<ShareRecipient> recipients;

    public NewRequest(List<ShareTarget> targets, List<ShareRecipient> recipients) {
        this(targets, recipients, true);
    }

    public NewRequest(List<ShareTarget> targets, List<ShareRecipient> recipients, boolean failOnError) {
        super();
        this.targets = targets;
        this.recipients = recipients;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public String getServletPath() {
        return "/ajax/share/management";
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_ACTION, "new") };
    }

    @Override
    public AbstractAJAXParser<? extends NewResponse> getParser() {
        return new AbstractAJAXParser<NewResponse>(failOnError) {
            @Override
            protected NewResponse createResponse(Response response) throws JSONException {
                return new NewResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONArray jTargets = new JSONArray();
        for (ShareTarget target : targets) {
            jTargets.put(writeTarget(target));
        }

        JSONArray jRecipients = new JSONArray();
        for (ShareRecipient recipient : recipients) {
            jRecipients.put(writeRecipient(recipient));
        }

        JSONObject json = new JSONObject();
        json.put("targets", jTargets);
        json.put("recipients", jRecipients);
        return json;
    }

    private JSONObject writeRecipient(ShareRecipient recipient) throws JSONException {
        JSONObject jRecipient = new JSONObject(5);
        RecipientType type = recipient.getType();
        jRecipient.put("type", type.name().toLowerCase());
        jRecipient.put("bits", recipient.getBits());
        jRecipient.put("activation_date", recipient.getActivationDate() == null ? null : recipient.getActivationDate().getTime());
        jRecipient.put("expiry_date", recipient.getExpiryDate() == null ? null : recipient.getExpiryDate().getTime());
        switch (type) {
            case USER:
            case GROUP:
                writeInternalRecipient((InternalRecipient) recipient, jRecipient);
                break;
            case ANONYMOUS:
                writeAnonymousRecipient((AnonymousRecipient) recipient, jRecipient);
                break;
            case GUEST:
                writeGuestRecipient((GuestRecipient) recipient, jRecipient);
                break;
        }

        return jRecipient;
    }

    private void writeGuestRecipient(GuestRecipient recipient, JSONObject jRecipient) throws JSONException {
        jRecipient.put("email_address", recipient.getEmailAddress());
        jRecipient.put("password", recipient.getPassword());
        jRecipient.put("display_name", recipient.getDisplayName());
        jRecipient.put("contact_id", recipient.getContactID());
        jRecipient.put("contact_folder", recipient.getContactFolder());
    }

    private void writeAnonymousRecipient(AnonymousRecipient recipient, JSONObject jRecipient) throws JSONException {
        jRecipient.put("password", recipient.getPassword());
    }

    private void writeInternalRecipient(InternalRecipient recipient, JSONObject jRecipient) throws JSONException {
        jRecipient.put("id", recipient.getEntity());
    }

    private JSONObject writeTarget(ShareTarget target) throws JSONException {
        JSONObject jTarget = new JSONObject(3);
        jTarget.put("module", Module.getModuleString(target.getModule(), -1));
        jTarget.put("folder", target.getFolder());
        jTarget.put("item", target.getItem());
        return jTarget;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

}
