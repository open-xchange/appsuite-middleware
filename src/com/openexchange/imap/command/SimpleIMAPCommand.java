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

import javax.mail.MessagingException;

import com.openexchange.imap.IMAPException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link SimpleIMAPCommand}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SimpleIMAPCommand extends AbstractIMAPCommand<Boolean> {

	private final String command;

	private final String[] args;
	
	private final boolean performNotifyResponseHandlers;

	/**
	 * Constructor to apply command as it is
	 * 
	 * @param imapFolder
	 */
	public SimpleIMAPCommand(final IMAPFolder imapFolder, final String command, final boolean performNotifyResponseHandlers) {
		super(imapFolder);
		this.command = command;
		args = ARGS_EMPTY;
		this.performNotifyResponseHandlers = performNotifyResponseHandlers;
	}

	/**
	 * Constructor to apply command to specified messages in folder
	 * 
	 * @param imapFolder
	 */
	public SimpleIMAPCommand(final IMAPFolder imapFolder, final String command, final long[] uids, final boolean performNotifyResponseHandlers) {
		super(imapFolder);
		if (uids == null) {
			returnDefaultValue = true;
		}
		this.command = command;
		args = IMAPNumArgSplitter.splitUIDArg(uids, true);
		this.performNotifyResponseHandlers = performNotifyResponseHandlers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#addLoopCondition()
	 */
	@Override
	protected boolean addLoopCondition() {
		return true;
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
		sb.append(command);
		if (java.util.Arrays.equals(ARGS_EMPTY, args)) {
			sb.append(args[argsIndex]);
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getDefaultValueOnEmptyFolder()
	 */
	@Override
	protected Boolean getDefaultValue() {
		return Boolean.TRUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getReturnVal()
	 */
	@Override
	protected Boolean getReturnVal() {
		return Boolean.TRUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#handleLastResponse(com.sun.mail.iap.Response)
	 */
	@Override
	protected void handleLastResponse(final Response lastResponse) throws MessagingException {
		if (!lastResponse.isOK()) {
			throw new MessagingException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, "Command \""
					+ command + "\" failed: " + lastResponse.getRest()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#handleResponse(com.sun.mail.iap.Response)
	 */
	@Override
	protected void handleResponse(final Response response) throws MessagingException {
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
		return performNotifyResponseHandlers;
	}

}
