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

package com.openexchange.mail.dataobjects;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.mail.internet.InternetAddress;

/**
 * MailMessage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailMessage {

	private static final Iterator<Map.Entry<String, String>> EMPTY_ITER = new Iterator<Map.Entry<String, String>>() {
		/**
		 * @return <tt>true</tt> if the iterator has more elements.
		 */
		public boolean hasNext() {
			return false;
		}

		/**
		 * @return The next element in the iteration.
		 */
		public Entry<String, String> next() {
			return null;
		}

		/**
		 * Removes from the underlying collection the last element returned by
		 * the iterator (optional operation).
		 */
		public void remove() {
		}

	};

	private static final InternetAddress[] EMPTY_ADDRS = new InternetAddress[0];

	/**
	 * The flags
	 */
	private int flags;

	private boolean b_flags;

	/**
	 * IMAP uid
	 */
	private long uid;

	private boolean b_uid;

	/**
	 * Mail id (unique within mailbox)
	 */
	private long id;

	private boolean b_id;

	/**
	 * From addresses
	 */
	private Set<InternetAddress> from;

	private boolean b_from;

	/**
	 * To addresses
	 */
	private Set<InternetAddress> to;

	private boolean b_to;

	/**
	 * Cc addresses
	 */
	private Set<InternetAddress> cc;

	private boolean b_cc;

	/**
	 * Bcc addresses
	 */
	private Set<InternetAddress> bcc;

	private boolean b_bcc;

	/**
	 * The level in a communication thread
	 */
	private int threadLevel;

	private boolean b_threadLevel;

	/**
	 * The subject
	 */
	private String subject;

	private boolean b_subject;

	/**
	 * The size
	 */
	private int size = -1;

	private boolean b_size;

	/**
	 * The sent date
	 */
	private Date sentDate;

	private boolean b_sentDate;

	/**
	 * The received date
	 */
	private Date receivedDate;

	private boolean b_receivedDate;

	/**
	 * The headers (if not explicitely set in other fields)
	 */
	private Map<String, String> headers;

	private boolean b_headers;

	/**
	 * Default constructor
	 */
	public MailMessage() {
		super();
	}

	/**
	 * Adds an email address to <i>From</i>
	 * 
	 * @param addr
	 *            The address
	 */
	public void addFrom(final InternetAddress addr) {
		if (null == addr) {
			return;
		} else if (null == from) {
			from = new HashSet<InternetAddress>();
			b_from = true;
		}
		from.add(addr);
	}

	/**
	 * Adds email addresses to <i>From</i>
	 * 
	 * @param addrs
	 *            The addresses
	 */
	public void addFrom(final InternetAddress[] addrs) {
		if (null == addrs || addrs.length == 0) {
			return;
		} else if (null == from) {
			from = new HashSet<InternetAddress>();
			b_from = true;
		}
		from.addAll(Arrays.asList(addrs));
	}

	/**
	 * @return <code>true</code> if <i>From</i> is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsFrom() {
		return b_from;
	}

	/**
	 * Removes the <i>From</i> addresses
	 */
	public void removeFrom() {
		from = null;
		b_from = false;
	}

	/**
	 * @return The <i>From</i> addresses
	 */
	public InternetAddress[] getFrom() {
		if (null == from) {
			return EMPTY_ADDRS;
		}
		return from.toArray(new InternetAddress[from.size()]);
	}

	/**
	 * Adds an email address to <i>To</i>
	 * 
	 * @param addr
	 *            The address
	 */
	public void addTo(final InternetAddress addr) {
		if (null == addr) {
			return;
		} else if (null == to) {
			to = new HashSet<InternetAddress>();
			b_to = true;
		}
		to.add(addr);
	}

	/**
	 * Adds email addresses to <i>To</i>
	 * 
	 * @param addrs
	 *            The addresses
	 */
	public void addTo(final InternetAddress[] addrs) {
		if (null == addrs || addrs.length == 0) {
			return;
		} else if (null == to) {
			to = new HashSet<InternetAddress>();
			b_to = true;
		}
		to.addAll(Arrays.asList(addrs));
	}

	/**
	 * @return <code>true</code> if <i>To</i> is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsTo() {
		return b_to;
	}

	/**
	 * Removes the <i>To</i> addresses
	 */
	public void removeTo() {
		to = null;
		b_to = false;
	}

	/**
	 * @return The <i>To</i> addresses
	 */
	public InternetAddress[] getTo() {
		if (null == to) {
			return EMPTY_ADDRS;
		}
		return to.toArray(new InternetAddress[to.size()]);
	}

	/**
	 * Adds an email address to <i>Cc</i>
	 * 
	 * @param addr
	 *            The address
	 */
	public void addCc(final InternetAddress addr) {
		if (null == addr) {
			return;
		} else if (null == cc) {
			cc = new HashSet<InternetAddress>();
			b_cc = true;
		}
		cc.add(addr);
	}

	/**
	 * Adds email addresses to <i>Cc</i>
	 * 
	 * @param addrs
	 *            The addresses
	 */
	public void addCc(final InternetAddress[] addrs) {
		if (null == addrs || addrs.length == 0) {
			return;
		} else if (null == cc) {
			cc = new HashSet<InternetAddress>();
			b_cc = true;
		}
		cc.addAll(Arrays.asList(addrs));
	}

	/**
	 * @return <code>true</code> if <i>Cc</i> is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsCc() {
		return b_cc;
	}

	/**
	 * Removes the <i>Cc</i> addresses
	 */
	public void removeCc() {
		cc = null;
		b_cc = false;
	}

	/**
	 * @return The <i>Cc</i> addresses
	 */
	public InternetAddress[] getCc() {
		if (null == cc) {
			return EMPTY_ADDRS;
		}
		return cc.toArray(new InternetAddress[cc.size()]);
	}

	/**
	 * Adds an email address to <i>Bcc</i>
	 * 
	 * @param addr
	 *            The address
	 */
	public void addBcc(final InternetAddress addr) {
		if (null == addr) {
			return;
		} else if (null == bcc) {
			bcc = new HashSet<InternetAddress>();
			b_bcc = true;
		}
		bcc.add(addr);
	}

	/**
	 * Adds email addresses to <i>Bcc</i>
	 * 
	 * @param addrs
	 *            The addresses
	 */
	public void addBcc(final InternetAddress[] addrs) {
		if (null == addrs || addrs.length == 0) {
			return;
		} else if (null == bcc) {
			bcc = new HashSet<InternetAddress>();
			b_bcc = true;
		}
		bcc.addAll(Arrays.asList(addrs));
	}

	/**
	 * @return <code>true</code> if <i>Bcc</i> is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsBcc() {
		return b_bcc;
	}

	/**
	 * Removes the <i>Bcc</i> addresses
	 */
	public void removeBcc() {
		bcc = null;
		b_bcc = false;
	}

	/**
	 * @return The <i>Bcc</i> addresses
	 */
	public InternetAddress[] getBcc() {
		if (null == bcc) {
			return EMPTY_ADDRS;
		}
		return bcc.toArray(new InternetAddress[bcc.size()]);
	}

	/**
	 * Gets the flags
	 * 
	 * @return the flags
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * @return <code>true</code> if flags is set; otherwise <code>false</code>
	 */
	public boolean containsFlags() {
		return b_flags;
	}

	/**
	 * Removes the flags
	 */
	public void removeFlags() {
		flags = 0;
		b_flags = false;
	}

	/**
	 * Sets the flags
	 * 
	 * @param flags
	 *            the flags to set
	 */
	public void setFlags(final int flags) {
		this.flags = flags;
		b_flags = true;
	}

	/**
	 * Gets the uid
	 * 
	 * @return the uid
	 */
	public long getUid() {
		return uid;
	}

	/**
	 * @return <code>true</code> if uid is set; otherwise <code>false</code>
	 */
	public boolean containsUid() {
		return b_uid;
	}

	/**
	 * Removes the uid
	 */
	public void removeUid() {
		uid = 0;
		b_uid = false;
	}

	/**
	 * Sets the uid
	 * 
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(final long uid) {
		this.uid = uid;
		b_uid = true;
	}

	/**
	 * Gets the id
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return <code>true</code> if id is set; otherwise <code>false</code>
	 */
	public boolean containsId() {
		return b_id;
	}

	/**
	 * Removes the id
	 */
	public void removeId() {
		id = 0;
		b_id = false;
	}

	/**
	 * Sets the id
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(final long id) {
		this.id = id;
		b_id = true;
	}

	/**
	 * Gets the threadLevel
	 * 
	 * @return the threadLevel
	 */
	public int getThreadLevel() {
		return threadLevel;
	}

	/**
	 * @return <code>true</code> if threadLevel is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsThreadLevel() {
		return b_threadLevel;
	}

	/**
	 * Removes the threadLevel
	 */
	public void removeThreadLevel() {
		threadLevel = 0;
		b_threadLevel = false;
	}

	/**
	 * Sets the threadLevel
	 * 
	 * @param threadLevel
	 *            the threadLevel to set
	 */
	public void setThreadLevel(final int threadLevel) {
		this.threadLevel = threadLevel;
		b_threadLevel = true;
	}

	/**
	 * Gets the subject
	 * 
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @return <code>true</code> if subject is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSubject() {
		return b_subject;
	}

	/**
	 * Removes the subject
	 */
	public void removeSubject() {
		subject = null;
		b_subject = false;
	}

	/**
	 * Sets the subject
	 * 
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(final String subject) {
		this.subject = subject;
		b_subject = true;
	}

	/**
	 * Gets the size
	 * 
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return <code>true</code> if size is set; otherwise <code>false</code>
	 */
	public boolean containsSize() {
		return b_size;
	}

	/**
	 * Removes the size
	 */
	public void removeSize() {
		size = -1;
		b_size = false;
	}

	/**
	 * Sets the size
	 * 
	 * @param size
	 *            the size to set
	 */
	public void setSize(final int size) {
		this.size = size;
		b_size = true;
	}

	/**
	 * Gets the sentDate
	 * 
	 * @return the sentDate
	 */
	public Date getSentDate() {
		return sentDate;
	}

	/**
	 * @return <code>true</code> if sentDate is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSentDate() {
		return b_sentDate;
	}

	/**
	 * Removes the sentDate
	 */
	public void removeSentDate() {
		sentDate = null;
		b_sentDate = false;
	}

	/**
	 * Sets the sentDate
	 * 
	 * @param sentDate
	 *            the sentDate to set
	 */
	public void setSentDate(final Date sentDate) {
		this.sentDate = sentDate;
		b_sentDate = true;
	}

	/**
	 * Gets the receivedDate
	 * 
	 * @return the receivedDate
	 */
	public Date getReceivedDate() {
		return receivedDate;
	}

	/**
	 * @return <code>true</code> if receivedDate is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsReceivedDate() {
		return b_receivedDate;
	}

	/**
	 * Removes the receivedDate
	 */
	public void removeReceivedDate() {
		receivedDate = null;
		b_receivedDate = false;
	}

	/**
	 * Sets the receivedDate
	 * 
	 * @param receivedDate
	 *            the receivedDate to set
	 */
	public void setReceivedDate(final Date receivedDate) {
		this.receivedDate = receivedDate;
		b_receivedDate = true;
	}

	/**
	 * Adds a header
	 * 
	 * @param name
	 *            The header name
	 * @param value
	 *            The header value
	 */
	public void addHeader(final String name, final String value) {
		if (null == name) {
			throw new IllegalArgumentException("Header name must not be null");
		} else if (value == null) {
			/*
			 * Don't need to put a null value
			 */
			return;
		} else if (null == headers) {
			headers = new HashMap<String, String>();
			b_headers = true;
		}
		headers.put(name, value);
	}

	/**
	 * @return <code>true</code> if headers is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsHeaders() {
		return b_headers;
	}

	/**
	 * Removes the headers
	 */
	public void removeHeaders() {
		headers = null;
		b_headers = false;
	}

	public int getHeadersSize() {
		if (null == headers) {
			return 0;
		}
		return headers.size();
	}

	public Iterator<Map.Entry<String, String>> getHeadersIterator() {
		if (null == headers) {
			return EMPTY_ITER;
		}
		return headers.entrySet().iterator();
	}
}
