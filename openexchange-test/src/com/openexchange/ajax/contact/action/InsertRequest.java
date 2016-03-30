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

package com.openexchange.ajax.contact.action;

import java.io.ByteArrayInputStream;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.container.Contact;

/**
 * Stores the parameters for inserting the contact.
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
	 * @param contactObj contact to insert.
	 */
	public InsertRequest(final Contact contactObj) {
		this(contactObj, true);
	}

	/**
	 * More detailed constructor.
	 * @param contactObj contact to insert.
	 * @param failOnError <code>true</code> to check the response for error
	 * messages.
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
            } catch (final JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }
	}

	public InsertRequest(final String json) throws JSONException{
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
		if(jsonObj != null) {
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
	        return new Parameter[] {
	            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW),
	            new Parameter(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(folderID)),
	            new FieldParameter("json", fieldContent),
	            new FileParameter("file", "open-xchange_image.jpg", new ByteArrayInputStream(contactObj.getImage1()), "image/jpg")
	        };
        }
		return new Parameter[] {
			new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW),
			new Parameter(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(folderID))
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
