package com.openexchange.admin.usermove.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.OXSOAPRMIMapper;
import com.openexchange.admin.soap.SOAPUtils;
import com.openexchange.admin.soap.dataobjects.Context;
import com.openexchange.admin.soap.dataobjects.User;
import com.openexchange.admin.usermove.rmi.OXUserMoveInterface;
import com.openexchange.admin.usermove.rmi.exceptions.OXUserMoveException;


public class OXUserMove extends OXSOAPRMIMapper {

    /**
     * @throws RemoteException
     */
    public OXUserMove() throws RemoteException {
        super(OXUserMoveInterface.class);
    }

    public User moveUser(final User user, final Context src, final Context dest, final Credentials auth) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXUserMoveException {
        reconnect();
        try {
            return new User(((OXUserMoveInterface)rmistub).moveUser(SOAPUtils.soapUser2User(user), SOAPUtils.soapContext2Context(src), SOAPUtils.soapContext2Context(dest), auth));
        } catch (final ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

}
