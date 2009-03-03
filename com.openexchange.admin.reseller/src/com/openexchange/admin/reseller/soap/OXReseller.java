/**
 * 
 */
package com.openexchange.admin.reseller.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.HashSet;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.OXSOAPRMIMapper;


/**
 * SOAP Service implementing RMI Interface OXResellerInterface
 * 
 * @author choeger
 *
 */
public class OXReseller extends OXSOAPRMIMapper implements OXResellerInterface {

    /**
     * @throws RemoteException
     */
    public OXReseller() throws RemoteException {
        super(OXResellerInterface.class);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#change(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void change(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).change(adm, creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#create(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin create(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).create(adm, creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#delete(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void delete(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).delete(adm, creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getAvailableRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public HashSet<Restriction> getAvailableRestrictions(Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).getAvailableRestrictions(creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin getData(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).getData(adm, creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getMultipleData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin[], com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin[] getMultipleData(ResellerAdmin[] admins, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).getMultipleData(admins, creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getRestrictionsFromContext(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public HashSet<Restriction> getRestrictionsFromContext(Context ctx, Credentials creds) throws RemoteException, InvalidDataException, OXResellerException, StorageException, InvalidCredentialsException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).getRestrictionsFromContext(ctx, creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#initDatabaseRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void initDatabaseRestrictions(Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).initDatabaseRestrictions(creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#list(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin[] list(String search_pattern, Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).list(search_pattern, creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#removeDatabaseRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void removeDatabaseRestrictions(Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).removeDatabaseRestrictions(creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#updateDatabaseModuleAccessRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void updateDatabaseModuleAccessRestrictions(Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).updateDatabaseModuleAccessRestrictions(creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

}
