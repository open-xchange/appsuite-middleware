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
import java.util.List;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.MandatoryFilter;
import com.openexchange.find.spi.ModuleSearchDriver;

/**
 * A {@link ModuleConfig} encapsulates the configuration for a specific module.
 * Each {@link ModuleSearchDriver} provides its configuration via
 * {@link ModuleSearchDriver#getConfiguration(com.openexchange.tools.session.ServerSession)}
 * . A modules configuration models the limitations of a concrete
 * {@link ModuleSearchDriver} as static facets and mandatory filters. Static
 * facets are facets that are not calculated during search time. There is no
 * need to deliver them in an autocomplete response, so they are part of the
 * configuration response. A mandatory filter is a filter that must be set
 * within every search request. Every mandatory filter belongs to a static
 * facet. Additionally it references a filter from that facet as its default.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class ModuleConfig implements Serializable {

    private static final long serialVersionUID = 4128963449124744664L;

    private final Module module;

    private final List<Facet> facets;

    private final List<MandatoryFilter> mandatoryFilters;

    public ModuleConfig(Module module, List<Facet> facets, List<MandatoryFilter> mandatoryFilters) {
        super();
        this.module = module;
        this.facets = facets;
        this.mandatoryFilters = mandatoryFilters;
    }

    public Module getModule() {
        return module;
    }

    /**
     * Returns the list of static facets. May be empty but never
     * <code>null</code>.
     */
    public List<Facet> getStaticFacets() {
        return facets;
    }

    /**
     * Returns the list of mandatory filters. May be empty but never
     * <code>null</code>.<br>
     * <br>
     * For every mandatory filter the value of
     * {@link MandatoryFilter#getFacet()} must be contained in
     * {@link ModuleConfig#getStaticFacets()}. Additionally the value of
     * {@link MandatoryFilter#getDefaultValue()} must be part of that static
     * facets value list.
     */
    public List<MandatoryFilter> getMandatoryFilters() {
        return mandatoryFilters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((facets == null) ? 0 : facets.hashCode());
        result = prime * result + ((mandatoryFilters == null) ? 0 : mandatoryFilters.hashCode());
        result = prime * result + ((module == null) ? 0 : module.hashCode());
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
        ModuleConfig other = (ModuleConfig) obj;
        if (facets == null) {
            if (other.facets != null)
                return false;
        } else if (!facets.equals(other.facets))
            return false;
        if (mandatoryFilters == null) {
            if (other.mandatoryFilters != null)
                return false;
        } else if (!mandatoryFilters.equals(other.mandatoryFilters))
            return false;
        if (module != other.module)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ModuleConfig [module=" + module + ", facets=" + facets + ", mandatoryFilters=" + mandatoryFilters + "]";
    }

}
