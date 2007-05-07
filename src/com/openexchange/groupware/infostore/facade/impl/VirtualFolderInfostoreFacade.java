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
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
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
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

@OXExceptionSource(
		classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_FACADE_IMPL_VIRTUALFOLDERINFOSTOREFACADE, 
		component = Component.INFOSTORE
		
)
public class VirtualFolderInfostoreFacade implements InfostoreFacade {

	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(VirtualFolderInfostoreFacade.class);
	
	public int countDocuments(long folderId, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		return 0;
	}

	public boolean exists(int id, int version, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		return false;
	}

	public Delta getDelta(long folderId, long updateSince, Metadata[] columns,
			boolean ignoreDeleted, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		return new DeltaImpl(SearchIteratorAdapter.EMPTY_ITERATOR,SearchIteratorAdapter.EMPTY_ITERATOR,SearchIteratorAdapter.EMPTY_ITERATOR,System.currentTimeMillis());
	}

	public Delta getDelta(long folderId, long updateSince, Metadata[] columns,
			Metadata sort, int order, boolean ignoreDeleted, Context ctx,
			User user, UserConfiguration userConfig) throws OXException {
		return new DeltaImpl(SearchIteratorAdapter.EMPTY_ITERATOR,SearchIteratorAdapter.EMPTY_ITERATOR,SearchIteratorAdapter.EMPTY_ITERATOR,System.currentTimeMillis());
	}

	public InputStream getDocument(int id, int version, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		virtualFolder(); return null;
	}

	public DocumentMetadata getDocumentMetadata(int id, int version,
			Context ctx, User user, UserConfiguration userConfig)
			throws OXException {
		virtualFolder(); return null;
	}

	public TimedResult getDocuments(long folderId, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getDocuments(long folderId, Metadata[] columns,
			Context ctx, User user, UserConfiguration userConfig)
			throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getDocuments(long folderId, Metadata[] columns,
			Metadata sort, int order, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getDocuments(int[] ids, Metadata[] columns, Context ctx,
			User user, UserConfiguration userConfig)
			throws IllegalAccessException, OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getVersions(int id, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getVersions(int id, Metadata[] columns, Context ctx,
			User user, UserConfiguration userConfig) throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public TimedResult getVersions(int id, Metadata[] columns, Metadata sort,
			int order, Context ctx, User user, UserConfiguration userConfig)
			throws OXException {
		return new TimedResultImpl(SearchIteratorAdapter.EMPTY_ITERATOR, System.currentTimeMillis());
	}

	public boolean hasFolderForeignObjects(long folderId, Context ctx,
			User user, UserConfiguration userConfig) throws OXException {
		return false;
	}

	public boolean isFolderEmpty(long folderId, Context ctx) throws OXException {
		return true;
	}

	public void lock(int id, long diff, SessionObject sessionObj)
			throws OXException {
		virtualFolder();
	}

	public void removeDocument(long folderId, long date,
			SessionObject sessionObj) throws OXException {
		virtualFolder();
	}

	public int[] removeDocument(int[] id, long date, SessionObject sessionObj)
			throws OXException {
		return id;
	}

	public void removeUser(int id, Context context, SessionObject session) throws OXException {
		
	}

	public int[] removeVersion(int id, int[] versionId, SessionObject sessionObj)
			throws OXException {
		return versionId;
	}

	public void saveDocument(DocumentMetadata document, InputStream data,
			long sequenceNumber, SessionObject sessionObj) throws OXException {
		virtualFolder();
	}

	public void saveDocument(DocumentMetadata document, InputStream data,
			long sequenceNumber, Metadata[] modifiedColumns,
			SessionObject sessionObj) throws OXException {
		virtualFolder();
	}

	public void saveDocumentMetadata(DocumentMetadata document,
			long sequenceNumber, SessionObject sessionObj) throws OXException {
		virtualFolder();
	}

	public void saveDocumentMetadata(DocumentMetadata document,
			long sequenceNumber, Metadata[] modifiedColumns,
			SessionObject sessionObj) throws OXException {
		virtualFolder();
	}
	
	public void unlock(int id, SessionObject sessionObj) throws OXException {
		
	}

	public void commit() throws TransactionException {
		
	}

	public void finish() throws TransactionException {
		
	}

	public void rollback() throws TransactionException {
		
	}

	public void setRequestTransactional(boolean transactional) {
		
	}

	public void setTransactional(boolean transactional) {
		
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
