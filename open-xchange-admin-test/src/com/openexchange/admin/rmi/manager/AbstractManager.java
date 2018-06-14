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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.rmi.manager;

import java.rmi.Naming;
import java.rmi.Remote;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link AbstractManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
abstract class AbstractManager {

    private final String rmiEndPointURL;
    private final Credentials masterCredentials;

    /**
     * Initialises a new {@link AbstractManager}.
     * 
     * @param rmiEndPointURL the RMI endpoint url
     * @param masterCredentials The master {@link Credentials}
     */
    public AbstractManager(String rmiEndPointURL, Credentials masterCredentials) {
        super();
        this.masterCredentials = masterCredentials;
        this.rmiEndPointURL = rmiEndPointURL;
    }

    /**
     * Gets the masterCredentials
     *
     * @return The masterCredentials
     */
    public Credentials getMasterCredentials() {
        return masterCredentials;
    }

    /**
     * Returns the {@link Remote} interface with the specified rmi name
     * 
     * @param rmiName The rmi name of the {@link Remote} interface
     * @return The {@link Remote} interface
     * @throws Exception if an error is occurred during RMI look-up
     */
    <T extends Remote> T getRemoteInterface(String rmiName, Class<T> clazz) throws Exception {
        return clazz.cast(Naming.lookup(rmiEndPointURL + rmiName));
    }
}
