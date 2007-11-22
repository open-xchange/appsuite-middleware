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

package com.openexchange.ajp13;

import static com.openexchange.ajp13.AJPv13ForwardRequest.parseQueryString;

import java.io.IOException;
import java.io.OutputStream;

import com.openexchange.ajp13.AJPv13Exception.AJPCode;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AJPv13RequestBody extends AJPv13Request {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13RequestBody.class);

	public AJPv13RequestBody(final byte[] payloadData) {
		super(payloadData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.ajp13.AJPv13Request#processRequest(com.openexchange.tools.ajp13.AJPv13RequestHandler)
	 */
	@Override
	public void processRequest(final AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, IOException {
		if (payloadData == null) {
			throw new AJPv13Exception(AJPCode.MISSING_PAYLOAD_DATA);
		}
		final int chunkContentLength;
		if (payloadData.length == 0 || (chunkContentLength = parseInt()) == 0) {
			/*
			 * Empty data package received
			 */
			if (ajpRequestHandler.isMoreDataExpected()) {
				/*
				 * Hmm... we actually expect more data
				 */
				if (LOG.isWarnEnabled()) {
					final AJPv13Exception ajpExc = new AJPv13Exception(AJPCode.UNEXPECTED_EMPTY_DATA_PACKAGE, Integer
							.valueOf(ajpRequestHandler.getTotalRequestedContentLength()), Integer
							.valueOf(ajpRequestHandler.getContentLength()));
					ajpExc.fillInStackTrace();
					LOG.warn(ajpExc.getMessage(), ajpExc);
				}
				/*
				 * Set data to null to indicate that no more data is available
				 * from web server
				 */
				ajpRequestHandler.makeEqual();
				ajpRequestHandler.getServletRequest().getOXInputStream().setData(null);
				return;
			}
			/*
			 * If we currently read from a chunked http input stream -
			 * transfer-encoding: chunked - , then 'content-length' header has
			 * not been set
			 */
			if (ajpRequestHandler.isNotSet() || ajpRequestHandler.isMoreDataReadThanExpected()) {
				ajpRequestHandler.makeEqual();
			}
			ajpRequestHandler.getServletRequest().getOXInputStream().setData(null);
			return;
		}
		/*
		 * Parse current size
		 */
		ajpRequestHandler.increaseTotalRequestedContentLength(chunkContentLength);
		final byte[] contentBytes = getByteSequence(chunkContentLength);
		/*
		 * Add payload data to servlet's request input stream
		 */
		ajpRequestHandler.getServletRequest().getOXInputStream().setData(contentBytes);
		/*
		 * Request Data is recognized as form data and all body chunks have
		 * already been received. Then turn post data into request parameters.
		 */
		if (ajpRequestHandler.isFormData()) {
			/*
			 * Read all form data prior to further processing
			 */
			if (!ajpRequestHandler.isAllDataRead()) {
				/*
				 * Request next body chunk package from web sever
				 */
				final OutputStream out = ajpRequestHandler.getAJPConnection().getOutputStream();
				out.write(AJPv13Response.getGetBodyChunkBytes(ajpRequestHandler.getNumOfBytesToRequestFor()));
				out.flush();
				ajpRequestHandler.processPackage();
				return;
			}
			/*
			 * Turn form's post data into request parameters
			 */
			String charEnc = ajpRequestHandler.getServletRequest().getCharacterEncoding();
			if (charEnc == null) {
				charEnc = ServerConfig.getProperty(Property.DefaultEncoding);
			}
			parseQueryString(ajpRequestHandler.getServletRequest(), new String(contentBytes, charEnc));
		}
	}

}
