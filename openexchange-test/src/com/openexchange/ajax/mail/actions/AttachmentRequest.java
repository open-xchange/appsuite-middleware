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

package com.openexchange.ajax.mail.actions;

import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link AttachmentRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class AttachmentRequest extends AbstractMailRequest<AttachmentResponse> {

	class AttachmentParser extends AbstractAJAXParser<AttachmentResponse> {

		/**
		 * Default constructor.
		 */
		AttachmentParser(final boolean failOnError) {
			super(failOnError);
		}

		@Override
		public AttachmentResponse parse(String body) throws JSONException {
		    if (body.length() == 0) {
		        Response response = new Response();
		        return new AttachmentResponse(response);
		    }
		    return super.parse(body);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected AttachmentResponse createResponse(final Response response) throws JSONException {
			return new AttachmentResponse(response);
		}
	}

	/**
	 * Unique identifier
	 */
	private final String[] folderAndIDAndSequenceID;

	private final boolean failOnError;

	private boolean saveToDisk;

	private boolean filter;

	public AttachmentRequest(final String folder, final String ID, final String sequenceId) {
		this(new String[] { folder, ID, sequenceId }, true);
	}

	/**
	 * Initializes a new {@link AttachmentRequest}
	 *
	 * @param mailPath
	 */
	public AttachmentRequest(final String[] folderAndIDAndSequenceID) {
		this(folderAndIDAndSequenceID, true);
	}

	/**
	 * Initializes a new {@link AttachmentRequest}
	 *
	 * @param mailPath
	 * @param failOnError
	 */
	public AttachmentRequest(final String[] folderAndIDAndSequenceID, final boolean failOnError) {
		super();
		this.folderAndIDAndSequenceID = folderAndIDAndSequenceID;
		this.failOnError = failOnError;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.openexchange.ajax.framework.AJAXRequest#getBody()
	 */
	@Override
    public Object getBody() throws JSONException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.openexchange.ajax.framework.AJAXRequest#getMethod()
	 */
	@Override
    public Method getMethod() {
		return Method.GET;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.openexchange.ajax.framework.AJAXRequest#getParameters()
	 */
	@Override
    public Parameter[] getParameters() {
		return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, Mail.ACTION_MATTACH),
				new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderAndIDAndSequenceID[0]),
				new Parameter(AJAXServlet.PARAMETER_ID, folderAndIDAndSequenceID[1]),
				new Parameter(Mail.PARAMETER_MAILATTCHMENT, folderAndIDAndSequenceID[2]),
				new Parameter(Mail.PARAMETER_SAVE, String.valueOf(saveToDisk ? 1 : 0)),
				new Parameter(Mail.PARAMETER_FILTER, filter ? "true" : "false")};
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.openexchange.ajax.framework.AJAXRequest#getParser()
	 */
	@Override
    public AttachmentParser getParser() {
		return new AttachmentParser(failOnError);
	}

	/**
	 * Gets the saveToDisk
	 *
	 * @return the saveToDisk
	 */
	public boolean isSaveToDisk() {
		return saveToDisk;
	}

	/**
	 * Sets the saveToDisk
	 *
	 * @param saveToDisk the saveToDisk to set
	 */
	public void setSaveToDisk(final boolean saveToDisk) {
		this.saveToDisk = saveToDisk;
	}

	/**
	 * Gets the filter
	 *
	 * @return the filter
	 */
	public boolean isFilter() {
		return filter;
	}

	/**
	 * Sets the filter
	 *
	 * @param filter the filter to set
	 */
	public void setFilter(final boolean filter) {
		this.filter = filter;
	}

}
