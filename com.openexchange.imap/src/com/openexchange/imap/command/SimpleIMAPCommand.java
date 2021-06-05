/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.imap.command;

import javax.mail.MessagingException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link SimpleIMAPCommand}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SimpleIMAPCommand extends AbstractIMAPCommand<Boolean> {

    private final String command;

    private final String[] args;

    /**
     * Initializes a new {@link SimpleIMAPCommand}
     *
     * @param imapFolder The IMAP folder
     * @param command The command to execute
     */
    public SimpleIMAPCommand(IMAPFolder imapFolder, String command) {
        super(imapFolder);
        this.command = command;
        args = ARGS_EMPTY;
    }

    /**
     * Initializes a new {@link SimpleIMAPCommand}
     *
     * @param imapFolder The IMAP folder
     * @param command The command to execute
     * @param uids The UIDs
     */
    public SimpleIMAPCommand(IMAPFolder imapFolder, String command, long[] uids) {
        super(imapFolder);
        if (uids == null) {
            returnDefaultValue = true;
        }
        this.command = command;
        args = uids == null ? ARGS_EMPTY : IMAPNumArgSplitter.splitUIDArg(uids, true, command.length());
    }

    @Override
    protected boolean addLoopCondition() {
        return true;
    }

    @Override
    protected String[] getArgs() {
        return args;
    }

    @Override
    protected String getCommand(int argsIndex) {
        final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
        sb.append(command);
        if (!java.util.Arrays.equals(ARGS_EMPTY, args)) {
            sb.append(args[argsIndex]);
        }
        return sb.toString();
    }

    @Override
    protected Boolean getDefaultValue() {
        return Boolean.TRUE;
    }

    @Override
    protected Boolean getReturnVal() {
        return Boolean.TRUE;
    }

    @Override
    protected boolean handleResponse(Response response) throws MessagingException {
        // Nothing to do
        return false;
    }

}
