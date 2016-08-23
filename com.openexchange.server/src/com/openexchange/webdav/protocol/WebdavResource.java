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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

public interface WebdavResource {

	void create() throws WebdavProtocolException;

	boolean exists() throws WebdavProtocolException;

	void delete() throws WebdavProtocolException;

	WebdavResource move(WebdavPath newUri) throws WebdavProtocolException;

	WebdavResource move(WebdavPath string, boolean noroot, boolean overwrite) throws WebdavProtocolException;

	WebdavResource copy(WebdavPath string) throws WebdavProtocolException;

	WebdavResource copy(WebdavPath string, boolean noroot, boolean overwrite) throws WebdavProtocolException;

	boolean isCollection();

	void putProperty(WebdavProperty prop) throws WebdavProtocolException;

	void removeProperty(String namespace, String name) throws WebdavProtocolException;

	public List<WebdavProperty> getAllProps() throws WebdavProtocolException;

	void save() throws WebdavProtocolException;

	WebdavPath getUrl();

    WebdavProperty getProperty(String namespace, String name) throws WebdavProtocolException;

    /**
     * Gets a property from the resource.
     *
     * @param property The requested property
     * @return The property
     */
    WebdavProperty getProperty(WebdavProperty property) throws WebdavProtocolException;

	Date getCreationDate() throws WebdavProtocolException;

	Date getLastModified() throws WebdavProtocolException;

	String getDisplayName() throws WebdavProtocolException;

	void setDisplayName(String displayName) throws WebdavProtocolException;

	String getResourceType() throws WebdavProtocolException;

	String getLanguage() throws WebdavProtocolException;

	void setLanguage(String language) throws WebdavProtocolException;

	Long getLength() throws WebdavProtocolException;

	void setLength(Long length) throws WebdavProtocolException;

	void setContentType(String type) throws WebdavProtocolException;

	String getContentType() throws WebdavProtocolException;

	String getETag() throws WebdavProtocolException;

	String getSource() throws WebdavProtocolException;

	void setSource(String source) throws WebdavProtocolException;

	void putBody(InputStream data) throws WebdavProtocolException;

	void putBodyAndGuessLength(InputStream body) throws WebdavProtocolException;

	InputStream getBody() throws WebdavProtocolException;

	WebdavCollection toCollection();

	void lock(WebdavLock lock) throws WebdavProtocolException;

	List<WebdavLock> getLocks() throws WebdavProtocolException;

	WebdavLock getLock(String token) throws WebdavProtocolException;

	void unlock(String token) throws WebdavProtocolException;

	List<WebdavLock> getOwnLocks() throws WebdavProtocolException;

	WebdavLock getOwnLock(String token) throws WebdavProtocolException;

	WebdavMethod[] getOptions() throws WebdavProtocolException;

	boolean isLockNull();

	WebdavResource reload() throws WebdavProtocolException;

    Protocol getProtocol();


}
