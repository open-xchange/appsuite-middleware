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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;


public interface WebdavResource {

	void create() throws OXException;

	boolean exists() throws OXException;

	void delete() throws OXException;

	WebdavResource move(WebdavPath newUri) throws OXException;

	WebdavResource move(WebdavPath string, boolean noroot, boolean overwrite) throws OXException;

	WebdavResource copy(WebdavPath string) throws OXException;

	WebdavResource copy(WebdavPath string, boolean noroot, boolean overwrite) throws OXException;

	boolean isCollection();

	void putProperty(WebdavProperty prop) throws OXException;

	void removeProperty(String namespace, String name) throws OXException;

	public List<WebdavProperty> getAllProps() throws OXException;

	void save() throws OXException;

	WebdavPath getUrl();

	WebdavProperty getProperty(String namespace, String name) throws OXException;

	Date getCreationDate() throws OXException;

	Date getLastModified() throws OXException;

	String getDisplayName() throws OXException;

	void setDisplayName(String displayName) throws OXException;

	String getResourceType() throws OXException;

	String getLanguage() throws OXException;

	void setLanguage(String language) throws OXException;

	Long getLength() throws OXException;

	void setLength(Long length) throws OXException;

	void setContentType(String type) throws OXException;

	String getContentType() throws OXException;

	String getETag() throws OXException;

	String getSource() throws OXException;

	void setSource(String source) throws OXException;

	void putBody(InputStream data) throws OXException;

	void putBodyAndGuessLength(InputStream body) throws OXException;

	InputStream getBody() throws OXException;

	WebdavCollection toCollection();

	void lock(WebdavLock lock) throws OXException;

	List<WebdavLock> getLocks() throws OXException;

	WebdavLock getLock(String token) throws OXException;

	void unlock(String token) throws OXException;

	List<WebdavLock> getOwnLocks() throws OXException;

	WebdavLock getOwnLock(String token) throws OXException;

	Protocol.WEBDAV_METHOD[] getOptions() throws OXException;

	boolean isLockNull();

	WebdavResource reload() throws OXException;

    Protocol getProtocol();


}
