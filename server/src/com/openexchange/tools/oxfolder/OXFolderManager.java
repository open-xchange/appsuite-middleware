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

package com.openexchange.tools.oxfolder;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;

/**
 * OXFolderManager offers routines for folder creation, update and deletion
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OXFolderManager {

	/**
	 * Creates a folder filled with values from given folder object. <b>NOTE:</b>
	 * given instance of <tt>FolderObject</tt> is going to be completely
	 * filled from storage. Thus it does not matter if you further work on this
	 * routine's return value or with parameter value.
	 * 
	 * 
	 * @return an instance of <tt>FolderObject</tt> representing newly created
	 *         folder
	 */
	public FolderObject createFolder(FolderObject fo, boolean checkPermissions, long createTime) throws OXException;

	/**
	 * Updates an existing folder according to changes contained in given folder
	 * object. <b>NOTE:</b> given instance of <tt>FolderObject</tt> is going
	 * to be completely filled from storage. Thus it does not matter if you
	 * further work on this routine's return value or with parameter value.
	 * 
	 * <p>
	 * Possible operations here: rename, move and/or permissions update: When a
	 * rename should be performed, given folder object should contain field
	 * 'folder name', so that invokation of
	 * <tt>FolderObject.containsFolderName()</tt> returns <tt>true</tt>. If
	 * a move should be done, routine
	 * <tt>FolderObject.containsParentFolderID()</tt> should return
	 * <tt>true</tt>. Last, but not least, if an update of folder's
	 * permissions should be done, routine
	 * <tt>FolderObject.containsPermissions()</tt> should return <tt>true</tt>.
	 * </p>
	 * 
	 * @return an instance of <tt>FolderObject</tt> representing modified
	 *         folder
	 */
	public FolderObject updateFolder(FolderObject fo, boolean checkPermissions, long lastModified) throws OXException;

	/**
	 * Deletes a folder identifed by given folder object. This operation causes
	 * a recursive traversal of all folder's subfolders to check if user can
	 * delete them, too. Furthermore user's permission on contained objects are
	 * checked as well. <b>NOTE:</b> given instance of <tt>FolderObject</tt>
	 * is going to be completely filled from storage. Thus it does not matter if
	 * you further work on this routine's return value or with parameter value.
	 * 
	 * @return an instance of <tt>FolderObject</tt> representing deleted
	 *         folder
	 */
	public FolderObject deleteFolder(FolderObject fo, boolean checkPermissions, long lastModified) throws OXException;

	/**
	 * Clears a folder's content so that all items located in given folder are
	 * going to be deleted. <b>NOTE:</b> the returned instance of
	 * <tt>FolderObject</tt> is the parameter object itself. Thus it does not
	 * matter if you further work on this routine's return value or with
	 * parameter value.
	 * 
	 * @return the cleaned instance of <tt>FolderObject</tt>
	 */
	public FolderObject clearFolder(FolderObject fo, boolean checkPermissions, long lastModified)
			throws OXException;

}
