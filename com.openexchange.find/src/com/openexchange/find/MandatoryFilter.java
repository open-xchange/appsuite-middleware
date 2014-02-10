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
package com.openexchange.find;

import java.io.Serializable;
import com.openexchange.find.spi.ModuleSearchDriver;

/**
 * A {@link MandatoryFilter} is part of  a modules {@link ModuleConfig}.
 * It denotes a filter must be contained in every {@link SearchRequest}
 * for its according {@link ModuleSearchDriver}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class MandatoryFilter implements Serializable {

    private static final long serialVersionUID = 4594713366656547759L;

    private final Facet facet;

    private final FacetValue defaultValue;


    public MandatoryFilter(Facet facet, FacetValue defaultValue) {
        super();
        this.facet = facet;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the filters {@link Facet}. Never <code>null</code>.
     */
    public Facet getFacet() {
        return facet;
    }

    /**
     * A default {@link FacetValue} contained in the list of values
     * of the filters facet. Never <code>null</code>.
     */
    public FacetValue getDefaultValue() {
        return defaultValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        result = prime * result + ((facet == null) ? 0 : facet.hashCode());
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
        MandatoryFilter other = (MandatoryFilter) obj;
        if (defaultValue == null) {
            if (other.defaultValue != null)
                return false;
        } else if (!defaultValue.equals(other.defaultValue))
            return false;
        if (facet == null) {
            if (other.facet != null)
                return false;
        } else if (!facet.equals(other.facet))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MandatoryFilter [facet=" + facet + ", defaultValue=" + defaultValue + "]";
    }

}
