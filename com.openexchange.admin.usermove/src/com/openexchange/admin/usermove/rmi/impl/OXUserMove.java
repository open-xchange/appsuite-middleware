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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.admin.usermove.rmi.impl;

import static com.openexchange.java.Autoboxing.i;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.admin.rmi.impl.OXUser;
import com.openexchange.admin.usermove.rmi.OXUserMoveInterface;
import com.openexchange.exception.OXException;
import com.openexchange.user.copy.UserCopyService;

public class OXUserMove extends OXCommonImpl implements OXUserMoveInterface {

    private static final String THE_GIVEN_SOURCE_USER_OBJECT_HAS_NO_ID = "The given source user object has no id";

    private static final String THE_GIVEN_SOURCE_USER_OBJECT_IS_NULL = "The given source user object is null";

    private final static Log LOG = LogFactory.getLog(OXUser.class);

    private final BundleContext context;

    private final UserCopyService service;

    public OXUserMove(final BundleContext context, final UserCopyService service) throws StorageException {
        super();
        this.context = context;
        this.service = service;
    }

    public User moveUser(final User user, final Context src, final Context dest, final Credentials auth) throws InvalidDataException, InvalidCredentialsException, StorageException {
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_GIVEN_SOURCE_USER_OBJECT_IS_NULL);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        if (null == user.getId()) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_GIVEN_SOURCE_USER_OBJECT_HAS_NO_ID);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        contextcheck(src, "source");
        contextcheck(dest, "destination");

        new BasicAuthenticator(context).doAuthentication(auth);

        final int newUserId;
        try {
            newUserId = service.copyUser(i(src.getId()), i(dest.getId()), i(user.getId()));
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        }
        return new User(newUserId);
    }

    private final void contextcheck(final Context ctx, final String type) throws InvalidDataException {
        if (null == ctx || null == ctx.getId()) {
            final InvalidDataException e = new InvalidDataException("Client sent invalid " + type + " context data object");
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }
}
