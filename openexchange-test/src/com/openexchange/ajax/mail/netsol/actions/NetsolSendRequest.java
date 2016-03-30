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

package com.openexchange.ajax.mail.netsol.actions;

import java.io.InputStream;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractUploadParser;
import com.openexchange.ajax.framework.Header;

/**
 * {@link NetsolSendRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class NetsolSendRequest implements AJAXRequest<NetsolSendResponse> {

	/**
	 * URL of the tasks AJAX interface.
	 */
	public static final String MAIL_URL = "/ajax/mail";

	private final String mailStr;

	private final InputStream upload;

	private final String uploadContentType;

	private final String uploadFilename;

	/*
	 * Mail object settings
	 */
	private String recipientTo;

	/**
	 * Initializes a new {@link NetsolSendRequest}
	 *
	 * @param mailStr
	 *            The mail string (JSON)
	 */
	public NetsolSendRequest(final String mailStr) {
		this(mailStr, null);
	}

	/**
	 * Initializes a new {@link NetsolSendRequest}
	 *
	 * @param mailStr
	 *            The mail string (JSON)
	 * @param upload
	 *            The upload input stream
	 */
	public NetsolSendRequest(final String mailStr, final InputStream upload) {
		this(mailStr, upload, "text/plain; charset=us-ascii", "text.txt");
	}

	/**
	 * Initializes a new {@link NetsolSendRequest}
	 *
	 * @param mailStr
	 *            The mail string (JSON)
	 * @param upload
	 *            The upload input stream
	 * @param uploadContentType
	 *            The upload's content type
	 * @param uploadFilename
	 *            The upload's filename
	 */
	public NetsolSendRequest(final String mailStr, final InputStream upload, final String uploadContentType,
			final String uploadFilename) {
		super();
		this.mailStr = mailStr;
		this.upload = upload;
		this.uploadContentType = uploadContentType;
		this.uploadFilename = uploadFilename;
	}

	@Override
    public Object getBody() {
		return null;
	}

	@Override
    public Method getMethod() {
		return Method.UPLOAD;
	}

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

	@Override
    public Parameter[] getParameters() {
		final Parameter[] retval = new Parameter[upload == null ? 2 : 3];
		retval[0] = new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		retval[1] = new FieldParameter("json_0", mailStr);
		if (upload != null) {
			retval[2] = new FileParameter("file_0", uploadFilename, upload, uploadContentType);
		}
		return retval;
	}

	@Override
    public String getServletPath() {
		return MAIL_URL;
	}

	@Override
    public SendParser getParser() {
		return new SendParser(true);
	}

	class SendParser extends AbstractUploadParser<NetsolSendResponse> {

		public SendParser(final boolean failOnError) {
			super(failOnError);
		}

		@Override
		protected NetsolSendResponse createResponse(final Response response) throws JSONException {
			return new NetsolSendResponse(response);
		}

	}
}
