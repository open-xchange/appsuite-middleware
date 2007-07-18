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

import com.openexchange.api2.MailInterfaceImpl;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * AbstractIMAPCommand
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class AbstractIMAPCommand<T> {

	private static final String ERR_01 = "No matching messages";

	protected static final String[] ARGS_EMPTY = { "" };

	protected static final String[] ARGS_ALL = { "1:*" };

	protected final IMAPFolder imapFolder;
	
	protected boolean returnDefaultValue;

	private final AbstractIMAPProtocolCommand protocolCommand;

	/**
	 * Constructor
	 */
	public AbstractIMAPCommand(final IMAPFolder imapFolder) {
		super();
		this.imapFolder = imapFolder;
		this.protocolCommand = new AbstractIMAPProtocolCommand(this);

	}

	private static final class AbstractIMAPProtocolCommand implements IMAPFolder.ProtocolCommand {

		private final AbstractIMAPCommand abstractIMAPCommand;

		public AbstractIMAPProtocolCommand(final AbstractIMAPCommand abstractIMAPCommand) {
			this.abstractIMAPCommand = abstractIMAPCommand;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun.mail.imap.protocol.IMAPProtocol)
		 */
		public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
			if (abstractIMAPCommand.returnDefaultValue()) {
				/*
				 * Abort processing
				 */
				return abstractIMAPCommand.getDefaultValueOnEmptyFolder();
			}
			final boolean notifyResponseHandlers = abstractIMAPCommand.performNotifyResponseHandlers();
			final String[] args = abstractIMAPCommand.getArgs();
			Response[] r = null;
			Response response = null;
			for (int k = 0; k < args.length; k++) {
				r = protocol.command(abstractIMAPCommand.getCommand(k), null);
				response = r[r.length - 1];
				try {
					abstractIMAPCommand.handleLastResponse(response);
				} catch (final MessagingException e) {
					if (e.getMessage().indexOf(ERR_01) > -1) {
						return abstractIMAPCommand.getDefaultValueOnEmptyFolder();
					}
					final ProtocolException pe = new ProtocolException(e.getMessage());
					pe.initCause(e);
					throw pe;
				}
				try {
					for (int index = 0; index < r.length && abstractIMAPCommand.addLoopCondition(); index++) {
						try {
							abstractIMAPCommand.handleResponse(r[index]);
						} catch (final MessagingException e) {
							final ProtocolException pe = new ProtocolException(e.getMessage());
							pe.initCause(e);
							throw pe;
						}
						/*
						 * Discard handled response
						 */
						r[index] = null;
					}
				} finally {
					if (notifyResponseHandlers) {
						/*
						 * Dispatch unhandled responses
						 */
						protocol.notifyResponseHandlers(r);
					}
					if (abstractIMAPCommand.performHandleResult()) {
						try {
							protocol.handleResult(response);
						} catch (final CommandFailedException cfe) {
							if (cfe.getMessage().indexOf(ERR_01) != -1) {
								/*
								 * Obviously this folder is empty or no matching
								 * messages were found
								 */
								return abstractIMAPCommand.getDefaultValueOnEmptyFolder();
							}
							throw cfe;
						}
					}
				}
			}
			return abstractIMAPCommand.getReturnVal();
		}
	}

	@SuppressWarnings("unchecked")
	public final T doCommand() throws MessagingException {
		final long start = System.currentTimeMillis();
		final Object obj = imapFolder.doCommand(protocolCommand);
		MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		return (T) obj;
	}

	/**
	 * Gets the IMAP command to be executed
	 * 
	 * @param argsIndex -
	 *            the args index
	 * @return the IMAP command to be executed
	 */
	protected abstract String getCommand(final int argsIndex);

	/**
	 * Gets the IMAP command's arguments whereas each argument <b>must not</b>
	 * exceed 16384 bytes
	 * 
	 * @return the IMAP command's arguments
	 */
	protected abstract String[] getArgs();

	/**
	 * Determine if <code>IMAPProtocol.notifyResponseHandlers(Response[])</code>
	 * shall be invoked
	 * 
	 * @return <code>true</code> if
	 *         <code>IMAPProtocol.notifyResponseHandlers(Response[])</code>
	 *         shall be invoked; otherwise <code>false</code>
	 */
	protected abstract boolean performNotifyResponseHandlers();

	/**
	 * Determine if <code>IMAPProtocol.handleResult(Response)</code> shall be
	 * invoked
	 * 
	 * @return <code>true</code> if
	 *         <code>IMAPProtocol.handleResult(Response)</code> shall be
	 *         invoked; otherwise <code>false</code>
	 */
	protected abstract boolean performHandleResult();

	/**
	 * Define a <code>boolean</code> value that is included in inner response
	 * loop
	 * 
	 * @return a <code>boolean</code> value
	 */
	protected abstract boolean addLoopCondition();

	/**
	 * Gets the default value that ought to be returned if the error
	 * <code>"No matching messages"</code> occurs
	 * 
	 * @return the default value
	 */
	protected abstract T getDefaultValueOnEmptyFolder();

	/**
	 * Indicates if processing should be stopped right at the beginning and the
	 * default value should be returned
	 * 
	 * @return <code>true</code> if processing should be stopped; otherwise
	 *         <code>false</code>
	 */
	protected final boolean returnDefaultValue() {
		return returnDefaultValue;
	}

	/**
	 * Handles the current response
	 * 
	 * @param response -
	 *            the response
	 * @throws MessagingException -
	 *             if a message-related error occurs
	 */
	protected abstract void handleResponse(Response response) throws MessagingException;

	/**
	 * Handles the last response which indicates response status:
	 * <code>OK</code>, <code>NO</code>, <code>BAD</code> or
	 * <code>BYE</code>
	 * 
	 * @param lastResponse -
	 *            the last response
	 * @throws MessagingException -
	 *             if a response-related error occurs
	 */
	protected abstract void handleLastResponse(Response lastResponse) throws MessagingException;

	/**
	 * Gets the return value
	 * 
	 * @return the return value
	 */
	protected abstract T getReturnVal();

}
