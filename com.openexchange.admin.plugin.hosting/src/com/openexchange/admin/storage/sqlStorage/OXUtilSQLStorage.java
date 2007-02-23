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
package com.openexchange.admin.storage.sqlStorage;

import com.openexchange.admin.rmi.dataobjects.Server;

import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;

/**
 * This class implements the global storage interface and creates a layer between the abstract
 * storage definition and a storage in a SQL accessible database
 * 
 * @author d7
 * @author cutmasta 
 *
 */
public abstract class OXUtilSQLStorage extends OXUtilStorageInterface {

	abstract public int registerFilestore(final Filestore fstore)  throws StorageException;
	
	abstract public void changeFilestore(final Filestore fstore) throws StorageException;
	
	abstract public Filestore[] listFilestores( final String search_pattern )  throws StorageException;
	
	abstract public void unregisterFilestore( final int store_id ) throws StorageException;
	
	abstract public int addMaintenanceReason( final MaintenanceReason reason )  throws StorageException;
	
	abstract public void deleteMaintenanceReason(final int[] reason_ids) throws StorageException;
	
	abstract public MaintenanceReason[] getMaintenanceReasons(final int[] reason_id) throws StorageException;
	
	abstract public MaintenanceReason[] getAllMaintenanceReasons() throws StorageException;
	
	abstract public int registerDatabase(final Database db) throws StorageException;
	
	abstract public void createDatabase( final Database db) throws StorageException;
	
	abstract public void deleteDatabase(final Database db) throws StorageException;
	
	abstract public int registerServer( final String serverName ) throws StorageException;
	
	abstract public void unregisterDatabase( final int db_id ) throws StorageException;
	
	abstract public void unregisterServer( final int server_id ) throws StorageException;
	
	abstract public Database[] searchForDatabase( final String search_pattern ) throws StorageException;
	
	abstract public Server[] searchForServer( final String search_pattern ) throws StorageException;

}
