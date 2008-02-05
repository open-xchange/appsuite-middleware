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

package com.openexchange.groupware.infostore.facade.impl;

import java.io.InputStream;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.results.TimedResultImpl;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

@OXExceptionSource(
		classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_FACADE_IMPL_VIRTUALFOLDERINFOSTOREFACADE, 
		component = Component.INFOSTORE
		
)
public class VirtualFolderInfostoreFacade implements InfostoreFacade {

	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(VirtualFolderInfostoreFacade.class);
	
	public int countDocuments(final long folderId, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		return 0;
	}

	public boolean exists(final int id, final int version, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		return false;
	}

	public Delta getDelta(final long folderId, final long updateSince, final Metadata[] columns,
			final boolean ignoreDeleted, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		return new DeltaImpl(SearchIteratorAdapter.EMPTY_ITERATOR,SearchIteratorAdapter.EMPTY_ITERATOR,SearchIteratorAdapter.EMPTY_ITERATOR,System.currentTimeMillis());
	}

	public Delta getDelta(final long folderId, final long updateSince, final Metadata[] columns,
			final Metadata sort, final int order, final boolean ignoreDeleted, final Context ctx,
			final User user, final UserConfiguration userConfig) throws OXException {
		return new DeltaImpl(SearchIteratorAdapter.EMPTY_ITERATOR,SearchIteratorAdapter.EMPTY_ITERATOR,SearchIteratorAdapter.EMPTY_ITERATOR,System.currentTimeMillis());
	}

	public InputStream getDocument(final int id, final int version, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		virtualFolder(); return null;
	}

	public DocumentMetadata getDocumentMetadata(final int id, final int version,
			final Context ctx, final User user, final UserConfiguration userConfig)
			throws OXException {
		virtualFolder(); return null;
	}

	public TimedResult getDocuments(final long folderId, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getDocuments(final long folderId, final Metadata[] columns,
			final Context ctx, final User user, final UserConfiguration userConfig)
			throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getDocuments(final long folderId, final Metadata[] columns,
			final Metadata sort, final int order, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getDocuments(final int[] ids, final Metadata[] columns, final Context ctx,
			final User user, final UserConfiguration userConfig)
			throws IllegalAccessException, OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getVersions(final int id, final Context ctx, final User user,
			final UserConfiguration userConfig) throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getVersions(final int id, final Metadata[] columns, final Context ctx,
			final User user, final UserConfiguration userConfig) throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getVersions(final int id, final Metadata[] columns, final Metadata sort,
			final int order, final Context ctx, final User user, final UserConfiguration userConfig)
			throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public boolean hasFolderForeignObjects(final long folderId, final Context ctx,
			final User user, final UserConfiguration userConfig) throws OXException {
		return false;
	}

	public boolean isFolderEmpty(final long folderId, final Context ctx) throws OXException {
		return true;
	}

	public void lock(final int id, final long diff, final ServerSession sessionObj)
			throws OXException {
		virtualFolder();
	}

	public void removeDocument(final long folderId, final long date,
			final ServerSession sessionObj) throws OXException {
		virtualFolder();
	}

	public int[] removeDocument(final int[] id, final long date, final ServerSession sessionObj)
			throws OXException {
		return id;
	}

	public void removeUser(final int id, final Context context, final ServerSession session) throws OXException {
		
	}

	public int[] removeVersion(final int id, final int[] versionId, final ServerSession sessionObj)
			throws OXException {
		return versionId;
	}

	public void saveDocument(final DocumentMetadata document, final InputStream data,
			final long sequenceNumber, final ServerSession sessionObj) throws OXException {
		virtualFolder();
	}

	public void saveDocument(final DocumentMetadata document, final InputStream data,
			final long sequenceNumber, final Metadata[] modifiedColumns,
			final ServerSession sessionObj) throws OXException {
		virtualFolder();
	}

	public void saveDocumentMetadata(final DocumentMetadata document,
			final long sequenceNumber, final ServerSession sessionObj) throws OXException {
		virtualFolder();
	}

	public void saveDocumentMetadata(final DocumentMetadata document,
			final long sequenceNumber, final Metadata[] modifiedColumns,
			final ServerSession sessionObj) throws OXException {
		virtualFolder();
	}
	
	public void unlock(final int id, final ServerSession sessionObj) throws OXException {
		
	}

	public void commit() throws TransactionException {
		
	}

	public void finish() throws TransactionException {
		
	}

	public void rollback() throws TransactionException {
		
	}

	public void setRequestTransactional(final boolean transactional) {
		
	}

	public void setTransactional(final boolean transactional) {
		
	}

	public void startTransaction() throws TransactionException {
		
	}
	
	@OXThrows(
			category=Category.USER_INPUT,
			desc="The folders to which this user has access, but that belong to other users, are collected in a virtual folder. This virtual folder cannot contain documents itself.",
			exceptionId=0,
			msg="This folder is a virtual folder. It cannot contain documents.")
	private void virtualFolder() throws OXException{
		throw EXCEPTIONS.create(0);
	}


}
