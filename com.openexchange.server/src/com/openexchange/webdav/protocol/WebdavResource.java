/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
