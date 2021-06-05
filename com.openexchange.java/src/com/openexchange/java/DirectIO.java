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

package com.openexchange.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * {@link DirectIO} - A utility class for direct byte buffers and I/O.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DirectIO {

    /**
     * Initializes a new {@link DirectIO}.
     */
    private DirectIO() {
        super();
    }

    private static volatile Method cleanerMethod;
    private static Method cleanerMethod() {
        Method m = cleanerMethod;
        if (null == m) {
            synchronized (DirectIO.class) {
                m = cleanerMethod;
                if (null == m) {
                    try {
                        m = ByteBuffer.class.getMethod("cleaner");
                        m.setAccessible(true);
                        cleanerMethod = m;
                    } catch (Exception e) {
                        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirectIO.class);
                        log.error("Couldn't initialze Java Reflection method for \"cleaner\".", e);
                    }
                }
            }
        }
        return m;
    }

    /**
     * DirectByteBuffers are garbage collected by using a phantom reference and a reference queue. Every once a while, the JVM checks the
     * reference queue and cleans the DirectByteBuffers. However, as this doesn't happen immediately after discarding all references to a
     * DirectByteBuffer, it's easy to OutOfMemoryError yourself using DirectByteBuffers. This function explicitly calls the Cleaner method
     * of a DirectByteBuffer.
     *
     * @param toBeDestroyed The DirectByteBuffer that will be "cleaned". Utilizes reflection.
     */
    public static void destroyDirectByteBuffer(final ByteBuffer toBeDestroyed) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
        if (null == toBeDestroyed || !toBeDestroyed.isDirect()) {
            return;
        }

        final Method cleanerMethod = cleanerMethod();
        final Object cleaner = cleanerMethod.invoke(toBeDestroyed);
        final Method cleanMethod = cleaner.getClass().getMethod("clean");
        cleanMethod.setAccessible(true);
        cleanMethod.invoke(cleaner);
    }

}
