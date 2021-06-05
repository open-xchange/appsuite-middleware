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

package com.sun.mail.pop3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.mail.MessagingException;

/**
 * {@link POP3Prober} - Probes support of <code><small>UIDL</small></code> and <code><small>TOP</small></code> POP3 commands.
 * <p>
 * Any occurred exception can be retrieved via {@link #getWarnings()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Prober {

    private final Protocol protocol;

    private final List<Exception> warnings;

    private final int total;

    /**
     * Initializes a new {@link POP3Prober}.
     *
     * @param pop3Store The connected POP3 store
     * @param pop3Folder The POP3 folder to check with
     * @throws MessagingException If initialization fails
     */
    public POP3Prober(final POP3Store pop3Store, final POP3Folder pop3Folder) throws MessagingException {
        super();
        total = pop3Folder.getMessageCount();
        protocol = (0 == total ? null : pop3Folder.getProtocol());
        warnings = new ArrayList<Exception>(2);
    }

    /**
     * Gets the UIDLs of contained messages.
     *
     * @return The UIDLs
     */
    public String[] getUIDLs() {
        try {
            final String[] uids = new String[total];
            if (!protocol.uidl(uids)) {
                return null;
            }
            return uids;
        } catch (IOException e) {
            warnings.add(e);
            return null;
        }
    }

    /**
     * Probes the <code><small>UIDL</small></code> command.
     *
     * @return <code>true</code> if <code><small>UIDL</small></code> command is supported; otherwise <code>false</code>
     */
    public boolean probeUIDL() {
        if (null == protocol) {
            /*
             * Nothing to probe present
             */
            return true;
        }
        try {
            return protocol.uidl(new String[1]);
        } catch (IOException e) {
            warnings.add(e);
            return false;
        }
    }

    /**
     * Probes the <code><small>TOP</small></code> command.
     *
     * @return <code>true</code> if <code><small>TOP</small></code> command is supported; otherwise <code>false</code>
     */
    public boolean probeTOP() {
        if (null == protocol) {
            /*
             * Nothing to probe present
             */
            return true;
        }
        try {
            return (null != protocol.top(1, 1));
        } catch (IOException e) {
            warnings.add(e);
            return false;
        }
    }

    /**
     * Gets an unmodifiable list of occurred warnings during probing.
     *
     * @return An unmodifiable list of occurred warnings
     */
    public List<Exception> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

}
