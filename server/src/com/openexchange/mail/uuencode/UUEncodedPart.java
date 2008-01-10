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

package com.openexchange.mail.uuencode;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.mail.internet.MimeUtility;

/**
 * UUEncodePart UUEncode part containing all needed informations about the
 * attachment.
 * 
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */

public class UUEncodedPart extends UUEncodedMultiPart {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UUEncodedPart.class);

	private final String sPossibleFileName;

	private final String bodyPart;

	private final int startIndex;

	private final int endIndex;

	/**
	 * Constructs a UUEncodePart object containing all informations about the
	 * attachment.
	 */
	UUEncodedPart(int startIndex, int endIndex, String bodyPart, String filename) throws Exception {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.bodyPart = bodyPart;
		this.sPossibleFileName = filename;
	}

	/**
	 * Return the filename attribute of the UUEncodedPart object
	 * 
	 * @return filename - The filename
	 */
	public String getFileName() {
		return (sPossibleFileName);
	}

	/**
	 * Return the file size attribute of the UUEncodedPart object. Note: This
	 * value may be different from the saved file. This is normal because this
	 * is the size of the raw (not encoded) object.
	 * 
	 * @return The file size
	 */
	public int getFileSize() {
		try {
			return (bodyPart.getBytes().length);
		} catch (NumberFormatException nfe) {
			return (-1);
		}
	}

	/**
	 * Return the start position of the attachment within the content.
	 * 
	 * @return beginIndex - The start position
	 */
	public int getIndexStart() {
		return (startIndex);
	}

	/**
	 * Return the end position of the attachment within the content.
	 * 
	 * @return beginIndex - The start position
	 */
	public int getIndexEnd() {
		return (endIndex);
	}

	/**
	 * Gets the inputStream attribute of the UUEncodedPart object
	 * 
	 * @return inStreamPart - The inputStream
	 */
	public InputStream getInputStream() {
		final ByteArrayInputStream bStream = new ByteArrayInputStream(bodyPart.getBytes());
		try {
			final InputStream inStreamPart = MimeUtility.decode(bStream, "uuencode");
			return (inStreamPart);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return (null);
		}
	}

	/**
	 * Gets the encoded part as StringBuffer
	 * 
	 * @return part - The part
	 */
	public StringBuilder getPart() {
		final StringBuilder encodedPart = new StringBuilder();
		try {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					encodedPart.append(line).append('\n');
				}
			} finally {
				if (br != null) {
					br.close();
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return (encodedPart);
	}

	/**
	 * Output an appropriately encoded byte stream to the given OutputStream.
	 * 
	 * @param out -
	 *            The inputStream
	 * @throws java.io.IOException
	 *             if an error occurs writing to the stream
	 */
	public void writeTo(final OutputStream out) throws IOException {
		BufferedOutputStream bos = null;
		final InputStream in = getInputStream();
		try {
			bos = new BufferedOutputStream(out);
			int iChar;
			while ((iChar = in.read()) != -1) {
				bos.write(iChar);
			}
		} catch (IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
			throw ioe;
		} finally {
			if (null != bos) {
				bos.flush();
				bos.close();
			}
		}
	}
}
