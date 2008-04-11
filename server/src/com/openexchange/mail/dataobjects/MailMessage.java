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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.utils.DateUtils;
import com.openexchange.mail.utils.MessageUtility;

/**
 * {@link MailMessage} - Abstract super class for all {@link MailMessage}
 * subclasses.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailMessage extends MailPart implements Serializable, Cloneable {

	private static final long serialVersionUID = 8585899349289256569L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailMessage.class);

	/**
	 * This message has been answered. This flag is set by clients to indicate
	 * that this message has been answered to.
	 * 
	 * @value 1
	 */
	public static final int FLAG_ANSWERED = 1;

	/**
	 * This message is marked deleted. Clients set this flag to mark a message
	 * as deleted. The expunge operation on a folder removes all messages in
	 * that folder that are marked for deletion.
	 * 
	 * @value 2
	 */
	public static final int FLAG_DELETED = 2;

	/**
	 * This message is a draft. This flag is set by clients to indicate that the
	 * message is a draft message.
	 * 
	 * @value 4
	 */
	public static final int FLAG_DRAFT = 4;

	/**
	 * This message is flagged. No semantic is defined for this flag. Clients
	 * alter this flag.
	 * 
	 * @value 8
	 */
	public static final int FLAG_FLAGGED = 8;

	/**
	 * This message is recent. Folder implementations set this flag to indicate
	 * that this message is new to this folder, that is, it has arrived since
	 * the last time this folder was opened.
	 * 
	 * @value 16
	 * 
	 */
	public static final int FLAG_RECENT = 16;

	/**
	 * This message is seen. This flag is implicitly set by the implementation
	 * when the this Message's content is returned to the client in some form.
	 * 
	 * @value 32
	 */
	public static final int FLAG_SEEN = 32;

	/**
	 * A special flag that indicates that this folder supports user defined
	 * flags
	 * 
	 * @value 64
	 */
	public static final int FLAG_USER = 64;

	/**
	 * Virtual Spam flag
	 * 
	 * @value 128
	 */
	public static final int FLAG_SPAM = 128;

	/*
	 * ------------------- Priority ------------------------------
	 */
	/**
	 * Highest priority
	 */
	public static final int PRIORITY_HIGHEST = 1;

	/**
	 * High priority
	 */
	public static final int PRIORITY_HIGH = 2;

	/**
	 * Normal priority
	 */
	public static final int PRIORITY_NORMAL = 3;

	/**
	 * Low priority
	 */
	public static final int PRIORITY_LOW = 4;

	/**
	 * Lowest priority
	 */
	public static final int PRIORITY_LOWEST = 5;

	/*
	 * ------------------- Color Label ------------------------------
	 */
	/**
	 * The prefix for a mail message's color labels stored as a user flag
	 */
	public static final String COLOR_LABEL_PREFIX = "cl_";

	/**
	 * The <code>int</code> value for no color label
	 */
	public static final int COLOR_LABEL_NONE = 0;

	/**
	 * Determines the corresponding <code>int</code> value of a given color
	 * label's string representation.
	 * <p>
	 * A color label's string representation matches the pattern:<br>
	 * &lt;value-of-{@link #COLOR_LABEL_PREFIX}&gt;&lt;color-label-int-value&gt;
	 * 
	 * @param cl
	 *            The color label's string representation
	 * @return The color label's <code>int</code> value
	 * @throws MailException
	 */
	public static int getColorLabelIntValue(final String cl) throws MailException {
		if (!cl.startsWith(COLOR_LABEL_PREFIX)) {
			throw new MailException(MailException.Code.UNKNOWN_COLOR_LABEL, cl);
		}
		return Integer.parseInt(cl.substring(3));
	}

	/**
	 * Generates the color label's string representation from given
	 * <code>int</code> value.
	 * <p>
	 * A color label's string representation matches the pattern:<br>
	 * &lt;value-of-{@link #COLOR_LABEL_PREFIX}&gt;&lt;color-label-int-value&gt;
	 * 
	 * @param cl
	 *            The color label's <code>int</code> value
	 * @return The color abel's string representation
	 */
	public static String getColorLabelStringValue(final int cl) {
		return new StringBuilder(COLOR_LABEL_PREFIX).append(cl).toString();
	}

	private static final InternetAddress[] EMPTY_ADDRS = new InternetAddress[0];

	/**
	 * The flags
	 */
	private int flags;

	private boolean b_flags;

	/**
	 * The previous \Seen state
	 */
	private boolean prevSeen;

	private boolean b_prevSeen;

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
	 * The sent date (the <code>Date</code> header)
	 */
	private Date sentDate;

	private boolean b_sentDate;

	/**
	 * The (internal) received date
	 */
	private Date receivedDate;

	private boolean b_receivedDate;

	/**
	 * User flags
	 */
	private Set<HeaderName> userFlags;

	private boolean b_userFlags;

	/**
	 * The color label (set through an user flag)
	 */
	private int colorLabel = COLOR_LABEL_NONE;

	private boolean b_colorLabel;

	/**
	 * The priority (the <code>X-Priority</code> header)
	 */
	private int priority = PRIORITY_NORMAL;

	private boolean b_priority;

	/**
	 * The message reference (on reply or forward)
	 */
	private String msgref;

	private boolean b_msgref;

	/**
	 * The <code>Disposition-Notification-To</code> header
	 */
	private InternetAddress dispositionNotification;

	private boolean b_dispositionNotification;

	/**
	 * The message folder fullname/ID
	 */
	private String folder;

	private boolean b_folder;

	/**
	 * The folder's separator
	 */
	private char separator;

	private boolean b_separator;

	/**
	 * Whether an attachment is present or not
	 */
	private boolean hasAttachment;

	private boolean b_hasAttachment;

	/**
	 * Whether a VCard should be appended or not
	 */
	private boolean appendVCard;

	private boolean b_appendVCard;

	/**
	 * Default constructor
	 */
	protected MailMessage() {
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
			b_from = true;
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
		if (null == addrs) {
			b_from = true;
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
		if (!b_from) {
			final String fromStr = getHeader(MessageHeaders.HDR_FROM);
			if (fromStr == null) {
				return EMPTY_ADDRS;
			}
			try {
				addFrom(InternetAddress.parse(fromStr, true));
			} catch (final AddressException e) {
				LOG.error(e.getMessage(), e);
				return EMPTY_ADDRS;
			}
		}
		return from == null ? EMPTY_ADDRS : from.toArray(new InternetAddress[from.size()]);
	}

	/**
	 * Adds an email address to <i>To</i>
	 * 
	 * @param addr
	 *            The address
	 */
	public void addTo(final InternetAddress addr) {
		if (null == addr) {
			b_to = true;
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
		if (null == addrs) {
			b_to = true;
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
		if (!b_to) {
			final String toStr = getHeader(MessageHeaders.HDR_TO);
			if (toStr == null) {
				return EMPTY_ADDRS;
			}
			try {
				addTo(InternetAddress.parse(toStr, true));
			} catch (final AddressException e) {
				LOG.error(e.getMessage(), e);
				return EMPTY_ADDRS;
			}
		}
		return to == null ? EMPTY_ADDRS : to.toArray(new InternetAddress[to.size()]);
	}

	/**
	 * Adds an email address to <i>Cc</i>
	 * 
	 * @param addr
	 *            The address
	 */
	public void addCc(final InternetAddress addr) {
		if (null == addr) {
			b_cc = true;
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
		if (null == addrs) {
			b_cc = true;
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
		if (!b_cc) {
			final String ccStr = getHeader(MessageHeaders.HDR_CC);
			if (ccStr == null) {
				return EMPTY_ADDRS;
			}
			try {
				addCc(InternetAddress.parse(ccStr, true));
			} catch (final AddressException e) {
				LOG.error(e.getMessage(), e);
				return EMPTY_ADDRS;
			}
		}
		return cc == null ? EMPTY_ADDRS : cc.toArray(new InternetAddress[cc.size()]);
	}

	/**
	 * Adds an email address to <i>Bcc</i>
	 * 
	 * @param addr
	 *            The address
	 */
	public void addBcc(final InternetAddress addr) {
		if (null == addr) {
			b_bcc = true;
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
		if (null == addrs) {
			b_bcc = true;
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
		if (!b_bcc) {
			final String bccStr = getHeader(MessageHeaders.HDR_TO);
			if (bccStr == null) {
				return EMPTY_ADDRS;
			}
			try {
				addBcc(InternetAddress.parse(bccStr, true));
			} catch (final AddressException e) {
				LOG.error(e.getMessage(), e);
				return EMPTY_ADDRS;
			}
		}
		return bcc == null ? EMPTY_ADDRS : bcc.toArray(new InternetAddress[bcc.size()]);
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
	 * @return <code>true</code> if flag \ANSWERED is set; otherwise
	 *         <code>false</code>
	 */
	public boolean isAnswered() {
		return ((flags & FLAG_ANSWERED) == FLAG_ANSWERED);
	}

	/**
	 * @return <code>true</code> if flag \DELETED is set; otherwise
	 *         <code>false</code>
	 */
	public boolean isDeleted() {
		return ((flags & FLAG_DELETED) == FLAG_DELETED);
	}

	/**
	 * @return <code>true</code> if flag \DRAFT is set; otherwise
	 *         <code>false</code>
	 */
	public boolean isDraft() {
		return ((flags & FLAG_DRAFT) == FLAG_DRAFT);
	}

	/**
	 * @return <code>true</code> if flag \FLAGGED is set; otherwise
	 *         <code>false</code>
	 */
	public boolean isFlagged() {
		return ((flags & FLAG_FLAGGED) == FLAG_FLAGGED);
	}

	/**
	 * @return <code>true</code> if flag \RECENT is set; otherwise
	 *         <code>false</code>
	 */
	public boolean isRecent() {
		return ((flags & FLAG_RECENT) == FLAG_RECENT);
	}

	/**
	 * @return <code>true</code> if flag \SEEN is set; otherwise
	 *         <code>false</code>
	 */
	public boolean isSeen() {
		return ((flags & FLAG_SEEN) == FLAG_SEEN);
	}

	/**
	 * @return <code>true</code> if virtual spam flag is set; otherwise
	 *         <code>false</code>
	 */
	public boolean isSpam() {
		return ((flags & FLAG_SPAM) == FLAG_SPAM);
	}

	/**
	 * @return <code>true</code> if flag \USER is set; otherwise
	 *         <code>false</code>
	 */
	public boolean isUser() {
		return ((flags & FLAG_USER) == FLAG_USER);
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
	 * Sets a system flag
	 * 
	 * @param flag
	 *            The system flag to set
	 * @param enable
	 *            <code>true</code> to enable; otherwise <code>false</code>
	 * @throws MailException
	 *             If an illegal flag argument is specified
	 */
	public void setFlag(final int flag, final boolean enable) throws MailException {
		if (flag == 1 || (flag % 2) != 0) {
			throw new MailException(MailException.Code.ILLEGAL_FLAG_ARGUMENT, Integer.valueOf(flag));
		}
		flags = enable ? (flags | flag) : (flags & ~flag);
	}

	/**
	 * Gets the previous \Seen state.
	 * <p>
	 * This flag is used when writing the message later on. There a check is
	 * performed whether header <code>Disposition-Notification-To</code> is
	 * indicated or not.
	 * 
	 * @return the previous \Seen state
	 */
	public boolean isPrevSeen() {
		return prevSeen;
	}

	/**
	 * @return <code>true</code> if previous \Seen state is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsPrevSeen() {
		return b_prevSeen;
	}

	/**
	 * Removes the previous \Seen state
	 */
	public void removePrevSeen() {
		prevSeen = false;
		b_prevSeen = false;
	}

	/**
	 * Sets the previous \Seen state.
	 * <p>
	 * This flag is used when writing the message later on. There a check is
	 * performed whether header <code>Disposition-Notification-To</code> is
	 * indicated or not.
	 * 
	 * @param prevSeen
	 *            the previous \Seen state to set
	 */
	public void setPrevSeen(final boolean prevSeen) {
		this.prevSeen = prevSeen;
		b_prevSeen = true;
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
		if (!b_subject) {
			final String subjectStr = getHeader(MessageHeaders.HDR_SUBJECT);
			if (subjectStr != null) {
				setSubject(MessageUtility.decodeMultiEncodedHeader(subjectStr));
			}
		}
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
	 * Gets the sentDate
	 * 
	 * @return the sentDate
	 */
	public Date getSentDate() {
		if (!b_sentDate) {
			final String sentDateStr = getHeader(MessageHeaders.HDR_DATE);
			if (sentDateStr != null) {
				setSentDate(DateUtils.getDateRFC822(sentDateStr));
			}
		}
		return sentDate == null ? null : new Date(sentDate.getTime());
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
		this.sentDate = sentDate == null ? null : new Date(sentDate.getTime());
		b_sentDate = true;
	}

	/**
	 * Gets the receivedDate
	 * 
	 * @return the receivedDate
	 */
	public Date getReceivedDate() {
		return receivedDate == null ? null : new Date(receivedDate.getTime());
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
		this.receivedDate = receivedDate == null ? null : new Date(receivedDate.getTime());
		b_receivedDate = true;
	}

	/**
	 * Adds given user flag
	 * 
	 * @param userFlag
	 *            The user flag to add
	 */
	public void addUserFlag(final String userFlag) {
		if (userFlag == null) {
			return;
		} else if (userFlags == null) {
			userFlags = new HashSet<HeaderName>();
			b_userFlags = true;
		}
		userFlags.add(HeaderName.valueOf(userFlag));
	}

	/**
	 * Adds given user flags
	 * 
	 * @param userFlags
	 *            The user flags to add
	 */
	public void addUserFlags(final String[] userFlags) {
		if (userFlags == null) {
			return;
		} else if (this.userFlags == null) {
			this.userFlags = new HashSet<HeaderName>();
			b_userFlags = true;
		}
		for (int i = 0; i < userFlags.length; i++) {
			this.userFlags.add(HeaderName.valueOf(userFlags[i]));
		}
	}

	/**
	 * @return <code>true</code> if userFlags is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsUserFlags() {
		return b_userFlags;
	}

	/**
	 * Removes the userFlags
	 */
	public void removeUserFlags() {
		userFlags = null;
		b_userFlags = false;
	}

	private static final String[] EMPTY_UF = new String[0];

	/**
	 * Gets the user flags
	 * 
	 * @return The user flags
	 */
	public String[] getUserFlags() {
		if (containsUserFlags() && null != userFlags) {
			final int size = userFlags.size();
			final List<String> retval = new ArrayList<String>(size);
			final Iterator<HeaderName> iter = userFlags.iterator();
			for (int i = 0; i < size; i++) {
				retval.add(iter.next().toString());
			}
			return retval.toArray(new String[size]);
		}
		return EMPTY_UF;
	}

	/**
	 * Gets the colorLabel
	 * 
	 * @return the colorLabel
	 */
	public int getColorLabel() {
		return colorLabel;
	}

	/**
	 * @return <code>true</code> if colorLabel is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsColorLabel() {
		return b_colorLabel;
	}

	/**
	 * Removes the colorLabel
	 */
	public void removeColorLabel() {
		colorLabel = COLOR_LABEL_NONE;
		b_colorLabel = false;
	}

	/**
	 * Sets the colorLabel
	 * 
	 * @param colorLabel
	 *            the colorLabel to set
	 */
	public void setColorLabel(final int colorLabel) {
		this.colorLabel = colorLabel;
		b_colorLabel = true;
	}

	/**
	 * Gets the priority
	 * 
	 * @return the priority
	 */
	public int getPriority() {
		if (!b_priority) {
			final String prioStr = getHeader(MessageHeaders.HDR_X_PRIORITY);
			if (prioStr != null) {
				setPriority(MIMEMessageConverter.parsePriority(prioStr));
			}
		}
		return priority;
	}

	/**
	 * @return <code>true</code> if priority is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsPriority() {
		return b_priority;
	}

	/**
	 * Removes the priority
	 */
	public void removePriority() {
		priority = PRIORITY_NORMAL;
		b_priority = false;
	}

	/**
	 * Sets the priority
	 * 
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(final int priority) {
		this.priority = priority;
		b_priority = true;
	}

	/**
	 * Gets the msgref
	 * 
	 * @return the msgref
	 */
	public String getMsgref() {
		return msgref;
	}

	/**
	 * @return <code>true</code> if msgref is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsMsgref() {
		return b_msgref;
	}

	/**
	 * Removes the msgref
	 */
	public void removeMsgref() {
		msgref = null;
		b_msgref = false;
	}

	/**
	 * Sets the msgref
	 * 
	 * @param msgref
	 *            the msgref to set
	 */
	public void setMsgref(final String msgref) {
		this.msgref = msgref;
		b_msgref = true;
	}

	/**
	 * Gets the dispositionNotification
	 * 
	 * @return the dispositionNotification
	 */
	public InternetAddress getDispositionNotification() {
		if (!b_dispositionNotification) {
			final String dispNotTo = getHeader(MessageHeaders.HDR_DISP_NOT_TO);
			if (dispNotTo != null) {
				try {
					setDispositionNotification(new InternetAddress(dispNotTo, true));
				} catch (final AddressException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		return dispositionNotification;
	}

	/**
	 * @return <code>true</code> if dispositionNotification is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsDispositionNotification() {
		return b_dispositionNotification;
	}

	/**
	 * Removes the dispositionNotification
	 */
	public void removeDispositionNotification() {
		dispositionNotification = null;
		b_dispositionNotification = false;
	}

	/**
	 * Sets the dispositionNotification
	 * 
	 * @param dispositionNotification
	 *            the dispositionNotification to set
	 */
	public void setDispositionNotification(final InternetAddress dispositionNotification) {
		this.dispositionNotification = dispositionNotification;
		b_dispositionNotification = true;
	}

	/**
	 * Gets the folder
	 * 
	 * @return the folder
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * @return <code>true</code> if folder is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsFolder() {
		return b_folder;
	}

	/**
	 * Removes the folder
	 */
	public void removeFolder() {
		folder = null;
		b_folder = false;
	}

	/**
	 * Sets the folder
	 * 
	 * @param folder
	 *            the folder to set
	 */
	public void setFolder(final String folder) {
		this.folder = folder;
		b_folder = true;
	}

	/**
	 * Gets the hasAttachment
	 * 
	 * @return the hasAttachment
	 */
	public boolean hasAttachment() {
		return hasAttachment;
	}

	/**
	 * @return <code>true</code> if hasAttachment is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsHasAttachment() {
		return b_hasAttachment;
	}

	/**
	 * Removes the hasAttachment
	 */
	public void removeHasAttachment() {
		hasAttachment = false;
		b_hasAttachment = false;
	}

	/**
	 * Sets the hasAttachment
	 * 
	 * @param hasAttachment
	 *            the hasAttachment to set
	 */
	public void setHasAttachment(final boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
		b_hasAttachment = true;
	}

	@Override
	public Object clone() {
		final MailMessage clone = (MailMessage) super.clone();
		if (from != null) {
			clone.from = new HashSet<InternetAddress>(from);
		}
		if (to != null) {
			clone.to = new HashSet<InternetAddress>(to);
		}
		if (cc != null) {
			clone.cc = new HashSet<InternetAddress>(cc);
		}
		if (bcc != null) {
			clone.bcc = new HashSet<InternetAddress>(bcc);
		}
		if (receivedDate != null) {
			clone.receivedDate = new Date(receivedDate.getTime());
		}
		if (sentDate != null) {
			clone.sentDate = new Date(sentDate.getTime());
		}
		if (userFlags != null) {
			clone.userFlags = new HashSet<HeaderName>(userFlags);
		}
		return clone;
	}

	/**
	 * Gets the appendVCard
	 * 
	 * @return the appendVCard
	 */
	public boolean isAppendVCard() {
		return appendVCard;
	}

	/**
	 * @return <code>true</code> if appendVCard is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsAppendVCard() {
		return b_appendVCard;
	}

	/**
	 * Removes the appendVCard
	 */
	public void removeAppendVCard() {
		appendVCard = false;
		b_appendVCard = false;
	}

	/**
	 * Sets the appendVCard
	 * 
	 * @param appendVCard
	 *            the appendVCard to set
	 */
	public void setAppendVCard(final boolean appendVCard) {
		this.appendVCard = appendVCard;
		b_appendVCard = true;
	}

	/**
	 * Gets the separator
	 * 
	 * @return the separator
	 */
	public char getSeparator() {
		return separator;
	}

	/**
	 * @return <code>true</code> if separator is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSeparator() {
		return b_separator;
	}

	/**
	 * Removes the separator
	 */
	public void removeSeparator() {
		this.separator = '0';
		b_separator = false;
	}

	/**
	 * Sets the separator
	 * 
	 * @param separator
	 *            the separator to set
	 */
	public void setSeparator(final char separator) {
		this.separator = separator;
		b_separator = true;
	}

	/**
	 * Gets the mail path
	 * 
	 * @return The mail path
	 */
	public MailPath getMailPath() {
		return new MailPath(getFolder(), getMailId());
	}

	/**
	 * Gets the implementation-specific unique ID of this mail in its mail
	 * folder. The ID returned by this method is used in storages to refer to a
	 * mail.
	 * 
	 * @return The ID of this mail
	 */
	public abstract long getMailId();

	/**
	 * Sets the implementation-specific unique mail ID of this mail in its mail
	 * folder. The ID returned by this method is used in storages to refer to a
	 * mail.
	 * 
	 * @param id
	 *            The mail ID
	 */
	public abstract void setMailId(long id);

	/**
	 * Gets the number of unread messages
	 * 
	 * @return The number of unread messages
	 */
	public abstract int getUnreadMessages();

	/**
	 * Sets the number of unread messages
	 * 
	 * @param unreadMessages
	 *            The number of unread messages
	 */
	public abstract void setUnreadMessages(int unreadMessages);
}
