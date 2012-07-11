

package com.openexchange.custom.parallels.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.OXLoginInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.OXSOAPRMIMapper;
import com.openexchange.custom.parallels.soap.dataobjects.Bundle;


public class OXServer extends OXSOAPRMIMapper {

    public OXServer() throws RemoteException {
        // RMI will only be used for login
        super(OXLoginInterface.class);
    }

    /**
     * Return the complete list of the server bundles
     * 
     * @param auth
     *             Credentials for authenticating against server.
     * @return
     *             An array of {@link Bundle}, describing the bundles
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws RemoteException
     *             General RMI Exception
     * @throws InvalidDataException
     * @throws StorageException
     */
    public Bundle[] getServerBundleList(final Credentials auth) throws RemoteException, InvalidCredentialsException, StorageException, InvalidDataException {
        reconnect();
        try {
            ((OXLoginInterface)rmistub).login(auth);
        } catch (final ConnectException e) {
            reconnect(true);
            ((OXLoginInterface)rmistub).login(auth);
        }
        try {
            final JMXHelper jmxHelper = new JMXHelper();
            return jmxHelper.listBundles();
        } catch (final Exception e) {
            log.error("Can not connect to JMX interface: " + e, e);
            throw new ConnectException("Can not connect to JMX interface: " + e);
        }
    }

    /**
     * Returns the version number of the server
     * 
     * @param auth
     *             Credentials for authenticating against server.
     * @return The version number
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws RemoteException
     *             General RMI Exception
     * @throws InvalidDataException
     * @throws StorageException
     */
    public String getServerVersion(final Credentials auth) throws RemoteException, InvalidCredentialsException, StorageException, InvalidDataException {
        reconnect();
        try {
            ((OXLoginInterface)rmistub).login(auth);
        } catch (final ConnectException e) {
            reconnect(true);
            ((OXLoginInterface)rmistub).login(auth);
        }
        try {
            final JMXHelper jmxHelper = new JMXHelper();
            return jmxHelper.getServerVersion();
        } catch (final Exception e) {
            log.error("Can not connect to JMX interface: " + e, e);
            throw new ConnectException("Can not connect to JMX interface: " + e);
        }
    }

}
