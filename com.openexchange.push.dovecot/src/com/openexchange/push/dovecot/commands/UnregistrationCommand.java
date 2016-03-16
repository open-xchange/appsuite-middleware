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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.push.dovecot.commands;

import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import org.slf4j.Logger;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.log.LogProperties;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPFolder.ProtocolCommand;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * {@link UnregistrationCommand} - Issues the special SETMETADATA command to un-register a push listener at Dovecot.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class UnregistrationCommand implements ProtocolCommand {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(UnregistrationCommand.class);

    private final IMAPFolder imapFolder;

    /**
     * Initializes a new {@link UnregistrationCommand}.
     *
     * @param imapFolder The IMAP folder
     */
    public UnregistrationCommand(IMAPFolder imapFolder) {
        super();
        this.imapFolder = imapFolder;
    }

    @Override
    public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
        // Craft IMAP command
        String command = "SETMETADATA \"\" (/private/vendor/vendor.dovecot/http-notify NIL)";

        // Issue command
        Response[] r = IMAPCommandsCollection.performCommand(protocol, command);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            LOGGER.info("Unregistered push notification for {} using: {}", imapFolder.getStore(), command);
            return Boolean.TRUE;
        } else if (response.isBAD()) {
            LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
            throw new BadCommandException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
        } else if (response.isNO()) {
            LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
            throw new CommandFailedException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
        } else {
            LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
            protocol.handleResult(response);
        }
        return Boolean.FALSE;
    }
}
