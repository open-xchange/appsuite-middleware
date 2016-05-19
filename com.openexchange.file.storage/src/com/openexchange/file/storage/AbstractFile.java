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

package com.openexchange.file.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.file.storage.meta.FileComparator;
import com.openexchange.file.storage.meta.FileFieldGet;
import com.openexchange.file.storage.meta.FileFieldHandling;

/**
 * {@link AbstractFile} - An abstract file.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractFile implements File {

    /** The boolean value indicating whether whether {@link #getFileSize()} returns the exact size w/o any encodings (e.g. base64) applied */
    protected boolean accurateSize;

    /**
     * Initializes a new {@link AbstractFile}.
     */
    protected AbstractFile() {
        super();
        accurateSize = true;
    }

    @Override
    public boolean isAccurateSize() {
        return accurateSize;
    }

    @Override
    public void setAccurateSize(boolean accurateSize) {
        this.accurateSize = accurateSize;
    }

    @Override
    public File dup() {
        return FileFieldHandling.dup(this);
    }

    @Override
    public void copyInto(final File other) {
        FileFieldHandling.copy(this, other);
    }

    @Override
    public void copyFrom(final File other) {
        FileFieldHandling.copy(other, this);
    }

    @Override
    public void copyInto(final File other, final Field... fields) {
        FileFieldHandling.copy(this, other, fields);
    }

    @Override
    public void copyFrom(final File other, final Field... fields) {
        FileFieldHandling.copy(other, this, fields);
    }

    @Override
    public Set<File.Field> differences(final File other) {
        return Field.inject(new AbstractFileFieldHandler() {

            @Override
            public Object handle(final Field field, final Object... args) {
                @SuppressWarnings("unchecked")
                final Set<Object> set = get(0, Set.class, args);
                final int comparison = new FileComparator(field).compare(AbstractFile.this, other);
                if (comparison != 0) {
                    set.add(field);
                }
                return set;
            }

        }, new HashSet<File.Field>());
    }

    @Override
    public boolean equals(final File other, final Field criterium, final Field... criteria) {
        final List<Field> fields = new ArrayList<Field>(1 + criteria.length);

        for (final Field field : fields) {
            if (0 != new FileComparator(field).compare(this, other)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(final Object other) {
        return equals((File) other, File.Field.ID, File.Field.values());
    }

    @Override
    public String toString() {
        return FileFieldHandling.toString(this);
    }

    @Override
    public boolean matches(final String pattern, final Field... fields) {
        final Pattern regex = Pattern.compile(wildcardToRegex(pattern), Pattern.CASE_INSENSITIVE);
        final FileFieldGet fileFieldGet = new FileFieldGet();
        for (final Field field : null == fields || 0 == fields.length ? DEFAULT_SEARCH_FIELDS : EnumSet.copyOf(Arrays.asList(fields))) {
            final Object value = field.doSwitch(fileFieldGet, this);
            if (null != value && regex.matcher(value.toString()).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts specified wildcard string to a regular expression
     *
     * @param wildcard The wildcard string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern pattern}
     */
    private static String wildcardToRegex(final String wildcard) {
        final StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}' || c == '|' || c == '\\'|| c == '+') {
                s.append('\\');
                s.append(c);
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }

}
