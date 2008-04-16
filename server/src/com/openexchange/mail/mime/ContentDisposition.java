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

package com.openexchange.mail.mime;

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Part;

import com.openexchange.mail.MailException;

/**
 * {@link ContentDisposition}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ContentDisposition extends ParameterizedHeader implements Serializable {

	private static final long serialVersionUID = 310827213193290169L;

	private static final Pattern PATTERN_CONTENT_DISP = Pattern.compile("(?:inline|attachment)",
			Pattern.CASE_INSENSITIVE);

	private static final String DEFAULT_CONTENT_DISP = Part.INLINE.toUpperCase(Locale.ENGLISH);

	private static final String PARAM_FILENAME = "filename";

	private String disposition;

	/**
	 * Initializes a new {@link ContentDisposition}
	 */
	public ContentDisposition() {
		super();
		disposition = DEFAULT_CONTENT_DISP;
		parameterList = new ParameterList();
	}

	/**
	 * Initializes a new {@link ContentDisposition}
	 * 
	 * @param contentDisp
	 *            The content disposition
	 * @throws MailException
	 *             If content disposition cannot be parsed
	 */
	public ContentDisposition(final String contentDisp) throws MailException {
		super();
		parseContentDisp(contentDisp);
	}

	private void parseContentDisp(final String contentDisp) throws MailException {
		parseContentDisp(contentDisp, true);
	}

	private void parseContentDisp(final String contentDispArg, final boolean paramList) throws MailException {
		if ((null == contentDispArg) || (contentDispArg.length() == 0)) {
			/*
			 * Nothing to parse
			 */
			disposition = DEFAULT_CONTENT_DISP;
			parameterList = new ParameterList();
			return;
		}
		final String contentDisp = prepareParameterizedHeader(contentDispArg);
		final Matcher cdMatcher = PATTERN_CONTENT_DISP.matcher(contentDisp);
		if (!cdMatcher.find()) {
			disposition = DEFAULT_CONTENT_DISP;
			parameterList = new ParameterList();
			return;
		}
		if (cdMatcher.start() != 0) {
			throw new MailException(MailException.Code.INVALID_CONTENT_TYPE, contentDispArg);
		}
		disposition = cdMatcher.group().toUpperCase(Locale.ENGLISH);
		if (paramList) {
			parameterList = new ParameterList(contentDisp.substring(cdMatcher.end()));
		}
	}

	/**
	 * Applies given content disposition to this content disposition
	 * 
	 * @param contentDisp
	 *            The content disposition to apply
	 */
	public void setContentType(final ContentDisposition contentDisp) {
		if (contentDisp == this) {
			return;
		}
		disposition = contentDisp.disposition;
		this.parameterList = (ParameterList) contentDisp.parameterList.clone();
	}

	/**
	 * @return disposition
	 */
	public String getDisposition() {
		return disposition;
	}

	/**
	 * Sets disposition
	 */
	public void setDisposition(final String disposition) {
		this.disposition = disposition;
	}

	/**
	 * Sets filename parameter
	 */
	public void setFilenameParameter(final String filename) {
		setParameter(PARAM_FILENAME, filename);
	}

	/**
	 * @return the filename value or <code>null</code> if not present
	 */
	public String getFilenameParameter() {
		return getParameter(PARAM_FILENAME);
	}

	/**
	 * @return <code>true</code> if filename parameter is present,
	 *         <code>false</code> otherwise
	 */
	public boolean containsFilenameParameter() {
		return containsParameter(PARAM_FILENAME);
	}

	/**
	 * Sets Content-Disposition
	 */
	public void setContentDisposition(final String contentDisp) throws MailException {
		parseContentDisp(contentDisp);
	}

	/**
	 * Checks if disposition is inline
	 * 
	 * @return <code>true</code> if disposition is inline; otherwise
	 *         <code>false</code>
	 */
	public boolean isInline() {
		return Part.INLINE.equals(disposition);
	}

	/**
	 * Checks if disposition is attachment
	 * 
	 * @return <code>true</code> if disposition is attachment; otherwise
	 *         <code>false</code>
	 */
	public boolean isAttachment() {
		return Part.ATTACHMENT.equals(disposition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(64);
		sb.append(disposition);
		parameterList.appendUnicodeString(sb);
		return sb.toString();
	}
}
