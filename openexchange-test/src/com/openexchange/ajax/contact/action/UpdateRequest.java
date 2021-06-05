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
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.container.Contact;

/**
 * Implements creating the necessary values for a contact update request. All
 * necessary values are read from the contact object. The contact must contain the folder and
 * object identifier and the last modification timestamp.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class UpdateRequest extends AbstractContactRequest<UpdateResponse> {

    private final Contact contactObj;
    private final boolean failOnError;
    private final int originFolder;
    private final boolean withImage;
    private String fieldContent;

    /**
     * Default constructor.
     * 
     * @param contactObj Contact object with updated attributes. This contact must contain
     *            the attributes parent folder identifier, object identifier and last
     *            modification timestamp.
     */
    public UpdateRequest(final Contact contactObj) {
        this(contactObj, true);
    }

    public UpdateRequest(final Contact contactObj, final boolean failOnError) {
        this(contactObj.getParentFolderID(), contactObj, failOnError);
    }

    public UpdateRequest(final int inFolder, final Contact entry, final boolean failOnError) {
        super();
        this.contactObj = entry;
        this.failOnError = failOnError;
        this.originFolder = inFolder;
        this.withImage = contactObj.containsImage1() && (null != contactObj.getImage1());

        if (withImage) {
            try {
                fieldContent = convert(contactObj).toString();
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
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
            return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE), new Parameter(AJAXServlet.PARAMETER_INFOLDER, Integer.toString(this.originFolder)), new Parameter(AJAXServlet.PARAMETER_ID, Integer.toString(contactObj.getObjectID())), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, Long.toString(contactObj.getLastModified().getTime())), new FieldParameter("json", fieldContent), new FileParameter("file", "open-xchange_image.jpg", new ByteArrayInputStream(contactObj.getImage1()), "image/jpg")
            };
        }

        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE), new Parameter(AJAXServlet.PARAMETER_INFOLDER, Integer.toString(this.originFolder)), new Parameter(AJAXServlet.PARAMETER_ID, Integer.toString(contactObj.getObjectID())), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, Long.toString(contactObj.getLastModified().getTime()))
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateParser getParser() {
        return new UpdateParser(failOnError, withImage);
    }

    /**
     * @return the contact
     */
    protected Contact getContact() {
        return contactObj;
    }
}
