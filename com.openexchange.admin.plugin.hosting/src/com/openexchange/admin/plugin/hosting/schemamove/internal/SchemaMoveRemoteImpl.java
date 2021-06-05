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

package com.openexchange.admin.plugin.hosting.schemamove.internal;

import java.util.Map;
import com.openexchange.admin.plugin.hosting.exceptions.TargetDatabaseException;
import com.openexchange.admin.plugin.hosting.schemamove.SchemaMoveService;
import com.openexchange.admin.plugin.hosting.schemamove.mbean.SchemaMoveRemote;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingServiceException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;

/**
 * {@link SchemaMoveRemoteImpl}
 *
 * @author <a href="lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class SchemaMoveRemoteImpl extends OXCommonImpl implements SchemaMoveRemote {

    private final SchemaMoveService schemaMoveService;

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchemaMoveRemoteImpl.class);

    private final static String THE_SOURCE_SCHEMA_NAME_IS_NOT_GIVEN = "The source schema name is not given.";

    private final static String THE_TARGET_SCHEMA_NAME_IS_NOT_GIVEN = "The target schema name is not given.";

    private final static String THE_TARGET_CLUSTER_ID_IS_INVALID = "The target cluster id is invalid.";

    /**
     * Initializes a new {@link SchemaMoveRemoteImpl}.
     */
    public SchemaMoveRemoteImpl(SchemaMoveService schemaMoveService) {
        super();
        this.schemaMoveService = schemaMoveService;
    }

    @Override
    public void disableSchema(final Credentials auth, String schemaName) throws TargetDatabaseException, StorageException, NoSuchObjectException, MissingServiceException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(schemaName);
        } catch (InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_SOURCE_SCHEMA_NAME_IS_NOT_GIVEN);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

        schemaMoveService.disableSchema(schemaName);
    }

    @Override
    public Map<String, String> getDbAccessInfoForSchema(final Credentials auth, String schemaName) throws StorageException, NoSuchObjectException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(schemaName);
        } catch (InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_SOURCE_SCHEMA_NAME_IS_NOT_GIVEN);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

        return schemaMoveService.getDbAccessInfoForSchema(schemaName);
    }

    @Override
    public Map<String, String> getDbAccessInfoForCluster(final Credentials auth, int clusterId) throws StorageException, NoSuchObjectException, InvalidCredentialsException, InvalidDataException {
        if (clusterId <= 0) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_TARGET_CLUSTER_ID_IS_INVALID);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

        return schemaMoveService.getDbAccessInfoForCluster(clusterId);
    }

    @Override
    public void enableSchema(final Credentials auth, String schemaName) throws StorageException, NoSuchObjectException, MissingServiceException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(schemaName);
        } catch (InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_SOURCE_SCHEMA_NAME_IS_NOT_GIVEN);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

        schemaMoveService.enableSchema(schemaName);
    }

    @Override
    public void restorePoolReferences(final Credentials auth, String sourceSchema, String targetSchema, int targetClusterId) throws StorageException, InvalidCredentialsException, InvalidDataException {
        try {
            doNullCheck(sourceSchema);
        } catch (InvalidDataException e) {
            InvalidDataException invalidDataException = new InvalidDataException(THE_SOURCE_SCHEMA_NAME_IS_NOT_GIVEN);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        try {
            doNullCheck(targetSchema);
        } catch (InvalidDataException e) {
            InvalidDataException invalidDataException = new InvalidDataException(THE_TARGET_SCHEMA_NAME_IS_NOT_GIVEN);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

        schemaMoveService.restorePoolReferences(sourceSchema, targetSchema, targetClusterId);
    }

    @Override
    public String createSchema(final Credentials auth, int targetClusterId) throws StorageException, InvalidCredentialsException, InvalidDataException {
        if (targetClusterId <= 0) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_TARGET_CLUSTER_ID_IS_INVALID);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

        return schemaMoveService.createSchema(targetClusterId);
    }

    @Override
    public void invalidateContexts(final Credentials auth, String schemaName, boolean invalidateSession) throws StorageException, InvalidCredentialsException, InvalidDataException, MissingServiceException {
        try {
            doNullCheck(schemaName);
        } catch (InvalidDataException e) {
            InvalidDataException invalidDataException = new InvalidDataException(THE_SOURCE_SCHEMA_NAME_IS_NOT_GIVEN);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

        schemaMoveService.invalidateContexts(schemaName, invalidateSession);
    }
}
