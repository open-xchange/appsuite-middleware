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

package com.openexchange.find.facet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.LinkedList;
import java.util.List;


/**
 * A {@link DefaultFacet} contains multiple {@link FacetValue}s and may be present
 * multiple times in search requests to filter results by a combination of different
 * values (e.g. "mails with 'foo' and 'bar' in subject").
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class DefaultFacet extends AbstractFacet {

    private static final long serialVersionUID = 7955913533889391522L;

    private List<FacetValue> values;

    public DefaultFacet(final FacetType type) {
        super(type);
        this.values = new LinkedList<FacetValue>();
    }

    public DefaultFacet(final FacetType type, final List<FacetValue> values) {
        super(type);
        checkNotNull(values);
        checkArgument(!values.isEmpty());
        setValues(new LinkedList<FacetValue>(values));
    }

    /**
     * Only meant to be called by the builders in {@link Facet}.
     */
    DefaultFacet() {
        super();
        this.values = new LinkedList<FacetValue>();
    }

    /**
     * Only meant to be called by the builders in {@link Facet}.
     */
    void setValues(List<FacetValue> values) {
        this.values = values;
    }

    @Override
    public String getStyle() {
        return "default";
    }

    /**
     * Returns a list of possible values.
     * E.g. "John Doe", "Jane Doe".
     */
    public List<FacetValue> getValues() {
        return values;
    }

    public void addValue(FacetValue value) {
        values.add(value);
    }

    @Override
    public void accept(FacetVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultFacet other = (DefaultFacet) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Facet [type=" + getType() + ", style=" + getStyle() + ", values=" + values + "]";
    }
}
