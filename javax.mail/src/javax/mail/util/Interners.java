/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.mail.util;

import com.google.common.collect.Interner;

/**
 * {@link Interners} - Utility class providing certain {@link Interner} instances for memory-saving String handling.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Interners {

    /**
     * Initializes a new {@link Interners}.
     */
    private Interners() {
        super();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final Interner<String> FULL_NAME_INTERNER = com.google.common.collect.Interners.newWeakInterner();

    /**
     * Gets the <code>Interner</code> for folder full names; e.g. <code>"INBOX"</code>.
     *
     * @return The <code>Interner</code> for folder full names
     */
    public static Interner<String> getFullNameInterner() {
        return FULL_NAME_INTERNER;
    }

    /**
     * Interns the given folder full name.
     *
     * @param fullName The folder full name
     * @return The interned folder full name or <code>null</code> (if passed argument is <code>null</code>)
     */
    public static String internFullName(String fullName) {
        return internNullable(fullName, ATTRIBUTE_INTERNER);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final Interner<String> ATTRIBUTE_INTERNER = com.google.common.collect.Interners.newWeakInterner();

    /**
     * Gets the <code>Interner</code> for folder attributes; e.g. <code>"\NoSelect"</code>.
     *
     * @return The <code>Interner</code> for folder attributes
     */
    public static Interner<String> getAttributeInterner() {
        return ATTRIBUTE_INTERNER;
    }

    /**
     * Interns the given attribute.
     *
     * @param attribute The attribute
     * @return The interned attribute or <code>null</code> (if passed argument is <code>null</code>)
     */
    public static String internAttribute(String attribute) {
        return internNullable(attribute, ATTRIBUTE_INTERNER);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final Interner<String> TRANSFER_ENCODING_INTERNER = com.google.common.collect.Interners.newWeakInterner();

    /**
     * Gets the <code>Interner</code> for transfer encoding names; e.g. <code>"QUOTED-PRINTABLE"</code> or <code>"7BIT"</code>.
     *
     * @return The <code>Interner</code> for transfer encoding names
     */
    public static Interner<String> getTransferEncodingInterner() {
        return TRANSFER_ENCODING_INTERNER;
    }

    /**
     * Interns the given transfer encoding name.
     *
     * @param encoding The transfer encoding name
     * @return The interned transfer encoding name or <code>null</code> (if passed argument is <code>null</code>)
     */
    public static String internTransferEncoding(String encoding) {
        return internNullable(encoding, TRANSFER_ENCODING_INTERNER);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final Interner<String> CONTENT_ATTRIBUTE_INTERNER = com.google.common.collect.Interners.newWeakInterner();

    /**
     * Gets the <code>Interner</code> for all names related to content type or disposition.
     * <p>
     * Be it type, sub-type, parameter name, disposition, whatever; e.g. <code>"TEXT"</code>, <code>"HTML"</code>, <code>"CHARSET"</code>,
     * or <code>"INLINE"</code>.
     *
     * @return The <code>Interner</code> for content type names
     */
    public static Interner<String> getContentAttributeInterner() {
        return CONTENT_ATTRIBUTE_INTERNER;
    }

    /**
     * Interns the given name related to content type or disposition.
     *
     * @param type The name related to content type or disposition
     * @return The interned name or <code>null</code> (if passed argument is <code>null</code>)
     */
    public static String internContentAttribute(String name) {
        return internNullable(name, CONTENT_ATTRIBUTE_INTERNER);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final Interner<String> COMMAND_KEY_INTERNER = com.google.common.collect.Interners.newWeakInterner();

    /**
     * Gets the <code>Interner</code> for command keys; e.g. <code>"FETCH"</code> or <code>"LIST"</code>.
     *
     * @return The <code>Interner</code> for command keys
     */
    public static Interner<String> getCommandKeyInterner() {
        return COMMAND_KEY_INTERNER;
    }

    /**
     * Interns the given command key.
     *
     * @param key The command key
     * @return The interned command key or <code>null</code> (if passed argument is <code>null</code>)
     */
    public static String internCommandKey(String key) {
        return internNullable(key, COMMAND_KEY_INTERNER);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static <V> V internNullable(V sample, Interner<V> interner) {
        return sample == null ? null : interner.intern(sample);
    }

}
