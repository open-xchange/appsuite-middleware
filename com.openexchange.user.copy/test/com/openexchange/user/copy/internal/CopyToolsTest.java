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

package com.openexchange.user.copy.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import com.openexchange.groupware.ldap.User;
import com.openexchange.user.copy.ObjectMapping;


/**
 * {@link CopyToolsTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CopyToolsTest extends TestCase {
    
    private static final int SRC_CTX = 1337;
    
    private static final int DST_CTX = 1338;    
    
    private static final int SRC_USR = 7331;
    
    private static final int DST_USR = 8331;
    
    
    public void testCopyTools() throws Exception {
        final ObjectMapping<Integer> contextIdMapping = createContextIdMapping();
        final ObjectMapping<Integer> userIdMapping = createUserIdMapping();
        final ObjectMapping<User> userMapping = createUserMapping();
        final Map<String, ObjectMapping<?>> mappingMap = new HashMap<String, ObjectMapping<?>>(3);
        mappingMap.put(Constants.CONTEXT_ID_KEY, contextIdMapping);
        mappingMap.put(Constants.USER_ID_KEY, userIdMapping);
        mappingMap.put(User.class.getName(), userMapping);
        
        final CopyTools tools = new CopyTools(mappingMap);
        assertEquals("src cids were not equals.", I(SRC_CTX), tools.getSourceContextId());
        assertEquals("dst cids were not equals.", I(DST_CTX), tools.getDestinationContextId());
        
        assertEquals("src uids were not equals.", I(SRC_USR), tools.getSourceUserId());
        
        assertEquals("src users were not equals.", new MockUser(SRC_USR), tools.getSourceUser());
        assertEquals("dst users were not equals.", new MockUser(DST_USR), tools.getDestinationUser());
    }
    
    private ObjectMapping<Integer> createContextIdMapping() {
        
        final ObjectMapping<Integer> contextIdMapping = new ObjectMapping<Integer>() {
            
            public Integer getSource(final int id) {
                return I(SRC_CTX);
            }
            
            public Integer getDestination(final Integer source) {
                return I(DST_CTX);
            }

            public Set<Integer> getSourceKeys() {
                return null;
            }
        };
        
        return contextIdMapping;
    }
    
    private ObjectMapping<Integer> createUserIdMapping() {
        final ObjectMapping<Integer> userIdMapping = new ObjectMapping<Integer>() {

            public Integer getSource(final int id) {
                return I(SRC_USR);
            }

            public Integer getDestination(final Integer source) {
                return null;
            }

            public Set<Integer> getSourceKeys() {
                return null;
            }
        };
        
        return userIdMapping;
    }
    
    private ObjectMapping<User> createUserMapping() {
        final ObjectMapping<User> userMapping = new ObjectMapping<User>() {

            public User getSource(final int id) {
                if (id != SRC_USR) {
                    return null;
                } else {
                    return new MockUser(SRC_USR);
                }
            }

            public User getDestination(final User source) {
                if (source.equals(new MockUser(SRC_USR))) {
                    return new MockUser(DST_USR);
                }
                
                return null;
            }

            public Set<Integer> getSourceKeys() {
                return null;
            }
        };
        
        return userMapping;
    }

}
