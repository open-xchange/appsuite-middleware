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

package com.openexchange.admin.console.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.openexchange.admin.console.AbstractTest;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.util.filestore.RegisterFilestore;

/**
 * @author cutmasta
 *
 */
public class RegisterFilestoreTest extends AbstractTest {
    
    
    @Test
    public void testRegisterFilestore() {
        
        resetBuffers();
        String store = "file:/tmp/"+System.currentTimeMillis();
        new RegisterFilestore(getAllOptionData(store)){
            @Override
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected 0 as return code!",0==this.returnCode);
    }
    
    @Test
    public void testRegisterFilestoreWithInvalidData() {
        
        resetBuffers();
        String store = "tmp/"+System.currentTimeMillis();
        new RegisterFilestore(getAllOptionData(store)){
            @Override
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid data as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_DATA==this.returnCode);
    }
    
    @Test
    public void testRegisterFilestoreWithMissingOption() {
        
        resetBuffers();
        
        new RegisterFilestore(getMissingOptionData()){
            @Override
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected missing option as return code!",BasicCommandlineOptions.SYSEXIT_MISSING_OPTION==this.returnCode);
    }
    
    @Test
    public void testRegisterFilestoreWithUnknownOption() {
        
        resetBuffers();
        
        new RegisterFilestore(getUnknownOptionData()){
            @Override
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected unknown option as return code!",BasicCommandlineOptions.SYSEXIT_UNKNOWN_OPTION==this.returnCode);
    }
    
    @Test
    public void testRegisterFilestoreWithInvalidCredentials() {
        
        resetBuffers();
        String store = "file:/tmp/"+System.currentTimeMillis();
        new RegisterFilestore(getAllOptionDataWithInvalidCredentials(store)){
            @Override
            protected void sysexit(int exitCode) {
                RegisterFilestoreTest.this.returnCode = exitCode;
            }
        };
        
        assertTrue("Expected invalid credentials as return code!",BasicCommandlineOptions.SYSEXIT_INVALID_CREDENTIALS==this.returnCode);
    }
    
    public static String[] getAllOptionData(String store){
        String[] tmp = {OPTION_SUPER_ADMIN_USER, 
                OPTION_SUPER_ADMIN_PWD,
                "--storepath="+store,
                "--storesize=1000",
                "--maxcontexts=1000"
                };
        return tmp;
    }
    
    public static String[] getAllOptionDataWithInvalidCredentials(String store){
        String[] tmp = {OPTION_SUPER_ADMIN_USER+"_xyzfoobar", 
                OPTION_SUPER_ADMIN_PWD+"_barfoo",
                "--storepath="+store,
                "--storesize=1000",
                "--maxcontexts=1000"
                };
        return tmp;
    }
    
    
    
}
