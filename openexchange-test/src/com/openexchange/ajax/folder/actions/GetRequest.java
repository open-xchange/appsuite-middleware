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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.folder.actions;

import org.json.JSONException;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link GetRequest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class GetRequest extends AbstractFolderRequest {

	class GetParser extends AbstractAJAXParser<GetResponse> {

		/**
		 * Default constructor.
		 */
		GetParser(final boolean failOnError) {
			super(failOnError);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected GetResponse createResponse(final Response response) throws JSONException {
			return new GetResponse(response);
		}
	}

	private final boolean failOnError;

	private final String folderIdentifier;

	private final int[] columns;

	/**
	 * Initializes a new {@link GetRequest}
	 */
	public GetRequest(final String folderIdentifier, final int[] columns, final boolean failOnError) {
		// TODO Auto-generated constructor stub
		super();
		this.folderIdentifier = folderIdentifier;
		this.columns = columns;
		this.failOnError = failOnError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.ajax.framework.AJAXRequest#getBody()
	 */
	public Object getBody() throws JSONException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.ajax.framework.AJAXRequest#getMethod()
	 */
	public Method getMethod() {
		return Method.GET;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.ajax.framework.AJAXRequest#getParameters()
	 */
	public Parameter[] getParameters() {
		return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET),
				new Parameter(AJAXServlet.PARAMETER_ID, folderIdentifier),
				new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.ajax.framework.AJAXRequest#getParser()
	 */
	public AbstractAJAXParser<GetResponse> getParser() {
		return new GetParser(failOnError);
	}

}
