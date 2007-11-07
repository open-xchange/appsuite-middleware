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

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;

import com.openexchange.imap.IMAPException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link FlagsIMAPCommand} - Enables/disables message's system flags e.g. \SEEN or
 * \DELETED
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class FlagsIMAPCommand extends AbstractIMAPCommand<Boolean> {

	private final String[] args;

	private final String flagsStr;

	private final boolean enable;

	private final boolean uid;

	private Boolean retval = Boolean.TRUE;

	/**
	 * Constructor
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param uids -
	 *            the UIDs
	 * @param flags -
	 *            the system flags
	 * @param enable -
	 *            whether to enable or disable affected system flags
	 * @param isSequential -
	 *            whether supplied UIDs are in sequential order or not
	 * @throws MessagingException -
	 *             if an unknown system flag is used
	 */
	public FlagsIMAPCommand(final IMAPFolder imapFolder, final long[] uids, final Flags flags, final boolean enable,
			final boolean isSequential) throws MessagingException {
		super(imapFolder);
		if (uids == null || uids.length == 0) {
			returnDefaultValue = true;
			args = ARGS_EMPTY;
			flagsStr = null;
		} else {
			args = isSequential ? new String[] { new StringBuilder(64).append(uids[0]).append(':').append(
					uids[uids.length - 1]).toString() } : IMAPNumArgSplitter.splitUIDArg(uids, true);
			final Flag[] systemFlags;
			if (flags == null || (systemFlags = flags.getSystemFlags()).length == 0) {
				returnDefaultValue = true;
				flagsStr = null;
			} else {
				final StringBuilder flagBuilder = new StringBuilder(200);
				flagBuilder.append(getFlagString(systemFlags[0]));
				for (int i = 1; i < systemFlags.length; i++) {
					flagBuilder.append(' ').append(getFlagString(systemFlags[i]));
				}
				flagsStr = flagBuilder.toString();
			}
		}
		this.enable = enable;
		this.uid = true;
	}

	/**
	 * Constructor to set system flags in all messages
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param flags -
	 *            the system flags
	 * @param enable -
	 *            whether to enable or disable affected system flags
	 * @throws MessagingException -
	 *             if an unknown system flag is used
	 */
	public FlagsIMAPCommand(final IMAPFolder imapFolder, final Flags flags, final boolean enable)
			throws MessagingException {
		super(imapFolder);
		args = ARGS_ALL;
		final Flag[] systemFlags;
		if (flags == null || (systemFlags = flags.getSystemFlags()).length == 0) {
			returnDefaultValue = true;
			flagsStr = null;
		} else {
			final StringBuilder flagBuilder = new StringBuilder(200);
			flagBuilder.append(getFlagString(systemFlags[0]));
			for (int i = 1; i < systemFlags.length; i++) {
				flagBuilder.append(' ').append(getFlagString(systemFlags[i]));
			}
			flagsStr = flagBuilder.toString();
		}
		this.enable = enable;
		this.uid = false;
	}

	public static final String FLAG_ANSWERED = "\\Answered";

	public static final String FLAG_DELETED = "\\Deleted";

	public static final String FLAG_DRAFT = "\\Draft";

	public static final String FLAG_FLAGGED = "\\Flagged";

	public static final String FLAG_RECENT = "\\Recent";

	public static final String FLAG_SEEN = "\\Seen";

	public static final String FLAG_USER = "\\User";

	private static String getFlagString(final Flag systemFlag) throws MessagingException {
		if (systemFlag.equals(Flags.Flag.ANSWERED)) {
			return FLAG_ANSWERED;
		} else if (systemFlag.equals(Flags.Flag.DELETED)) {
			return FLAG_DELETED;
		} else if (systemFlag.equals(Flags.Flag.DRAFT)) {
			return FLAG_DRAFT;
		} else if (systemFlag.equals(Flags.Flag.FLAGGED)) {
			return FLAG_FLAGGED;
		} else if (systemFlag.equals(Flags.Flag.RECENT)) {
			return FLAG_RECENT;
		} else if (systemFlag.equals(Flags.Flag.SEEN)) {
			return FLAG_SEEN;
		} else if (systemFlag.equals(Flags.Flag.USER)) {
			return FLAG_USER;
		}
		throw new MessagingException("Unknown System Flag");
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
		// UID STORE %s %sFLAGS (%s)
		final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
		if (uid) {
			sb.append("UID ");
		}
		sb.append("STORE ");
		sb.append(args[argsIndex]);
		sb.append(' ').append(enable ? '+' : '-');
		sb.append("FLAGS (").append(flagsStr).append(')');
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
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#handleLastResponse(com.sun.mail.iap.Response)
	 */
	@Override
	protected void handleLastResponse(final Response lastResponse) throws MessagingException {
		if (!lastResponse.isOK()) {
			throw new MessagingException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR,
					"UID STORE failed: " + lastResponse.getRest()));
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
		return true;
	}

}
