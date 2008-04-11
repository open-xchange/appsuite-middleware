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

package com.openexchange.spamhandler.spamassassin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link SpamAssassinSpamHandler}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SpamAssassinSpamHandler extends SpamHandler {

	private static final MailField[] FIELDS_HEADER_CT = { MailField.HEADERS, MailField.CONTENT_TYPE };

	private static final String NAME = "SpamAssassin";

	private static final SpamAssassinSpamHandler instance = new SpamAssassinSpamHandler();

	/**
	 * Gets the singleton instance of {@link SpamAssassinSpamHandler}
	 * 
	 * @return The singleton instance of {@link SpamAssassinSpamHandler}
	 */
	public static SpamAssassinSpamHandler getInstance() {
		return instance;
	}

	/**
	 * Initializes a new {@link SpamAssassinSpamHandler}
	 */
	private SpamAssassinSpamHandler() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.spamhandler.SpamHandler#getSpamHandlerName()
	 */
	@Override
	public String getSpamHandlerName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.spamhandler.SpamHandler#handleHam(java.lang.String,
	 *      long[], boolean, com.openexchange.mail.api.MailAccess)
	 */
	@Override
	public void handleHam(final String spamFullname, final long[] mailIDs, final boolean move,
			final MailAccess<?, ?> mailAccess) throws MailException {
		/*
		 * Mark as ham. In contrast to mark as spam this is a very time sucking
		 * operation. In order to deal with the original messages that are
		 * wrapped inside a SpamAssassin-created message it must be extracted.
		 * Therefore we need to access message's content and cannot deal only
		 * with UIDs
		 */
		final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(spamFullname, mailIDs, FIELDS_HEADER_CT);
		/*
		 * Separate the plain from the nested messages inside spam folder
		 */
		SmartLongArray plainIDs = new SmartLongArray(mailIDs.length);
		SmartLongArray extractIDs = new SmartLongArray(mailIDs.length);
		for (int i = 0; i < mails.length; i++) {
			final String spamHdr = mails[i].getHeader(MessageHeaders.HDR_X_SPAM_FLAG);
			final String spamChecker = mails[i].getHeader("X-Spam-Checker-Version");
			final ContentType contentType = mails[i].getContentType();
			if (spamHdr != null
					&& "yes".regionMatches(true, 0, spamHdr, 0, 3)
					&& contentType.isMimeType(MIMETypes.MIME_MULTIPART_ALL)
					&& (spamChecker == null ? true
							: spamChecker.toLowerCase(Locale.ENGLISH).indexOf("spamassassin") != -1)) {
				extractIDs.append(mailIDs[i]);
			} else {
				plainIDs.append(mailIDs[i]);
			}
		}
		final String confirmedHamFullname = mailAccess.getFolderStorage().getConfirmedHamFolder();
		{
			/*
			 * Copy plain messages to confirmed ham and INBOX
			 */
			final long[] plainIDsArr = plainIDs.toArray();
			plainIDs = null;
			mailAccess.getMessageStorage().copyMessages(spamFullname, confirmedHamFullname, plainIDsArr, true);
			if (move) {
				mailAccess.getMessageStorage()
						.moveMessages(spamFullname, SpamHandler.FULLNAME_INBOX, plainIDsArr, true);
			}
		}
		/*
		 * Handle spam assassin messages
		 */
		final long[] spamArr = extractIDs.toArray();
		final List<MailMessage> nestedMails = new ArrayList<MailMessage>(spamArr.length);
		extractIDs = null;
		final long[] exc = new long[1];
		for (int i = 0; i < spamArr.length; i++) {
			final MailPart wrapped = mailAccess.getMessageStorage().getAttachment(spamFullname, spamArr[i], "2");
			wrapped.loadContent();
			MailMessage tmp = null;
			if (null == wrapped) {
				tmp = null;
			} else if (wrapped instanceof MailMessage) {
				tmp = (MailMessage) wrapped;
			} else if (wrapped.getContentType().isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
				tmp = (MailMessage) (wrapped.getContent());
			}
			if (null == tmp) {
				/*
				 * Handle like a plain spam message
				 */
				exc[0] = spamArr[i];
				mailAccess.getMessageStorage().copyMessages(spamFullname, confirmedHamFullname, exc, true);
				if (move) {
					mailAccess.getMessageStorage().moveMessages(spamFullname, SpamHandler.FULLNAME_INBOX, exc, true);
				}
			} else {
				nestedMails.add(tmp);
			}
		}
		final long[] ids = mailAccess.getMessageStorage().appendMessages(confirmedHamFullname,
				nestedMails.toArray(new MailMessage[nestedMails.size()]));
		if (move) {
			mailAccess.getMessageStorage().copyMessages(confirmedHamFullname, FULLNAME_INBOX, ids, true);
			mailAccess.getMessageStorage().deleteMessages(spamFullname, spamArr, true);
		}
	}

	/**
	 * 
	 * SmartLongArray - A tiny helper class to increase arrays of
	 * <code>long</code> as dynamically lists
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class SmartLongArray {
		/**
		 * Pointer to keep track of position in the array
		 */
		private int pointer;

		private long[] array;

		private final int growthSize;

		public SmartLongArray() {
			this(1024);
		}

		public SmartLongArray(final int initialSize) {
			this(initialSize, (initialSize / 4));
		}

		public SmartLongArray(final int initialSize, final int growthSize) {
			this.growthSize = growthSize;
			array = new long[initialSize];
		}

		public SmartLongArray append(final long i) {
			if (pointer >= array.length) {
				/*
				 * time to grow!
				 */
				final long[] tmpArray = new long[array.length + growthSize];
				System.arraycopy(array, 0, tmpArray, 0, array.length);
				array = tmpArray;
			}
			array[pointer++] = i;
			return this;
		}

		public long[] toArray() {
			final long[] trimmedArray = new long[pointer];
			System.arraycopy(array, 0, trimmedArray, 0, trimmedArray.length);
			return trimmedArray;
		}
	}
}
