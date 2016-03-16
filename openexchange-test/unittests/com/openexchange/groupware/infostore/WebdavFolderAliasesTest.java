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
package com.openexchange.groupware.infostore;

import junit.framework.TestCase;
import com.openexchange.groupware.infostore.webdav.InMemoryAliases;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class WebdavFolderAliasesTest extends TestCase {

    private WebdavFolderAliases aliases = null;

    private final String alias1 = "Alias 1";
    private final int id1 = 42;
    private final int parent1 = 23;

    private final String alias2 = "Alias 2";
    private final int id2 = 1337;
    private final int parent2 = 2017;


    @Override
    public void setUp() {
        aliases = new InMemoryAliases();
        aliases.registerNameWithIDAndParent(alias1, id1, parent1);
        aliases.registerNameWithIDAndParent(alias2, id2, parent2);
    }

    public void testLookupByID() {
        assertEquals(alias1, aliases.getAlias(id1));
        assertEquals(alias2, aliases.getAlias(id2));
        assertEquals(null, aliases.getAlias(666));
    }

    public void testLookupByNameAndParent() {
        assertEquals(id1, aliases.getId(alias1, parent1));
        assertEquals(id2, aliases.getId(alias2, parent2));
        assertEquals(WebdavFolderAliases.NOT_REGISTERED, aliases.getId("unregistered alias", 12));
        assertEquals(WebdavFolderAliases.NOT_REGISTERED, aliases.getId(alias1, parent2));
        assertEquals(WebdavFolderAliases.NOT_REGISTERED, aliases.getId(alias2, parent1));
    }

}
