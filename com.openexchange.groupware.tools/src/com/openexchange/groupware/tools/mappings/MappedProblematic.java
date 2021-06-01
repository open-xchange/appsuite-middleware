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
