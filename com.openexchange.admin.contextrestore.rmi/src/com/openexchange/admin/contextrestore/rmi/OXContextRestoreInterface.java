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

package com.openexchange.admin.contextrestore.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;


/**
 * This class defines the Open-Xchange API for restoring OX Contexts.<br><br>
 *
 * At the moment this API defines only one call
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface OXContextRestoreInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXContextRestore";

    /**
     * This method is used to restore one single context
     *
     * @param ctx Context object
     * @param filenames The file names of the MySQL dump files which contain the backup of the context. <b>Note</b> that these files
     *                  have to be available to the admin daemon, so they must reside on the machine on which the admin
     *                  daemon is running.
     * @param optConfigDbName The optional name of the ConfigDB schema
     * @param auth Credentials for authenticating against server.
     * @param dryrun <code>true</code> to perform a dry run; otherwise <code>false</code>
     * @return The restored context's URI
     * @throws RemoteException General RMI Exception
     * @throws InvalidDataException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws OXContextRestoreException
     * @throws DatabaseUpdateException
     * @throws PoolException
     * @throws NoSuchContextException
     * @throws
     */
    public String restore(final Context ctx, final String[] filenames, final String optConfigDbName, final Credentials auth, boolean dryrun) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXContextRestoreException, DatabaseUpdateException;

}
