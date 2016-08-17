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

package com.openexchange.contacts.json.actions;

import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.contacts.json.RequestTools;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;


/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "update", description = "Update a contact.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "Folder identifier through that the contact is accessed. This is necessary for checking the permissions."),
    @Parameter(name = "id", description = "Object ID of the updated contact."),
    @Parameter(name = "timestamp", description = "Timestamp of the updated contact. If the contact was modified after the specified timestamp, then the update must fail.")
}, requestBody = "Contact object as described in Common object data and Detailed contact data. Only modified fields are present.",
responseDescription = "Nothing, except the standard response object with empty data, the timestamp of the updated contact, and maybe errors.")
@OAuthAction(ContactActionFactory.OAUTH_WRITE_SCOPE)
public class UpdateAction extends ContactAction {

    /**
     * Initializes a new {@link UpdateAction}.
     * @param serviceLookup
     */
    public UpdateAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(final ContactRequest request) throws OXException {
        boolean containsImage = request.containsImage();
        JSONObject json = request.getContactJSON(containsImage);

        String imageBase64 = null;
        {
            Object imageObject = json.opt("image1");
            if (imageObject instanceof String) {
                imageBase64 = (String) imageObject;
                if (com.openexchange.java.Strings.isEmpty(imageBase64)) {
                    imageBase64 = null;
                } else {
                    json.remove("image1");
                }
            }
        }

        Contact contact;
		try {
			contact = ContactMapper.getInstance().deserialize(json, ContactMapper.getInstance().getAllFields(ContactAction.VIRTUAL_FIELDS));
		} catch (JSONException e) {
			throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
		}

        if (containsImage) {
            if (!json.has("image1") || !com.openexchange.java.Strings.isEmpty(json.opt("image1").toString())) {
                RequestTools.setImageData(request, contact);
            }
        } else if (null != imageBase64) {
            try {
                final byte[] image1 = Base64.decodeBase64(imageBase64);
                if (null != image1 && image1.length > 0) {
                    final String mimeType = json.optString("image1_content_type", "image/jpeg");
                    RequestTools.setImageData(contact, image1, mimeType);
                }
            } catch (final RuntimeException e) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }

        getContactService().updateContact(request.getSession(), request.getFolderID(), request.getObjectID(), contact,
        		new Date(request.getTimestamp()));
        return new AJAXRequestResult(new JSONObject(0), contact.getLastModified(), "json");
    }
}
