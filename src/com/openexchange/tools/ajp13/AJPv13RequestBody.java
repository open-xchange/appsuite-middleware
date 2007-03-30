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



package com.openexchange.tools.ajp13;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.openexchange.tools.ajp13.AJPv13Exception.AJPCode;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class AJPv13RequestBody extends AJPv13Request {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJPv13RequestBody.class);

	public AJPv13RequestBody(final byte[] payloadData) {
		super(payloadData);
	}

	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.ajp13.AJPv13Request#processRequest(com.openexchange.tools.ajp13.AJPv13RequestHandler)
	 */
	@Override
	public void processRequest(final AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, IOException {
		if (payloadData == null) {
			throw new AJPv13Exception(AJPCode.MISSING_PAYLOAD_DATA);
		}
		if (payloadData.length == 0) {
			/*
			 * Empty data package received
			 */
			if (ajpRequestHandler.isMoreDataExpected()) {
				/*
				 * Hmm... we actually expect more data
				 */
				if (ajpRequestHandler.emptyDataPackageAlreadyReceived()) {
					/*
					 * Ok, we detected an empty data package twice. Assume that
					 * client really sent all data to servlet container although
					 * client indicated that data is still left to send because
					 * total requested content length is less than data's
					 * content length
					 */
					if (LOG.isWarnEnabled()) {
						final AJPv13Exception ajpExc = new AJPv13Exception(AJPCode.UNEXPECTED_EMPTY_DATA_PACKAGE,
								ajpRequestHandler.getTotalRequestedContentLength(), ajpRequestHandler.getContentLength());
						ajpExc.fillInStackTrace();
						LOG.warn(ajpExc.getMessage(), ajpExc);
					}
					/*
					 * Set data to null to indicate that no more data is
					 * available from web server
					 */
					ajpRequestHandler.makeEqual();
					ajpRequestHandler.getServletRequestObj().getOXInputStream().setData(null);
				} else {
					/*
					 * Request expected data one more time
					 */
					final AJPv13Response response = new AJPv13Response(AJPv13Response.GET_BODY_CHUNK_PREFIX_CODE,
							ajpRequestHandler.getNumOfBytesToRequestFor());
					final byte[] ajpBytes = response.getResponseBytes();
					final OutputStream out = ajpRequestHandler.getAJPConnection().getOutputStream();
					out.write(ajpBytes);
					out.flush();
					ajpRequestHandler.setEmptyDataPackageReceived(true);
					ajpRequestHandler.processPackage();
				}
				return;
			}
			/*
			 * If we currently read from a chunked http input stream -
			 * transfer-encoding: chunked - , then 'content-length' header has
			 * not been set
			 */
			if (!ajpRequestHandler.containsContentLength() || ajpRequestHandler.isMoreDataReadThanExpected()) {
				ajpRequestHandler.makeEqual();
			}
			ajpRequestHandler.getServletRequestObj().getOXInputStream().setData(null);
			return;
		}
		/*
		 * Parse current size
		 */
		final int chunkContentLength = parseInt();
		ajpRequestHandler.increaseTotalRequestedContentLength(chunkContentLength);
		final byte[] contentBytes = getByteSequence(chunkContentLength);
		/*
		 * Add payload data to servlet's request input stream
		 */
		ajpRequestHandler.getServletRequestObj().getOXInputStream().setData(contentBytes);
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
				final AJPv13Response response = new AJPv13Response(AJPv13Response.GET_BODY_CHUNK_PREFIX_CODE,
						ajpRequestHandler.getNumOfBytesToRequestFor());
				final byte[] ajpBytes = response.getResponseBytes();
				final OutputStream out = ajpRequestHandler.getAJPConnection().getOutputStream();
				out.write(ajpBytes);
				out.flush();
				ajpRequestHandler.processPackage();
				return;
			}
			/*
			 * Turn form's post data into request parameters
			 */
			final String charEnc = ajpRequestHandler.getServletRequestObj().getCharacterEncoding();
			final String[] parameters = new String(contentBytes, charEnc).split(" *& *");
			for (int i = 0; i < parameters.length; i++) {
				final String parameterName = parameters[i].substring(0, parameters[i].indexOf('='));
				final String parameterValue = decodeURL(parameters[i].substring(parameters[i].indexOf('=') + 1),
						charEnc);
				ajpRequestHandler.getServletRequestObj().setParameter(parameterName, parameterValue);
			}
		}
	}

	private static final String decodeURL(final String encodedURL, final String characterEncoding) {
		try {
			return URLDecoder.decode(encodedURL, characterEncoding);
		} catch (UnsupportedEncodingException e) {
			LOG.error(e);
		}
		return "";
	}

//	private final byte[] getData() {
//		final byte[] retval = new byte[payloadData.length - 2];
//		System.arraycopy(payloadData, 2, retval, 0, retval.length);
//		return retval;
//	}

}
