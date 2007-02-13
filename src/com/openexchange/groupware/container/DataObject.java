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



package com.openexchange.groupware.container;

import java.util.Date;

/**
 * DataObject
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public abstract class DataObject extends SystemObject {
	
	public static final int OBJECT_ID = 1;
	
	public static final int CREATED_BY = 2;
	
	public static final int MODIFIED_BY = 3;
	
	public static final int CREATION_DATE = 4;
	
	public static final int LAST_MODIFIED = 5;	
	
	protected int objectId = 0;
	protected int createdBy = 0;
	protected int modifiedBy = 0;
	protected Date creationDate = null;
	protected Date lastModified = null;
	
	protected boolean b_object_id = false;
	protected boolean b_created_by = false;
	protected boolean b_modified_by = false;
	protected boolean b_creation_date = false;	
	protected boolean b_last_modified = false;	
	
	// GET METHODS
	public int getObjectID( ) {
		return objectId;
	}
	
	public int getCreatedBy( ) {
		return createdBy;
	}
	
	public int getModifiedBy() {
		return modifiedBy;
	}
	
	public Date getCreationDate( ) {
		return creationDate;
	}
	
	public Date getLastModified () {
		return lastModified;
	}
	
	// SET METHODS
	public void setObjectID( final int object_id ) {
		this.objectId = object_id;
		b_object_id = true;
	}
	
	public void setCreatedBy( final int created_by ) {
		this.createdBy = created_by;
		b_created_by = true;
	}
	
	public void setModifiedBy( final int modified_by ) {
		this.modifiedBy = modified_by;
		b_modified_by = true;
	}
	
	public void setCreationDate( final Date creation_date ) {
		this.creationDate = creation_date;
		b_creation_date = true;
	}
	
	public void setLastModified ( final Date last_modified ) {
		this.lastModified = last_modified;
		b_last_modified = true;
	}
	
	// REMOVE METHODS
	public void removeObjectID() {
		objectId = 0;
		b_object_id = false;
	}
	
	public void removeCreatedBy() {
		createdBy = 0;
		b_created_by = false;
	}
	
	public void removeModifiedBy() {
		modifiedBy = 0;
		b_modified_by = false;
	}
	
	public void removeCreationDate() {
		creationDate = null;
		b_creation_date = false;
	}
	
	public void removeLastModified() {
		lastModified = null;
		b_last_modified = false;
	}
	
	// CONTAINS METHODS
	public boolean containsObjectID() {
		return b_object_id;
	}
	
	public boolean containsCreatedBy() {
		return b_created_by;
	}
	
	public boolean containsModifiedBy() {
		return b_modified_by;
	}
	
	public boolean containsCreationDate() {
		return b_creation_date;
	}
	
	public boolean containsLastModified() {
		return b_last_modified;
	}
	
	public void reset() {
		objectId = 0;
		createdBy = 0;
		modifiedBy = 0;
		creationDate = null;
		lastModified = null;
		b_object_id = false;
		b_created_by = false;
		b_modified_by = false;
		b_creation_date = false;
		b_last_modified = false;
	}
}
