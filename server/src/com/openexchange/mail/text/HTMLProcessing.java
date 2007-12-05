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

package com.openexchange.mail.text;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.tidy.Tidy;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.mime.ContentType;

/**
 * {@link HTMLProcessing}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class HTMLProcessing {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(HTMLProcessing.class);

	/**
	 * Initializes a new {@link HTMLProcessing}
	 */
	private HTMLProcessing() {
		super();
	}

	private static final String HTML_META_TEMPLATE = "\r\n    <meta content=\"#CT#\" http-equiv=\"Content-Type\">";

	private static final Pattern PAT_META_CT = Pattern.compile("<meta[^>]*?http-equiv=\"?content-type\"?[^>]*?>",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Creates valid HTML from specified HTML content conform to W3C standards.
	 * 
	 * @param htmlContentArg
	 *            The HTML content
	 * @param contentType
	 *            The corresponding content type (including charset parameter)
	 * @return The HTML content conform to W3C standards
	 */
	public static String getConformHTML(final String htmlContentArg, final ContentType contentType) {
		/*
		 * Validate with JTidy library
		 */
		final String htmlContent;
		{
			String charset = contentType.getParameter("charset");
			if (null == charset) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Missing charset in HTML content type. Using fallback \"US-ASCII\" instead.");
				}
				charset = "US-ASCII";
				contentType.setParameter("charset", charset);
			}
			htmlContent = validate(htmlContentArg, charset);
		}
		/*
		 * Check for meta tag in validated html content which indicates
		 * documents content type. Add if missing.
		 */
		final int start = htmlContent.indexOf("<head>") + 6;
		final Matcher m = PAT_META_CT.matcher(htmlContent.substring(start, htmlContent.indexOf("</head>")));
		if (!m.find()) {
			final StringBuilder sb = new StringBuilder(htmlContent);
			sb.insert(start, HTML_META_TEMPLATE.replaceFirst("#CT#", contentType.toString()));
			return sb.toString();
		}
		return htmlContent;
	}

	/**
	 * Validates specified HTML content with <a
	 * href="http://tidy.sourceforge.net/">tidy html</a> library
	 * 
	 * @param htmlContent
	 *            The HTML content
	 * @param charset
	 *            The character set encoding
	 * @return The validated HTML content
	 */
	public static String validate(final String htmlContent, final String charset) {
		/*
		 * Obtain a new Tidy instance
		 */
		final Tidy tidy = new Tidy();
		/*
		 * Set desired config options using tidy setters
		 */
		tidy.setXHTML(true);
		tidy.setConfigurationFromFile(SystemConfig.getProperty(SystemConfig.Property.TidyConfiguration));
		tidy.setForceOutput(true);
		tidy.setOutputEncoding(charset);
		/*
		 * Suppress tidy outputs
		 */
		tidy.setShowErrors(0);
		tidy.setShowWarnings(false);
		tidy.setErrout(new PrintWriter(new StringWriter()));
		/*
		 * Run tidy, providing an input and output stream
		 */
		final StringWriter writer = new StringWriter(htmlContent.length());
		tidy.parse(new StringReader(htmlContent), writer);
		return writer.toString();
	}
}
