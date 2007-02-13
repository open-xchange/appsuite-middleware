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

import java.sql.Connection;
import java.sql.SQLException;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.EffectivePermission;
import com.openexchange.server.OCLPermission;

public class EffectiveInfostorePermission {
	private EffectivePermission permission;
	private User user;
	private DocumentMetadata document;

	public EffectiveInfostorePermission(EffectivePermission permission, DocumentMetadata document, User user) {
		this.document = document;
		this.user = user;
		this.permission = permission;
	}
	
	public boolean canReadObject(){
		return permission.canReadAllObjects() || (permission.canReadOwnObjects() && document.getCreatedBy() != user.getId());	
	}
	
	public boolean canDeleteObject() {
		return permission.canDeleteAllObjects() || (permission.canDeleteOwnObjects() && document.getCreatedBy() != user.getId());	
	}
	
	public boolean canWriteObject(){
		return permission.canWriteAllObjects() || (permission.canWriteOwnObjects() && document.getCreatedBy() != user.getId());	
	}

	public boolean canCreateObjects() {
		return permission.canCreateObjects();
	}

	public boolean canCreateSubfolders() {
		return permission.canCreateSubfolders();
	}

	public boolean canDeleteAllObjects() {
		return permission.canDeleteAllObjects();
	}

	public boolean canDeleteOwnObjects() {
		return permission.canDeleteOwnObjects();
	}

	public boolean canReadAllObjects() {
		return permission.canReadAllObjects();
	}

	public boolean canReadOwnObjects() {
		return permission.canReadOwnObjects();
	}

	public boolean canWriteAllObjects() {
		return permission.canWriteAllObjects();
	}

	public boolean canWriteOwnObjects() {
		return permission.canWriteOwnObjects();
	}

	public void deletePermission(Context ctx, Connection writeCon) throws SQLException {
		permission.deletePermission(ctx, writeCon);
	}

	public boolean equals(Object obj) {
		return permission.equals(obj);
	}

	public int getDeletePermission() {
		return permission.getDeletePermission();
	}

	public int getEntity() {
		return permission.getEntity();
	}

	public int getFolderPermission() {
		return permission.getFolderPermission();
	}

	public int getFuid() {
		return permission.getFuid();
	}

	public String getName() {
		return permission.getName();
	}

	public int getReadPermission() {
		return permission.getReadPermission();
	}

	public OCLPermission getUnderlyingPermission() {
		return permission.getUnderlyingPermission();
	}

	public int getWritePermission() {
		return permission.getWritePermission();
	}

	public int hashCode() {
		return permission.hashCode();
	}

	public boolean hasModuleAccess(int folderModule) {
		return permission.hasModuleAccess(folderModule);
	}

	public boolean isFolderAdmin() {
		return permission.isFolderAdmin();
	}

	public boolean isFolderVisible() {
		return permission.isFolderVisible();
	}

	public boolean isGroupPermission() {
		return permission.isGroupPermission();
	}

	public boolean loadPermissions(Context ctx, Connection con) throws SQLException {
		return permission.loadPermissions(ctx, con);
	}

	public boolean setAllObjectPermission(int pr, int pw, int pd) {
		return permission.setAllObjectPermission(pr, pw, pd);
	}

	public boolean setAllPermission(int fp, int opr, int opw, int opd) {
		return permission.setAllPermission(fp, opr, opw, opd);
	}

	public boolean setDeleteObjectPermission(int p) {
		return permission.setDeleteObjectPermission(p);
	}

	public void setEntity(int entity) {
		permission.setEntity(entity);
	}

	public void setFolderAdmin(boolean folderAdmin) {
		permission.setFolderAdmin(folderAdmin);
	}

	public boolean setFolderPermission(int p) {
		return permission.setFolderPermission(p);
	}

	public void setFuid(int pid) {
		permission.setFuid(pid);
	}

	public void setGroupPermission(boolean groupPermission) {
		permission.setGroupPermission(groupPermission);
	}

	public void setName(String name) {
		permission.setName(name);
	}

	public boolean setReadObjectPermission(int p) {
		return permission.setReadObjectPermission(p);
	}

	public boolean setWriteObjectPermission(int p) {
		return permission.setWriteObjectPermission(p);
	}

	public void storePermissions(Context ctx, Connection writeCon, boolean action) throws Exception {
		permission.storePermissions(ctx, writeCon, action);
	}

	public void storePermissions(Context ctx, Connection writeCon, int fuid, boolean insert) throws Exception {
		permission.storePermissions(ctx, writeCon, fuid, insert);
	}

	public String toString() {
		return permission.toString();
	}

	public int getObjectID() {
		return document.getId();
	}
	
	public DocumentMetadata getObject(){
		return document;
	}
}
