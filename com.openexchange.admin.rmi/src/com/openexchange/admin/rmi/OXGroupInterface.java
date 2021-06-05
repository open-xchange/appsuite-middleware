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
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This interface defines the Open-Xchange API Version 2 for creating and
 * manipulating OX groups within an OX context.<br><br>
 *
 * <b>Example:</b>
 * <pre>
 * final OXGroupInterface iface = (OXGroupInterface)Naming.lookup("rmi:///oxhost/"+OXGroupInterface.RMI_NAME);
 *
 * final Context ctx = new Context(1);
 *
 * Group grp = new Group();
 * grp.setDisplayname("display name");
 * grp.setName("name");
 *
 * final Credentials auth = new Credentials();
 * auth.setLogin("admin");
 * auth.setPassword("secret");
 *
 * Group created = iface.create(ctx,group,auth);
 *
 * </pre>
 *
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface OXGroupInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXGroup_V2";

    /**
     * Adds a new member to the group within given context.
     *
     * @param ctx
     *                Context object
     * @param grp_id
     *                The ID of the group in which the new members should be
     *                added.
     * @param members
     *                User objects with the user_id field set.
     * @param auth
     *                Credentials for authenticating against server.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     * @throws NoSuchGroupException
     */
    public void addMember(final Context ctx, final Group grp, final User[] members, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, NoSuchGroupException;

    /**
     * Method for changing group data in given context
     *
     * @param ctx
     *                Context object
     * @param grp
     *                Group to change.
     * @param auth
     *                Credentials for authenticating against server.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     */
    public void change(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException;

    /**
     * Create new group in given context.
     *
     * @param ctx
     *                Context object.
     * @param grp
     *                Group which should be created.
     * @param auth
     *                Credentials for authenticating against server.
     * @return Group containing the id of the new group.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     */
    public Group create(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException;

    /**
     * Method for deleting group within given context.
     *
     * @param ctx
     *                Context object
     * @param grp
     *                Group which should be deleted from the server.
     * @param auth
     *                Credentials for authenticating against server.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     */
    public void delete(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException;

    /**
     * Delete group within given context.
     *
     * @param ctx
     *                Context object
     * @param grps
     *                Contains all groups which should be deleted from the
     *                server.
     * @param auth
     *                Credentials for authenticating against server.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     */
    public void delete(final Context ctx, final Group[] grps, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException;

    /**
     * Fetch a group from server.
     *
     * @param ctx
     *                Context object
     * @param grp
     *                the group to retrieve from server.
     * @param auth
     *                Credentials for authenticating against server.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @return The Group with its data.
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     */
    public Group getData(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException;

    /**
     * Fetch specified groups from server. Can be used to fetch group data
     * including extensions.
     *
     * @param ctx
     * @param grps
     * @param auth
     * @return Groups including extension data if requested!
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws NoSuchGroupException
     * @throws DatabaseUpdateException
     */
    public Group[] getData(final Context ctx, final Group[] grps, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchGroupException, DatabaseUpdateException;

    /**
     * Gets the default group of the specified context. This method obsoletes
     * the old variant which returned an int value
     *
     * @param ctx
     * @param auth
     * @return The id of the default group of the context
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public Group getDefaultGroup(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Get User IDs of the members of this group. This method obsoletes the old
     * variant which returned an int array.
     *
     * @param ctx
     *                Context object
     * @param grp
     *                group from which to retrieve the members.
     * @param auth
     *                Credentials for authenticating against server.
     * @return User IDs.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     */
    public User[] getMembers(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException;

    /**
     * List groups within context matching the pattern.
     *
     * @param ctx
     *                Context object.
     * @param pattern
     *                Search pattern to search for e.g. "*mygroup*"
     * @param auth
     *                Credentials for authenticating against server.
     * @return Groups which matched the supplied search pattern.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occured.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     */
    public Group[] list(final Context ctx, final String pattern, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException;

    /**
     * List all groups within context.
     *
     * @param ctx
     *                Context object.
     * @param auth
     *                Credentials for authenticating against server.
     * @return Groups which matched the supplied search pattern.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     */
    public Group[] listAll(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException;

    /**
     *
     * @param ctx
     *                Context object.
     * @param usr
     *                User object
     * @param auth
     *                Credentials for authenticating against server.
     * @return
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 if the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public Group[] listGroupsForUser(final Context ctx, final User usr, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Remove member(s) from group.
     *
     * @param ctx
     *                Context object
     * @param grp
     *                the group from which the members should be removed.
     * @param members
     *                User IDs.
     * @param auth
     *                Credentials for authenticating against server.
     * @throws RemoteException
     *                 General RMI Exception
     * @throws InvalidCredentialsException
     *                 When the supplied credentials were not correct or
     *                 invalid.
     * @throws NoSuchContextException
     *                 If the context does not exist in the system.
     * @throws StorageException
     *                 When an error in the subsystems occurred.
     * @throws InvalidDataException
     *                 If the data sent within the method contained invalid
     *                 data.
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     * @throws NoSuchUserException
     */
    public void removeMember(final Context ctx, final Group grp, final User[] members, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException;
}
