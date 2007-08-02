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
package com.openexchange.admin.rmi.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;


public abstract class OXContextCommonImpl extends OXCommonImpl {

    public OXContextCommonImpl() throws StorageException {
        super();
    }

    private final static Log log = LogFactory.getLog(OXContextCommonImpl.class);
    
    protected void createchecks(final Context ctx, final User admin_user, final OXToolStorageInterface tool) throws StorageException, ContextExistsException, InvalidDataException {
        if (tool.existsContext(ctx)) {
            throw new ContextExistsException("Context already exists!");
        }        
        
        try {
            if (!admin_user.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields in admin user not set: " + admin_user.getUnsetMembers());               
            }
        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }
        try {
            if (!ctx.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields in context not set: " + ctx.getUnsetMembers());               
            }
        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    protected abstract Context createmaincall(final Context ctx, final User admin_user, Database db) throws StorageException, InvalidDataException;

    protected Context createcommon(final Context ctx, final User admin_user, final Database db, final Credentials auth) throws InvalidCredentialsException, ContextExistsException, InvalidDataException, StorageException {
        try{
            doNullCheck(ctx,admin_user);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context or user not correct");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator().doAuthentication(auth);
        
        if(log.isDebugEnabled()){
            log.debug(ctx + " - " + admin_user + " ");
        }
        
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            createchecks(ctx, admin_user, tool);
            return createmaincall(ctx, admin_user, db);
        } catch (final ContextExistsException e) {
            log.error(e.getMessage(),e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
