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

package com.openexchange.publish.sql;

import static com.openexchange.publish.PublicationErrorMessage.IDGiven;
import static com.openexchange.publish.PublicationErrorMessage.PublicationNotFound;
import static com.openexchange.publish.PublicationErrorMessage.SQLException;
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
import com.openexchange.datatypes.genericonf.storage.GenericConfigStorageException;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationStorage;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.sql.tools.SQLTools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class PublicationSQLStorage implements PublicationStorage {

    private DBProvider dbProvider;

    private PublicationTargetDiscoveryService discoveryService;

    private GenericConfigurationStorageService storageService;

    public PublicationSQLStorage(DBProvider provider, GenericConfigurationStorageService simConfigurationStorageService, PublicationTargetDiscoveryService discoveryService) {
        this.dbProvider = provider;
        this.storageService = simConfigurationStorageService;
        this.discoveryService = discoveryService;
    }

    public void forgetPublication(Publication publication) throws PublicationException {
        if (!exist(publication.getId(), publication.getContext())) {
            return;
        }

        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(publication.getContext());
            writeConnection.setAutoCommit(false);
            delete(publication, writeConnection);
            writeConnection.commit();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            throw new PublicationException(e);
        } finally {
            tryToClose(publication.getContext(), writeConnection);
        }
    }

    public Publication getPublication(Context ctx, int publicationId) throws PublicationException {
        Publication retval = null;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id").FROM(publications).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));

            List<Object> values = new ArrayList<Object>();
            values.add(ctx.getContextId());
            values.add(publicationId);

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            List<Publication> publications = parseResultSet(resultSet, ctx, readConnection);

            if (publications.size() > 0) {
                retval = publications.get(0);
            }
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new PublicationException(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    public List<Publication> getPublications(Context ctx, String module, String entityId) throws PublicationException {
        List<Publication> retval = null;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id").FROM(publications).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("module", PLACEHOLDER)).AND(new EQUALS("entity", PLACEHOLDER)));

            List<Object> values = new ArrayList<Object>();
            values.add(ctx.getContextId());
            values.add(module);
            values.add(entityId);

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = parseResultSet(resultSet, ctx, readConnection);
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new PublicationException(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    public List<Publication> getPublications(Context ctx, String publicationTarget) throws PublicationException {
        List<Publication> retval = null;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id").FROM(publications).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("target_id", PLACEHOLDER)));

            List<Object> values = new ArrayList<Object>();
            values.add(ctx.getContextId());
            values.add(publicationTarget);

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = parseResultSet(resultSet, ctx, readConnection);
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new PublicationException(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    public void rememberPublication(Publication publication) throws PublicationException {
        if (publication.getId() > 0) {
            throw IDGiven.create();
        }

        Connection writeConnection = null;

        try {
            writeConnection = dbProvider.getWriteConnection(publication.getContext());
            writeConnection.setAutoCommit(false);
            int id = save(publication, writeConnection);
            publication.setId(id);
            writeConnection.commit();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            throw new PublicationException(e);
        } finally {
            tryToClose(publication.getContext(), writeConnection);
        }
    }

    public void updatePublication(Publication publication) throws PublicationException {
        if (!exist(publication.getId(), publication.getContext())) {
            throw PublicationNotFound.create();
        }

        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(publication.getContext());
            writeConnection.setAutoCommit(false);
            update(publication, writeConnection);
            writeConnection.commit();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            throw new PublicationException(e);
        } finally {
           tryToClose(publication.getContext(), writeConnection);
        }
    }

    public Collection<Publication> search(Context ctx, String targetId, Map<String, Object> query) throws PublicationException {
        List<Publication> retval = new ArrayList<Publication>();
        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            List<Integer> configurationIds = storageService.search(readConnection, ctx, query);

            if (configurationIds.size() > 0) {
                SELECT select = new SELECT(ASTERISK).FROM(publications).WHERE(
                    new IN("configuration_id", SQLTools.createLIST(configurationIds.size(), PLACEHOLDER)).AND(new EQUALS(
                        "target_id",
                        PLACEHOLDER)));

                List<Object> values = new ArrayList<Object>();
                values.addAll(configurationIds);
                values.add(targetId);

                builder = new StatementBuilder();
                resultSet = builder.executeQuery(readConnection, select, values);

                retval.addAll(parseResultSet(resultSet, ctx, readConnection));
            }
        } catch (AbstractOXException e) {
            throw new PublicationException(e);
        } catch (SQLException e) {
            throw SQLException.create(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

    public void deletePublicationsOfUser(int userID, Context context) throws PublicationException {
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(context);
            writeConnection.setAutoCommit(false);
            deleteWhereUserID(userID, context, writeConnection);
            writeConnection.commit();
        } catch (GenericConfigStorageException e) {
            throw new PublicationException(e);
        } catch (PublicationException e){
            throw e;
        } catch (SQLException e) {
            
        } catch (TransactionException e) {
            throw new PublicationException(e);
        } finally {
            tryToClose(context, writeConnection);
        }
    }

    private void tryToClose(Context context, Connection writeConnection) throws PublicationException {
        if (writeConnection != null) {
            try {
                writeConnection.rollback();
                writeConnection.setAutoCommit(true);
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseWriteConnection(context, writeConnection);
            }
        }
    }

    private void deleteWhereUserID(int userid, Context context, Connection writeConnection) throws SQLException, GenericConfigStorageException, PublicationException {
        DELETE delete = new DELETE().FROM(publications).WHERE(new EQUALS("user_id", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER)));

        List<Object> values = new ArrayList<Object>();
        values.add(userid);
        values.add(context.getContextId());

        new StatementBuilder().executeStatement(writeConnection, delete, values);

        storageService.delete(writeConnection, context, userid);
    }

    private void delete(Publication publication, Connection writeConnection) throws SQLException, GenericConfigStorageException, PublicationException {
        DELETE delete = new DELETE().FROM(publications).WHERE(new EQUALS("id", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER)));

        List<Object> values = new ArrayList<Object>();
        values.add(publication.getId());
        values.add(publication.getContext().getContextId());

        new StatementBuilder().executeStatement(writeConnection, delete, values);

        storageService.delete(writeConnection, publication.getContext(), getConfigurationId(publication));
    }

    private int save(Publication publication, Connection writeConnection) throws GenericConfigStorageException, SQLException {
        int configId = storageService.save(writeConnection, publication.getContext(), publication.getConfiguration());

        int id = IDGenerator.getId(publication.getContext(), Types.PUBLICATION, writeConnection);

        INSERT insert = new INSERT().INTO(publications).SET("id", PLACEHOLDER).SET("cid", PLACEHOLDER).SET("user_id", PLACEHOLDER).SET(
            "entity",
            PLACEHOLDER).SET("module", PLACEHOLDER).SET("configuration_id", PLACEHOLDER).SET("target_id", PLACEHOLDER);

        List<Object> values = new ArrayList<Object>();
        values.add(id);
        values.add(publication.getContext().getContextId());
        values.add(publication.getUserId());
        values.add(publication.getEntityId());
        values.add(publication.getModule());
        values.add(configId);
        values.add(publication.getTarget().getId());

        new StatementBuilder().executeStatement(writeConnection, insert, values);
        return id;
    }

    private void update(Publication publication, Connection writeConnection) throws PublicationException, GenericConfigStorageException, SQLException {
        if (publication.getConfiguration() != null) {
            int configId = getConfigurationId(publication);
            storageService.update(writeConnection, publication.getContext(), configId, publication.getConfiguration());
        }

        UPDATE update = new UPDATE(publications);
        List<Object> values = new ArrayList<Object>();

        if (publication.getUserId() > 0) {
            update.SET("user_id", PLACEHOLDER);
            values.add(publication.getUserId());
        }
        if (publication.getEntityId() != null) {
            update.SET("entity", PLACEHOLDER);
            values.add(publication.getEntityId());
        }
        if (publication.getModule() != null) {
            update.SET("module", PLACEHOLDER);
            values.add(publication.getModule());
        }
        if (publication.getTarget() != null) {
            update.SET("target_id", PLACEHOLDER);
            values.add(publication.getTarget().getId());
        }

        update.WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));
        values.add(publication.getContext().getContextId());
        values.add(publication.getId());

        if (values.size() > 2) {
            new StatementBuilder().executeStatement(writeConnection, update, values);
        }
    }

    private List<Publication> parseResultSet(ResultSet resultSet, Context ctx, Connection readConnection) throws SQLException, GenericConfigStorageException, PublicationException {
        List<Publication> retval = new ArrayList<Publication>();

        while (resultSet.next()) {
            Publication publication = new Publication();
            publication.setContext(ctx);
            publication.setEntityId(resultSet.getString("entity"));
            publication.setId(resultSet.getInt("id"));
            publication.setModule(resultSet.getString("module"));
            publication.setUserId(resultSet.getInt("user_id"));

            Map<String, Object> content = new HashMap<String, Object>();
            storageService.fill(readConnection, ctx, resultSet.getInt("configuration_id"), content);

            publication.setConfiguration(content);
            publication.setTarget(discoveryService.getTarget(resultSet.getString("target_id")));

            retval.add(publication);
        }

        return retval;
    }

    private int getConfigurationId(Publication publication) throws PublicationException {
        int retval = 0;
        Connection readConection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConection = dbProvider.getReadConnection(publication.getContext());

            SELECT select = new SELECT("configuration_id").FROM(publications).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));

            List<Object> values = new ArrayList<Object>();
            values.add(publication.getContext().getContextId());
            values.add(publication.getId());

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConection, select, values);

            if (resultSet.next()) {
                retval = resultSet.getInt("configuration_id");
            }
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new PublicationException(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(publication.getContext(), readConection);
            }
        }
        return retval;
    }

    private boolean exist(int id, Context ctx) throws PublicationException {
        boolean retval = false;

        Connection readConnection = null;
        ResultSet resultSet = null;
        StatementBuilder builder = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id").FROM(publications).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));

            List<Object> values = new ArrayList<Object>();
            values.add(ctx.getContextId());
            values.add(id);

            builder = new StatementBuilder();
            resultSet = builder.executeQuery(readConnection, select, values);
            retval = resultSet.next();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new PublicationException(e);
        } finally {
            try {
                if (builder != null) {
                    builder.closePreparedStatement(null, resultSet);
                }
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }

        return retval;
    }

}
