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

package com.openexchange.mail.mime.utils;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.session.Session;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * {@link MIMEMessageUtility} - Utilities for MIME messages.
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
	 * @return <code>true</code> if given html content contains inlined images;
	 *         otherwise <code>false</code>
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
	 * @return an instance of <code>{@link List}</code> containing all occuring
	 *         content IDs
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
	 * "Content-ID". The leading/trailing character '<code>&lt;</code>'/'
	 * <code>&gt;</code>' are ignored during comparison
	 * 
	 * @param contentId1Arg
	 *            The first content ID
	 * @param contentId2Arg
	 *            The second content ID
	 * @return <code>true</code> if both are equal; otherwise <code>false</code>
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
	public static boolean hasReferencedLocalImages(final String htmlContent, final Session session) {
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

	/*
	 * Mulipart subtype constants
	 */
	private static final String MULTI_SUBTYPE_ALTERNATIVE = "ALTERNATIVE";

	// private static final String MULTI_SUBTYPE_MIXED = "MIXED";

	private static final String MULTI_SUBTYPE_SIGNED = "SIGNED";

	/**
	 * Checks if given multipart contains (file) attachments
	 * 
	 * @param mp
	 *            The multipart to examine
	 * @param subtype
	 *            The multipart's subtype multipart contains (file) attachments;
	 *            otherwise <code>false</code>
	 * @return <code>true</code> if given
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws MailException
	 *             If a mail error occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public static boolean hasAttachments(final Multipart mp, final String subtype) throws MessagingException,
			MailException, IOException {
		if (MULTI_SUBTYPE_ALTERNATIVE.equalsIgnoreCase(subtype)) {
			if (mp.getCount() > 2) {
				return true;
			}
			return hasAttachments0(mp);
		} else if (MULTI_SUBTYPE_SIGNED.equalsIgnoreCase(subtype)) {
			if (mp.getCount() > 2) {
				return true;
			}
			return hasAttachments0(mp);
		} else if (mp.getCount() > 1) {
			return true;
		} else {
			return hasAttachments0(mp);
		}
	}

	private static boolean hasAttachments0(final Multipart mp) throws MessagingException, MailException, IOException {
		boolean found = false;
		final int count = mp.getCount();
		final ContentType ct = new ContentType();
		for (int i = 0; i < count && !found; i++) {
			final BodyPart part = mp.getBodyPart(i);
			final String[] tmp = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
			if (tmp != null && tmp.length > 0) {
				ct.setContentType(MimeUtility.unfold(tmp[0]));
			} else {
				ct.setContentType(MIMETypes.MIME_DEFAULT);
			}
			if (ct.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
				found |= hasAttachments((Multipart) part.getContent(), ct.getSubType());
			}
		}
		return found;
	}

	/**
	 * Checks if given BODYSTRUCTURE item indicates to contain (file)
	 * attachments
	 * 
	 * @param bodystructure
	 *            The BODYSTRUCTURE item
	 * @return <code>true</code> if given BODYSTRUCTURE item indicates to
	 *         contain (file) attachments; otherwise <code>false</code>
	 */
	public static boolean hasAttachments(final BODYSTRUCTURE bodystructure) {
		if (bodystructure.isMulti()) {
			if (MULTI_SUBTYPE_ALTERNATIVE.equalsIgnoreCase(bodystructure.subtype)) {
				if (bodystructure.bodies.length > 2) {
					return true;
				}
				return hasAttachments0(bodystructure);
			} else if (MULTI_SUBTYPE_SIGNED.equalsIgnoreCase(bodystructure.subtype)) {
				if (bodystructure.bodies.length > 2) {
					return true;
				}
				return hasAttachments0(bodystructure);
			} else if (bodystructure.bodies.length > 1) {
				return true;
			} else {
				return hasAttachments0(bodystructure);
			}
		}
		return false;
	}

	private static boolean hasAttachments0(final BODYSTRUCTURE bodystructure) {
		boolean found = false;
		for (int i = 0; (i < bodystructure.bodies.length) && !found; i++) {
			found |= hasAttachments(bodystructure.bodies[i]);
		}
		return found;
	}

	private static final Pattern ENC_PATTERN = Pattern.compile("(=\\?\\S+?\\?\\S+?\\?)(.+?)(\\?=)");

	/**
	 * Decodes a multi-mime-encoded header value using the algorithm specified
	 * in RFC 2047, Section 6.1
	 * <p>
	 * If the charset-conversion fails for any sequence, an
	 * {@link UnsupportedEncodingException} is thrown.
	 * <p>
	 * If the String is not an RFC 2047 style encoded header, it is returned
	 * as-is
	 * 
	 * @param headerValArg
	 *            The possibly encoded header value
	 * @return The possibly decoded header value
	 */
	public static String decodeMultiEncodedHeader(final String headerValArg) {
		if (headerValArg == null) {
			return null;
		}
		final String hdrVal = MimeUtility.unfold(headerValArg);
		final Matcher m = ENC_PATTERN.matcher(hdrVal);
		if (m.find()) {
			final StringBuilder sb = new StringBuilder(hdrVal.length());
			int lastMatch = 0;
			do {
				try {
					sb.append(hdrVal.substring(lastMatch, m.start()));
					sb.append(Matcher.quoteReplacement(MimeUtility.decodeWord(m.group())));
					lastMatch = m.end();
				} catch (final UnsupportedEncodingException e) {
					LOG.error("Unsupported character-encoding in encoded-word: " + m.group(), e);
					sb.append(Matcher.quoteReplacement(m.group()));
					lastMatch = m.end();
				} catch (final ParseException e) {
					LOG.error("String is not an encoded-word as per RFC 2047: " + m.group(), e);
					sb.append(Matcher.quoteReplacement(m.group()));
					lastMatch = m.end();
				}
			} while (m.find());
			sb.append(hdrVal.substring(lastMatch));
			return sb.toString();
		}
		return hdrVal;
	}

	/**
	 * Parse the given sequence of addresses into InternetAddress objects by
	 * invoking <code>{@link InternetAddress#parse(String, boolean)}</code>. If
	 * <code>strict</code> is false, simple email addresses separated by spaces
	 * are also allowed. If <code>strict</code> is true, many (but not all) of
	 * the RFC822 syntax rules are enforced. In particular, even if
	 * <code>strict</code> is true, addresses composed of simple names (with no
	 * "@domain" part) are allowed. Such "illegal" addresses are not uncommon in
	 * real messages.
	 * <p>
	 * Non-strict parsing is typically used when parsing a list of mail
	 * addresses entered by a human. Strict parsing is typically used when
	 * parsing address headers in mail messages.
	 * <p>
	 * Additionally the personal parts are MIME encoded using default MIME
	 * charset.
	 * 
	 * @param addresslist
	 *            - comma separated address strings
	 * @param strict
	 *            - <code>true</code> to enforce RFC822 syntax; otherwise
	 *            <code>false</code>
	 * @return array of <code>InternetAddress</code> objects
	 * @throws AddressException
	 *             - if parsing fails
	 */
	public static InternetAddress[] parseAddressList(final String addresslist, final boolean strict)
			throws AddressException {
		final InternetAddress[] addrs = InternetAddress
				.parse(replaceWithComma(MimeUtility.unfold(addresslist)), strict);
		try {
			for (int i = 0; i < addrs.length; i++) {
				addrs[i].setPersonal(addrs[i].getPersonal(), MailConfig.getDefaultMimeCharset());
			}
		} catch (final UnsupportedEncodingException e) {
			/*
			 * Cannot occur since default charset is checked on global mail
			 * configuration initialization
			 */
			LOG.error(e.getLocalizedMessage(), e);
		}
		return addrs;
	}

	private static final Pattern PATTERN_REPLACE = Pattern.compile("([^\"]\\S+?)(\\s*)([;])(\\s*)");

	private static String replaceWithComma(final String addressList) {
		final StringBuilder sb = new StringBuilder(addressList.length());
		final Matcher m = PATTERN_REPLACE.matcher(addressList);
		int lastMatch = 0;
		while (m.find()) {
			sb.append(addressList.substring(lastMatch, m.start()));
			sb.append(m.group(1)).append(m.group(2)).append(',').append(m.group(4));
			lastMatch = m.end();
		}
		sb.append(addressList.substring(lastMatch));
		return sb.toString();
	}

	private static final Pattern PAT_QUOTED = Pattern.compile("(^\")([^\"]+?)(\"$)");

	private static final Pattern PAT_QUOTABLE_CHAR = Pattern.compile("[.,:;<>\"]");

	/**
	 * Quotes given personal part of an Internet address according to RFC 822
	 * syntax if needed; otherwise the personal is returned unchanged.
	 * <p>
	 * This method guarantees that the resulting string can be used to build an
	 * Internet address according to RFC 822 syntax so that the
	 * <code>{@link InternetAddress#parse(String)}</code> constructor won't
	 * throw an instance of <code>{@link AddressException}</code>.
	 * 
	 * <pre>
	 * final String quotedPersonal = quotePersonal(&quot;Doe, Jane&quot;);
	 * 
	 * final String buildAddr = quotedPersonal + &quot; &lt;someone@somewhere.com&gt;&quot;;
	 * System.out.println(buildAddr);
	 * //Plain Address: &quot;=?UTF-8?Q?Doe=2C_Jan=C3=A9?=&quot; &lt;someone@somewhere.com&gt;
	 * 
	 * final InternetAddress ia = new InternetAddress(buildAddr);
	 * System.out.println(ia.toUnicodeString());
	 * //Unicode Address: &quot;Doe, Jane&quot; &lt;someone@somewhere.com&gt;
	 * </pre>
	 * 
	 * @param personalArg
	 *            The personal's string representation
	 * @return The properly quoted personal for building an Internet address
	 *         according to RFC 822 syntax
	 */
	public static String quotePersonal(final String personalArg) {
		try {
			final String personal = MimeUtility.encodeWord(personalArg);
			if (PAT_QUOTED.matcher(personal).matches() ? false : PAT_QUOTABLE_CHAR.matcher(personal).find()) {
				/*
				 * Quote
				 */
				return new StringBuilder(personal.length() + 2).append('"').append(
						personal.replaceAll("\"", "\\\\\\\"")).append('"').toString();
			}
			return personal;
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Unsupported encoding in a message detected and monitored.", e);
			mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			return personalArg;
		}
	}
}
