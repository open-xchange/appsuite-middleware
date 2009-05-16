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

import static com.openexchange.publish.PublicationErrorMessage.SQLException;
import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.schema.Tables.publications;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.storage.GenericConfigStorageException;
import com.openexchange.datatypes.genericonf.storage.SimConfigurationStorageService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationStorage;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.SELECT;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class PublicationSQLStorage implements PublicationStorage {

    private DBProvider dbProvider;
    private SimPublicationTargetDiscoveryService discoveryService;
    private SimConfigurationStorageService storageService;

    public PublicationSQLStorage(DBProvider provider, SimConfigurationStorageService simConfigurationStorageService, SimPublicationTargetDiscoveryService discoveryService) {
        this.dbProvider = provider;
        this.storageService = simConfigurationStorageService;
        this.discoveryService = discoveryService;
    }

    public void forgetPublication(Publication publication) throws PublicationException {
        Connection writeConnection = null;
        try {
            writeConnection = dbProvider.getWriteConnection(publication.getContext());
            writeConnection.setAutoCommit(false);
            
            DELETE delete = new DELETE().FROM(publications).WHERE(new EQUALS("id", PLACEHOLDER).AND(new EQUALS("cid", PLACEHOLDER)));
            
            List<Object> values = new ArrayList<Object>();
            values.add(publication.getId());
            values.add(publication.getContext().getContextId());
            
            new StatementBuilder().executeStatement(writeConnection, delete, values);
            
            storageService.delete(writeConnection, publication.getContext(), publication.getId());
            
            writeConnection.commit();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            throw new PublicationException(e);
        } finally {
            if (writeConnection != null ) {
                try {
                    writeConnection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw SQLException.create(e);
                } finally {
                    dbProvider.releaseWriteConnection(publication.getContext(), writeConnection);
                }
            }
        }
    }

    public Publication getPublication(Context ctx, int publicationId) throws PublicationException {
        Publication retval = null;
        
        Connection readConnection = null;
        ResultSet resultSet = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id").
            FROM(publications).
            WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("id", PLACEHOLDER)));
            
            List<Object> values = new ArrayList<Object>();
            values.add(ctx.getContextId());
            values.add(publicationId);
            
            resultSet = new StatementBuilder().executeQuery(readConnection, select, values);
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
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                throw SQLException.create(e);
            } finally {
                dbProvider.releaseReadConnection(ctx, readConnection);
            }
        }
        
        return retval;
    }

    public List<Publication> getPublications(Context ctx, String module, int entityId) throws PublicationException {
        List<Publication> retval = null;
        
        Connection readConnection = null;
        ResultSet resultSet = null;
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id").
            FROM(publications).
            WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("module", PLACEHOLDER)).AND(new EQUALS("entity", PLACEHOLDER)));
            
            List<Object> values = new ArrayList<Object>();
            values.add(ctx.getContextId());
            values.add(module);
            values.add(entityId);
            
            resultSet = new StatementBuilder().executeQuery(readConnection, select, values);
            retval = parseResultSet(resultSet, ctx, readConnection);
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new PublicationException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
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
        try {
            readConnection = dbProvider.getReadConnection(ctx);
            SELECT select = new SELECT("id", "cid", "user_id", "entity", "module", "configuration_id", "target_id").
            FROM(publications).
            WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("target_id", PLACEHOLDER)));
            
            List<Object> values = new ArrayList<Object>();
            values.add(ctx.getContextId());
            values.add(publicationTarget);
            
            resultSet = new StatementBuilder().executeQuery(readConnection, select, values);
            retval = parseResultSet(resultSet, ctx, readConnection);
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            new PublicationException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
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
        Connection writeConnection = null;
        
        try {
            writeConnection = dbProvider.getWriteConnection(publication.getContext());
            writeConnection.setAutoCommit(false);
            
            int configId = storageService.save(writeConnection, publication.getContext(), publication.getConfiguration(), publication.getTarget().getFormDescription());
            
            int id = IDGenerator.getId(publication.getContext(), Types.PUBLICATION, writeConnection);
            
            INSERT insert = new INSERT().
            INTO(publications).
            SET("id", PLACEHOLDER).
            SET("cid", PLACEHOLDER).
            SET("user_id", PLACEHOLDER).
            SET("entity", PLACEHOLDER).
            SET("module", PLACEHOLDER).
            SET("configuration_id", PLACEHOLDER).
            SET("target_id", PLACEHOLDER);
            
            List<Object> values = new ArrayList<Object>();
            values.add(id);
            values.add(publication.getContext().getContextId());
            values.add(publication.getUserId());
            values.add(publication.getEntityId());
            values.add(publication.getModule());
            values.add(configId);
            values.add(publication.getTarget().getId());
            
            new StatementBuilder().executeStatement(writeConnection, insert, values);
            
            publication.setId(id);
            
            writeConnection.commit();
        } catch (SQLException e) {
            throw SQLException.create(e);
        } catch (AbstractOXException e) {
            throw new PublicationException(e);
        } finally {
            if (writeConnection != null ) {
                try {
                    writeConnection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw SQLException.create(e);
                } finally {
                    dbProvider.releaseWriteConnection(publication.getContext(), writeConnection);
                }
            }
        }
        
    }
    
    private List<Publication> parseResultSet(ResultSet resultSet, Context ctx, Connection readConnection) throws SQLException, GenericConfigStorageException {
        List<Publication> retval = new ArrayList<Publication>();
        
        while (resultSet.next()) {
            Publication publication = new Publication();
            publication.setContext(ctx);
            publication.setEntityId(resultSet.getInt("entity"));
            publication.setId(resultSet.getInt("id"));
            publication.setModule(resultSet.getString("module"));
            publication.setUserId(resultSet.getInt("user_id"));
            
            DynamicFormDescription form = new DynamicFormDescription();
            Map<String, Object> content = new HashMap<String, Object>();
            storageService.fill(readConnection, ctx, resultSet.getInt("configuration_id"), content, form);
            
            publication.setConfiguration(content);
            publication.setTarget(discoveryService.getTarget(resultSet.getString("target_id")));
            
            retval.add(publication);
        }
        
        return retval;
    }

}
