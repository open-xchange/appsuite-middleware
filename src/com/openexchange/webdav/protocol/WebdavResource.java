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

package com.openexchange.webdav.protocol;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;


public interface WebdavResource {

	void create() throws WebdavException;

	boolean exists() throws WebdavException;

	void delete() throws WebdavException;
	
	WebdavResource move(String newUri) throws WebdavException;
	
	WebdavResource move(String string, boolean noroot, boolean overwrite) throws WebdavException;
	
	WebdavResource copy(String string) throws WebdavException;
	
	WebdavResource copy(String string, boolean noroot, boolean overwrite) throws WebdavException;
	
	boolean isCollection();

	void putProperty(WebdavProperty prop) throws WebdavException;

	void removeProperty(String namespace, String name) throws WebdavException;
	
	public List<WebdavProperty> getAllProps() throws WebdavException;

	void save() throws WebdavException;

	String getUrl();

	WebdavProperty getProperty(String namespace, String name) throws WebdavException;

	Date getCreationDate() throws WebdavException;

	Date getLastModified() throws WebdavException;

	String getDisplayName() throws WebdavException;

	void setDisplayName(String displayName) throws WebdavException;

	String getResourceType() throws WebdavException;

	String getLanguage() throws WebdavException;

	void setLanguage(String language) throws WebdavException;

	Long getLength() throws WebdavException;

	void setLength(Long length) throws WebdavException;

	void setContentType(String type) throws WebdavException;

	String getContentType() throws WebdavException;

	String getETag() throws WebdavException;
	
	String getSource() throws WebdavException;
	
	void setSource(String source) throws WebdavException;

	void putBody(InputStream data) throws WebdavException;

	void putBodyAndGuessLength(InputStream body) throws WebdavException;

	InputStream getBody() throws WebdavException;

	WebdavCollection toCollection();

	void lock(WebdavLock lock) throws WebdavException;

	List<WebdavLock> getLocks() throws WebdavException;

	WebdavLock getLock(String token) throws WebdavException;

	void unlock(String token) throws WebdavException;

	List<WebdavLock> getOwnLocks() throws WebdavException;

	WebdavLock getOwnLock(String token) throws WebdavException;

	Protocol.WEBDAV_METHOD[] getOptions() throws WebdavException;

	boolean isLockNull();

	WebdavResource reload() throws WebdavException;


}
