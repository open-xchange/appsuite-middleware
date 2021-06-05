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

package com.openexchange.ajax.contact.action;

import java.io.ByteArrayInputStream;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.container.Contact;

/**
 * Stores the parameters for inserting the contact.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class InsertRequest extends AbstractContactRequest<InsertResponse> {

    /**
     * Contact to insert.
     */
    Contact contactObj;

    JSONObject jsonObj;

    final boolean withImage;

    String fieldContent;

    final int folderID;

    /**
     * Should the parser fail on error in server response.
     */
    final boolean failOnError;

    /**
     * Default constructor.
     * 
     * @param contactObj contact to insert.
     */
    public InsertRequest(final Contact contactObj) {
        this(contactObj, true);
    }

    /**
     * More detailed constructor.
     * 
     * @param contactObj contact to insert.
     * @param failOnError <code>true</code> to check the response for error
     *            messages.
     */
    public InsertRequest(final Contact contactObj, final boolean failOnError) {
        super();
        this.contactObj = contactObj;
        this.folderID = contactObj.getParentFolderID();
        this.jsonObj = null;
        this.failOnError = failOnError;
        this.withImage = contactObj.containsImage1() && (null != contactObj.getImage1());

        if (withImage) {
            try {
                fieldContent = convert(contactObj).toString();
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public InsertRequest(final String json) throws JSONException {
        super();
        this.contactObj = null;
        this.withImage = false;
        this.jsonObj = new JSONObject(json);
        this.folderID = jsonObj.getInt("folder_id");
        this.failOnError = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        if (jsonObj != null) {
            return jsonObj;
        }
        return convert(contactObj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return withImage ? Method.UPLOAD : Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        if (withImage) {
            return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW), new Parameter(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(folderID)), new FieldParameter("json", fieldContent), new FileParameter("file", "open-xchange_image.jpg", new ByteArrayInputStream(contactObj.getImage1()), "image/jpg")
            };
        }
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW), new Parameter(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(folderID))
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InsertParser getParser() {
        return new InsertParser(failOnError, withImage);
    }
}
