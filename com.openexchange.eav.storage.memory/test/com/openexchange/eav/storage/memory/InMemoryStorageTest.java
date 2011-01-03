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

package com.openexchange.eav.storage.memory;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import com.openexchange.eav.EAVContainerType;
import com.openexchange.eav.EAVException;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVSetTransformation;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.EAVUnitTest;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;

/**
 * {@link InMemoryStorageTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InMemoryStorageTest extends EAVUnitTest {

    private static final EAVPath PARENT = new EAVPath("contacts", "23", "12");
    
    private InMemoryStorage storage;
    private Context ctx;
    private Context otherCtx;

    public void setUp() {
        this.storage = new InMemoryStorage();
        this.ctx = new SimContext(1);
        this.otherCtx = new SimContext(2);
    }

    /*
     * First we test the happy paths.
     */
    
    public void testInsert() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
                            N("exampleString", "Hallo"),
                            N("exampleBoolean", true),
                            N("exampleNumber", 12),
                            N("exampleFloat", 12.1),
                            N("exampleDate", EAVType.DATE, 12),
                            N("exampleTime", EAVType.TIME, 12),
                            N("multiples", 
                                N("strings", "Hello", "World","what's", "up"),
                                N("bools", true, true, false, true, false, false, false, true),
                                N("numbers", 12,13,14,15),
                                N("dates", 12,13,14,15,16),
                                N("times", 12,13,14,15,16)
                            )
                        );
        
        storage.insert(ctx, PARENT, tree);
        
        EAVNode node = storage.get(ctx, PARENT.append("com.openexchange.test"));
        
        assertEquals(tree, node);
        
    } 

    public void testUpdateSingleValue() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
                            N("exampleString", "Hello")
                        );
        
        EAVNode expected = N("com.openexchange.test",
                                N("exampleString", "World")
                            );
        
        
        storage.insert(ctx, PARENT, tree);
        storage.update(ctx, PARENT.append("com.openexchange.test"), N("exampleString", "World"));
        EAVNode node = storage.get(ctx, PARENT.append("com.openexchange.test"));
        
        assertEquals(expected, node);
    }

    public void testUpdateObjectIncrementally() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                N("exampleString", "Hello")
                            )
                       );

        
        EAVNode update = N("subObject", 
                            N("exampleString", "Good day"),
                            N("otherString", "World"),
                            N("subsubObject", 
                                N("subsubsubObject", 
                                    N("subsubsubObjectString", "Tadaaa!")
                                )
                            )
                        );
        
        
        EAVNode expected = N("com.openexchange.test", 
                                N("subObject",
                                    N("exampleString", "Good day"),
                                    N("otherString", "World"),
                                    N("subsubObject", 
                                        N("subsubsubObject", 
                                            N("subsubsubObjectString", "Tadaaa!")
                                        )
                                    )
                                )
                            );
        
        storage.insert(ctx, PARENT, tree);
        storage.update(ctx, PARENT.append(tree.getName()), update);
        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));
    
        assertEquals(expected, loaded);
    }
    
    public void testRemoveValueWithUpdateToNull() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                N("exampleString", "Hello")
                             )
                        );


        EAVNode update = N("subObject", 
                              NULL("exampleString")
                          );


        EAVNode expected = N("com.openexchange.test", 
                                EMPTY_OBJECT("subObject")
                            );

        
        storage.insert(ctx, PARENT, tree);
        storage.update(ctx, PARENT.append(tree.getName()), update);
        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));
    
        assertEquals(expected, loaded);
    
    }
    
    public void testRemoveSubtreeWithUpdateToNull() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                   N("exampleString", "Hello")
                            )
                        );


        EAVNode update = NULL("subObject");

        EAVNode expected = EMPTY_OBJECT("com.openexchange.test");

        storage.insert(ctx, PARENT, tree);
        storage.update(ctx, PARENT.append(tree.getName()), update);
        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));
        
        assertEquals(expected, loaded);
    }
    
    public void testRemoveDeeperNestingWithUpdateToNull() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                N("exampleString", "Hello")
                            )
                        );


        EAVNode update = N("com.openexchange.test", 
                            NULL("subObject")
                          );

        EAVNode expected = EMPTY_OBJECT("com.openexchange.test");

        storage.insert(ctx, PARENT, tree);
        storage.update(ctx, PARENT, update);
        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));
        
        assertEquals(expected, loaded);
    }
    
    // Note: For a detailed test of set operations: see: SetUpdaterTest
    public void testUpdateArrayBatch() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
                            SET("testSet", "Hallo", "Welt")
                        );
        
        EAVSetTransformation update = TRANS("com.openexchange.test",
                                            TRANS("testSet", ADD("Welt", "Wie", "geht", "es"))
                                      );
    
        EAVNode expected = N("com.openexchange.test", 
                                SET("testSet", "Hallo", "Welt", "Wie", "geht", "es")
                            );
        
        storage.insert(ctx, PARENT, tree);
        storage.updateSets(ctx, PARENT, update);
        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));
        
        assertEquals(expected, loaded);
    }
        
    public void testReplaceSubtree() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                N("exampleString", "Hello")
                            )
                        );
        
        EAVNode update = N("subObject", 
                            N("otherAttribute", 12)
                         );
        EAVNode expected = N("com.openexchange.test", 
                                N("subObject", 
                                    N("otherAttribute", 12)
                                )
                            );
        
        storage.insert(ctx, PARENT, tree);
        storage.replace(ctx, PARENT.append(tree.getName()), update);
        
        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));
        
        assertEquals(expected, loaded);
    }
    
    public void testReplaceSingleValueChangingType() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
            N("subObject", 
                  N("attribute", "Hello")
             )
        );

        EAVNode update = N("attribute", 12);

        EAVNode expected = N("com.openexchange.test", 
                                N("subObject", 
                                    N("attribute", 12)
                                )
                            );

        storage.insert(ctx, PARENT, tree);
        storage.replace(ctx, PARENT.append(tree.getName()).append("subObject"), update);

        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));

        assertEquals(expected, loaded);
    }

    // Note: Regular 'get' was tested at length in the upper test cases.
    
    public void testGetShouldOmitBinaries() throws EAVException, UnsupportedEncodingException{
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                N("attribute", "Hello"),
                                N("binary", "Hello".getBytes("UTF-8"))
                            )
                        );
        
        EAVNode expected = N("com.openexchange.test",
                                N("subObject",
                                    N("attribute", "Hello")
                                )
                            );
        
        storage.insert(ctx, PARENT, tree);
        EAVNode loaded = storage.get(ctx, PARENT.append("com.openexchange.test"));
        
        assertEquals(expected, loaded);
    }
 
    public void testGetAllBinaries() throws EAVException, UnsupportedEncodingException{
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                  N("attribute", "Hello"),
                                  N("binary", "Hello".getBytes("UTF-8"))
                            )
                        );

        // Note: We can't reuse the 'tree' instance for checking, since the InputStream will be consumed on insert.
        EAVNode expected = N("com.openexchange.test", 
                                N("subObject", 
                                    N("attribute", "Hello"),
                                    N("binary", "Hello".getBytes("UTF-8"))
                                )
                            );

        storage.insert(ctx, PARENT, tree);
        EAVNode loaded = storage.get(ctx, PARENT.append("com.openexchange.test"), true);

        assertEquals(expected, loaded);
        
    }
    
    public void testGetCertainBinaries() throws EAVException, UnsupportedEncodingException{
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                N("attribute", "Hello"),
                                N("smallBinary", "Hello".getBytes("UTF-8")),
                                N("smallBinary2", "World".getBytes("UTF-8")),
                                N("largeBinary", "Gigabytes of data".getBytes("UTF-8"))
                            )
                       );

        EAVNode expected = N("com.openexchange.test", 
                                N("subObject", 
                                    N("attribute", "Hello"),
                                    N("smallBinary", "Hello".getBytes("UTF-8")),
                                    N("smallBinary2", "World".getBytes("UTF-8"))
                                 )
                            );

        storage.insert(ctx, PARENT, tree);
        
        EAVPath subObjectPath = new EAVPath("subObject");
        
        EAVNode loaded = storage.get(ctx, PARENT.append("com.openexchange.test"), new HashSet<EAVPath>(subObjectPath.subpaths("smallBinary", "smallBinary2")));

        assertEquals(expected, loaded);

    }
    
    public void testGetSpecificBinary() throws EAVException, UnsupportedEncodingException {
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                N("attribute", "Hello"),
                                N("binary", "Hello".getBytes("UTF-8"))
                            )
                        );

        EAVNode expected = N("binary", "Hello".getBytes("UTF-8"));
        storage.insert(ctx, PARENT, tree);
        
        EAVNode loaded = storage.get(ctx, PARENT.append("com.openexchange.test","subObject", "binary"));

        assertEquals(expected, loaded);

    }
    
    public void testDeleteSingleValue() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
                            N("subObject", 
                                 N("attribute", "Hello")
                            )
                        );

       
        EAVNode expected = N("com.openexchange.test", 
                                EMPTY_OBJECT("subObject")
                            );


        storage.insert(ctx, PARENT, tree);
        storage.delete(ctx, PARENT.append(tree.getName()).append("subObject").append("attribute"));

        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));

        assertEquals(expected, loaded);
    }
    
    public void testDeleteSubtree() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
            N("subObject", 
                  N("attribute", "Hello")
             )
        );

       
        EAVNode expected = EMPTY_OBJECT("com.openexchange.test");

        storage.insert(ctx, PARENT, tree);
        storage.delete(ctx, PARENT.append(tree.getName()).append("subObject"));

        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));

        assertEquals(expected, loaded);
    }
    
    public void testGetTypes() throws EAVException, UnsupportedEncodingException {
        EAVNode tree = N("com.openexchange.test", 
            N("exampleString", "Hallo"),
            N("exampleBoolean", true),
            N("exampleNumber", 12),
            N("exampleFloat", 12.1),
            N("exampleDate", EAVType.DATE, 12),
            N("exampleTime", EAVType.TIME, 12),
            N("exampleBinary", "Hello World".getBytes("UTF-8")),
            N("multiples", 
                N("strings", "Hello", "World","what's", "up"),
                N("bools", true, true, false, true, false, false, false, true),
                N("numbers", 12,13,14,15),
                N("dates", EAVType.DATE, 12,13,14,15,16),
                N("times", EAVType.TIME, 12,13,14,15,16)
            )
        );

        storage.insert(ctx, PARENT, tree);
    
        EAVTypeMetadataNode types = storage.getTypes(ctx, PARENT, tree);
        
        assertType(types, EAVType.STRING, "exampleString");
        assertType(types, EAVType.BOOLEAN, "exampleBoolean");
        assertType(types, EAVType.NUMBER, "exampleNumber");
        assertType(types, EAVType.NUMBER, "exampleFloat");
        assertType(types, EAVType.DATE, "exampleDate");
        assertType(types, EAVType.TIME, "exampleTime");
        assertType(types, EAVType.BINARY, "exampleBinary");
        
        
        assertType(types, EAVType.STRING, EAVContainerType.MULTISET, "multiples", "strings");
        assertType(types, EAVType.BOOLEAN, EAVContainerType.MULTISET, "multiples", "bools");
        assertType(types, EAVType.NUMBER, EAVContainerType.MULTISET, "multiples", "numbers");
        assertType(types, EAVType.DATE,EAVContainerType.MULTISET,  "multiples", "dates");
        assertType(types, EAVType.TIME, EAVContainerType.MULTISET, "multiples", "times");
        
    }
    
    public void testMultiTenant() throws EAVException {
        EAVNode tree1 = N("com.openexchange.test", 
                            N("subObject", 
                                N("exampleString", "ctx 1")
                            )
                        );
        
        EAVNode tree2 = N("com.openexchange.test", 
                            N("subObject", 
                                N("exampleString", "ctx 1")
                            )
                        );

        storage.insert(ctx, PARENT, tree1);
        storage.insert(otherCtx, PARENT, tree2);
        
       assertEquals(tree1, storage.get(ctx, PARENT.append(tree1.getName())));
       assertEquals(tree2, storage.get(ctx, PARENT.append(tree2.getName())));
       
    }
    
    /*
     * Error conditions
     */
    
    public void testInsertOnExistingPathShouldFail() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
            N("exampleString", "Hello")
        );
        
        storage.insert(ctx, PARENT, tree);
        
        try {
            storage.insert(ctx, PARENT, tree);
            fail("Could insert on existing path");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.PATH_TAKEN.getDetailNumber());
        }
    }
    
    public void testUpdateWithWrongTypesShouldFail() throws EAVException{
        EAVNode tree = N("com.openexchange.test",
                            N("exampleString", "Hello")
                        );
        
        EAVNode update = N("exampleString", 12);
        
        storage.insert(ctx, PARENT, tree);
        try {
            storage.update(ctx, PARENT.append(tree.getName()), update);
            fail("Could update with wrong type");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.TYPE_MISMATCH.getDetailNumber());
        }
    }
    
    public void testUpdateWithWrongContainerTypeShouldFail() throws EAVException {
        EAVNode tree = N("com.openexchange.test",
                            SET("exampleSet", "Hello")
                        );

        EAVNode update = MULTISET("exampleSet", "Hello");

        storage.insert(ctx, PARENT, tree);
        try {
             storage.update(ctx, PARENT.append(tree.getName()), update);
             fail("Could update with wrong type");
         } catch (EAVException x) {
             assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.TYPE_MISMATCH.getDetailNumber());
         }
    }
    
    public void testUpdatingUnknownPathShouldFail() throws EAVException{
        try {
            storage.update(ctx, new EAVPath("unknown"), EMPTY_OBJECT("bla"));
            fail("Could update unknown path");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.UNKNOWN_PATH.getDetailNumber());
        }
    }

    public void testUpdateArraysWithWrongTypesShouldFail() throws EAVException{
        EAVNode tree = N("com.openexchange.test",
                            SET("exampleSet", "Hello")
                        );
        
        EAVSetTransformation update = TRANS("exampleSet", ADD(12));
        
        storage.insert(ctx, PARENT, tree);
        try {
            storage.updateSets(ctx, PARENT.append("com.openexchange.test"), update);
            fail("Could update set of Strings adding a Number");
        } catch (EAVException x){
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.TYPE_MISMATCH.getDetailNumber());
        }
    }
    
    public void testUpdateArraysWithWrongContainerTypeShouldFail() throws EAVException{
        EAVNode tree = N("com.openexchange.test",
                            N("notASet", "Hello")
                        );
        
        EAVSetTransformation update = TRANS("notASet", ADD("World"));
        
        storage.insert(ctx, PARENT, tree);
        try {
            storage.updateSets(ctx, PARENT.append("com.openexchange.test"), update);
            fail("Could update a scalar adding a value");
        } catch (EAVException x){
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.CAN_ONLY_ADD_AND_REMOVE_FROM_SET_OR_MULTISET.getDetailNumber());
        }
    }

    
    public void testUpdatingArraysAtUnknownPathShouldFail() throws EAVException{
        try {
            storage.updateSets(ctx, new EAVPath("unknown"), TRANS("someAttribute", ADD(12)));
            fail("Could add to nonexisting list");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.UNKNOWN_PATH.getDetailNumber());
        }
    }
    
    public void testUpdatingArraysWithUnknownNestedElementShouldFail() throws EAVException {
        EAVNode tree = N("com.openexchange.test",
                            SET("exampleSet", "Hello")
                        );
        
        EAVSetTransformation update = TRANS("com.openexchange.test",
                                            TRANS("unknown", ADD(12))
                                        );
        
        storage.insert(ctx, PARENT, tree);
        
        try {
            storage.updateSets(ctx, PARENT, update);
            fail("Could add to non existing element");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.UNKNOWN_PATH.getDetailNumber());
        }
    }
    
    public void testGetUnknownPathShouldFail() {
        try {
            storage.get(ctx, new EAVPath("unknown"));
            fail("Could load unknown path");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.UNKNOWN_PATH.getDetailNumber());
        }
        
        try {
            storage.get(ctx, new EAVPath("unknown"), true);
            fail("Could load unknown path");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.UNKNOWN_PATH.getDetailNumber());
        }
        
        try {
            storage.get(ctx, new EAVPath("unknown"), new HashSet<EAVPath>());
            fail("Could load unknown path");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.UNKNOWN_PATH.getDetailNumber());
        }
    }
    
    public void testGetMetadataForUnknownPathShouldFail() {
        try {
            storage.getTypes(ctx, new EAVPath("unknown"), new EAVNode());
            fail("Could load unknown path");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVErrorMessage.UNKNOWN_PATH.getDetailNumber());
        }
    }
}
