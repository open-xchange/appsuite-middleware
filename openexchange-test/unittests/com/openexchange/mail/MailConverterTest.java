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

package com.openexchange.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

/**
 * MailConverterTest
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailConverterTest extends AbstractMailTest {

	/**
	 * 
	 */
	public MailConverterTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailConverterTest(final String name) {
		super(name);
	}

	public void testMessageConverter() {
		try {
			final Session session = Session.getDefaultInstance(getDefaultSessionProperties());
			final IMAPStore imapStore = (IMAPStore) session.getStore("imap");
			try {
				imapStore.connect(getServer(), getPort(), getLogin(), getPassword());

				final IMAPFolder inbox = (IMAPFolder) imapStore.getFolder("INBOX");
				final Set<String> fullnames = new TreeSet<String>();
				fullnames.add("INBOX");
				getMailboxFolders(fullnames, inbox);

				final StringBuilder sb = new StringBuilder(256);

				for (String fullname : fullnames) {
					traverseFolderMessages(fullname, imapStore, sb);
				}
			} finally {
				imapStore.close();
			}
		} catch (final MessagingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void getMailboxFolders(final Set<String> fullnames, final Folder folder) throws MessagingException {
		final Folder[] folders = folder.list();
		for (int i = 0; i < folders.length; i++) {
			fullnames.add(folders[i].getFullName());
			getMailboxFolders(fullnames, folders[i]);
		}
	}

	private void traverseFolderMessages(final String fullname, final IMAPStore imapStore, final StringBuilder sb)
			throws MessagingException, IOException, Exception {
		final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(fullname);
		try {
			imapFolder.open(IMAPFolder.READ_ONLY);
		} catch (final Exception e) {
			System.err.println("Cannot open \"" + fullname + "\": " + e.getLocalizedMessage());
			e.printStackTrace();
			return;
		}
		try {
			final int count = imapFolder.getMessageCount();
			for (int i = 1; i <= count; i++) {
				final IMAPMessage imapMessage = (IMAPMessage) imapFolder.getMessage(i);
				sb.setLength(0);
				System.out.println(sb.append(fullname).append(" -> Message No.").append(imapMessage.getMessageNumber())
						.append('/').append(count));
				try {
					/*
					 * To mail message
					 */
					final MailMessage mail = MIMEMessageConverter.convertMessage(imapMessage);
					/*
					 * ... and back to JavaMail message
					 */
					/* final Message convertedMsg = */MIMEMessageConverter.convertMailMessage(mail);
				} catch (final Exception e) {
					final ByteArrayOutputStream out = new ByteArrayOutputStream(4 * 8192);
					System.err.println("Failed Message Conversion! Message's Source:\n");
					imapMessage.writeTo(out);
					System.err.println(new String(out.toByteArray()));
					throw e;
				}
			}
		} finally {
			try {
				imapFolder.close(false);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static final void dumpMsg(final StringBuilder sb, final Message msg) throws Exception {
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
		} catch (final MailException pex) {
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

	private static final String indentStr = "                                               ";

	private static int level;

	/**
	 * Print a, possibly indented, string.
	 */
	private static final void pr(final String s, final StringBuilder sb) {
		sb.append(indentStr.substring(0, level * 2));
		sb.append(s).append('\n');
	}
}
