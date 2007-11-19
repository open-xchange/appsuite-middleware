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



package com.openexchange.groupware.contact;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.server.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 ContactSql
 @author <a href="mailto:ben.pahne@comfire.de">Benjamin Frederic Pahne</a>

 */

public interface ContactSql {

	public String getWhere();
	
	public String getOrder();
	
	public String getSqlCommand();

	public String getSelect();

	public void setSelect(final String select);

	public void setOrder(final String order);

	public void setWhere(final String where);

	public void setFolder(final int folder);

	public void setObjectID(final int objectID);
	
	public void setReadOnlyOwnFolder(final int onlyown);

	public void setContactSearchObject(final ContactSearchObject cso);

	public void setObjectArray(final int[][] object_id);

	public void getInternalUsers();

	public void setInternalUser(final int userid);

	public void setStartCharacter(final String start_character);

	public void setStartCharacterField(final int field);

	public void setStartCharacterField(final String field);
		
	public void setSearchHabit(final String habit);

	public void getAllChangedSince(final long chs);

	public void getAllCreatedSince(final long crs);

	public void getAllSince(final long bs) throws OXException;
	
	public String buildContactSelectString(final int cols[]);

	public String getRangeSearch(String field, String a, String b, String sh);

	public StringBuilder buildAllFolderSearchString(int user, int[] group, Session so, Connection readcon) throws SQLException, DBPoolingException, OXException, SearchIteratorException;
	
	
	/**************** simple sql calls *********************/
	
	public String iFgetRightsSelectString();
	public String iFgetNumberOfContactsString();
	public String iFgetRightsSelectString(int uid, int cid);
	public String iFgetFolderSelectString(int fid, int cid);
	public String iFcontainsForeignObjectInFolder(int fid, int uid,int cid);
	public String iFtrashContactsFromFolderUpdateString(int fid, int cid);
	public String iFupdateContactImageString();
	public String iFwriteContactImage();
	public String iFgetContactImage(int contact_id, int cid);
	public String iFgetContactImageLastModified(int id, int cid);
	public String iFgetdeleteLinkEntriesByIdsString();
	public String iFwriteContactLinkArrayInsert();
	public String iFgetFillLinkArrayString(int id, int cid);
	public String iFdeleteDistributionListEntriesByIds(int cid);
	public String iFdeleteDistributionListEntriesByIds2();
	public String iFupdateDistributionListEntriesByIds();
	public String iFwriteDistributionListArrayInsert();
	public String iFfillDistributionListArray(int id, int cid);
	public StringBuilder iFgetContactById(StringBuilder sb);
	public StringBuilder iFperformContactStorageUpdate(StringBuilder update, long lmd, int id, int cid);
	public StringBuilder iFperformContactStorageInsert(StringBuilder insert_fields, StringBuilder insert_values, int user, long lmd, int cid, int id);
	public StringBuilder iFgetColsString(int[] cols);	
	public StringBuilder iFgetColsStringFromDeleteTable(int[] cols);
	public String iFdeleteContactObject(int oid, int cid);
	
	public void iFdeleteContact(int id, int cid, Statement del) throws SQLException;
	public void iFtrashContactsFromFolder(boolean deleteit,Statement del, int oid, int cid) throws SQLException;
	public void iFtrashDistributionList(boolean delete, int id, int cid, Statement smt) throws SQLException;
	public void iFtrashLinks(boolean delete,Statement smt, int id, int cid) throws SQLException;
	public void iFtrashImage(boolean delete, Statement smt, int id, int cid) throws SQLException;
	public void iFgiveUserContacToAdmin(final Statement smt, final int oid, final Session so, final int admin_fid) throws SQLException;
	public void iFtrashAllUserContacts(boolean delete, Statement del, int cid, int oid, int uid, ResultSet rs, Session so) throws SQLException;
	public void iFtrashAllUserContactsDeletedEntries(Statement del, int cid, int uid, Session so) throws SQLException;
	public void iFtrashAllUserContactsDeletedEntriesFromAdmin(Statement del, int cid, int uid) throws SQLException;
	
	public void iFtrashTheAdmin(final Statement del, final int cid, final int uid) throws SQLException;
	
}
