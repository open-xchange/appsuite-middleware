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

package com.openexchange.publish.sql;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.schema.Tables.publications;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.publish.Entity;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationErrorMessage;
import com.openexchange.publish.PublicationStorage;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.Expression;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.LIST;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.sql.tools.SQLTools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class PublicationSQLStorage implements PublicationStorage {

    private final DBProvider dbProvider;

    private final DBTransactionPolicy txPolicy;

    private final PublicationTargetDiscoveryService discoveryService;

    private final GenericConfigurationStorageService storageService;

    public PublicationSQLStorage(final DBProvider provider, final GenericConfigurationStorageService simConfigurationStorageService, final PublicationTargetDiscoveryService discoveryService) {
        this(provider, DBTransactionPolicy.NORMAL_TRANSACTIONS, simConfigurationStorageService, discoveryService);
    }


    public PublicationSQLStorage(final DBProvider provider, final DBTransactionPolicy txPolicy, final GenericConfigurationStorageService simConfigurationStorageService, final PublicationTargetDiscoveryService discoveryService) {
        this.dbProvider = provider;
        this.storageService = simConfigurationStorageService;
        this.discoveryService = discoveryService;
        this.txPolicy = txPolicy;
    }

    @Override
    public void forgetPublication(final Publication publication) throws OXException {
        if (!exist(publication.getId(), publication.getContext())) {
            return;
        }

        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(publication.getContext());
            txPolicy.setAutoCommit(writeConnection, false);
            delete(publication, writeConnection);
            txPolicy.commit(writeConnection);
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            tryToClose(publication.getContext(), writeConnection);
        }
    }

    @Override
    public Publication getPublication(final Context ctx, final int publicationId) throws OXException {
        Publication retval = null;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final SELECT select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id", "enabled", "created").FROM(publications).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));
            values.add(I(publicationId));

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            final List<Publication> publications = parseResultSet(resultSet, ctx, readConnection);

            if (publications.size() > 0) {
                retval = publications.get(0);
            }
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw PublicationErrorMessage.SQL_ERROR.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    @Override
    public List<Publication> getPublications(final Context ctx, final String module, final String entityId) throws OXException {
        List<Publication> retval = null;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final SELECT select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id", "enabled", "created").FROM(publications).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("module", PLACEHOLDER)).AND(new EQUALS("entity", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));
            values.add(module);
            values.add(entityId);

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = parseResultSet(resultSet, ctx, readConnection);
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw PublicationErrorMessage.SQL_ERROR.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    @Override
    public List<Publication> getPublications(final Context ctx, final String publicationTarget) throws OXException {
        List<Publication> retval = null;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final SELECT select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id", "enabled", "created").FROM(publications).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("target_id", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));
            values.add(publicationTarget);

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = parseResultSet(resultSet, ctx, readConnection);
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw PublicationErrorMessage.SQL_ERROR.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    @Override
    public List<Publication> getPublicationsOfUser(final Context ctx, final int userId)  throws OXException {
        return getPublicationsOfUser(ctx, userId, null);
    }

    @Override
    public List<Publication> getPublicationsOfUser(final Context ctx, final int userId, final String module)  throws OXException {
        List<Publication> retval = null;

        final int contextId = ctx.getContextId();
        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);

            SELECT select;
            final List<Object> values = new ArrayList<Object>();
            if (module == null) {
                select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id", "enabled", "created").FROM(publications).WHERE(
                    new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("user_id", PLACEHOLDER)));

                values.add(I(contextId));
                values.add(I(userId));
            } else {
                select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id", "enabled", "created").FROM(publications).WHERE(
                    new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("user_id", PLACEHOLDER)).AND(new EQUALS("module", PLACEHOLDER)));

                values.add(I(contextId));
                values.add(I(userId));
                values.add(module);
            }


            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = parseResultSet(resultSet, ctx, readConnection);
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw PublicationErrorMessage.SQL_ERROR.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    @Override
    public void rememberPublication(final Publication publication) throws OXException {
        if (publication.getId() > 0) {
            throw PublicationErrorMessage.ID_GIVEN_EXCEPTION.create();
        }

        Connection writeConnection = null;

        try {
            writeConnection = dbProvider.getWriteConnection(publication.getContext());
            txPolicy.setAutoCommit(writeConnection, false);
            final int id = save(publication, writeConnection);
            publication.setId(id);
            txPolicy.commit(writeConnection);
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            tryToClose(publication.getContext(), writeConnection);
        }
    }

    @Override
    public void updatePublication(final Publication publication) throws OXException {
        if (!exist(publication.getId(), publication.getContext())) {
            throw PublicationErrorMessage.PUBLICATION_NOT_FOUND_EXCEPTION.create();
        }

        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(publication.getContext());
            txPolicy.setAutoCommit(writeConnection, false);
            update(publication, writeConnection);
            txPolicy.commit(writeConnection);
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            tryToClose(publication.getContext(), writeConnection);
        }
    }

    @Override
    public Collection<Publication> search(final Context ctx, final String targetId, final Map<String, Object> query) throws OXException {
        final List<Publication> retval = new ArrayList<Publication>();
        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final List<Integer> configurationIds = storageService.search(readConnection, ctx, query);

            if (configurationIds.size() > 0) {
                final SELECT select = new SELECT(ASTERISK).FROM(publications).WHERE(
                    new IN("configuration_id", SQLTools.createLIST(configurationIds.size(), PLACEHOLDER)).AND(new EQUALS(
                        "target_id",
                        PLACEHOLDER)).AND(new EQUALS("cid", PLACEHOLDER)));

                final List<Object> values = new ArrayList<Object>();
                values.addAll(configurationIds);
                values.add(targetId);
                values.add(ctx.getContextId());

                builder = new StatementBuilder();
                resultSet = builder.executeQuery(readConnection, select, values);

                retval.addAll(parseResultSet(resultSet, ctx, readConnection));
            }
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw PublicationErrorMessage.SQL_ERROR.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    @Override
    public void deletePublicationsOfUser(final int userID, final Context context) throws OXException {
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(writeConnection, false);
            deleteWhereUserID(userID, context, writeConnection);
            txPolicy.commit(writeConnection);
        } catch (final OXException e) {
            throw e;
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            tryToClose(context, writeConnection);
        }
    }

    @Override
    public void deletePublicationsInContext(final int contextId, final Context ctx) throws OXException {
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(ctx);
            deleteWhereContextID(contextId, ctx, writeConnection);
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            tryToClose(ctx, writeConnection);
        }
    }

    private void tryToClose(final Context context, final Connection writeConnection) throws OXException {
        if (writeConnection != null) {
            try {
                if (!writeConnection.getAutoCommit()) {
                    txPolicy.rollback(writeConnection);
                }
                txPolicy.setAutoCommit(writeConnection, true);
            } catch (final SQLException e) {
                throw PublicationErrorMessage.SQL_ERROR.create(e);
            } finally {
                dbProvider.releaseWriteConnection(context, writeConnection);
            }
        }
    }

    private void deleteWhereUserID(final int userid, final Context context, final Connection writeConnection) throws SQLException, OXException, OXException {
        final List<Publication> publicated = getPublicationsOfUser(context, userid);
        for(final Publication pub: publicated){
            delete(pub,writeConnection);
            storageService.delete(writeConnection, context, getConfigurationId(pub));
        }
    }

    private void deleteWhereContextID(final int contextId, final Context ctx, final Connection writeConnection) throws SQLException, OXException {
        final DELETE delete = new DELETE().FROM(publications).WHERE(new EQUALS("cid", PLACEHOLDER));

        final List<Object> values = new ArrayList<Object>();
        values.add(I(ctx.getContextId()));

        new StatementBuilder().executeStatement(writeConnection, delete, values);

        storageService.delete(writeConnection, ctx);
    }

    private void delete(final Publication publication, final Connection writeConnection) throws SQLException, OXException, OXException {
        final DELETE delete = new DELETE().FROM(publications).WHERE(new EQUALS("id", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER)));

        final List<Object> values = new ArrayList<Object>();
        values.add(I(publication.getId()));
        values.add(I(publication.getContext().getContextId()));

        new StatementBuilder().executeStatement(writeConnection, delete, values);

        storageService.delete(writeConnection, publication.getContext(), getConfigurationId(publication));
    }

    private int save(final Publication publication, final Connection writeConnection) throws OXException, SQLException {
        final int configId = storageService.save(writeConnection, publication.getContext(), publication.getConfiguration());

        final int id = IDGenerator.getId(publication.getContext(), Types.PUBLICATION, writeConnection);

        final INSERT insert = new INSERT().INTO(publications).SET("id", PLACEHOLDER).SET("cid", PLACEHOLDER).SET("user_id", PLACEHOLDER).SET(
            "entity",
            PLACEHOLDER).SET("module", PLACEHOLDER).SET("configuration_id", PLACEHOLDER).SET("target_id", PLACEHOLDER).SET("enabled", PLACEHOLDER).SET("created", PLACEHOLDER);

        final List<Object> values = new ArrayList<Object>();
        values.add(I(id));
        values.add(I(publication.getContext().getContextId()));
        values.add(I(publication.getUserId()));
        values.add(publication.getEntityId());
        values.add(publication.getModule());
        values.add(I(configId));
        values.add(publication.getTarget().getId());
        values.add(publication.isEnabled());
        values.add(System.currentTimeMillis());

        new StatementBuilder().executeStatement(writeConnection, insert, values);
        return id;
    }

    private void update(final Publication publication, final Connection writeConnection) throws OXException, OXException, SQLException {
        if (publication.getConfiguration() != null) {
            final int configId = getConfigurationId(publication);
            storageService.update(writeConnection, publication.getContext(), configId, publication.getConfiguration());
        }

        final UPDATE update = new UPDATE(publications);
        final List<Object> values = new ArrayList<Object>();

        if (publication.containsUserId()) {
            update.SET("user_id", PLACEHOLDER);
            values.add(I(publication.getUserId()));
        }
        if (publication.containsEntityId()) {
            update.SET("entity", PLACEHOLDER);
            values.add(publication.getEntityId());
        }
        if (publication.containsModule()) {
            update.SET("module", PLACEHOLDER);
            values.add(publication.getModule());
        }
        if (publication.containsTarget()) {
            update.SET("target_id", PLACEHOLDER);
            values.add(publication.getTarget().getId());
        }

        if (publication.containsEnabled()) {
            update.SET("enabled", PLACEHOLDER);
            values.add(publication.isEnabled());
        }

        update.WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));
        values.add(I(publication.getContext().getContextId()));
        values.add(I(publication.getId()));

        if (values.size() > 2) {
            new StatementBuilder().executeStatement(writeConnection, update, values);
        }
    }

    private List<Publication> parseResultSet(final ResultSet resultSet, final Context ctx, final Connection readConnection) throws SQLException, OXException, OXException {
        final List<Publication> retval = new ArrayList<Publication>();

        while (resultSet.next()) {
            final Publication publication = new Publication();
            publication.setContext(ctx);
            publication.setEntityId(resultSet.getString("entity"));
            publication.setId(resultSet.getInt("id"));
            publication.setModule(resultSet.getString("module"));
            publication.setUserId(resultSet.getInt("user_id"));
            publication.setEnabled(resultSet.getBoolean("enabled"));
            publication.setCreated(resultSet.getLong("created"));

            final Map<String, Object> content = new HashMap<String, Object>();
            storageService.fill(readConnection, ctx, resultSet.getInt("configuration_id"), content);

            publication.setConfiguration(content);
            publication.setTarget(discoveryService.getTarget(resultSet.getString("target_id")));

            retval.add(publication);
        }

        return retval;
    }

    private int getConfigurationId(final Context ctx, final int contextId, final int publicationId) throws OXException {
        int retval = 0;
        Connection readConection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConection = dbProvider.getReadConnection(ctx);

            final SELECT select = new SELECT("configuration_id").FROM(publications).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(contextId));
            values.add(I(publicationId));

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConection, select, values);

            if (resultSet.next()) {
                retval = resultSet.getInt("configuration_id");
            }
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw PublicationErrorMessage.SQL_ERROR.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConection);
            }
        }
        return retval;
    }


    private int getConfigurationId(final Publication publication) throws OXException {
        return getConfigurationId(publication.getContext(), publication.getContext().getContextId(), publication.getId());
    }

    private boolean exist(final int id, final Context ctx) throws OXException {
        boolean retval = false;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final SELECT select = new SELECT("id").FROM(publications).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));
            values.add(I(id));

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = resultSet.next();
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw PublicationErrorMessage.SQL_ERROR.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    @Override
    public Map<Entity, Boolean> isPublished(final List<Entity> entities, final Context ctx) throws OXException {
        final Map<Entity, Boolean> retval = new HashMap<Entity, Boolean>();
        for (final Entity entity : entities) {
            retval.put(entity, Boolean.FALSE);
        }

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            final ArrayList<Expression> placeholders = new ArrayList<Expression>(entities.size());
            for(int i = 0; i < entities.size(); i++) {
                placeholders.add(PLACEHOLDER);
            }
            final SELECT select = new SELECT("module, entity").FROM(publications).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new IN("entity", new LIST(placeholders))));

            final List<Object> values = new ArrayList<Object>();
            values.add(I(ctx.getContextId()));
            for (final Entity entity : entities) {
                values.add(entity.getId());
            }

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            while (resultSet.next()) {
                final String entityType = resultSet.getString(1);
                final int entityId = resultSet.getInt(2);
                final Entity found = new Entity(entityType, entityId);
                if (retval.containsKey(found)) {
                    retval.put(found, Boolean.TRUE);
                }
            }
        } catch (final SQLException e) {
            throw PublicationErrorMessage.SQL_ERROR.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (final SQLException e) {
                throw PublicationErrorMessage.SQL_ERROR.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }
        return retval;
    }

}
