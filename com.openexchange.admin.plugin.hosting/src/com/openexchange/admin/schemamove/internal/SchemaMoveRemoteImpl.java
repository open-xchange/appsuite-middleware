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

package com.openexchange.admin.schemamove.internal;

import java.util.Map;
import org.osgi.framework.BundleContext;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingServiceException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.admin.schemamove.SchemaMoveService;
import com.openexchange.admin.schemamove.mbean.SchemaMoveRemote;

/**
 * {@link SchemaMoveRemoteImpl}
 *
 * @author <a href="lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class SchemaMoveRemoteImpl extends OXCommonImpl implements SchemaMoveRemote {

    private final SchemaMoveService schemaMoveService;

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchemaMoveRemoteImpl.class);

    private final BundleContext context;

    private final static String THE_SOURCE_SCHEMA_NAME_IS_NOT_GIVEN = "The source schema name is not given.";

    private final static String THE_TARGET_SCHEMA_NAME_IS_NOT_GIVEN = "The target schema name is not given.";

    private final static String THE_TARGET_CLUSTER_ID_IS_INVALID = "The target cluster id is invalid.";

    /**
     * Initializes a new {@link SchemaMoveRemoteImpl}.
     */
    public SchemaMoveRemoteImpl(BundleContext context, SchemaMoveService schemaMoveService) {
        super();
        this.schemaMoveService = schemaMoveService;
        this.context = context;
    }

    @Override
    public void disableSchema(final Credentials auth, String schemaName) throws TargetDatabaseException, StorageException, NoSuchObjectException, MissingServiceException, InvalidCredentialsException, InvalidDataException {
        try{
            doNullCheck(schemaName);
        } catch (InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_SOURCE_SCHEMA_NAME_IS_NOT_GIVEN);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

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

        new BasicAuthenticator(context).doAuthentication(auth);

        return schemaMoveService.getDbAccessInfoForSchema(schemaName);
    }

    @Override
    public Map<String, String> getDbAccessInfoForCluster(final Credentials auth, int clusterId) throws StorageException, NoSuchObjectException, InvalidCredentialsException, InvalidDataException {
        if (clusterId <= 0) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_TARGET_CLUSTER_ID_IS_INVALID);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

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

        new BasicAuthenticator(context).doAuthentication(auth);

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

        new BasicAuthenticator(context).doAuthentication(auth);

        schemaMoveService.restorePoolReferences(sourceSchema, targetSchema, targetClusterId);
    }

    @Override
    public String createSchema(final Credentials auth, int targetClusterId) throws StorageException, InvalidCredentialsException, InvalidDataException {
        if (targetClusterId <= 0) {
            final InvalidDataException invalidDataException = new InvalidDataException(THE_TARGET_CLUSTER_ID_IS_INVALID);
            LOG.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

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

        new BasicAuthenticator(context).doAuthentication(auth);

        schemaMoveService.invalidateContexts(schemaName, invalidateSession);
    }
}
