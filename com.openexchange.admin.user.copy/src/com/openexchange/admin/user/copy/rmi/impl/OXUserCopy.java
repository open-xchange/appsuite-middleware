/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.user.copy.rmi.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.rmi.RemoteException;
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
import com.openexchange.admin.rmi.exceptions.RemoteExceptionUtils;
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

    private final UserCopyService service;

    public OXUserCopy(final UserCopyService service) {
        super();
        this.service = service;
    }

    @Override
    public User copyUser(final User user, final Context src, final Context dest, final Credentials auth) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, NoSuchUserException, DatabaseUpdateException, NoSuchContextException, UserExistsException {
        try {
            doNullCheck(user);
        } catch (InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_GIVEN_SOURCE_USER_OBJECT_IS_NULL);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

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

        } catch (InvalidDataException e) {
            LOG.error("", e);
            throw e;
        } catch (StorageException e) {
            LOG.error("", e);
            throw e;
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw RemoteExceptionUtils.convertException(e);
        }

        int srcContextId = i(src.getId());
        int dstContextId = i(dest.getId());
        int srcUserId = i(user.getId());

        final int newUserId;
        try {
            newUserId = service.copyUser(srcContextId, dstContextId, srcUserId);
        } catch (OXException e) {
            LOG.error("", e);
            final StorageException storageException = new StorageException(e.getMessage());
            setStackTraceSafe(storageException, e);
            throw new StorageException(e.getMessage());
        }

        try {
            Filestore2UserUtil.copyFilestore2UserEntry(srcContextId, srcUserId, dstContextId, newUserId, AdminDaemon.getCache());
        } catch (Exception e) {
            LOG.info("Failed to copy filestore2User entry for user {} in context {} to user {} in context {}", user.getId(), src.getId(), I(newUserId), dest.getId());
        }

        LOG.info("User {} successfully copied to Context {} from Context {}", user.getId(), dest.getId(), src.getId());

        return new User(newUserId);
    }

    private void setStackTraceSafe(final Exception dest, final Exception src) {
        try {
            dest.setStackTrace(src.getStackTrace());
        } catch (Exception x) {
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
