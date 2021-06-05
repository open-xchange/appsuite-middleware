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
package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This interface defines methods for doing a "login" for an user or admin.<br>
 * Can be usefull in UI`s which provide login masks for checking login informations.<br><br>
 *
 * <b>Example:</b>
 * <pre>
 * final OXLoginInterface iface = (OXLoginInterface)Naming.lookup("rmi:///oxhost/"+OXLoginInterface.RMI_NAME);
 *
 * final Context ctx = new Context(1);
 *
 * final Credentials auth = new Credentials();
 * auth.setLogin("myuser");
 * auth.setPassword("secret");
 *
 * try {
 * User account_data = iface.login2User(ctx,auth);
 *  // Do something after user logged in successfully.
 * }catch(InvalidCredentialsException ice){
 *  // show error in UI.
 * }
 * </pre>
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface OXLoginInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXLogin_V2";

    /**
     * Login method to check if given credentials are correct.
     *
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     */
    public void login(Context ctx,Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException,DatabaseUpdateException;

    /**
     * Login method for a context admin or a normal user.
     *
     * @param ctx
     * @param auth
     * @return An user object with all data of the user who just logged in.
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     */
    public User login2User(Context ctx,Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException,DatabaseUpdateException;

    /**
     * Login method for the master admin account.
     *
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    public void login(Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException;

}
