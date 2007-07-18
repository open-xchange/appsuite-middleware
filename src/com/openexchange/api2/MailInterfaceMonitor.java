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



package com.openexchange.api2;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

import com.openexchange.ajax.Mail;
import com.openexchange.imap.connection.DefaultIMAPConnection;
import com.openexchange.tools.mail.ContentType;
import com.sun.mail.imap.IMAPFolder;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailInterfaceMonitor implements MailInterfaceMonitorMBean {

	private static final int USE_TIME_COUNT = 1000;

	private final long[] avgUseTimeArr;

	private int avgUseTimePointer;

	private long maxUseTime;

	private long minUseTime = Long.MAX_VALUE;

	private int numBrokenConnections;

	private int numTimeoutConnections;

	private int numSuccessfulLogins;

	private int numFailedLogins;
	
	private int numActive;

	private final Lock useTimeLock = new ReentrantLock();
	
	private final Map<String, Integer> unsupportedEnc;

	/**
	 * Constructor
	 */
	public MailInterfaceMonitor() {
		super();
		avgUseTimeArr = new long[USE_TIME_COUNT];
		unsupportedEnc = new HashMap<String, Integer>();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumActive()
	 */
	public int getNumActive() {
		return numActive;
	}
	
	public void changeNumActive(final boolean increment) {
		numActive += increment ? 1 : -1;
		if (numActive < 0) {
			numActive = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getAvgUseTime()
	 */
	public double getAvgUseTime() {
		long duration = 0;
		for (int i = 0; i < avgUseTimeArr.length; i++) {
			duration += avgUseTimeArr[i];
		}
		return (duration / (double) avgUseTimeArr.length);
	}

	/**
	 * Adds given use time to average use time array and invokes the
	 * setMaxUseTime() and setMinUseTime() methods
	 */
	public void addUseTime(final long time) {
		if (useTimeLock.tryLock()) {
			/*
			 * Add use time only when lock could be acquired
			 */
			try {
				avgUseTimeArr[avgUseTimePointer++] = time;
				avgUseTimePointer = avgUseTimePointer % avgUseTimeArr.length;
				setMaxUseTime(time);
				setMinUseTime(time);
			} finally {
				useTimeLock.unlock();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getMaxUseTime()
	 */
	public long getMaxUseTime() {
		return maxUseTime;
	}

	/**
	 * Sets the max use time to the maximum of given <code>maxUseTime</code>
	 * and existing value
	 */
	private final void setMaxUseTime(final long maxUseTime) {
		this.maxUseTime = Math.max(maxUseTime, this.maxUseTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetMaxUseTime()
	 */
	public void resetMaxUseTime() {
		maxUseTime = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getMinUseTime()
	 */
	public long getMinUseTime() {
		return minUseTime;
	}

	private final void setMinUseTime(final long minUseTime) {
		this.minUseTime = Math.min(minUseTime, this.minUseTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetMinUseTime()
	 */
	public void resetMinUseTime() {
		minUseTime = Long.MAX_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumBrokenConnections()
	 */
	public int getNumBrokenConnections() {
		return numBrokenConnections;
	}

	/**
	 * Changes number of broken connections
	 */
	public void changeNumBrokenConnections(final boolean increment) {
		numBrokenConnections += increment ? 1 : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumTimeoutConnections()
	 */
	public int getNumTimeoutConnections() {
		return numTimeoutConnections;
	}

	/**
	 * Changes number of timed-out connections
	 */
	public void changeNumTimeoutConnections(final boolean increment) {
		numTimeoutConnections += increment ? 1 : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumSuccessfulLogins()
	 */
	public int getNumSuccessfulLogins() {
		return numSuccessfulLogins;
	}

	/**
	 * Changes number of successful logins
	 */
	public void changeNumSuccessfulLogins(final boolean increment) {
		numSuccessfulLogins += increment ? 1 : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getNumFailedLogins()
	 */
	public int getNumFailedLogins() {
		return numFailedLogins;
	}

	/**
	 * Changes number of failes logins
	 */
	public void changeNumFailedLogins(final boolean increment) {
		numFailedLogins += increment ? 1 : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetNumBrokenConnections()
	 */
	public void resetNumBrokenConnections() {
		numBrokenConnections = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetNumTimeoutConnections()
	 */
	public void resetNumTimeoutConnections() {
		numTimeoutConnections = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetNumSuccessfulLogins()
	 */
	public void resetNumSuccessfulLogins() {
		numSuccessfulLogins = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#resetNumFailedLogins()
	 */
	public void resetNumFailedLogins() {
		numFailedLogins = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getUnsupportedEncodingExceptions()
	 */
	public String getUnsupportedEncodingExceptions() {
		final int size = unsupportedEnc.size();
		if (size == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(100);
		final Iterator<Entry<String, Integer>> iter = unsupportedEnc.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			final Entry<String, Integer> entry = iter.next();
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" times");
		}
		return sb.toString();
	}

	/**
	 * Adds an occurence of an unsupported encoding
	 * 
	 * @param encoding -
	 *            the unsupported encoding
	 */
	public void addUnsupportedEncodingExceptions(final String encoding) {
		final String key = encoding.toLowerCase(Locale.ENGLISH);
		final Integer num = unsupportedEnc.get(key);
		if (null == num) {
			unsupportedEnc.put(key, Integer.valueOf(1));
		} else {
			unsupportedEnc.put(key, Integer.valueOf(num.intValue() + 1));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.MailInterfaceMonitorMBean#getMessage(java.lang.String,
	 *      int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getMessage(final String imapServer, final int imapPort, final String login, final String password,
			final String msgUID) {
		Mail.MailIdentifier mailIdentifier = null;
		try {
			mailIdentifier = new Mail.MailIdentifier(msgUID);
		} catch (OXException e) {
			return "Invalid Message UID. Please specify UID in correct pattern: [path-to-folder]/[UID] (e.g. INBOX/12)";
		}
		try {
			final StringBuilder sb = new StringBuilder();
			DefaultIMAPConnection dic = null;
			Store store = null;
			IMAPFolder f = null;
			try {
				final Properties props = System.getProperties();
				props.put("mail.mime.base64.ignoreerrors", "true");
				dic = new DefaultIMAPConnection();
				dic.setProperties(props);
				dic.setImapServer(imapServer, imapPort < 0 ? 143 : imapPort);
				dic.setUsername(login);
				dic.setPassword(password);
				try {
					dic.connect();
					store = dic.getIMAPStore();
				} catch (AuthenticationFailedException e) {
					return "Wrong credentials";
				}
				f = (IMAPFolder) store.getFolder(mailIdentifier.getFolder());
				f.open(Folder.READ_ONLY);
				final Message msg = f.getMessageByUID(mailIdentifier.getMsgUID());
				dumpMsg(sb, msg);
			} finally {
				if (f != null && f.isOpen()) {
					f.close(false);
				}
				if (dic != null) {
					dic.close();
				}
			}
			return sb.toString();
		} catch (MessagingException e) {
			return e.getMessage();
		} catch (Throwable t) {
			return "Message could not be dumped: " + t.getMessage();
		}
	}
	
	private final void dumpMsg(final StringBuilder sb, final Message msg) throws Exception {
		dumpPart(msg, sb);
	}
	
	private static final String SEPERATOR = "---------------------------";
	
	private static void dumpPart(final Part p, final StringBuilder out) throws Exception {
		if (p instanceof Message) {
			dumpEnvelope((Message) p, out);
		}
		/**
		 * Dump input stream ..
		 * 
		 * InputStream is = p.getInputStream(); // If "is" is not already
		 * buffered, wrap a BufferedInputStream // around it. if (!(is
		 * instanceof BufferedInputStream)) is = new BufferedInputStream(is);
		 * int c; while ((c = is.read()) != -1) System.out.write(c);
		 * 
		 */
		final String ct = p.getContentType();
		try {
			pr("CONTENT-TYPE: " + (new ContentType(ct)).toString(), out);
		} catch (OXException pex) {
			pr("BAD CONTENT-TYPE: " + ct, out);
		}
		final String filename = p.getFileName();
		if (filename != null) {
			pr("FILENAME: " + filename, out);
		}
		/*
		 * Using isMimeType to determine the content type avoids fetching the
		 * actual content data until we need it.
		 */
		if (p.isMimeType("text/plain")) {
			pr("This is plain text", out);
			pr(SEPERATOR, out);
			pr((String) p.getContent(), out);
		} else if (p.isMimeType("multipart/*")) {
			pr("This is a Multipart", out);
			pr(SEPERATOR, out);
			final Multipart mp = (Multipart) p.getContent();
			level++;
			final int count = mp.getCount();
			for (int i = 0; i < count; i++) {
				dumpPart(mp.getBodyPart(i), out);
			}
			level--;
		} else if (p.isMimeType("message/rfc822")) {
			pr("This is a Nested Message", out);
			pr(SEPERATOR, out);
			level++;
			dumpPart((Part) p.getContent(), out);
			level--;
		} else {
			/*
			 * If we actually want to see the data, and it's not a MIME type we
			 * know, fetch it and check its Java type.
			 */
			final Object o = p.getContent();
			if (o instanceof String) {
				pr("This is a string", out);
				pr(SEPERATOR, out);
				System.out.println((String) o);
			} else if (o instanceof InputStream) {
				pr("This is just an input stream", out);
				pr(SEPERATOR, out);
				final InputStream is = (InputStream) o;
				int c;
				while ((c = is.read()) != -1) {
					System.out.write(c);
				}
			} else {
				pr("This is an unknown type", out);
				pr(SEPERATOR, out);
				pr(o.toString(), out);
			}
		}
	}
	
	private static final void dumpEnvelope(final Message m, final StringBuilder out) throws Exception {
		pr("This is the message envelope", out);
		pr(SEPERATOR, out);
		Address[] a;
		// FROM
		if ((a = m.getFrom()) != null) {
			for (int j = 0; j < a.length; j++) {
				pr("FROM: " + a[j].toString(), out);
			}
		}
		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
			for (int j = 0; j < a.length; j++) {
				pr("TO: " + a[j].toString(), out);
				final InternetAddress ia = (InternetAddress) a[j];
				if (ia.isGroup()) {
					final InternetAddress[] aa = ia.getGroup(false);
					for (int k = 0; k < aa.length; k++) {
						pr("  GROUP: " + aa[k].toString(), out);
					}
				}
			}
		}
		// SUBJECT
		pr("SUBJECT: " + m.getSubject(), out);
		// DATE
		final Date d = m.getSentDate();
		pr("SendDate: " + (d != null ? d.toString() : "UNKNOWN"), out);
		// FLAGS
		final Flags flags = m.getFlags();
		final StringBuilder sb = new StringBuilder();
		final Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags
		boolean first = true;
		for (int i = 0; i < sf.length; i++) {
			String s;
			final Flags.Flag f = sf[i];
			if (f == Flags.Flag.ANSWERED) {
				s = "\\Answered";
			} else if (f == Flags.Flag.DELETED) {
				s = "\\Deleted";
			} else if (f == Flags.Flag.DRAFT) {
				s = "\\Draft";
			} else if (f == Flags.Flag.FLAGGED) {
				s = "\\Flagged";
			} else if (f == Flags.Flag.RECENT) {
				s = "\\Recent";
			} else if (f == Flags.Flag.SEEN) {
				s = "\\Seen";
			} else {
				continue; // skip it
			}
			if (first) {
				first = false;
			} else {
				sb.append(' ');
			}
			sb.append(s);
		}
		final String[] uf = flags.getUserFlags(); // get the user flag strings
		for (int i = 0; i < uf.length; i++) {
			if (first) {
				first = false;
			} else {
				sb.append(' ');
			}
			sb.append(uf[i]);
		}
		pr("FLAGS: " + sb.toString(), out);
		// X-MAILER
		final String[] hdrs = m.getHeader("X-Mailer");
		if (hdrs != null) {
			pr("X-Mailer: " + hdrs[0], out);
		} else {
			pr("X-Mailer NOT available", out);
		}
	}
	
	private static String indentStr = "                                               ";

	private static int level;
	
	/**
	 * Print a, possibly indented, string.
	 */
	private static final void pr(final String s, final StringBuilder sb) {
		sb.append(indentStr.substring(0, level * 2));
		sb.append(s).append('\n');
	}
	

}
