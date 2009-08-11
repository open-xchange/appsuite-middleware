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

package com.openexchange.eav.storage.db;

import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVStorage;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.TreeTools;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.SimContext;



public class SQLStorageTest extends AbstractEAVDBStorageTest {
    
    private int cid = 1;
    
    private int objectId = 123;
    
    private int nodeId = 666;
    
    private String parentName = "pathPart";
    
    private String name = "myValue";
    
    private int parent = 0;
    
    private String payload = "utzelwutzel";
    
    public void setUp() throws Exception {
        super.setUp();
        
        removeAllPathIndexEntries();
        clearTables();
        
        createPathIndexEntry(cid, Types.APPOINTMENT, objectId);
//        createPathEntry(1, cid, Types.APPOINTMENT, objectId, parent, parentName, 0, null);
        createPathEntry(1, cid, Types.APPOINTMENT, objectId, nodeId, name, parent, EAVType.STRING.getKeyword());
//        createPathEntry(1, cid, Types.APPOINTMENT, objectId, nodeId+1, name + "2", parent, EAVType.NUMBER.getKeyword());
        createPayloadEntry("eav_text1", cid, "set", nodeId, payload);
        createPayloadEntry("eav_text1", cid, "set", nodeId, payload + 2);
//        createPayloadEntry("eav_int1", cid, "single", nodeId+1, 1000);
    }
    
    public void testGeneric() throws Exception {
        EAVStorage storage = new Storage(getDBProvider());
        EAVPath path = new EAVPath("calendar", "348", ""+objectId, name);
        EAVNode node = storage.get(new SimContext(1), path);
        System.out.println(TreeTools.getStructureString(node));
    }

    public void tearDown() throws Exception {
//        removeAllPathIndexEntries();
//        clearTables();
        
        super.tearDown();
    }
}
