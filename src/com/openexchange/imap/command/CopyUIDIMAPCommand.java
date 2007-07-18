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

import static com.openexchange.imap.IMAPUtils.prepareStringArgument;

import java.util.Arrays;

import javax.mail.MessagingException;

import com.openexchange.imap.OXMailException;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.tools.Collections.SmartLongArray;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPResponse;

/**
 * CopyUIDIMAPCommand - Copies messages from given folder to given destination
 * folder just using their UIDs
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CopyUIDIMAPCommand extends AbstractIMAPCommand<long[]> {

	private static final long[] DEFAULT_RETVAL = new long[0];

	private final boolean fast;

	private final int length;

	private final String[] args;

	private final long[] uids;

	private final long[] retval;

	private final String destFolderName;

	private int fetchRespIndex;

	/**
	 * Constructor
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
	public CopyUIDIMAPCommand(final IMAPFolder imapFolder, final long[] uids, final String destFolderName,
			final boolean isSequential, final boolean fast) {
		super(imapFolder);
		this.uids = uids == null ? DEFAULT_RETVAL : uids;
		returnDefaultValue = (this.uids.length == 0);
		this.fast = fast;
		this.destFolderName = prepareStringArgument(destFolderName);
		length = this.uids.length;
		args = length == 0 ? ARGS_EMPTY : (isSequential ? new String[] { new StringBuilder(64).append(this.uids[0])
				.append(':').append(this.uids[this.uids.length - 1]).toString() } : IMAPNumArgSplitter.splitUIDArg(this.uids));
		if (fast) {
			retval = null;
		} else {
			retval = new long[length];
			Arrays.fill(retval, -1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.command.AbstractIMAPCommand#addLoopCondition()
	 */
	@Override
	protected boolean addLoopCondition() {
		return (fetchRespIndex < length);
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
		sb.append("UID COPY ");
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
	protected long[] getDefaultValueOnEmptyFolder() {
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
			throw new MessagingException(OXMailException.getFormattedMessage(MailCode.PROTOCOL_ERROR,
					"UID COPY failed: " + lastResponse.getRest()));
		}
	}

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
		try {
			final IMAPResponse ir = (IMAPResponse) response;
			/**
			 * Parse response:
			 * 
			 * <pre>
			 * OK [COPYUID 1184051486 10031:10523,10525:11020,11022:11027,11030:11047,11050:11051,11053:11558 1024:2544] Completed
			 * </pre>
			 */
			UIDCopyResponse copyResp = null;
			int atomCount = 0;
			String next = null;
			while ((next = ir.readAtom()) != null) {
				switch (++atomCount) {
				case 1:
					if (!"[COPYUID".equals(next)) {
						return;
					}
					copyResp = new UIDCopyResponse();
					break;
				case 3:
					copyResp.src = next;
					break;
				case 4:
					copyResp.dest = next.replaceFirst("\\]", "");
					break;
				default:
					break;
				}
			}
			if (copyResp == null) {
				return;
			}
			copyResp.fillResponse(uids, retval);
		} finally {
			fetchRespIndex++;
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

	private static final class UIDCopyResponse {

		private String src;

		private String dest;

		public UIDCopyResponse() {
			super();
		}

		public String getDest() {
			return dest;
		}

		public void setDest(final String dest) {
			this.dest = dest;
		}

		public String getSrc() {
			return src;
		}

		public void setSrc(final String src) {
			this.src = src;
		}

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

		private static final long[] toLongArray(final String s) {
			final SmartLongArray arr = new SmartLongArray();
			final String[] sa = s.split(" *, *");
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
