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

package com.openexchange.webdav.action.ifheader;

import com.openexchange.exception.OXException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.WEBDAV_METHOD;

/**
 * {@link StandardIfHeaderApplyTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class StandardIfHeaderApplyTest extends TestCase {

    public void testETagMustMatch() throws OXException {
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

    public void testETagMustNotMatch() throws OXException {
        IfHeaderApply apply = getApply();

        IfHeaderEntity entity = new IfHeaderEntity.ETag("1234567");
        entity.setMatches(false);

        WebdavResource resource = resourceWithETag("1234567");

        assertFalse(apply.matches(entity, resource));

        resource = resourceWithETag("654321");

        assertTrue(apply.matches(entity, resource));
    }

    public void testLockMustMatch() throws OXException {
        IfHeaderApply apply = getApply();

        IfHeaderEntity entity = new IfHeaderEntity.LockToken("1234567");
        entity.setMatches(true);

        WebdavResource resource = resourceWithLock("1234567");

        assertTrue(apply.matches(entity, resource));

        resource = resourceWithLock("654321");

        assertFalse(apply.matches(entity, resource));
    }

    public void testLockMustNotMatch() throws OXException {
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
            public String getETag() throws OXException {
                return eTag;
            }

            @Override
            public WebdavResource copy(WebdavPath string) throws OXException {
                return null;
            }

            @Override
            public WebdavResource copy(WebdavPath string, boolean noroot, boolean overwrite) throws OXException {
                return null;
            }

            @Override
            public void create() throws OXException {
            }

            @Override
            public void delete() throws OXException {
            }

            @Override
            public boolean exists() throws OXException {
                return false;
            }

            @Override
            public List<WebdavProperty> getAllProps() throws OXException {
                return null;
            }

            @Override
            public InputStream getBody() throws OXException {
                return null;
            }

            @Override
            public String getContentType() throws OXException {
                return null;
            }

            @Override
            public Date getCreationDate() throws OXException {
                return null;
            }

            @Override
            public String getDisplayName() throws OXException {
                return null;
            }

            @Override
            public String getLanguage() throws OXException {
                return null;
            }

            @Override
            public Date getLastModified() throws OXException {
                return null;
            }

            @Override
            public Long getLength() throws OXException {
                return null;
            }

            @Override
            public WebdavLock getLock(String token) throws OXException {
                return null;
            }

            @Override
            public List<WebdavLock> getLocks() throws OXException {
                return null;
            }

            @Override
            public WEBDAV_METHOD[] getOptions() throws OXException {
                return null;
            }

            @Override
            public WebdavLock getOwnLock(String token) throws OXException {
                return null;
            }

            @Override
            public List<WebdavLock> getOwnLocks() throws OXException {
                return null;
            }

            @Override
            public WebdavProperty getProperty(String namespace, String name) throws OXException {
                return null;
            }

            @Override
            public String getResourceType() throws OXException {
                return null;
            }

            @Override
            public String getSource() throws OXException {
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
            public void lock(WebdavLock lock) throws OXException {
            }

            @Override
            public WebdavResource move(WebdavPath newUri) throws OXException {
                return null;
            }

            @Override
            public WebdavResource move(WebdavPath string, boolean noroot, boolean overwrite) throws OXException {
                return null;
            }

            @Override
            public void putBody(InputStream data) throws OXException {
            }

            @Override
            public void putBodyAndGuessLength(InputStream body) throws OXException {
            }

            @Override
            public void putProperty(WebdavProperty prop) throws OXException {
            }

            @Override
            public WebdavResource reload() throws OXException {
                return null;
            }

            @Override
            public void removeProperty(String namespace, String name) throws OXException {
            }

            @Override
            public void save() throws OXException {
            }

            @Override
            public void setContentType(String type) throws OXException {
            }

            @Override
            public void setDisplayName(String displayName) throws OXException {
            }

            @Override
            public void setLanguage(String language) throws OXException {
            }

            @Override
            public void setLength(Long length) throws OXException {
            }

            @Override
            public void setSource(String source) throws OXException {
            }

            @Override
            public WebdavCollection toCollection() {
                return null;
            }

            @Override
            public void unlock(String token) throws OXException {
            }

            @Override
            public Protocol getProtocol() {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }

    protected WebdavResource resourceWithLock(final String lock) {
        return new WebdavResource() {

            @Override
            public WebdavLock getLock(String token) throws OXException {
                if(!lock.equals(token)) {
                    return null;
                }
                WebdavLock webdavLock = new WebdavLock();
                webdavLock.setToken(lock);
                return webdavLock;
            }

            @Override
            public List<WebdavLock> getLocks() throws OXException {
                return Arrays.asList(getLock(lock));
            }


            @Override
            public String getETag() throws OXException {
                return null;
            }

            @Override
            public WebdavResource copy(WebdavPath string) throws OXException {
                return null;
            }

            @Override
            public WebdavResource copy(WebdavPath string, boolean noroot, boolean overwrite) throws OXException {
                return null;
            }

            @Override
            public void create() throws OXException {
            }

            @Override
            public void delete() throws OXException {
            }

            @Override
            public boolean exists() throws OXException {
                return false;
            }

            @Override
            public List<WebdavProperty> getAllProps() throws OXException {
                return null;
            }

            @Override
            public InputStream getBody() throws OXException {
                return null;
            }

            @Override
            public String getContentType() throws OXException {
                return null;
            }

            @Override
            public Date getCreationDate() throws OXException {
                return null;
            }

            @Override
            public String getDisplayName() throws OXException {
                return null;
            }

            @Override
            public String getLanguage() throws OXException {
                return null;
            }

            @Override
            public Date getLastModified() throws OXException {
                return null;
            }

            @Override
            public Long getLength() throws OXException {
                return null;
            }
            @Override
            public WEBDAV_METHOD[] getOptions() throws OXException {
                return null;
            }

            @Override
            public WebdavLock getOwnLock(String token) throws OXException {
                return null;
            }

            @Override
            public List<WebdavLock> getOwnLocks() throws OXException {
                return null;
            }

            @Override
            public WebdavProperty getProperty(String namespace, String name) throws OXException {
                return null;
            }

            @Override
            public String getResourceType() throws OXException {
                return null;
            }

            @Override
            public String getSource() throws OXException {
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
            public void lock(WebdavLock lock) throws OXException {
            }

            @Override
            public WebdavResource move(WebdavPath newUri) throws OXException {
                return null;
            }

            @Override
            public WebdavResource move(WebdavPath string, boolean noroot, boolean overwrite) throws OXException {
                return null;
            }

            @Override
            public void putBody(InputStream data) throws OXException {
            }

            @Override
            public void putBodyAndGuessLength(InputStream body) throws OXException {
            }

            @Override
            public void putProperty(WebdavProperty prop) throws OXException {
            }

            @Override
            public WebdavResource reload() throws OXException {
                return null;
            }

            @Override
            public void removeProperty(String namespace, String name) throws OXException {
            }

            @Override
            public void save() throws OXException {
            }

            @Override
            public void setContentType(String type) throws OXException {
            }

            @Override
            public void setDisplayName(String displayName) throws OXException {
            }

            @Override
            public void setLanguage(String language) throws OXException {
            }

            @Override
            public void setLength(Long length) throws OXException {
            }

            @Override
            public void setSource(String source) throws OXException {
            }

            @Override
            public WebdavCollection toCollection() {
                return null;
            }

            @Override
            public void unlock(String token) throws OXException {
            }

            @Override
            public Protocol getProtocol() {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }
}
