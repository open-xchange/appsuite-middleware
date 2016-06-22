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

package com.openexchange.admin.user.copy.rmi.impl;

import static com.openexchange.java.Autoboxing.i;
import org.osgi.framework.BundleContext;
import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.UserExistsException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.admin.user.copy.rmi.OXUserCopyInterface;
import com.openexchange.exception.OXException;
import com.openexchange.user.copy.UserCopyService;

public class OXUserCopy extends OXCommonImpl implements OXUserCopyInterface {

    private static final String THE_GIVEN_SOURCE_USER_OBJECT_IS_NULL = "The given source user object is null";

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXUserCopy.class);

    private final BundleContext context;

    private final UserCopyService service;

    public OXUserCopy(final BundleContext context, final UserCopyService service) throws StorageException {
        super();
        this.context = context;
        this.service = service;
    }

    @Override
    public User copyUser(final User user, final Context src, final Context dest, final Credentials auth) throws InvalidDataException, InvalidCredentialsException, StorageException, NoSuchUserException, DatabaseUpdateException, NoSuchContextException, UserExistsException {
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_GIVEN_SOURCE_USER_OBJECT_IS_NULL);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            contextcheck(src, "source");
            contextcheck(dest, "destination");

            checkContextAndSchema(src);
            checkContextAndSchema(dest);

            try {
                setIdOrGetIDFromNameAndIdObject(src, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            user.testMandatoryCreateFieldsNull();
            final Integer userid = user.getId();


            if (!tool.existsUser(src, userid.intValue())) {
                final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user " + userid + " in context " + src.getId());
                LOG.error(noSuchUserException.getMessage(), noSuchUserException);
                throw noSuchUserException;
            }
            if (tool.existsUserName(dest, user.getName())) {
                final UserExistsException userExistsExeption = new UserExistsException("User " + user.getName() + " already exists in context " + dest.getId());
                LOG.error(userExistsExeption.getMessage(), userExistsExeption);
                throw userExistsExeption;
            }

        } catch (final InvalidDataException e1) {
            LOG.error(e1.getMessage(), e1);
            throw e1;
        } catch (final StorageException e1) {
            LOG.error(e1.getMessage(), e1);
            throw e1;
        }

        int srcContextId = i(src.getId());
        int dstContextId = i(dest.getId());
        int srcUserId = i(user.getId());

        final int newUserId;
        try {
            newUserId = service.copyUser(srcContextId, dstContextId, srcUserId);
        } catch (final OXException e) {
            LOG.error("", e);
            final StorageException storageException = new StorageException(e.getMessage());
            setStackTraceSafe(storageException, e);
            throw new StorageException(e.getMessage());
        }

        try {
            Filestore2UserUtil.copyFilestore2UserEntry(srcContextId, srcUserId, dstContextId, newUserId, AdminDaemon.getCache());
        } catch (Exception e) {
            LOG.info("Failed to copy filestore2User entry for user {} in context {} to user {} in context {}", user.getId(), src.getId(), newUserId, dest.getId());
        }

        LOG.info("User {} successfully copied to Context {} from Context {}", user.getId(), dest.getId(), src.getId());

        return new User(newUserId);
    }

    private void setStackTraceSafe(final Exception dest, final Exception src) {
        try {
            dest.setStackTrace(src.getStackTrace());
        } catch (final Exception x) {
            // Ignore
        }
    }

    private final void contextcheck(final Context ctx, final String type) throws InvalidDataException {
        if (null == ctx || null == ctx.getId()) {
            final InvalidDataException e = new InvalidDataException("Client sent invalid " + type + " context data object");
            LOG.error("", e);
            throw e;
        }
    }
}
