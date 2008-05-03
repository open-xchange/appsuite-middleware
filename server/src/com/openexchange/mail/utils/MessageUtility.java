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

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import com.openexchange.configuration.ServerConfig;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;

/**
 * {@link MessageUtility} - Provides various helper methods for message
 * processing
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MessageUtility {

	private static final String STR_EMPTY = "";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageUtility.class);

	/**
	 * No instantiation
	 */
	private MessageUtility() {
		super();
	}

	/**
	 * Reads the string out of MIME part's input stream. On first try the input
	 * stream retrieved by <code>javax.mail.Part.getInputStream()</code> is
	 * used. If an I/O error occurs (<code>java.io.IOException</code>) then
	 * the next try is with part's raw input stream. If everything fails an
	 * empty string is returned.
	 * 
	 * @param p -
	 *            the <code>javax.mail.Part</code> object
	 * @param ct -
	 *            the part's content type
	 * @return the string read from part's input stream or the empty string ""
	 *         if everything failed
	 * @throws MessagingException -
	 *             if an error occurs in part's getter methods
	 */
	public static String readMimePart(final Part p, final ContentType ct) throws MessagingException {
		/*
		 * Use specified charset if available else use default one
		 */
		String charset = ct.getCharsetParameter();
		if (null == charset) {
			charset = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
		}
		return readMimePart(p, charset);
	}

	/**
	 * Reads the string out of MIME part's input stream. On first try the input
	 * stream retrieved by <code>javax.mail.Part.getInputStream()</code> is
	 * used. If an I/O error occurs (<code>java.io.IOException</code>) then
	 * the next try is with part's raw input stream. If everything fails an
	 * empty string is returned.
	 * 
	 * @param p -
	 *            the <code>javax.mail.Part</code> object
	 * @param charset -
	 *            the charset
	 * @return the string read from part's input stream or the empty string ""
	 *         if everything failed
	 * @throws MessagingException -
	 *             if an error occurs in part's getter methods
	 */
	public static String readMimePart(final Part p, final String charset) throws MessagingException {
		try {
			return readStream(p.getInputStream(), charset);
		} catch (final IOException e) {
			/*
			 * Try to get data from raw input stream
			 */
			final InputStream rawIn;
			if (p instanceof MimeBodyPart) {
				rawIn = ((MimeBodyPart) p).getRawInputStream();
			} else if (p instanceof MimeMessage) {
				rawIn = ((MimeMessage) p).getRawInputStream();
			} else {
				/*
				 * Neither a MimeBodyPart nor a MimeMessage
				 */
				return STR_EMPTY;
			}
			try {
				return readStream(rawIn, charset);
			} catch (final IOException e1) {
				LOG.error(e1.getLocalizedMessage(), e1);
				return STR_EMPTY;
			}
		}
	}

	/**
	 * Reads the stream content from given mail part
	 * 
	 * @param mailPart
	 *            The mail part
	 * @param charset
	 *            The charset encoding used to generate a {@link String} object
	 *            from raw bytes
	 * @return the <code>String</code> read from mail part's stream
	 * @throws IOException
	 * @throws MailException
	 */
	public static String readMailPart(final MailPart mailPart, final String charset) throws IOException, MailException {
		return readStream(mailPart.getInputStream(), charset);
	}

	private static final int BUFSIZE = 8192; // 8K

	private static final int STRBLD_SIZE = 32768; // 32K

	/**
	 * Reads a string from given input stream using direct buffering
	 * 
	 * @param inStream -
	 *            the input stream
	 * @param charset -
	 *            the charset
	 * @return the <code>String</code> read from input stream
	 * @throws IOException -
	 *             if an I/O error occurs
	 */
	public static String readStream(final InputStream inStream, final String charset) throws IOException {
		InputStreamReader isr = null;
		try {
			int count = 0;
			final char[] c = new char[BUFSIZE];
			isr = new InputStreamReader(inStream, charset);
			if ((count = isr.read(c)) > 0) {
				final StringBuilder sb = new StringBuilder(STRBLD_SIZE);
				do {
					sb.append(c, 0, count);
				} while ((count = isr.read(c)) > 0);
				return sb.toString();
			}
			return STR_EMPTY;
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Unsupported encoding in a message detected and monitored.", e);
			mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
			return STR_EMPTY;
		} finally {
			if (null != isr) {
				try {
					isr.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	private static final Lock LOCK_DATE_FORMAT = new ReentrantLock();

	private static final SimpleDateFormat dateFormat;

	static {
		dateFormat = new SimpleDateFormat("dd'-'MMM'-'yyyy");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		final DateFormatSymbols dfs = dateFormat.getDateFormatSymbols();
		final String[] shortMonths = new String[12];
		shortMonths[Calendar.JANUARY] = "Jan";
		shortMonths[Calendar.FEBRUARY] = "Feb";
		shortMonths[Calendar.MARCH] = "Mar";
		shortMonths[Calendar.APRIL] = "Apr";
		shortMonths[Calendar.MAY] = "May";
		shortMonths[Calendar.JUNE] = "Jun";
		shortMonths[Calendar.JULY] = "Jul";
		shortMonths[Calendar.AUGUST] = "Aug";
		shortMonths[Calendar.SEPTEMBER] = "Sep";
		shortMonths[Calendar.OCTOBER] = "Oct";
		shortMonths[Calendar.NOVEMBER] = "Nov";
		shortMonths[Calendar.DECEMBER] = "Dec";
		dfs.setShortMonths(shortMonths);
		dateFormat.setDateFormatSymbols(dfs);
	}

	/**
	 * Formats given time argument to a date argument ready for being used in a
	 * IMAP VERSION 4rev1 <code>SEARCH</code> command (as per RFC 3501). Note
	 * that only day-related informations are allowed, other informations are
	 * discarded.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * SEARCH SINCE 01-Jan-2001
	 * </pre>
	 * 
	 * @param time
	 *            The time argument to format
	 * @return The RFC 3501 string representation of specified time
	 */
	public static String formatDateForIMAPSearch(final long time) {
		return formatDateForIMAPSearch(new Date(time));
	}

	/**
	 * Formats given date argument to a date argument ready for being used in a
	 * IMAP VERSION 4rev1 <code>SEARCH</code> command (as per RFC 3501). Note
	 * that only day-related informations are allowed, other informations are
	 * discarded.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * SEARCH SINCE 01-Jan-2001
	 * </pre>
	 * 
	 * @param date
	 *            The date to format
	 * @return The RFC 3501 string representation of specified time
	 */
	public static String formatDateForIMAPSearch(final Date date) {
		/*
		 * Only exclusive access to date format instance
		 */
		LOCK_DATE_FORMAT.lock();
		try {
			return dateFormat.format(date);
		} finally {
			LOCK_DATE_FORMAT.unlock();
		}
	}
}
