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

package com.openexchange.file.storage.search;

import static com.openexchange.java.Strings.toLowerCase;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.java.Strings;

/**
 * {@link MetaTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class MetaTerm implements SearchTerm<String> {

    private final String pattern;

    /**
     * Initializes a new {@link MetaTerm}.
     */
    public MetaTerm(final String pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public void visit(final SearchTermVisitor visitor) throws OXException {
        if (null != visitor) {
            visitor.visit(this);
        }
    }

    @Override
    public void addField(final Collection<Field> col) {
        if (null != col) {
            col.add(Field.META);
        }
    }

    @Override
    public boolean matches(final File file) throws OXException {
        if (Strings.isEmpty(pattern)) {
            return false;
        }

        final Map<String, Object> meta = file.getMeta();
        if (null == meta || meta.isEmpty()) {
            return false;
        }

        return lookUpMap(toLowerCase(pattern), meta);
    }

    private boolean lookUpMap(final String lookUp, final Map<String, Object> map) {
        if (null == map || map.isEmpty()) {
            return false;
        }

        for (final Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            if (toLowerCase(key).indexOf(lookUp) >= 0) {
                return true;
            }

            if (lookUpObject(lookUp, entry.getValue())) {
                return true;
            }
        }

        return false;
    }

    private boolean lookUpCollection(final String lookUp, final Collection<Object> col) {
        if (null == col || col.isEmpty()) {
            return false;
        }

        for (final Object object : col) {
            if (lookUpObject(lookUp, object)) {
                return true;
            }
        }

        return false;
    }

    private boolean lookUpObject(final String lookUp, final Object value) {
        if (value instanceof Map) {
            return lookUpMap(lookUp, (Map<String, Object>) value);
        }
        if (value instanceof Collection) {
            return lookUpCollection(lookUp, (Collection<Object>) value);
        }
        if (toLowerCase(value.toString()).indexOf(lookUp) >= 0) {
            return true;
        }
        return false;
    }

}
