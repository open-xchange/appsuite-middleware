/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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

        if (args == null) {
            args = new Object[0];
        }

        assertEquals("Argument list size is not equal. Wrong method called? Method: "+methodName, getArgumentLength(), args.length);

        int index = 0;
        for (Object object : args) {
            if (int[].class.isInstance(this.args[index])) {
                // We only care for content;
                if (!int[].class.isInstance(object)) {
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
