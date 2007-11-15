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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ParseException;

import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.mail.ContentType;

/**
 * {@link MIMEMessageUtility}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MIMEMessageUtility {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MIMEMessageUtility.class);

	/**
	 * No instantiation
	 */
	private MIMEMessageUtility() {
		super();
	}

	private static final Pattern PATTERN_EMBD_IMG = Pattern.compile("(<img.*src=\"?cid:)([^\"]+)(\"?[^/>]*/?>)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern PATTERN_EMBD_IMG_ALT = Pattern.compile(
			"(<img.*src=\"?)([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)(\"?[^/>]*/?>)", Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);

	/**
	 * Detects if given html content contains inlined images
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * &lt;img src=&quot;cid:s345asd845@12drg&quot;&gt;
	 * </pre>
	 * 
	 * @param htmlContent
	 *            The html content
	 * @return <code>true</code> if given html content contains inlined
	 *         images; otherwise <code>false</code>
	 */
	public static boolean hasEmbeddedImages(final String htmlContent) {
		return PATTERN_EMBD_IMG.matcher(htmlContent).find() || PATTERN_EMBD_IMG_ALT.matcher(htmlContent).find();
	}

	/**
	 * Gathers all occuring content IDs in html content and returns them as a
	 * list
	 * 
	 * @param htmlContent
	 *            The html content
	 * @return an instance of <code>{@link List}</code> containing all
	 *         occuring content IDs
	 */
	public static List<String> getContentIDs(final String htmlContent) {
		final List<String> retval = new ArrayList<String>();
		Matcher m = PATTERN_EMBD_IMG.matcher(htmlContent);
		while (m.find()) {
			retval.add(m.group(2));
		}
		m = PATTERN_EMBD_IMG_ALT.matcher(htmlContent);
		while (m.find()) {
			retval.add(m.group(2));
		}
		return retval;
	}

	/**
	 * Compares (case insensitive) the given values of message header
	 * "Content-ID". The leading/trailing character '<code>&lt;</code>'/'<code>&gt;</code>'
	 * are ignored during comparison
	 * 
	 * @param contentId1Arg
	 *            The first content ID
	 * @param contentId2Arg
	 *            The second content ID
	 * @return <code>true</code> if both are equal; otherwise
	 *         <code>false</code>
	 */
	public static boolean equalsCID(final String contentId1Arg, final String contentId2Arg) {
		if (null != contentId1Arg && null != contentId2Arg) {
			final String contentId1 = contentId1Arg.charAt(0) == '<' ? contentId1Arg.substring(1, contentId1Arg
					.length() - 1) : contentId1Arg;
			final String contentId2 = contentId2Arg.charAt(0) == '<' ? contentId2Arg.substring(1, contentId2Arg
					.length() - 1) : contentId2Arg;
			return contentId1.equalsIgnoreCase(contentId2);
		}
		return false;
	}

	public static final Pattern PATTERN_REF_IMG = Pattern.compile(
			"(<img[^/>]*?)(src=\")([^\"]+)(id=)([^\"&]+)(?:(&[^\"]+\")|(\"))([^/>]*/?>)", Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);

	/**
	 * Detects if given html content contains references to local image files
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * &lt;img src=&quot;[url-to-image]&amp;id=123dfr567zh&quot;&gt;
	 * </pre>
	 * 
	 * @param htmlContent
	 *            The html content
	 * @param session
	 *            The user session
	 * @return <code>true</code> if given html content contains references to
	 *         local image files; otherwise <code>false</code>
	 */
	public static boolean hasReferencedLocalImages(final String htmlContent, final SessionObject session) {
		final Matcher m = PATTERN_REF_IMG.matcher(htmlContent);
		while (m.find()) {
			if (session.touchUploadedFile(m.group(5))) {
				return true;
			}
		}
		return false;
	}

	private static final String PARAM_FILENAME = "filename";

	/**
	 * Determines specified part's real filename if any available
	 * 
	 * @param part
	 *            The part whose filename should be determined
	 * @return The part's real filename or <code>null</code> if none present
	 */
	public static String getRealFilename(final MailPart part) {
		if (part.getFileName() != null) {
			return part.getFileName();
		}
		final String hdr = part.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION);
		if (hdr == null) {
			return getContentTypeFilename(part);
		}
		try {
			final String retval = new ContentDisposition(hdr).getParameter(PARAM_FILENAME);
			if (retval == null) {
				return getContentTypeFilename(part);
			}
			return retval;
		} catch (final ParseException e) {
			return getContentTypeFilename(part);
		}
	}

	private static final String PARAM_NAME = "name";

	private static String getContentTypeFilename(final MailPart part) {
		if (part.containsContentType()) {
			return part.getContentType().getParameter(PARAM_NAME);
		}
		final String hdr = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
		if (hdr == null || hdr.length() == 0) {
			return null;
		}
		try {
			return new ContentType(hdr).getParameter(PARAM_NAME);
		} catch (final MailException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}
}
