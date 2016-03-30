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

package com.openexchange.webdav.action;

import com.openexchange.exception.OXException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.webdav.protocol.CollectionTest;
import com.openexchange.webdav.protocol.TestWebdavFactoryBuilder;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

public abstract class ActionTestCase extends TestCase {

    protected List<WebdavPath> clean = new LinkedList<WebdavPath>();

    protected WebdavFactory factory = null;

    protected WebdavPath testCollection = null;

    @Override
    public void setUp() throws Exception {
        TestWebdavFactoryBuilder.setUp();
        factory = TestWebdavFactoryBuilder.buildFactory();
        factory.beginRequest();
        try {
            testCollection = new WebdavPath("public_infostore", "testCollection"+System.currentTimeMillis());
            final WebdavCollection coll = factory.resolveCollection(testCollection);
            coll.create();
            clean.add(coll.getUrl());

            CollectionTest.createStructure(coll, factory);
        } catch (final Exception x) {
            x.printStackTrace();
            tearDown();
            throw x;
        }
    }

    @Override
    public void tearDown() throws Exception {
        try {
            for(final WebdavPath url : clean) {
                factory.resolveResource(url).delete();
            }
        } finally {
            factory.endRequest(200);
            TestWebdavFactoryBuilder.tearDown();
        }
    }

    public String getContent(final WebdavPath url) throws OXException, IOException {
        final WebdavResource res = factory.resolveResource(url);
        final byte[] bytes = new byte[(int)res.getLength().longValue()];
        final InputStream in = res.getBody();
        in.read(bytes);
        return new String(bytes, com.openexchange.java.Charsets.UTF_8);
    }
}
