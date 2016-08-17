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
package com.openexchange.admin.rmi.impl;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.NameAndIdObject;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;

/**
 * General abstraction class used by all impl classes
 *
 * @author d7
 */
public abstract class OXCommonImpl {

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXCommonImpl.class);

    protected final OXToolStorageInterface tool;

    public OXCommonImpl() {
        tool = OXToolStorageInterface.getInstance();
    }

    protected final void contextcheck(final Context ctx) throws InvalidCredentialsException {
        if (null == ctx || null == ctx.getId()) {
            final InvalidCredentialsException e = new InvalidCredentialsException("Client sent invalid context data object");
            LOGGER.error("", e);
            throw e;
        }
    }

    protected void setUserIdInArrayOfUsers(final Context ctx, final User[] users) throws InvalidDataException, StorageException, NoSuchObjectException {
        for (final User user : users) {
            setIdOrGetIDFromNameAndIdObject(ctx, user);
        }
    }

    protected void setIdOrGetIDFromNameAndIdObject(final Context ctx, final NameAndIdObject nameandid) throws StorageException, InvalidDataException, NoSuchObjectException {
        final Integer id = nameandid.getId();
        if (null == id) {
            final String name = nameandid.getName();
            if (null != name) {
                if (nameandid instanceof User) {
                    try {
                        nameandid.setId(I(tool.getUserIDByUsername(ctx, name)));
                    } catch (NoSuchUserException e) {
                        throw new NoSuchObjectException(e);
                    }
                } else if (nameandid instanceof Group) {
                    try {
                        nameandid.setId(I(tool.getGroupIDByGroupname(ctx, name)));
                    } catch (NoSuchGroupException e) {
                        throw new NoSuchObjectException(e);
                    }
                } else if (nameandid instanceof Resource) {
                    try {
                        nameandid.setId(I(tool.getResourceIDByResourcename(ctx, name)));
                    } catch (NoSuchResourceException e) {
                        throw new NoSuchObjectException(e);
                    }
                } else if (nameandid instanceof Context) {
                    nameandid.setId(I(tool.getContextIDByContextname(name)));
                } else if (nameandid instanceof Database) {
                    nameandid.setId(I(tool.getDatabaseIDByDatabasename(name)));
                } else if (nameandid instanceof Server) {
                    nameandid.setId(I(tool.getServerIDByServername(name)));
                }
            } else {
                final String simpleName = nameandid.getClass().getSimpleName().toLowerCase();
                throw new InvalidDataException("One " + simpleName + "object has no " + simpleName + "id or " + simpleName + "name");
            }
        }
    }

    /**
     * @param objects
     * @throws InvalidDataException
     */
    protected final static void doNullCheck(final Object... objects) throws InvalidDataException {
        for (final Object object : objects) {
            if (object == null) {
                throw new InvalidDataException();
            }
        }
    }

    /**
     * Checks whether the context exists and updates the schema if needed
     * @param ctx
     * @throws StorageException
     * @throws com.openexchange.admin.rmi.exceptions.DatabaseUpdateException
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException
     */
    protected void checkContextAndSchema(final Context ctx) throws StorageException, DatabaseUpdateException, NoSuchContextException {
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException("The context " + ctx.getId() + " does not exist!");
        }
        if (tool.checkAndUpdateSchemaIfRequired(ctx)) {
            DatabaseUpdateException e = tool.generateDatabaseUpdateException(ctx.getId().intValue());
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

}
