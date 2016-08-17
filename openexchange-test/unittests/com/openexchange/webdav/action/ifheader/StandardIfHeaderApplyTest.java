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

package com.openexchange.webdav.action.ifheader;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavMethod;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import junit.framework.TestCase;

/**
 * {@link StandardIfHeaderApplyTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class StandardIfHeaderApplyTest extends TestCase {

    public void testETagMustMatch() throws WebdavProtocolException {
        IfHeaderApply apply = getApply();

        IfHeaderEntity entity = new IfHeaderEntity.ETag("1234567");
        entity.setMatches(true);

        WebdavResource resource = resourceWithETag("1234567");

        assertTrue(apply.matches(entity, resource));

        resource = resourceWithETag("654321");

        assertFalse(apply.matches(entity, resource));

    }

    protected StandardIfHeaderApply getApply() {
        return new StandardIfHeaderApply();
    }

    public void testETagMustNotMatch() throws WebdavProtocolException {
        IfHeaderApply apply = getApply();

        IfHeaderEntity entity = new IfHeaderEntity.ETag("1234567");
        entity.setMatches(false);

        WebdavResource resource = resourceWithETag("1234567");

        assertFalse(apply.matches(entity, resource));

        resource = resourceWithETag("654321");

        assertTrue(apply.matches(entity, resource));
    }

    public void testLockMustMatch() throws WebdavProtocolException {
        IfHeaderApply apply = getApply();

        IfHeaderEntity entity = new IfHeaderEntity.LockToken("1234567");
        entity.setMatches(true);

        WebdavResource resource = resourceWithLock("1234567");

        assertTrue(apply.matches(entity, resource));

        resource = resourceWithLock("654321");

        assertFalse(apply.matches(entity, resource));
    }

    public void testLockMustNotMatch() throws WebdavProtocolException {
        IfHeaderApply apply = getApply();

        IfHeaderEntity entity = new IfHeaderEntity.LockToken("1234567");
        entity.setMatches(false);

        WebdavResource resource = resourceWithLock("1234567");

        assertFalse(apply.matches(entity, resource));

        resource = resourceWithLock("654321");

        assertTrue(apply.matches(entity, resource));
    }

    protected WebdavResource resourceWithETag(final String eTag) {
        return new WebdavResource() {

            @Override
            public String getETag() throws WebdavProtocolException {
                return eTag;
            }

            @Override
            public WebdavResource copy(WebdavPath string) throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavResource copy(WebdavPath string, boolean noroot, boolean overwrite) throws WebdavProtocolException {
                return null;
            }

            @Override
            public void create() throws WebdavProtocolException {
            }

            @Override
            public void delete() throws WebdavProtocolException {
            }

            @Override
            public boolean exists() throws WebdavProtocolException {
                return false;
            }

            @Override
            public List<WebdavProperty> getAllProps() throws WebdavProtocolException {
                return null;
            }

            @Override
            public InputStream getBody() throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getContentType() throws WebdavProtocolException {
                return null;
            }

            @Override
            public Date getCreationDate() throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getDisplayName() throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getLanguage() throws WebdavProtocolException {
                return null;
            }

            @Override
            public Date getLastModified() throws WebdavProtocolException {
                return null;
            }

            @Override
            public Long getLength() throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavLock getLock(String token) throws WebdavProtocolException {
                return null;
            }

            @Override
            public List<WebdavLock> getLocks() throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavMethod[] getOptions() throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
                return null;
            }

            @Override
            public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavProperty getProperty(String namespace, String name) throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getResourceType() throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getSource() throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavPath getUrl() {
                return null;
            }

            @Override
            public boolean isCollection() {
                return false;
            }

            @Override
            public boolean isLockNull() {
                return false;
            }

            @Override
            public void lock(WebdavLock lock) throws WebdavProtocolException {
            }

            @Override
            public WebdavResource move(WebdavPath newUri) throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavResource move(WebdavPath string, boolean noroot, boolean overwrite) throws WebdavProtocolException {
                return null;
            }

            @Override
            public void putBody(InputStream data) throws WebdavProtocolException {
            }

            @Override
            public void putBodyAndGuessLength(InputStream body) throws WebdavProtocolException {
            }

            @Override
            public void putProperty(WebdavProperty prop) throws WebdavProtocolException {
            }

            @Override
            public WebdavResource reload() throws WebdavProtocolException {
                return null;
            }

            @Override
            public void removeProperty(String namespace, String name) throws WebdavProtocolException {
            }

            @Override
            public void save() throws WebdavProtocolException {
            }

            @Override
            public void setContentType(String type) throws WebdavProtocolException {
            }

            @Override
            public void setDisplayName(String displayName) throws WebdavProtocolException {
            }

            @Override
            public void setLanguage(String language) throws WebdavProtocolException {
            }

            @Override
            public void setLength(Long length) throws WebdavProtocolException {
            }

            @Override
            public void setSource(String source) throws WebdavProtocolException {
            }

            @Override
            public WebdavCollection toCollection() {
                return null;
            }

            @Override
            public void unlock(String token) throws WebdavProtocolException {
            }

            @Override
            public Protocol getProtocol() {
                // Nothing to do
                return null;
            }

            @Override
            public WebdavProperty getProperty(WebdavProperty property) throws WebdavProtocolException {
                return getProperty(property.getNamespace(), property.getName());
            }

        };
    }

    protected WebdavResource resourceWithLock(final String lock) {
        return new WebdavResource() {

            @Override
            public WebdavLock getLock(String token) throws WebdavProtocolException {
                if(!lock.equals(token)) {
                    return null;
                }
                WebdavLock webdavLock = new WebdavLock();
                webdavLock.setToken(lock);
                return webdavLock;
            }

            @Override
            public List<WebdavLock> getLocks() throws WebdavProtocolException {
                return Arrays.asList(getLock(lock));
            }


            @Override
            public String getETag() throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavResource copy(WebdavPath string) throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavResource copy(WebdavPath string, boolean noroot, boolean overwrite) throws WebdavProtocolException {
                return null;
            }

            @Override
            public void create() throws WebdavProtocolException {
            }

            @Override
            public void delete() throws WebdavProtocolException {
            }

            @Override
            public boolean exists() throws WebdavProtocolException {
                return false;
            }

            @Override
            public List<WebdavProperty> getAllProps() throws WebdavProtocolException {
                return null;
            }

            @Override
            public InputStream getBody() throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getContentType() throws WebdavProtocolException {
                return null;
            }

            @Override
            public Date getCreationDate() throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getDisplayName() throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getLanguage() throws WebdavProtocolException {
                return null;
            }

            @Override
            public Date getLastModified() throws WebdavProtocolException {
                return null;
            }

            @Override
            public Long getLength() throws WebdavProtocolException {
                return null;
            }
            @Override
            public WebdavMethod[] getOptions() throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
                return null;
            }

            @Override
            public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavProperty getProperty(String namespace, String name) throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getResourceType() throws WebdavProtocolException {
                return null;
            }

            @Override
            public String getSource() throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavPath getUrl() {
                return null;
            }

            @Override
            public boolean isCollection() {
                return false;
            }

            @Override
            public boolean isLockNull() {
                return false;
            }

            @Override
            public void lock(WebdavLock lock) throws WebdavProtocolException {
            }

            @Override
            public WebdavResource move(WebdavPath newUri) throws WebdavProtocolException {
                return null;
            }

            @Override
            public WebdavResource move(WebdavPath string, boolean noroot, boolean overwrite) throws WebdavProtocolException {
                return null;
            }

            @Override
            public void putBody(InputStream data) throws WebdavProtocolException {
            }

            @Override
            public void putBodyAndGuessLength(InputStream body) throws WebdavProtocolException {
            }

            @Override
            public void putProperty(WebdavProperty prop) throws WebdavProtocolException {
            }

            @Override
            public WebdavResource reload() throws WebdavProtocolException {
                return null;
            }

            @Override
            public void removeProperty(String namespace, String name) throws WebdavProtocolException {
            }

            @Override
            public void save() throws WebdavProtocolException {
            }

            @Override
            public void setContentType(String type) throws WebdavProtocolException {
            }

            @Override
            public void setDisplayName(String displayName) throws WebdavProtocolException {
            }

            @Override
            public void setLanguage(String language) throws WebdavProtocolException {
            }

            @Override
            public void setLength(Long length) throws WebdavProtocolException {
            }

            @Override
            public void setSource(String source) throws WebdavProtocolException {
            }

            @Override
            public WebdavCollection toCollection() {
                return null;
            }

            @Override
            public void unlock(String token) throws WebdavProtocolException {
            }

            @Override
            public Protocol getProtocol() {
                // Nothing to do
                return null;
            }

            @Override
            public WebdavProperty getProperty(WebdavProperty property) throws WebdavProtocolException {
                return getProperty(property.getNamespace(), property.getName());
            }

        };
    }
}
