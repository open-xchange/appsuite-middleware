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

import static com.openexchange.eav.EAVDSL.N;
import static com.openexchange.eav.EAVDSL.TYPE;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.eav.EAVContainerType;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SQLStorageTest extends AbstractEAVDBStorageTest {

    private int cid = 1;
    
    Context ctx = new SimContext(cid);

    private int objectId = 123;

    private EAVNode node, nodeWithoutBinary, rightChild, rightChildSubset, rightChildWithoutBinary, singleNode, multiNode, binaryNode, unbinaryNode;
    
    private EAVTypeMetadataNode rightChildTypes, rightChildTypesSubset;

    private EAVPath pathToBinary;

    private Storage storage;

    public void setUp() throws Exception {
        super.setUp();
        
        storage = new Storage(getDBProvider());

        removeAllPathIndexEntries();
        clearTables();

        createPathIndexEntry(cid, Types.APPOINTMENT, objectId);
        
        createPathEntry(1, cid, Types.APPOINTMENT, objectId, 1000, "root", 0, null, null);
            createPathEntry(1, cid, Types.APPOINTMENT, objectId, 1100, "leftChild", 1000, null, null);
                createPathEntry(1, cid, Types.APPOINTMENT, objectId, 1110, "leaf_1", 1100, EAVType.STRING.getKeyword(), EAVContainerType.SINGLE.getKeyword());
                createPathEntry(1, cid, Types.APPOINTMENT, objectId, 1120, "leaf_2", 1100, EAVType.BOOLEAN.getKeyword(), EAVContainerType.SET.getKeyword());
            createPathEntry(1, cid, Types.APPOINTMENT, objectId, 1200, "rightChild", 1000, null, null);
                createPathEntry(1, cid, Types.APPOINTMENT, objectId, 1210, "grandChild", 1200, null, null);
                    createPathEntry(1, cid, Types.APPOINTMENT, objectId, 1211, "leaf_3", 1210, EAVType.NUMBER.getKeyword(), EAVContainerType.SINGLE.getKeyword());
                    createPathEntry(1, cid, Types.APPOINTMENT, objectId, 1212, "leaf_4", 1210, EAVType.NUMBER.getKeyword(), EAVContainerType.SET.getKeyword());
                    createPathEntry(1, cid, Types.APPOINTMENT, objectId, 1213, "leaf_5", 1210, EAVType.BINARY.getKeyword(), EAVContainerType.SINGLE.getKeyword());

        createPayloadEntry("eav_text1", cid, 1110, "Hello World");
        createPayloadEntry("eav_bool1", cid, 1120, true);
        createPayloadEntry("eav_bool1", cid, 1120, false);
        createPayloadEntry("eav_bool1", cid, 1120, true);
        createPayloadEntry("eav_int1", cid, 1211, 123);
        createPayloadEntry("eav_int1", cid, 1212, 111);
        createPayloadEntry("eav_int1", cid, 1212, 222);
        createPayloadEntry("eav_int1", cid, 1212, 333);
        createPayloadEntry("eav_int1", cid, 1212, 444);
        createPayloadEntry("eav_blob1", cid, 1213, "abcdefghijklmnopqrstuvwxyz");
        
        singleNode = N("leaf_3", 123);
        
        multiNode = N("leaf_4", 111, 222, 333, 444);
        
        binaryNode = N("leaf_5", "abcdefghijklmnopqrstuvwxyz".getBytes());
        
        unbinaryNode = N("leaf_5");
        
        rightChild = N("rightChild",
            N("grandChild",
                singleNode,
                multiNode,
                binaryNode
            )
        );
        
        rightChildSubset = N("rightChild",
            N("grandChild",
                singleNode,
                multiNode
            )
        );
        
        rightChildWithoutBinary = N("rightChild",
            N("grandChild",
                singleNode,
                multiNode,
                unbinaryNode
            )
        );
        
        node = N("root",
            N("leftChild",
                N("leaf_1", "Hello World"),
                N("leaf_2", true, false, true)
            ),
            rightChild
        );
        
        nodeWithoutBinary = N("root",
            N("leftChild",
                N("leaf_1", "Hello World"),
                N("leaf_2", true, false, true)
            ),
            rightChildWithoutBinary
        );
        
        rightChildTypes = TYPE("rightChild",
            TYPE("grandChild",
                TYPE("leaf_3", EAVType.NUMBER, EAVContainerType.SINGLE),
                TYPE("leaf_4", EAVType.NUMBER, EAVContainerType.SET),
                TYPE("leaf_5", EAVType.BINARY, EAVContainerType.SINGLE)
            )
        );
        
        rightChildTypesSubset = TYPE("rightChild",
            TYPE("grandChild",
                TYPE("leaf_3", EAVType.NUMBER, EAVContainerType.SINGLE),
                TYPE("leaf_4", EAVType.NUMBER, EAVContainerType.SET)
            )
        );
        
        pathToBinary = new EAVPath("rightChild", "grandChild", "leaf_5");
    }

    public void testCompleteTreeWithBinaries() throws Exception {
        EAVPath path = new EAVPath("calendar", "348", "" + objectId, "root");
        EAVNode node = storage.get(ctx, path, true);
        assertEquals(this.node, node);
    }

    public void testCompleteTreeWithoutBinaries() throws Exception {
        EAVPath path = new EAVPath("calendar", "348", "" + objectId, "root");
        EAVNode node = storage.get(ctx, path, false);
        assertEquals(this.nodeWithoutBinary, node);
    }
    
    public void testCompleteTreeWithBinaryByPath() throws Exception {
        EAVPath path = new EAVPath("calendar", "348", "" + objectId, "root");
        Set<EAVPath> paths = new HashSet<EAVPath>();
        paths.add(pathToBinary);
        EAVNode node = storage.get(ctx, path, paths);
        assertEquals(this.node, node);
    }
    
    public void testSubTree() throws Exception {
        EAVPath path = new EAVPath("calendar", "348", "" + objectId, "root", "rightChild");
        EAVNode node = storage.get(ctx, path, true);
        assertEquals(this.rightChild, node);
    }
    
    public void testSingleValue() throws Exception {
        EAVPath path = new EAVPath("calendar", "348", "" + objectId, "root", "rightChild", "grandChild", "leaf_3");
        EAVNode node = storage.get(ctx, path, true);
        assertEquals(this.singleNode, node);
    }
    
    public void testMultiValue() throws Exception {
        EAVPath path = new EAVPath("calendar", "348", "" + objectId, "root", "rightChild", "grandChild", "leaf_4");
        EAVNode node = storage.get(ctx, path, true);
        assertEquals(this.multiNode, node);
    }
    
    public void testBinaryValueExplicit() throws Exception {
        EAVPath path = new EAVPath("calendar", "348", "" + objectId, "root", "rightChild", "grandChild", "leaf_5");
        EAVNode node = storage.get(ctx, path, true);
        assertEquals(this.binaryNode, node);
    }
    
    public void testBinaryValueImplicit() throws Exception {
        EAVPath path = new EAVPath("calendar", "348", "" + objectId, "root", "rightChild", "grandChild", "leaf_5");
        EAVNode node = storage.get(ctx, path);
        assertEquals(this.binaryNode, node);
    }
    
    public void testTypes() throws Exception {
        EAVPath path = new EAVPath("calendar", "348", "" + objectId, "root", "rightChild");
        EAVTypeMetadataNode types = storage.getTypes(ctx, path, this.rightChild);
        assertEquals(this.rightChildTypes, types);
        types = storage.getTypes(ctx, path, this.rightChildSubset);
        assertEquals(this.rightChildTypesSubset, types);
    }

    public void tearDown() throws Exception {
         removeAllPathIndexEntries();
         clearTables();

        super.tearDown();
    }
}
