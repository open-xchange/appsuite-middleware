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

package com.openexchange.eav.storage.memory;

import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVSetTransformation;
import com.openexchange.eav.EAVType;
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

    public void setUp() {
        this.storage = new InMemoryStorage();
        this.ctx = new SimContext(1);
        
    }

    /*
     * First we test the happy paths.
     */
    
    public void testInsert() {
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

    public void testUpdateSingleValue() {
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

    public void testUpdateObjectIncrementally() {
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
    
    public void testRemoveValueWithUpdateToNull() {
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
    
    public void testRemoveSubtreeWithUpdateToNull() {
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
    
    public void testRemoveDeeperNextingWithUpdateToNull() {
        
    }
    
    public void testAddToSet() {
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
        storage.updateArrays(ctx, PARENT.append(tree.getName()), update);
        EAVNode loaded = storage.get(ctx, PARENT.append(tree.getName()));
        
        assertEquals(expected, loaded);
    }
    
    public void testRemoveFromSet() {
        
    }
    
    public void testAddToMultiset() {
        
    }
    
    public void testRemoveFromMultiset() {
        
    }
    
    public void testUpdateArrayBatch() {
        
    }
    
    public void testReplaceSubtree() {
        
    }
    
    public void testReplaceSingleValueChangingType() {

    }

    public void testGet() {
        
    }
    
    public void testGetShouldOmitBinaries() {
        
    }
 
   public void testGetAllBinaries() {
        
    }
    
    public void testGetCertainBinaries() {
        
    }
    
    public void testDeleteSingleValue() {
        
    }
    
    public void testDeleteSubtree() {
        
    }

    /*
     * Error conditions
     */
    
    public void testInsertOnExistingPathShouldFail() {

    }
    
    public void testUpdateWithWrongTypesShouldFail() {
        
    }
    
    public void testUpdatingUnknownPathShouldFail() {
        
    }

    public void testUpdateArraysWithWrongTypesShouldFail() {
        
    }
    
    public void testUpdatingArraysAtUnknownPathShouldFail() {
        
    }
    
    

}
