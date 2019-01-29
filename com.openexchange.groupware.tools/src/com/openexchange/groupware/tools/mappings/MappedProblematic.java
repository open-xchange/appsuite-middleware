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

package com.openexchange.groupware.tools.mappings;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException.ProblematicAttribute;

/**
 * {@link MappedProblematic}
 * <p/>
 * {@link ProblematicAttribute} implementation providing the mapping to the problematic attribute.
 *
 * @param <O> The type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class MappedProblematic<O> implements ProblematicAttribute {

	/**
     * Extracts all mapped problematics from the supplied problematic attributes.
     *
     * @param problematics The problematic attributes
     * @return The mapped problematics
     */
	public static <O> List<MappedProblematic<O>> extract(ProblematicAttribute[] problematics) {
        List<MappedProblematic<O>> mappedProblematics = new ArrayList<MappedProblematic<O>>();
		if (null != problematics) {
			for (ProblematicAttribute problematic : problematics) {
				if (MappedProblematic.class.isInstance(problematic)) {
                    mappedProblematics.add((MappedProblematic<O>) problematic);
				}
			}
		}
        return mappedProblematics;
	}

	/**
     * Removes problematic attributes from the supplied object.
     *
     * @param e The problematic attributes
     * @param object The object to remove the property values for
     * @return <code>true</code> if at least one problematic attributes was removed, <code>false</code>, otherwise
     */
    public static <O> boolean remove(ProblematicAttribute[] problematics, O object) {
        List<MappedProblematic<O>> mappedProblematics = MappedProblematic.extract(problematics);
        if (null != mappedProblematics && 0 < mappedProblematics.size()) {
            for (MappedProblematic<O> problematic : mappedProblematics) {
                problematic.remove(object);
			}
            return true;
		}
        return false;
	}

    protected final Mapping<?, O> mapping;

	/**
     * Initializes a new {@link MappedProblematic}.
     *
     * @param mapping The corresponding mapping
     */
    public MappedProblematic(Mapping<? extends Object, O> mapping) {
        super();
		this.mapping = mapping;
	}

	/**
	 * Gets the backing database mapping for the truncated attribute.
	 *
	 * @return the mapping
	 */
	public Mapping<?, O> getMapping() {
		return this.mapping;
	}

	/**
     * Removes the mapped property value from the supplied object.
     *
     * @param object The object to remove the property's value for
     */
	public void remove(O object) {
		this.mapping.remove(object);
	}

}
