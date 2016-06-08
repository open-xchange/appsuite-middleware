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

package com.openexchange.ajax.mail.filter.api.request;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.filter.api.parser.DeleteParser;

/**
 * Stores parameters for the delete request.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class DeleteRequest extends AbstractMailFilterRequest {

	private final String objectId;

	private boolean failOnError = true;

	/**
	 * Default constructor.
	 */
	public DeleteRequest(final String objectId) {
		this(objectId, true);
	}

	public DeleteRequest(final String objectId, final boolean failOnError) {
		super();
		this.objectId = objectId;
		this.failOnError = failOnError;
	}

	public void setFailOnError(final boolean failOnError) {
		this.failOnError = failOnError;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public Object getBody() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(DataFields.ID, objectId);

		return json;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public Method getMethod() {
		return Method.PUT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public Parameter[] getParameters() {
		return new Parameter[] {
				new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE),
				new Parameter(AJAXServlet.PARAMETER_ID, objectId)
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public AbstractAJAXParser getParser() {
		return new DeleteParser(failOnError);
	}
}
