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

package com.openexchange.contacts.json.actions;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.contacts.json.RequestTools;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RestrictedAction(module = IDBasedContactAction.MODULE_NAME, type = RestrictedAction.Type.WRITE)
public class NewAction extends IDBasedContactAction {

    /**
     * Initializes a new {@link NewAction}.
     *
     * @param serviceLookup
     */
    public NewAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedContactsAccess access, ContactRequest request) throws OXException {
        boolean containsImage = request.containsImage();
        JSONObject json = request.getContactJSON(containsImage);

        String imageBase64 = null;
        {
            Object imageObject = json.opt("image1");
            if (imageObject instanceof CharSequence) {
                imageBase64 = imageObject.toString();
                if (Strings.isEmpty(imageBase64)) {
                    imageBase64 = null;
                } else {
                    json.remove("image1");
                }
            }
        }

        String folderID = json.optString("folder_id", null);
        if (null == folderID) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("folder_id");
        }

        Contact contact;
        try {
            contact = ContactMapper.getInstance().deserialize(json, ContactMapper.getInstance().getAllFields(IDBasedContactAction.VIRTUAL_FIELDS));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
        }

        if (containsImage) {
            if (!json.has("image1") || Strings.isNotEmpty(json.opt("image1").toString())) {
                RequestTools.setImageData(request, contact);
            }
        } else if (null != imageBase64) {
            try {
                final byte[] image1 = Base64.decodeBase64(imageBase64);
                if (null != image1 && image1.length > 0) {
                    final String mimeType = json.optString("image1_content_type", "image/jpeg");
                    RequestTools.setImageData(contact, image1, mimeType);
                }
            } catch (RuntimeException e) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        access.createContact(folderID, contact);
        try {
            return new AJAXRequestResult(new JSONObject(1).put("id", contact.getId()), contact.getLastModified(), "json");
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }
}
