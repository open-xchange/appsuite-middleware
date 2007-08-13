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

package com.openexchange.imap;

import static com.openexchange.imap.UserSettingMailStorage.getInstance;

import java.io.Serializable;
import java.sql.Connection;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;

/**
 * UserSettingMail
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class UserSettingMail implements DeleteListener, Cloneable, Serializable {

	public static final class Signature implements Cloneable, Serializable {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 357223875887317509L;

		private String id;

		private String signature;

		public Signature(final String id, final String signature) {
			this.id = id;
			this.signature = signature;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone() {
			try {
				final Signature clone = (Signature) super.clone();
				clone.id = this.id;
				clone.signature = this.signature;
				return clone;
			} catch (final CloneNotSupportedException e) {
				/*
				 * Cannot occur since we are cloneable
				 */
				LOG.error(e.getMessage(), e);
				throw new InternalError(e.getMessage());
			}
		}

		public String getId() {
			return id;
		}

		public String getSignature() {
			return signature;
		}

	}

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5787223065275414178L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UserSettingMail.class);

	/*
	 * Integer constants for on/off options
	 */
	/**
	 * If this bit is set, html content is displayed on message display
	 */
	public static final int INT_DISPLAY_HTML_INLINE_CONTENT = 1;

	/**
	 * If this bit is set, the quote levels of a plain text message are
	 * colorized
	 */
	public static final int INT_USE_COLOR_QUOTE = 2;

	/**
	 * If this bit is set, emoticons like <tt>:-)</tt> are replaced with a
	 * little graphic
	 */
	public static final int INT_SHOW_GRAPHIC_EMOTICONS = 4;

	/**
	 * If this bit is set, no copy of a deleted message is created in default
	 * "trash" folder. The message is hard deleted and no more present at all.
	 */
	public static final int INT_HARD_DELETE_MSGS = 8;

	/**
	 * If this bit is set, a forwarded message is appended as an
	 * <tt>message/rfc822</tt> attachment instead of an inline forward
	 */
	public static final int INT_FORWARD_AS_ATTACHMENT = 16;

	/**
	 * If this bit is set, a VCard created from user's contact data is appended
	 * to a sent message
	 */
	public static final int INT_APPEND_VCARD = 32;

	/**
	 * If this bit is set, the user is notified if a message contains a read
	 * acknowledgement
	 */
	public static final int INT_NOTIFY_ON_READ_ACK = 64;

	/**
	 * This constant is currently not in use
	 */
	public static final int INT_MSG_PREVIEW = 128;

	/**
	 * If this bit is set, the user will receive notification messages on
	 * appointment events (creation, deletion & change)
	 */
	public static final int INT_NOTIFY_APPOINTMENTS = 256;

	/**
	 * If this bit is set, the user will receive notification messages on task
	 * events (creation, deletion & change)
	 */
	public static final int INT_NOTIFY_TASKS = 512;

	/**
	 * If this bit is set, no message body text is extracted (and dispalyed)
	 * from the message to which the user replies
	 */
	public static final int INT_IGNORE_ORIGINAL_TEXT_ON_REPLY = 1024;

	/**
	 * If this bit is set, no copy of a sent mail is created in default "sent"
	 * folder
	 */
	public static final int INT_NO_COPY_INTO_SENT_FOLDER = 2048;

	/**
	 * If this bit is set, the spam feature is enabled
	 */
	public static final int INT_SPAM_ENABLED = 4096;

	/**
	 * If this bit is set, only plain text is allowed when composing
	 * reply/forward messages. The user will see the html2text converted content
	 * when replying to/inline-forwarding a html message.
	 */
	public static final int INT_TEXT_ONLY_COMPOSE = 8192;

	/*
	 * Other constants
	 */
	public static final int MSG_FORMAT_TEXT_ONLY = 1;

	public static final int MSG_FORMAT_HTML_ONLY = 2;

	public static final int MSG_FORMAT_BOTH = 3;

	public static final String STD_TRASH = "Trash";

	public static final String STD_DRAFTS = "Drafts";

	public static final String STD_SENT = "Sent";

	public static final String STD_SPAM = "Spam";

	public static final String STD_CONFIRMED_SPAM = "Confirmed Spam";

	public static final String STD_CONFIRMED_HAM = "Confirmed Ham";

	private boolean modifiedDuringSession;

	private boolean displayHtmlInlineContent;

	private boolean useColorQuote;

	private boolean showGraphicEmoticons;

	private boolean hardDeleteMsgs;

	private boolean forwardAsAttachment;

	private boolean appendVCard;

	private boolean notifyOnReadAck;

	private boolean notifyAppointments;

	private boolean notifyTasks;

	private boolean msgPreview;

	private boolean ignoreOriginalMailTextOnReply;

	private boolean noCopyIntoStandardSentFolder;

	private boolean spamEnabled;

	private boolean textOnlyCompose;

	private Signature[] signatures;

	private String sendAddr;

	private String replyToAddr;

	private int msgFormat = MSG_FORMAT_TEXT_ONLY;

	private String[] displayMsgHeaders;

	private int autoLinebreak = 80;

	private String stdTrashName;

	private String stdDraftsName;

	private String stdSentName;

	private String stdSpamName;

	private String confirmedSpam;

	private String confirmedHam;

	private long uploadQuota;

	private long uploadQuotaPerFile;

	public UserSettingMail() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			final UserSettingMail clone = (UserSettingMail) super.clone();
			if (displayMsgHeaders != null) {
				clone.displayMsgHeaders = new String[displayMsgHeaders.length];
				System.arraycopy(displayMsgHeaders, 0, clone.displayMsgHeaders, 0, displayMsgHeaders.length);
			}
			if (signatures != null) {
				clone.signatures = new Signature[signatures.length];
				for (int i = 0; i < signatures.length; i++) {
					clone.signatures[i] = (Signature) signatures[i].clone();
				}
			}
			return clone;
		} catch (final CloneNotSupportedException e) {
			LOG.error(e.getMessage(), e);
			throw new InternalError(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.delete.DeleteListener#deletePerformed(com.openexchange.groupware.delete.DeleteEvent,
	 *      java.sql.Connection, java.sql.Connection)
	 */
	public void deletePerformed(final DeleteEvent delEvent, final Connection readCon, final Connection writeCon)
			throws DeleteFailedException {
		if (delEvent.getType() == DeleteEvent.TYPE_USER) {
			try {
				getInstance().deleteUserSettingMail(delEvent.getId(), delEvent.getContext(), writeCon);
			} catch (final OXException e) {
				throw new DeleteFailedException(e);
			}
		}
	}

	public int getAutoLinebreak() {
		return autoLinebreak;
	}

	/**
	 * Generates a bit pattern from this settings
	 * 
	 * @return a bit pattern from this settings
	 */
	public int getBitsValue() {
		int retval = 0;
		retval = displayHtmlInlineContent ? (retval | INT_DISPLAY_HTML_INLINE_CONTENT) : retval;
		retval = useColorQuote ? (retval | INT_USE_COLOR_QUOTE) : retval;
		retval = showGraphicEmoticons ? (retval | INT_SHOW_GRAPHIC_EMOTICONS) : retval;
		retval = hardDeleteMsgs ? (retval | INT_HARD_DELETE_MSGS) : retval;
		retval = forwardAsAttachment ? (retval | INT_FORWARD_AS_ATTACHMENT) : retval;
		retval = appendVCard ? (retval | INT_APPEND_VCARD) : retval;
		retval = notifyOnReadAck ? (retval | INT_NOTIFY_ON_READ_ACK) : retval;
		retval = msgPreview ? (retval | INT_MSG_PREVIEW) : retval;
		retval = notifyAppointments ? (retval | INT_NOTIFY_APPOINTMENTS) : retval;
		retval = notifyTasks ? (retval | INT_NOTIFY_TASKS) : retval;
		retval = ignoreOriginalMailTextOnReply ? (retval | INT_IGNORE_ORIGINAL_TEXT_ON_REPLY) : retval;
		retval = noCopyIntoStandardSentFolder ? (retval | INT_NO_COPY_INTO_SENT_FOLDER) : retval;
		retval = spamEnabled ? (retval | INT_SPAM_ENABLED) : retval;
		retval = textOnlyCompose ? (retval | INT_TEXT_ONLY_COMPOSE) : retval;
		return retval;
	}

	public String getConfirmedHam() {
		return confirmedHam;
	}

	public String getConfirmedSpam() {
		return confirmedSpam;
	}

	public String[] getDisplayMsgHeaders() {
		if (displayMsgHeaders == null) {
			return null;
		}
		final String[] retval = new String[displayMsgHeaders.length];
		System.arraycopy(displayMsgHeaders, 0, retval, 0, displayMsgHeaders.length);
		return retval;
	}

	public int getMsgFormat() {
		return msgFormat;
	}

	public String getReplyToAddr() {
		return replyToAddr;
	}

	public String getSendAddr() {
		return sendAddr;
	}

	public Signature[] getSignatures() {
		if (signatures == null) {
			return null;
		}
		final Signature[] retval = new Signature[signatures.length];
		System.arraycopy(signatures, 0, retval, 0, signatures.length);
		return retval;
	}

	public String getStdDraftsName() {
		return stdDraftsName;
	}

	public String getStdSentName() {
		return stdSentName;
	}

	public String getStdSpamName() {
		return stdSpamName;
	}

	public String getStdTrashName() {
		return stdTrashName;
	}

	public long getUploadQuota() {
		return uploadQuota;
	}

	public long getUploadQuotaPerFile() {
		return uploadQuotaPerFile;
	}

	public boolean isAppendVCard() {
		return appendVCard;
	}

	public boolean isDisplayHtmlInlineContent() {
		return displayHtmlInlineContent;
	}

	public boolean isForwardAsAttachment() {
		return forwardAsAttachment;
	}

	public boolean isHardDeleteMsgs() {
		return hardDeleteMsgs;
	}

	public boolean isIgnoreOriginalMailTextOnReply() {
		return ignoreOriginalMailTextOnReply;
	}

	public boolean isModifiedDuringSession() {
		return modifiedDuringSession;
	}

	public boolean isMsgPreview() {
		return msgPreview;
	}

	public boolean isNoCopyIntoStandardSentFolder() {
		return noCopyIntoStandardSentFolder;
	}

	public boolean isNotifyAppointments() {
		return notifyAppointments;
	}

	public boolean isNotifyOnReadAck() {
		return notifyOnReadAck;
	}

	public boolean isNotifyTasks() {
		return notifyTasks;
	}

	public boolean isShowGraphicEmoticons() {
		return showGraphicEmoticons;
	}

	/**
	 * @return <code>true</code> if both global property for spam enablement
	 *         <small><b>AND</b></small> user-defined property for spam
	 *         enablement are turned on; otherwise <code>false</code>
	 * @throws IMAPException
	 */
	public boolean isSpamEnabled() throws IMAPException {
		return (IMAPProperties.isSpamEnabled() && spamEnabled);
	}

	public boolean isTextOnlyCompose() {
		return textOnlyCompose;
	}

	public boolean isUseColorQuote() {
		return useColorQuote;
	}

	/**
	 * Parses given bit pattern and applies it to this settings
	 * 
	 * @param onOffOptions
	 *            The bit pattern
	 */
	public void parseBits(final int onOffOptions) {
		displayHtmlInlineContent = ((onOffOptions & INT_DISPLAY_HTML_INLINE_CONTENT) == INT_DISPLAY_HTML_INLINE_CONTENT);
		useColorQuote = ((onOffOptions & INT_USE_COLOR_QUOTE) == INT_USE_COLOR_QUOTE);
		showGraphicEmoticons = ((onOffOptions & INT_SHOW_GRAPHIC_EMOTICONS) == INT_SHOW_GRAPHIC_EMOTICONS);
		hardDeleteMsgs = ((onOffOptions & INT_HARD_DELETE_MSGS) == INT_HARD_DELETE_MSGS);
		forwardAsAttachment = ((onOffOptions & INT_FORWARD_AS_ATTACHMENT) == INT_FORWARD_AS_ATTACHMENT);
		appendVCard = ((onOffOptions & INT_APPEND_VCARD) == INT_APPEND_VCARD);
		notifyOnReadAck = ((onOffOptions & INT_NOTIFY_ON_READ_ACK) == INT_NOTIFY_ON_READ_ACK);
		msgPreview = ((onOffOptions & INT_MSG_PREVIEW) == INT_MSG_PREVIEW);
		notifyAppointments = ((onOffOptions & INT_NOTIFY_APPOINTMENTS) == INT_NOTIFY_APPOINTMENTS);
		notifyTasks = ((onOffOptions & INT_NOTIFY_TASKS) == INT_NOTIFY_TASKS);
		ignoreOriginalMailTextOnReply = ((onOffOptions & INT_IGNORE_ORIGINAL_TEXT_ON_REPLY) == INT_IGNORE_ORIGINAL_TEXT_ON_REPLY);
		noCopyIntoStandardSentFolder = ((onOffOptions & INT_NO_COPY_INTO_SENT_FOLDER) == INT_NO_COPY_INTO_SENT_FOLDER);
		spamEnabled = ((onOffOptions & INT_SPAM_ENABLED) == INT_SPAM_ENABLED);
		textOnlyCompose = ((onOffOptions & INT_TEXT_ONLY_COMPOSE) == INT_TEXT_ONLY_COMPOSE);
	}

	public void setAppendVCard(final boolean appendVCard) {
		this.appendVCard = appendVCard;
		modifiedDuringSession = true;
	}

	public void setAutoLinebreak(final int autoLineBreak) {
		this.autoLinebreak = autoLineBreak >= 0 ? autoLineBreak : 0;
		modifiedDuringSession = true;
	}

	public void setConfirmedHam(final String confirmedHam) {
		this.confirmedHam = confirmedHam;
	}

	public void setConfirmedSpam(final String confirmedSpam) {
		this.confirmedSpam = confirmedSpam;
	}

	public void setDisplayHtmlInlineContent(final boolean htmlPreview) {
		this.displayHtmlInlineContent = htmlPreview;
		modifiedDuringSession = true;
	}

	public void setDisplayMsgHeaders(final String[] displayMsgHeaders) {
		if (displayMsgHeaders == null) {
			this.displayMsgHeaders = null;
			modifiedDuringSession = true;
			return;
		}
		this.displayMsgHeaders = new String[displayMsgHeaders.length];
		System.arraycopy(displayMsgHeaders, 0, this.displayMsgHeaders, 0, displayMsgHeaders.length);
	}

	public void setForwardAsAttachment(final boolean forwardAsAttachment) {
		this.forwardAsAttachment = forwardAsAttachment;
		modifiedDuringSession = true;
	}

	public void setHardDeleteMsgs(final boolean hardDeleteMessages) {
		this.hardDeleteMsgs = hardDeleteMessages;
		modifiedDuringSession = true;
	}

	public void setIgnoreOriginalMailTextOnReply(final boolean appendOriginalMailTextToReply) {
		this.ignoreOriginalMailTextOnReply = appendOriginalMailTextToReply;
		modifiedDuringSession = true;
	}

	void setModifiedDuringSession(final boolean modifiedDuringSession) {
		this.modifiedDuringSession = modifiedDuringSession;
	}

	public void setMsgFormat(final int msgFormat) {
		this.msgFormat = msgFormat;
		modifiedDuringSession = true;
	}

	public void setMsgPreview(final boolean msgPreview) {
		this.msgPreview = msgPreview;
		modifiedDuringSession = true;
	}

	public void setNoCopyIntoStandardSentFolder(final boolean noCopyIntoStandardSentFolder) {
		this.noCopyIntoStandardSentFolder = noCopyIntoStandardSentFolder;
		modifiedDuringSession = true;
	}

	public void setNotifyAppointments(final boolean notifyAppointments) {
		this.notifyAppointments = notifyAppointments;
		modifiedDuringSession = true;
	}

	public void setNotifyOnReadAck(final boolean notifyOnReadAck) {
		this.notifyOnReadAck = notifyOnReadAck;
		modifiedDuringSession = true;
	}

	public void setNotifyTasks(final boolean notifyTasks) {
		this.notifyTasks = notifyTasks;
		modifiedDuringSession = true;
	}

	public void setReplyToAddr(final String replyToAddr) {
		this.replyToAddr = replyToAddr;
		modifiedDuringSession = true;
	}

	public void setSendAddr(final String sendAddr) {
		this.sendAddr = sendAddr;
		modifiedDuringSession = true;
	}

	public void setShowGraphicEmoticons(final boolean showGraphicEmoticons) {
		this.showGraphicEmoticons = showGraphicEmoticons;
		modifiedDuringSession = true;
	}

	public void setSignatures(final Signature[] signatures) {
		if (signatures == null) {
			this.signatures = null;
			modifiedDuringSession = true;
			return;
		}
		this.signatures = new Signature[signatures.length];
		System.arraycopy(signatures, 0, this.signatures, 0, signatures.length);
		modifiedDuringSession = true;
	}

	public void setSpamEnabled(final boolean spamEnabled) {
		this.spamEnabled = spamEnabled;
		modifiedDuringSession = true;
	}

	public void setStdDraftsName(final String stdDraftsName) {
		this.stdDraftsName = stdDraftsName;
		modifiedDuringSession = true;
	}

	public void setStdSentName(final String stdSentName) {
		this.stdSentName = stdSentName;
		modifiedDuringSession = true;
	}

	public void setStdSpamName(final String stdSpamName) {
		this.stdSpamName = stdSpamName;
		modifiedDuringSession = true;
	}

	public void setStdTrashName(final String stdTrashName) {
		this.stdTrashName = stdTrashName;
		modifiedDuringSession = true;
	}

	public void setTextOnlyCompose(final boolean textOnlyCompose) {
		this.textOnlyCompose = textOnlyCompose;
		modifiedDuringSession = true;
	}

	public void setUploadQuota(final long uploadQuota) {
		this.uploadQuota = uploadQuota;
		modifiedDuringSession = true;
	}

	public void setUploadQuotaPerFile(final long uploadQuotaPerFile) {
		this.uploadQuotaPerFile = uploadQuotaPerFile;
		modifiedDuringSession = true;
	}

	public void setUseColorQuote(final boolean useColorQuote) {
		this.useColorQuote = useColorQuote;
		modifiedDuringSession = true;
	}
}
