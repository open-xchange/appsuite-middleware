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

package com.openexchange.ajax.mail.actions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.java.Strings;

/**
 * {@link AttachmentRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class AttachmentRequest extends AbstractMailRequest<AttachmentResponse> {

	class AttachmentParser extends AbstractAJAXParser<AttachmentResponse> {

	    private String strBody;
        private byte[] bytesBody;

		/**
		 * Default constructor.
		 */
		AttachmentParser(final boolean failOnError) {
			super(failOnError);
		}

		@Override
		public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
		    assertEquals("Response code is not okay. (" + resp.getStatusLine().getReasonPhrase() + ")", HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());
		    Header contentType = resp.getFirstHeader("Content-Type");
		    if (Strings.asciiLowerCase(contentType.getValue()).startsWith("text/")) {
                strBody = EntityUtils.toString(resp.getEntity());
            } else {
                bytesBody = EntityUtils.toByteArray(resp.getEntity());
            }
		    return "{}";
		}

		@Override
		public AttachmentResponse parse(String body) throws JSONException {
		    if (body.length() == 0) {
		        return new AttachmentResponse(new Response());
		    }
		    return null == strBody ? new AttachmentResponse(bytesBody) : new AttachmentResponse(strBody);
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
    private boolean fromStructure;

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

    /**
     * Sets the <code>fromStructure</code> flag
     *
     * @param fromStructure The <code>fromStructure</code> flag to set
     */
    public AttachmentRequest setFromStructure(boolean fromStructure) {
        this.fromStructure = fromStructure;
        return this;
    }

	@Override
    public Object getBody() throws JSONException {
		return null;
	}

	@Override
    public Method getMethod() {
		return Method.GET;
	}

	@Override
    public Parameter[] getParameters() {
	    List<Parameter> l = new LinkedList<Parameter>();
	    l.add(new Parameter(AJAXServlet.PARAMETER_ACTION, Mail.ACTION_MATTACH));
	    l.add( new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderAndIDAndSequenceID[0]) );
	    l.add( new Parameter(AJAXServlet.PARAMETER_ID, folderAndIDAndSequenceID[1]) );
	    l.add( new Parameter(Mail.PARAMETER_MAILATTCHMENT, folderAndIDAndSequenceID[2]) );
	    l.add( new Parameter(Mail.PARAMETER_SAVE, String.valueOf(saveToDisk ? 1 : 0)) );
	    l.add( new Parameter(Mail.PARAMETER_FILTER, filter ? "true" : "false") );
	    l.add( new Parameter("from_structure", fromStructure ? "true" : "false") );
	    return l.toArray(new Parameter[0]);
	}

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
