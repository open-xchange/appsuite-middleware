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

package com.openexchange.groupware.infostore;

import java.io.InputStream;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurity;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tx.Service;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;

public interface InfostoreFacade extends Service{
	/**
	 * Special Version used if you want to retrieve the latest version of an infostore document
	 */
	public static int CURRENT_VERSION = -1;
	public static int NEW = -1;
	
	public static final int ASC = 1;
	public static final int DESC = -1;
	
	public boolean exists(int id, int version, Context ctx, User user, UserConfiguration userConfig) throws OXException;
	
	public DocumentMetadata getDocumentMetadata(int id, int version, Context ctx, User user, UserConfiguration userConfig) throws OXException;
	public void saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Session sessionObj) throws OXException ; // No modifiedColumns means all columns
	public void saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, Session sessionObj) throws OXException ;
	
	public InputStream getDocument(int id, int version, Context ctx, User user, UserConfiguration userConfig) throws OXException;
	public void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Session sessionObj) throws OXException ;
	public void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, Session sessionObj) throws OXException ;
	
	public void removeDocument(long folderId, long date, Session sessionObj) throws OXException;
	public int[] removeDocument(int id[], long date, Session sessionObj) throws OXException;
	public int[] removeVersion(int id, int[] versionId, Session sessionObj) throws OXException;
	
	public TimedResult getDocuments(long folderId, Context ctx, User user, UserConfiguration userConfig) throws OXException;
	public TimedResult getDocuments(long folderId, Metadata[] columns, Context ctx, User user, UserConfiguration userConfig) throws OXException;

	// order is either ASC or DESC
	public TimedResult getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, Context ctx, User user, UserConfiguration userConfig) throws OXException;

	public TimedResult getVersions(int id, Context ctx, User user, UserConfiguration userConfig) throws OXException;
	public TimedResult getVersions(int id, Metadata[] columns, Context ctx, User user, UserConfiguration userConfig) throws OXException;

	// order is either ASC or DESC
	public TimedResult getVersions(int id, Metadata[] columns, Metadata sort, int order, Context ctx, User user, UserConfiguration userConfig) throws OXException;

	public TimedResult getDocuments(int[] ids, Metadata[] columns, Context ctx, User user, UserConfiguration userConfig) throws IllegalAccessException, OXException;
	
	public Delta getDelta(long folderId, long updateSince, Metadata[] columns, boolean ignoreDeleted, Context ctx, User user, UserConfiguration userConfig) throws OXException;

	// order is either ASC or DESC
	public Delta getDelta(long folderId, long updateSince, Metadata[] columns, Metadata sort, int order, boolean ignoreDeleted, Context ctx, User user, UserConfiguration userConfig) throws OXException;
	
	public int countDocuments(long folderId, Context ctx, User user, UserConfiguration userConfig) throws OXException;
	
	public boolean hasFolderForeignObjects(long folderId, Context ctx, User user, UserConfiguration userConfig) throws OXException;
	public boolean isFolderEmpty(long folderId, Context ctx) throws OXException;

	public void removeUser(int id, Context context, Session session) throws OXException;

	public void unlock(int id, Session sessionObj) throws OXException;

	public void lock(int id, long diff, Session sessionObj) throws OXException;

}
