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

package com.openexchange.imap.command;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.OXMailException.MailCode;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.FetchResponse;

/**
 * ExtractSpamMsgIMAPCommand - fetches the nested messages out of the spam
 * messages
 * <p>
 * <b>NOTE:</b> this class assumes that the messages identified through
 * <code>uids</code> are structured in a specific manner that is their content
 * type is <code>multipart/mixed</code> and content consists of two parts
 * whereby the latter one is a nested rfc822 message
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ExtractSpamMsgIMAPCommand extends AbstractIMAPCommand<Message[]> {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ExtractSpamMsgIMAPCommand.class);

	private static final long[] L1 = new long[0];

	private final long[] uids;

	private final int length;

	private final String[] args;

	private final List<Message> msgList;

	private final Session dummySession;

	private int fetchIndex;

	private int bodyIndex = -1;

	public ExtractSpamMsgIMAPCommand(final IMAPFolder imapFolder, final long[] uids) throws OXException {
		super(imapFolder);
		this.uids = uids == null ? L1 : uids;
		returnDefaultValue = (this.uids.length == 0);
		this.length = this.uids.length;
		args = length == 0 ? ARGS_EMPTY : IMAPNumArgSplitter.splitUIDArg(uids);
		msgList = new ArrayList<Message>(length);
		dummySession = Session.getDefaultInstance(MailInterfaceImpl.getDefaultIMAPProperties());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#addLoopCondition()
	 */
	@Override
	protected boolean addLoopCondition() {
		return (fetchIndex < length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getArgs()
	 */
	@Override
	protected String[] getArgs() {
		return args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getCommand(int)
	 */
	@Override
	protected String getCommand(final int argsIndex) {
		final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
		sb.append("UID FETCH ");
		sb.append(args[argsIndex]);
		sb.append(" (BODY[2])");
		return sb.toString();
	}

	private static final Message[] EMPTY_ARR = new Message[0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getDefaultValueOnEmptyFolder()
	 */
	@Override
	protected Message[] getDefaultValueOnEmptyFolder() {
		return EMPTY_ARR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getReturnVal()
	 */
	@Override
	protected Message[] getReturnVal() {
		return msgList.toArray(new Message[msgList.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#handleLastResponse(com.sun.mail.iap.Response)
	 */
	@Override
	protected void handleLastResponse(final Response lastResponse) throws MessagingException {
		if (!lastResponse.isOK()) {
			throw new MessagingException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
					"UID FETCH failed: " + lastResponse.getRest()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#handleResponse(com.sun.mail.iap.Response)
	 */
	@Override
	protected void handleResponse(final Response response) throws MessagingException {
		if (response == null) {
			return;
		} else if (!(response instanceof FetchResponse)) {
			return;
		}
		final FetchResponse fetchResponse = ((FetchResponse) response);
		if (bodyIndex == -1) {
			bodyIndex = searchBodyIndex(fetchResponse);
		}
		try {
			msgList.add(new MimeMessage(dummySession, ((BODY) fetchResponse.getItem(bodyIndex))
					.getByteArrayInputStream()));
		} catch (final ClassCastException e) {
			LOG.warn("No BODY item at index " + bodyIndex);
			bodyIndex = searchBodyIndex(fetchResponse);
			msgList.add(new MimeMessage(dummySession, ((BODY) fetchResponse.getItem(bodyIndex))
					.getByteArrayInputStream()));
		} catch (final ArrayIndexOutOfBoundsException e) {
			LOG.warn("Invalid index " + bodyIndex);
			bodyIndex = searchBodyIndex(fetchResponse);
			msgList.add(new MimeMessage(dummySession, ((BODY) fetchResponse.getItem(bodyIndex))
					.getByteArrayInputStream()));
		} catch (final Exception e) {
			/*
			 * I know: Don't catch raw exceptions. But I want to be sure this
			 * routines terminates with success
			 */
			LOG.warn("Unexpected exception " + e.getMessage(), e);
			bodyIndex = searchBodyIndex(fetchResponse);
			msgList.add(new MimeMessage(dummySession, ((BODY) fetchResponse.getItem(bodyIndex))
					.getByteArrayInputStream()));
		}
		fetchIndex++;
	}

	private static int searchBodyIndex(final FetchResponse fetchResponse) throws MessagingException {
		/*
		 * Search index of BODY item
		 */
		int bodyIndex = -1;
		final int itemCount = fetchResponse.getItemCount();
		for (int j = 0; j < itemCount && bodyIndex == -1; j++) {
			if (fetchResponse.getItem(j) instanceof BODY) {
				bodyIndex = j;
			}
		}
		if (bodyIndex == -1) {
			throw new MessagingException("Missing BODY item in FETCH response");
		}
		return bodyIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#performHandleResult()
	 */
	@Override
	protected boolean performHandleResult() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#performNotifyResponseHandlers()
	 */
	@Override
	protected boolean performNotifyResponseHandlers() {
		return false;
	}

}
