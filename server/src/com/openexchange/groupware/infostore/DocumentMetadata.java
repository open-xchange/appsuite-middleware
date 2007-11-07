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

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public interface DocumentMetadata extends Serializable{
	
	// versioned
	public String getProperty(String key);
	
	// versioned
	public Set getPropertyNames();
	
	// versioned persistent
	public Date getLastModified();
	public void setLastModified(Date now);
	
	// persistent
	public Date getCreationDate();
	public void setCreationDate(Date creationDate);
	
	// versioned persistent
	public int getModifiedBy();
	public void setModifiedBy(int lastEditor);
	
	// persistent
	public long getFolderId();
	public void setFolderId(long folderId);
	
	// persistent
	public String getTitle();
	public void setTitle(String title);
	
	// versioned persistent
	public int getVersion();
	public void setVersion(int version);
	
	// versioned transient
	public String getContent();
	
	// versioned persistent
	public long getFileSize();
	public void setFileSize(long length);
	
	// versioned persistent
	public String getFileMIMEType();
	public void setFileMIMEType(String type);
	
	// versioned persistent
	public String getFileName();
	public void setFileName(String fileName);
	
	// persistent
	public int getId();
	public void setId(int id);
	
	// persistent
	public int getCreatedBy();
	public void setCreatedBy(int cretor);
	
	// persistent
	public String getDescription();
	public void setDescription(String description);
	
	// persistent
	public String getURL();
	public void setURL(String url);
	
	// versioned persistent
	public long getSequenceNumber();

	public String getCategories();
	public void setCategories(String categories);

	public Date getLockedUntil();
	public void setLockedUntil(Date lockedUntil);

	public String getFileMD5Sum();
	public void setFileMD5Sum(String sum);

	public int getColorLabel();
	public void setColorLabel(int color);

	public boolean isCurrentVersion();
	public void setIsCurrentVersion(boolean bool);

	public String getVersionComment();
	public void setVersionComment(String string);

	public void setFilestoreLocation(String string);

	public String getFilestoreLocation();

	
	
	
	
}
