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

package com.openexchange.spamhandler.spamassassin.api;


/**
 * This interface is used to write a provider which provides all information needed to
 * access a spamd installation. This are hostname, port and the username
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface SpamdProvider {

    /**
     * Get the hostname of the system where spamd is running
     *
     * @return the hostname of the spamd system
     */
    public String getHostname();

    /**
     * Get the port of the spamd daemon
     *
     * @return -1 if no special port is required, the default one 783 will be used then
     */
    public int getPort();

    /**
     * The username for spamd.
     *
     * @return
     */
    public String getUsername();
}
