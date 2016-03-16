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

package com.openexchange.sim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.lang.reflect.Method;


/**
 * {@link Called}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class Called implements Expectation {

    private String methodName = null;
    private Object[] args = null;

    public Called(String methodName, Object[] args) {
        super();
        this.methodName = methodName;
        this.args = args;
    }

    public int getArgumentLength() {
        return args.length;
    }

    public Object getArgument(int i) {
        return args[i];
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public void verify(Method method, Object[] args) {
        String methodName = method.getName();
        assertEquals(getMethodName(), methodName);

        if(args == null) {
            args = new Object[0];
        }

        assertEquals("Argument list size is not equal. Wrong method called? Method: "+methodName, getArgumentLength(), args.length);

        int index = 0;
        for (Object object : args) {
            if(int[].class.isInstance(this.args[index])) {
                // We only care for content;
                if(!int[].class.isInstance(object)) {
                    fail("Argument mismatch at "+index);
                }
                int[] expected = (int[]) this.args[index++];
                int[] provided = (int[]) object;

                assertEquals("Argument mismatch at "+index+". Different Array sizes.", expected.length, provided.length);

                for(int i = 0; i < expected.length; i++){
                    assertEquals("Argument mismatch at "+index+". Different values in array at index "+i, expected[i], provided[i]);
                }

            } else {
                assertEquals("Argument mismatch at "+index, this.args[index++],object);
            }
        }

    }

    @Override
    public String toString() {
        return methodName;
    }

}
