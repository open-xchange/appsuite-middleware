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
 *    on the web site http://www.open-xchange.com/EN/legal/fetchRespIndex.html.
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

import static com.openexchange.imap.IMAPCommandsCollection.prepareStringArgument;

import java.util.Arrays;

import javax.mail.MessagingException;

import com.openexchange.imap.IMAPException;
import com.openexchange.tools.Collections.SmartLongArray;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPResponse;

/**
 * {@link CopyIMAPCommand} - Copies messages from given folder to given
 * destination folder just using their sequence numbers/UIDs
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CopyIMAPCommand extends AbstractIMAPCommand<long[]> {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(CopyIMAPCommand.class);

	private static final long[] DEFAULT_RETVAL = new long[0];

	private final boolean uid;

	private final boolean fast;

	private final int length;

	private final String[] args;

	private final long[] uids;

	private final long[] retval;

	private final String destFolderName;

	private boolean proceed = true;

	/**
	 * Constructor using UIDs and consequently performs a <code>UID COPY</code>
	 * command
	 * 
	 * @param imapFolder -
	 *            the imap folder
	 * @param uids -
	 *            the UIDs of the messages that shall be copied
	 * @param destFolderName -
	 *            the destination folder fullname
	 * @param isSequential -
	 *            whether UIDs are sequential or not
	 * @param fast -
	 *            <code>true</code> to ignore corresponding UIDs of copied
	 *            messages and {@link #getReturnVal()} returns <code>null</code>
	 */
	public CopyIMAPCommand(final IMAPFolder imapFolder, final long[] uids, final String destFolderName,
			final boolean isSequential, final boolean fast) {
		super(imapFolder);
		this.uids = uids == null ? DEFAULT_RETVAL : uids;
		uid = true;
		returnDefaultValue = (this.uids.length == 0);
		this.fast = fast;
		this.destFolderName = prepareStringArgument(destFolderName);
		length = this.uids.length;
		args = length == 0 ? ARGS_EMPTY : (isSequential ? new String[] { new StringBuilder(64).append(this.uids[0])
				.append(':').append(this.uids[this.uids.length - 1]).toString() } : IMAPNumArgSplitter.splitUIDArg(
				this.uids, false));
		if (fast) {
			retval = DEFAULT_RETVAL;
		} else {
			retval = new long[length];
			Arrays.fill(retval, -1);
		}
	}

	/**
	 * Constructor to copy all messages of given folder to given destination
	 * folder by performing a <code>COPY 1:*</code> command
	 * 
	 * @param imapFolder -
	 *            the ima folder
	 * @param destFolderName -
	 *            the destination folder
	 */
	public CopyIMAPCommand(final IMAPFolder imapFolder, final String destFolderName) {
		super(imapFolder);
		fast = true;
		uid = false;
		this.uids = DEFAULT_RETVAL;
		this.destFolderName = prepareStringArgument(destFolderName);
		retval = DEFAULT_RETVAL;
		args = ARGS_ALL;
		length = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#addLoopCondition()
	 */
	@Override
	protected boolean addLoopCondition() {
		return (fast ? false : proceed);
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
		if (uid) {
			sb.append("UID ");
		}
		sb.append("COPY ");
		sb.append(args[argsIndex]);
		sb.append(' ').append(destFolderName);
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getDefaultValueOnEmptyFolder()
	 */
	@Override
	protected long[] getDefaultValue() {
		return DEFAULT_RETVAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#getReturnVal()
	 */
	@Override
	protected long[] getReturnVal() {
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
					"UID COPY failed: " + lastResponse.getRest()));
		}
	}

	private static final String COPYUID = "copyuid";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#handleResponse(com.sun.mail.iap.Response)
	 */
	@Override
	protected void handleResponse(final Response response) throws MessagingException {
		if (fast) {
			return;
		} else if (!(response instanceof IMAPResponse)) {
			return;
		}
		final String resp = ((IMAPResponse) response).toString().toLowerCase();
		/**
		 * Parse response:
		 * 
		 * <pre>
		 * OK [COPYUID 1184051486 10031:10523,10525:11020,11022:11027,11030:11047,11050:11051,11053:11558 1024:2544] Completed
		 * </pre>
		 * <pre>
		 * * 45 EXISTS..* 2 RECENT..A4 OK [COPYUID 1185853191 7,32 44:45] Completed
		 * </pre>
		 */
		int pos = -1;
		if ((pos = resp.indexOf(COPYUID)) != -1) {
			final COPYUIDResponse copyuidResp = new COPYUIDResponse();
			/*
			 * Find next starting ATOM in IMAP response
			 */
			pos += COPYUID.length();
			while (Character.isWhitespace(resp.charAt(pos))) {
				pos++;
			}
			/*
			 * Split by ATOMs
			 */
			final String[] sa = resp.substring(pos).split("\\s+");
			if (sa.length >= 3) {
				for (int i = 1; i < sa.length; i++) {
					if (i == 1) {
						copyuidResp.src = sa[i];
					} else if (i == 2) {
						copyuidResp.dest = sa[i].replaceFirst("\\]", "");
					}
				}
				copyuidResp.fillResponse(uids, retval);
			} else {
				LOG.error(new StringBuilder(128).append("Invalid COPYUID response: ").append(resp).toString());
			}
			proceed = false;
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn(new StringBuilder(128).append("Missing COPYUID response code: ").append(resp).toString());
			}
		}
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

	private static final class COPYUIDResponse {

		private String src;

		private String dest;

		public COPYUIDResponse() {
			super();
		}

		/**
		 * Fills given <code>retval</code> with UIDs from destination folder
		 * while keeping order of source UIDs in <code>uids</code>
		 * 
		 * @param uids
		 *            The source UIDs
		 * @param retval
		 *            The destination UIDs to fill
		 */
		public void fillResponse(final long[] uids, final long[] retval) {
			final long[] srcArr = toLongArray(src);
			final long[] destArr = toLongArray(dest);
			for (int in = 0; in < srcArr.length; in++) {
				final long currentUID = srcArr[in];
				int index = 0;
				Inner: for (; index < uids.length; index++) {
					if (uids[index] == currentUID) {
						break Inner;
					}
				}
				retval[index] = destArr[in];
			}
		}

		private static long[] toLongArray(final String uidSet) {
			final SmartLongArray arr = new SmartLongArray();
			final String[] sa = uidSet.split(" *, *");
			Next: for (int i = 0; i < sa.length; i++) {
				final int pos = sa[i].indexOf(':');
				if (pos == -1) {
					arr.append(Long.parseLong(sa[i]));
					continue Next;
				}
				final long endUID = Long.parseLong(sa[i].substring(pos + 1));
				for (long j = Long.parseLong(sa[i].substring(0, pos)); j <= endUID; j++) {
					arr.append(j);
				}
			}
			return arr.toArray();
		}
	}

}
