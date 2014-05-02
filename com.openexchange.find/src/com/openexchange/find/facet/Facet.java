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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
package com.openexchange.find.facet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link Facet}s are used to refine a search by filtering results based
 * on categories.<br>
 * <br>
 * Example:<br>
 * You are searching in the mail module. A possible facet here can be "contacts".
 * A possible {@link FacetValue} might be "John Doe".
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class Facet implements Serializable {

    private static final long serialVersionUID = -8445505622030799014L;

    private final FacetType type;
    private final List<FacetValue> values;
    private final List<String> flags;

    /**
     * Initializes a new {@link Facet}.
     *
     * @param type The type
     * @param values The values
     * @throws NullPointerException If one of given parameters is <code>null</code>
     * @throws IllegalArgumentException If values is empty
     */
    public Facet(final FacetType type, final List<FacetValue> values) {
        super();
        checkNotNull(type);
        checkNotNull(values);
        checkArgument(!values.isEmpty());
        this.type = type;
        this.values = values;
        flags = new LinkedList<String>();
        for (FacetType conflicting : type.conflictingFacets()) {
            flags.add("conflicts:" + conflicting.getId());
        }
    }

    /**
     * @return The facets type, never <code>null</code>.
     */
    public FacetType getType() {
        return type;
    }

    /**
     * Returns a list of possible values.
     * E.g. "John Doe", "Jane Doe".
     */
    public List<FacetValue> getValues() {
        return values;
    }

    /**
     * Gets the facets flags.
     *
     * @return A list of flags; never <code>null</code> but possibly empty.
     */
    public List<String> getFlags() {
        return flags;
    }

    /**
     * Adds a flag to this facet.
     *
     * @param flag The flag.
     */
    public void addFlag(String flag) {
        flags.add(flag);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Facet other = (Facet) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Facet [type=" + type + ", values=" + values + "]";
    }

}
