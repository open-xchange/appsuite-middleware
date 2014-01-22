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

/**
 * {@link LimitedStringBuilder} - A limited <code>StringBuilder</code>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LimitedStringBuilder implements Appendable, CharSequence {

    private final int limit;
    private final StringBuilder sb;

    /**
     * Initializes a new {@link LimitedStringBuilder}.
     */
    public LimitedStringBuilder(final int limit) {
        super();
        this.limit = limit;
        this.sb = new StringBuilder(limit + 8);
    }

    @Override
    public int length() {
        return sb.length();
    }

    /**
     * Appends abbreviating dots character sequence.
     */
    public LimitedStringBuilder appendDots() {
        sb.append("...");
        return this;
    }

    private LimitExceededException newLimitExceededException() {
        return new LimitExceededException("Exceeded limit of " + limit + " characters.");
    }

    private void checkLimit(final CharSequence s) {
        if (sb.length() + s.length() > limit) {
            throw newLimitExceededException();
        }
    }

    public LimitedStringBuilder append(final Object obj) {
        final String s = String.valueOf(obj);
        checkLimit(s);
        sb.append(s);
        return this;
    }

    public LimitedStringBuilder append(final String str) {
        final String s = null == str ? "null" : str;
        checkLimit(s);
        sb.append(s);
        return this;
    }

    @Override
    public char charAt(final int index) {
        return sb.charAt(index);
    }

    @Override
    public LimitedStringBuilder append(final CharSequence cs) {
        final CharSequence s = null == cs ? "null" : cs;
        checkLimit(s);
        sb.append(s);
        return this;
    }

    @Override
    public LimitedStringBuilder append(final CharSequence cs, final int start, final int end) {
        final CharSequence s = null == cs ? "null" : cs;
        if (sb.length() + (end - start) > limit) {
            throw newLimitExceededException();
        }
        sb.append(s, start, end);
        return this;
    }

    public LimitedStringBuilder append(final char[] str) {
        if (null == str) {
            return append("null");
        }
        if (sb.length() + str.length > limit) {
            throw newLimitExceededException();
        }
        sb.append(str);
        return this;
    }

    public LimitedStringBuilder append(final char[] str, final int offset, final int len) {
        if (null == str) {
            return append("null");
        }
        if (sb.length() + (len - offset) > limit) {
            throw newLimitExceededException();
        }
        sb.append(str, offset, len);
        return this;
    }

    public LimitedStringBuilder append(final boolean b) {
        final String s = String.valueOf(b);
        checkLimit(s);
        sb.append(s);
        return this;
    }

    @Override
    public LimitedStringBuilder append(final char c) {
        final String s = String.valueOf(c);
        checkLimit(s);
        sb.append(s);
        return this;
    }

    public LimitedStringBuilder append(final int i) {
        final String s = String.valueOf(i);
        checkLimit(s);
        sb.append(s);
        return this;
    }

    public int codePointBefore(final int index) {
        return sb.codePointBefore(index);
    }

    public LimitedStringBuilder append(final long lng) {
        final String s = String.valueOf(lng);
        checkLimit(s);
        sb.append(s);
        return this;
    }

    public LimitedStringBuilder append(final float f) {
        final String s = String.valueOf(f);
        checkLimit(s);
        sb.append(s);
        return this;
    }

    public LimitedStringBuilder append(final double d) {
        final String s = String.valueOf(d);
        checkLimit(s);
        sb.append(s);
        return this;
    }

    public LimitedStringBuilder delete(final int start, final int end) {
        sb.delete(start, end);
        return this;
    }

    public LimitedStringBuilder deleteCharAt(final int index) {
        sb.deleteCharAt(index);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public void setCharAt(final int index, final char ch) {
        sb.setCharAt(index, ch);
    }

    public String substring(final int start) {
        return sb.substring(start);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return sb.subSequence(start, end);
    }

    public String substring(final int start, final int end) {
        return sb.substring(start, end);
    }
    
}
