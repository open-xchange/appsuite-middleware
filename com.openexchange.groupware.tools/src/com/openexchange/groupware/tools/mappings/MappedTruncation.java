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
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.exception.OXException.Truncated;

/**
 * {@link MappedTruncation} - {@link Truncated} implementation providing the
 * mapping to the truncated attribute.
 *
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MappedTruncation<O> implements Truncated {

	/**
	 * Extracts all mapped truncations from the supplied problematic attributes.
	 *
	 * @param e the problematic attributes
	 * @return the mapped truncations
	 */
	public static <O> List<MappedTruncation<O>> extract(ProblematicAttribute[] problematics) {
		List<MappedTruncation<O>> truncations = new ArrayList<MappedTruncation<O>>();
		if (null != problematics) {
			for (ProblematicAttribute problematic : problematics) {
				if (MappedTruncation.class.isInstance(problematic)) {
					truncations.add((MappedTruncation<O>)problematic);
				}
			}
		}
		return truncations;
	}

	/**
	 * Truncates all problematic attributes to the maximum allowed length in
	 * the supplied object.
	 *
	 * @param e the problematic attributes
	 * @param object the object to truncate the property values for
	 * @return <code>true</code>, if the value was actually truncated,
	 * <code>false</code>, otherwise
	 */
	public static <O> boolean truncate(ProblematicAttribute[] problematics, O object) throws OXException {
		boolean hasTrimmed = false;
		List<MappedTruncation<O>> truncations = MappedTruncation.extract(problematics);
		if (null != truncations && 0 < truncations.size()) {
			for (MappedTruncation<O> truncation : truncations) {
				hasTrimmed |= truncation.truncate(object);
			}
		}
		return hasTrimmed;
	}

	/**
	 * Removes problematic attributes from the supplied object.
	 *
	 * @param e the problematic attributes
	 * @param object the object to remove the property values for
	 */
	public static <O> void remove(ProblematicAttribute[] problematics, O object) {
		List<MappedTruncation<O>> truncations = MappedTruncation.extract(problematics);
		if (null != truncations && 0 < truncations.size()) {
			for (MappedTruncation<O> truncation : truncations) {
				truncation.remove(object);
			}
		}
	}

	private final int maxSize;
	private final int length;
	private final Mapping<?, O> mapping;
	private final String readableName;

	/**
	 * Initializes a new {@link MappedTruncation}.
	 *
	 * @param mapping the corresponding mapping
	 * @param maxSize the maximum allowed size for the property
	 * @param length the actual length
	 */
	public MappedTruncation(Mapping<? extends Object, O> mapping, int maxSize, int length, String readableName) {
		this.mapping = mapping;
		this.maxSize = maxSize;
		this.length = length;
		this.readableName = readableName;
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
	 * Truncates the current property value to the maximum allowed length if it is
	 * longer.
	 *
	 * @param object the object to truncate the property's value for
	 * @return <code>true</code>, if the value was actually truncated,
	 * <code>false</code>, otherwise
	 */
	public boolean truncate(O object) throws OXException {
		return this.mapping.truncate(object, this.maxSize);
	}

	/**
	 * Removes the mapped property value from the supplied object.
	 *
	 * @param object the object to remove the property's value for
	 */
	public void remove(O object) {
		this.mapping.remove(object);
	}

	@Override
	@Deprecated
	public int getId() {
		// Nothing to do
		return 0;
	}

	@Override
	public int getMaxSize() {
		return this.maxSize;
	}

    @Override
    public int getLength() {
        return this.length;
    }

    public String getReadableName() {
        return this.readableName;
    }

}
