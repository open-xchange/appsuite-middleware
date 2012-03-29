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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.publish.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tx.TransactionException;


/**
 * {@link SimInfostoreFacade}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimInfostoreFacade implements InfostoreFacade {

    private final Map<Integer, byte[]> files = new HashMap<Integer, byte[]>();

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#countDocuments(long, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public int countDocuments(long folderId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#exists(int, int, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public boolean exists(int id, int version, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getDelta(long, long, com.openexchange.groupware.infostore.utils.Metadata[], boolean, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, boolean ignoreDeleted, Context ctx, User user, UserConfiguration userConfig) throws OXException, SearchIteratorException, OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getDelta(long, long, com.openexchange.groupware.infostore.utils.Metadata[], com.openexchange.groupware.infostore.utils.Metadata, int, boolean, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public Delta<DocumentMetadata> getDelta(long folderId, long updateSince, Metadata[] columns, Metadata sort, int order, boolean ignoreDeleted, Context ctx, User user, UserConfiguration userConfig) throws OXException, SearchIteratorException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getDocument(int, int, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public InputStream getDocument(int id, int version, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        return new ByteArrayInputStream(files.get(id));
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getDocumentMetadata(int, int, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public DocumentMetadata getDocumentMetadata(int id, int version, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getDocuments(long, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getDocuments(long, com.openexchange.groupware.infostore.utils.Metadata[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getDocuments(long, com.openexchange.groupware.infostore.utils.Metadata[], com.openexchange.groupware.infostore.utils.Metadata, int, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getDocuments(int[], com.openexchange.groupware.infostore.utils.Metadata[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public TimedResult<DocumentMetadata> getDocuments(int[] ids, Metadata[] columns, Context ctx, User user, UserConfiguration userConfig) throws IllegalAccessException, OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getVersions(int, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public TimedResult<DocumentMetadata> getVersions(int id, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getVersions(int, com.openexchange.groupware.infostore.utils.Metadata[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public TimedResult<DocumentMetadata> getVersions(int id, Metadata[] columns, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#getVersions(int, com.openexchange.groupware.infostore.utils.Metadata[], com.openexchange.groupware.infostore.utils.Metadata, int, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public TimedResult<DocumentMetadata> getVersions(int id, Metadata[] columns, Metadata sort, int order, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#hasFolderForeignObjects(long, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.userconfiguration.UserConfiguration)
     */
    @Override
    public boolean hasFolderForeignObjects(long folderId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#isFolderEmpty(long, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public boolean isFolderEmpty(long folderId, Context ctx) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#lock(int, long, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void lock(int id, long diff, ServerSession sessionObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#removeDocument(long, long, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void removeDocument(long folderId, long date, ServerSession sessionObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#removeDocument(int[], long, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public int[] removeDocument(int[] id, long date, ServerSession sessionObj) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#removeUser(int, com.openexchange.groupware.contexts.Context, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void removeUser(int id, Context context, ServerSession session) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#removeVersion(int, int[], com.openexchange.tools.session.ServerSession)
     */
    @Override
    public int[] removeVersion(int id, int[] versionId, ServerSession sessionObj) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#saveDocument(com.openexchange.groupware.infostore.DocumentMetadata, java.io.InputStream, long, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, ServerSession sessionObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#saveDocument(com.openexchange.groupware.infostore.DocumentMetadata, java.io.InputStream, long, com.openexchange.groupware.infostore.utils.Metadata[], com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, ServerSession sessionObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#saveDocumentMetadata(com.openexchange.groupware.infostore.DocumentMetadata, long, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, ServerSession sessionObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#saveDocumentMetadata(com.openexchange.groupware.infostore.DocumentMetadata, long, com.openexchange.groupware.infostore.utils.Metadata[], com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, ServerSession sessionObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#setSessionHolder(com.openexchange.sessiond.impl.SessionHolder)
     */
    @Override
    public void setSessionHolder(SessionHolder sessionHolder) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#touch(int, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void touch(int id, ServerSession session) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.infostore.InfostoreFacade#unlock(int, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public void unlock(int id, ServerSession sessionObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tx.Service#commit()
     */
    @Override
    public void commit() throws TransactionException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tx.Service#finish()
     */
    @Override
    public void finish() throws TransactionException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tx.Service#rollback()
     */
    @Override
    public void rollback() throws TransactionException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tx.Service#setCommitsTransaction(boolean)
     */
    @Override
    public void setCommitsTransaction(boolean commits) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tx.Service#setRequestTransactional(boolean)
     */
    @Override
    public void setRequestTransactional(boolean transactional) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tx.Service#setTransactional(boolean)
     */
    @Override
    public void setTransactional(boolean transactional) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tx.Service#startTransaction()
     */
    @Override
    public void startTransaction() throws TransactionException {
        // TODO Auto-generated method stub

    }

    public void simulateDocument(int cid, int folder, int id, String string, byte[] bytes) {
        files.put(id, bytes);
    }

}
