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

package com.openexchange.groupware.infostore.database.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;

public class DocumentMetadataImpl implements DocumentMetadata {
	
	private static final long serialVersionUID = 954199261404066624L;
	
	private Date lastModified;
	private Date creationDate;
	private int modifiedBy;
	private long folderId;
	private String name;
	private int version;
	private int relevance;
	private String content;
	private int id = InfostoreFacade.NEW;
	private long contentLength;
	private String description;
	private String url;
	private int createdBy;
	private String contentType;
	private String filename;
	private Date lockedUntil;
	private String categories;
	private String md5;
	private String versionComment;
	private boolean currentVersion;
	private int colorLabel;
	private String filespoolPath;


	private Map<String,String> properties = new HashMap<String,String>();

	
	public DocumentMetadataImpl(){
		
	}
	
	public DocumentMetadataImpl(int id){
		this.id = id;
	}
	
	public DocumentMetadataImpl(DocumentMetadata dm){
		SetSwitch setSwitch = new SetSwitch(this);
		GetSwitch getSwitch = new GetSwitch(dm);
		for(Metadata attr : Metadata.VALUES) {
			setSwitch.setValue(attr.doSwitch(getSwitch));
			attr.doSwitch(setSwitch);
		}
	}
	
	public String getProperty(String key) {
		return (String)properties.get(key);
	}

	public Set getPropertyNames() {
		return properties.keySet();
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public long getFolderId() {
		return folderId;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public String getTitle() {
		return name;
	}

	public int getRelevance() {
		return relevance;
	}

	public int getVersion() {
		return version;
	}

	public int hashCode(){
		return getId();
	}
	
	public boolean equals(Object o){
		if (o instanceof DocumentMetadata) {
			DocumentMetadata other = (DocumentMetadata) o;
			return id == other.getId();
		}
		return false;
	}

	public Map getProperties() {
		return properties;
	}

	public void setProperties(Map properties) {
		this.properties = properties;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public void setFolderId(long folderId) {
		this.folderId = folderId;
	}


	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public void setTitle(String name) {
		this.name = name;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	public String getContent(){
		return content;
	}
	
	public int getId(){
		return id;
	}
	
	public long getFileSize(){
		return contentLength;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setFileSize(long contentLength) {
		this.contentLength = contentLength;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFileMIMEType() {
		return contentType;
	}

	public void setFileMIMEType(String contentType) {
		this.contentType = contentType;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public String getFileName() {
		return filename;
	}

	public void setFileName(String filename) {
		this.filename = filename;
	}

	public int getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(int modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public long getSequenceNumber() {
		if(lastModified == null)
			return 0;
		return lastModified.getTime();
	}

	public void setSequenceNumber(long sequenceNumber) {
	}
	
	public void setCategories(String categories) {
		this.categories = categories;
	}
	
	public String getCategories(){
		return this.categories;
	}
	
	public Date getLockedUntil() {
		return lockedUntil;
	}

	public void setLockedUntil(Date lockedUntil) {
		this.lockedUntil = lockedUntil;
	}
	
	public void setFileMD5Sum(String sum){
		this.md5 = sum;
	}
	
	public String getFileMD5Sum(){
		return this.md5;
	}
	
	protected void setFileSpoolPath(String filespoolPath){
		this.filespoolPath = filespoolPath;
	}
	
	protected String getFileSpoolPath(){
		return this.filespoolPath;
	}

	public int getColorLabel() {
		return colorLabel;
	}

	public void setColorLabel(int color) {
		this.colorLabel=color;
	}

	public boolean isCurrentVersion() {
		return currentVersion;
	}

	public void setIsCurrentVersion(boolean bool) {
		this.currentVersion=bool;
	}

	public String getVersionComment() {
		return versionComment;
	}

	public void setVersionComment(String comment) {
		this.versionComment=comment;
	}

	public String getFilestoreLocation() {
		return getFileSpoolPath();
	}

	public void setFilestoreLocation(String string) {
		setFileSpoolPath(string);
	}
	
}
