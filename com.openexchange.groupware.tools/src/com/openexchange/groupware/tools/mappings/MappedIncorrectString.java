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
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.IncorrectString;
import com.openexchange.exception.OXException.ProblematicAttribute;

/**
 * {@link MappedIncorrectString} - {@link ProblematicAttribute} implementation providing the mapping to the problematic attribute.
 *
 * @param <O> The type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MappedIncorrectString<O> implements IncorrectString {

	/**
	 * Extracts all mapped incorrect strings from the supplied problematic attributes.
	 *
	 * @param e The problematic attributes
	 * @return The mapped incorrect strings
	 */
	public static <O> List<MappedIncorrectString<O>> extract(ProblematicAttribute[] problematics) {
		List<MappedIncorrectString<O>> truncations = new ArrayList<MappedIncorrectString<O>>();
		if (null != problematics) {
			for (ProblematicAttribute problematic : problematics) {
				if (MappedIncorrectString.class.isInstance(problematic)) {
					truncations.add((MappedIncorrectString<O>) problematic);
				}
			}
		}
		return truncations;
	}

	/**
	 * Replaces all incorrect character sequences in problematic attributes with the supplied replacement string.
	 *
	 * @param problematics The problematic attributes
	 * @param object The object to correct the property values for
	 * @param replacement The replacement string to apply
	 * @return <code>true</code>, if the value was actually changed, <code>false</code>, otherwise
	 */
	public static <O> boolean replace(ProblematicAttribute[] problematics, O object, String replacement) throws OXException {
		boolean hasReplaced = false;
		List<MappedIncorrectString<O>> incorrectStrings = MappedIncorrectString.extract(problematics);
		if (null != incorrectStrings && 0 < incorrectStrings.size()) {
			for (MappedIncorrectString<O> incorrectString : incorrectStrings) {
				hasReplaced |= incorrectString.replace(object, replacement);
			}
		}
		return hasReplaced;
	}

	/**
	 * Removes problematic attributes from the supplied object.
	 *
	 * @param e The problematic attributes
	 * @param object The object to remove the property values for
	 */
	public static <O> void remove(ProblematicAttribute[] problematics, O object) {
		List<MappedIncorrectString<O>> incorrectStrings = MappedIncorrectString.extract(problematics);
		if (null != incorrectStrings && 0 < incorrectStrings.size()) {
			for (MappedIncorrectString<O> incorrectString : incorrectStrings) {
				incorrectString.remove(object);
			}
		}
	}

	private final Mapping<?, O> mapping;
    private final String incorrectString;
    private final String readableName;

	/**
	 * Initializes a new {@link MappedIncorrectString}.
	 *
	 * @param mapping The corresponding mapping
	 * @param incorrectString The incorrect string
	 * @param readableName A readable name
	 */
	public MappedIncorrectString(Mapping<? extends Object, O> mapping, String incorrectString, String readableName) {
		this.mapping = mapping;
		this.incorrectString = incorrectString;
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
     * Replaces all incorrect character sequences in the current property value of the object problematic with the supplied replacement string.
     *
	 * @param object The object to replace invalid strings in the property's value for
     * @param replacement The replacement string to apply
	 * @return <code>true</code>, if the characters were actually replaced in the value, <code>false</code>, otherwise
	 */
	public boolean replace(O object, String replacement) throws OXException {
       return mapping.replaceAll(object, incorrectString, replacement);
	}

	/**
	 * Removes the mapped property value from the supplied object.
	 *
	 * @param object the object to remove the property's value for
	 */
	public void remove(O object) {
		this.mapping.remove(object);
	}

    public String getReadableName() {
        return this.readableName;
    }

    @Override
    @Deprecated
    public int getId() {
        // Nothing to do
        return 0;
    }

    @Override
    public String getIncorrectString() {
        return incorrectString;
    }

}
