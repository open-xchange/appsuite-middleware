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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.admin.rmi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.auth.AuthenticationFactory;
import com.openexchange.admin.auth.AuthenticationInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 *
 * @author cutmasta
 */
public class BasicAuthenticator {
    
    private final static Log log = LogFactory.getLog (BasicAuthenticator.class);
    
    private AuthenticationInterface sqlAuth = null;
    private AuthenticationInterface fileAuth = null;
    
    /** */
    public BasicAuthenticator() {
        super();
        sqlAuth  = AuthenticationFactory.getInstanceSQL();
        fileAuth = AuthenticationFactory.getInstanceFile();
    }
    
    /**
     * Authenticates the master admin!
     * @param authdata
     * @throws InvalidCredentialsException
     */
    public void doAuthentication(Credentials authdata) throws InvalidCredentialsException{
        if(!fileAuth.authenticate(authdata)){
            final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException("Authentication failed for user " + authdata.getLogin());
            log.error("Master authentication: ", invalidCredentialsException);
            throw invalidCredentialsException;
        }
    }
    
    /**
     * 
     * Authenticates ONLY the context admin!
     * This method also validates the Context object data!
     * @param authdata
     * @param ctx
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws InvalidDataException 
     */
    public void doAuthentication(Credentials authdata,Context ctx) throws InvalidCredentialsException, StorageException, InvalidDataException{
        contextcheck(ctx);
        if(!sqlAuth.authenticate(authdata,ctx)){
            final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException("Authentication failed for user " + authdata.getLogin());
            log.error("Admin authentication: ", invalidCredentialsException);
            throw invalidCredentialsException;
        }
    }
    
    /**
     * Authenticates all users within a context!
     * This method also validates the Context object data!
     * @param authdata
     * @param ctx
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws InvalidDataException 
     */
    public void doUserAuthentication(Credentials authdata,Context ctx) throws InvalidCredentialsException, StorageException, InvalidDataException{
        contextcheck(ctx);
        if(!sqlAuth.authenticateUser(authdata,ctx)){
            final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException("Authentication failed for user " + authdata.getLogin());
            log.error("User authentication: ", invalidCredentialsException);
            throw invalidCredentialsException;
        }
    }

    protected final void contextcheck(final Context ctx) throws InvalidDataException {
        if (null == ctx || null == ctx.getIdAsInt()) {
            throw new InvalidDataException("The context object has invalid data");
        }
    }
    
    /**
     * @param objects
     * @throws InvalidDataException
     */
    protected final static void doNullCheck(Object...objects ) throws InvalidDataException
    {
        for (Object object : objects) {
            if(object==null){
                throw new InvalidDataException();
            }
        }
    }
    
}
