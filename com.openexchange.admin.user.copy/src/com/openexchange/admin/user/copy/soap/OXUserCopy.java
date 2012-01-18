package com.openexchange.admin.user.copy.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.UserExistsException;
import com.openexchange.admin.soap.OXSOAPRMIMapper;
import com.openexchange.admin.soap.SOAPUtils;
import com.openexchange.admin.soap.dataobjects.Context;
import com.openexchange.admin.soap.dataobjects.User;
import com.openexchange.admin.user.copy.rmi.OXUserCopyInterface;


public class OXUserCopy extends OXSOAPRMIMapper {

    /**
     * @throws RemoteException
     */
    public OXUserCopy() throws RemoteException {
        super(OXUserCopyInterface.class);
    }

    public User copyUser(final User user, final Context src, final Context dest, final Credentials auth) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, NoSuchUserException, DatabaseUpdateException, NoSuchContextException, UserExistsException {
        reconnect();
        try {
            return new User(((OXUserCopyInterface)rmistub).copyUser(SOAPUtils.soapUser2User(user), SOAPUtils.soapContext2Context(src), SOAPUtils.soapContext2Context(dest), auth));
        } catch (final ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

}
