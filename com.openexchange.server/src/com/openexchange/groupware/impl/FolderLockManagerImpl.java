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

package com.openexchange.groupware.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.webdav.Lock;
import com.openexchange.groupware.infostore.webdav.LockManagerImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;

public class FolderLockManagerImpl extends LockManagerImpl<FolderLock> implements
        FolderLockManager {

    private final static String findLocks = "SELECT * FROM oxfolder_lock WHERE cid = ? AND ((entity = ?) OR (entity = ? AND depth = 1) OR (entity IN (%%path%%) AND depth = "+INFINITE+" ) )";

    public FolderLockManagerImpl(){
        super("oxfolder_lock");
    }

    public FolderLockManagerImpl(final DBProvider provider) {
        super(provider, "oxfolder_lock");
    }

    @Override
    public List<Lock> findLocks(final int entity, final Context ctx, final User user) throws OXException {
        return new ArrayList<Lock>(loadOwnLocks(Arrays.asList(Integer.valueOf(entity)), ctx, user).get(Integer.valueOf(entity)));
    }

    @Override
    public List<FolderLock> findAllLocks(final int entity, final Context ctx, final User user) throws OXException {
        return findFolderLocks(entity, ctx, user);
    }

    @Override
    public List<FolderLock> findFolderLocks(final int entity, final Context ctx, final User user) throws OXException {
        final FolderTreeUtil treeUtil = new FolderTreeUtilImpl(getProvider());
        List<Integer> path = treeUtil.getPath(entity, ctx, user);
        final int parent = path.get(path.size()-2).intValue();
        path = path.subList(0, path.size()-2);
        final String query = findLocks.replaceAll("%%path%%", join(path).toString());
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = getReadConnection(ctx);
            stmt = readCon.prepareStatement(query);
            set(1, stmt, null, Integer.valueOf(ctx.getContextId()), Integer.valueOf(entity), Integer.valueOf(parent));
            rs = stmt.executeQuery();
            final List<FolderLock> locks = new ArrayList<FolderLock>();
            while(rs.next()) {
                final FolderLock lock = newLock();
                fillLock(lock, rs);
                if(lock.getTimeout()<1) {
                    removeLock(lock.getId(), ctx);
                    lockExpired(lock);
                } else {
                    locks.add(lock);
                }
            }
            return locks;
        } catch (final SQLException x) {
            throw new OXException();
        } catch (final OXException e) {
            throw e;
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, readCon);
        }
    }

    @Override
    public Map<Integer, List<FolderLock>> loadOwnLocks(final List<Integer> entities, final Context ctx, final User user) throws OXException {
        return findLocksByEntity(entities, ctx);
    }

    @Override
    public void insertLock(final int entity, final Lock lock, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException{
        createLockForceId(entity, lock.getId(), lock.getTimeout(), lock.getScope(), lock.getType(), lock.getOwnerDescription(),ctx,user,Integer.valueOf(((FolderLock) lock).getDepth()));
    }

    @Override
    public int lock(final int entity, final long timeout, final Scope scope, final Type type, final int depth, final String ownerDesc, final Context ctx, final User user) throws OXException {
        return createLock(entity, timeout, scope, type, ownerDesc, ctx, user, Integer.valueOf(depth));
    }

    @Override
    public void unlock(final int id, final Context ctx, final User user) throws OXException {
        removeLock(id, ctx);
    }

    @Override
    protected FolderLock newLock() {
        return new FolderLock();
    }

    @Override
    protected void fillLock(final FolderLock lock, final ResultSet rs) throws SQLException {
        super.fillLock(lock, rs);
        lock.setDepth(rs.getInt("depth"));
    }

    @Override
    protected String initAdditionalFIND_BY_ENTITY(final String findByEntity) {
        return findByEntity.replaceAll("%%additional_fields%%", ", depth");
    }

    @Override
    protected String initAdditionalINSERT(String insert) {
        insert = initAdditionalFIND_BY_ENTITY(insert);
        return insert.replaceAll("%%additional_question_marks%%", ", ?");
    }

    @Override
    protected int getType(){
        return Types.INFOSTORE;
    }

    @Override
    public void removeAll(final int entity, final Context context, final User userObject) throws OXException {
        removeAllFromEntity(entity, context);
    }
}
