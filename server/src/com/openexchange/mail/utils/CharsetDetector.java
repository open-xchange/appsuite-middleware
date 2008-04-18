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

package com.openexchange.mail.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 * {@link CharsetDetector} - A charset detector based on <a
 * href="http://jchardet.sourceforge.net/">jcharset</a> library.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CharsetDetector {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(CharsetDetector.class);

	private static final String STR_US_ASCII = "US-ASCII";

	/**
	 * Initializes a new {@link CharsetDetector}
	 */
	private CharsetDetector() {
		super();
	}

	/**
	 * Detects the charset of specified input stream's data.
	 * <p>
	 * <b>Note</b>: Specified input stream is going to be closed in this
	 * method.
	 * 
	 * @param in
	 *            The input stream to examine
	 * @return The detected charset or <i>US-ASCII</i> if no matching/supported
	 *         charset could be found
	 */
	public static String detectCharset(final InputStream in) {
		if (null == in) {
			throw new IllegalArgumentException("input stream is null");
		}
		final nsDetector det = new nsDetector(nsPSMDetector.ALL);
		/*
		 * Set an observer: The Notify() will be called when a matching charset
		 * is found.
		 */
		final CharsetDetectionObserver observer = new CharsetDetectionObserver();
		det.Init(observer);
		try {
			try {
				final byte[] buf = new byte[1024];
				int len;
				boolean done = false;
				boolean isAscii = true;

				while ((len = in.read(buf, 0, buf.length)) != -1) {
					/*
					 * Check if the stream is only ascii.
					 */
					if (isAscii) {
						isAscii = det.isAscii(buf, len);
					}
					/*
					 * DoIt if non-ascii and not done yet.
					 */
					if (!isAscii && !done) {
						done = det.DoIt(buf, len, false);
					}
				}
				det.DataEnd();
				/*
				 * Check if content is ascii
				 */
				if (isAscii) {
					return STR_US_ASCII;
				}
				{
					/*
					 * Check observer
					 */
					final String charset = observer.getCharset();
					if (null != charset && Charset.isSupported(charset)) {
						return charset;
					}
				}
				/*
				 * Choose first possible charset
				 */
				final String prob[] = det.getProbableCharsets();
				for (int i = 0; i < prob.length; i++) {
					if (Charset.isSupported(prob[i])) {
						return prob[i];
					}
				}
				return STR_US_ASCII;
			} finally {
				try {
					in.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		} catch (final IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return STR_US_ASCII;
		}
	}

	/**
	 * {@link CharsetDetectionObserver} - A charset detection observer according
	 * to <a href="http://jchardet.sourceforge.net/">jcharset</a> API
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class CharsetDetectionObserver implements nsICharsetDetectionObserver {

		private String charset;

		/**
		 * Initializes a new {@link CharsetDetectionObserver}
		 */
		public CharsetDetectionObserver() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.mozilla.intl.chardet.nsICharsetDetectionObserver#Notify(java.lang.String)
		 */
		public void Notify(final String charset) {
			this.charset = charset;
		}

		/**
		 * @return The charset applied to this observer
		 */
		public String getCharset() {
			return charset;
		}
	}
}
