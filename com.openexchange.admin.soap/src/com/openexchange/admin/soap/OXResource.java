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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
package com.openexchange.admin.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.dataobjects.Context;
import com.openexchange.admin.soap.dataobjects.Resource;

/**
 * SOAP Service implementing RMI Interface OXResourceInterface
 *
 * @author choeger
 *
 */
/*
 * Note: cannot implement interface OXResourceInterface because method
 * overloading is not supported
 */
public class OXResource extends OXSOAPRMIMapper {

    public OXResource() throws RemoteException {
        super(OXResourceInterface.class);
    }

    /**
     * Same as {@link OXResourceInterface#change(Context, Resource, Credentials)}
     *
     * @param ctx
     * @param res
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchResourceException
     */
    public void change(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        reconnect();
        try {
            ((OXResourceInterface)rmistub).change(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResource2Resource(res), auth);
        } catch( ConnectException e) {
            reconnect(true);
            ((OXResourceInterface)rmistub).change(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResource2Resource(res), auth);
        }
    }

    /**
     * Same as {@link OXResourceInterface#create(Context, Resource, Credentials)}
     *
     * @param ctx
     * @param res
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public Resource create(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return new Resource(((OXResourceInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResource2Resource(res), auth));
        } catch( ConnectException e) {
            reconnect(true);
            return new Resource(((OXResourceInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResource2Resource(res), auth));
        }
    }

    /**
     * Same as {@link OXResourceInterface#delete(Context, Resource, Credentials)}
     *
     * @param ctx
     * @param res
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchResourceException
     */
    public void delete(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        reconnect();
        try {
            ((OXResourceInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResource2Resource(res), auth);
        } catch( ConnectException e) {
            reconnect(true);
            ((OXResourceInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResource2Resource(res), auth);
        }
    }

    /**
     * Same as {@link OXResourceInterface#getData(Context, Resource, Credentials)}
     *
     * @param ctx
     * @param res
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchResourceException
     */
    public Resource getData(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        reconnect();
        try {
            return new Resource(((OXResourceInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResource2Resource(res), auth));
        } catch( ConnectException e) {
            reconnect(true);
            return new Resource(((OXResourceInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResource2Resource(res), auth));
        }
    }

    /**
     * Same as {@link OXResourceInterface#getData(Context, Resource[], Credentials)}
     *
     * @param ctx
     * @param resources
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws NoSuchResourceException
     * @throws DatabaseUpdateException
     */
    public Resource[] getMultipleData(final Context ctx, final Resource[] resources, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchResourceException, DatabaseUpdateException {
        reconnect();
        try {
            return SOAPUtils.resources2SoapResources(((OXResourceInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResources2Resources(resources), auth));
        } catch( ConnectException e) {
            reconnect(true);
            return SOAPUtils.resources2SoapResources(((OXResourceInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapResources2Resources(resources), auth));
        }
    }

    /**
     * Same as {@link OXResourceInterface#list(Context, String, Credentials)}
     *
     * @param ctx
     * @param pattern
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public Resource[] list(final Context ctx, final String pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return SOAPUtils.resources2SoapResources(((OXResourceInterface)rmistub).list(SOAPUtils.soapContext2Context(ctx), pattern, auth));
        } catch( ConnectException e) {
            reconnect(true);
            return SOAPUtils.resources2SoapResources(((OXResourceInterface)rmistub).list(SOAPUtils.soapContext2Context(ctx), pattern, auth));
        }
    }

    /**
     * Same as {@link OXResourceInterface#listAll(Context, Credentials)}
     *
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public Resource[] listAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return list(ctx, "*", auth);
        } catch( ConnectException e) {
            reconnect(true);
            return list(ctx, "*", auth);
        }
    }

}
