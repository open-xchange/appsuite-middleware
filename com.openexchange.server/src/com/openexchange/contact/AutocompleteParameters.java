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
package com.openexchange.contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;

/**
 * An {@link AutocompleteParameters} instance encapsulates additional parameters
 * for the auto-complete methods in {@link ContactService} and {@link ContactStorage}
 * to provide a further extensibility.<br>
 * <br>
 * <b>Common parameter keys are defined as constants within this class. Implementations
 * of {@link ContactStorage} must heed those parameters if they have an influence on
 * the search results.</b>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class AutocompleteParameters implements Map<String, Object> {

	/**
	 * The parameter key to indicate if the returned contacts should have at least one e-mail address.
	 * If so, the value must be {@link Boolean#TRUE}, otherwise <code>false</code> is assumed.<br>
	 * <b>Default value: <code>true</code></b>
	 */
	public static final String REQUIRE_EMAIL = "require_email";

	/**
	 * The parameter key to specify if distribution lists shall be ignored and not be part of the
	 * returned results. If so, the value must be {@link Boolean#TRUE}, otherwise <code>false</code>
	 * is assumed.<br>
	 * <b>Default value: <code>false</code></b>
	 */
	public static final String IGNORE_DISTRIBUTION_LISTS = "ignore_distribution_lists";

    public static final String USER_ID = "user_id";

    /**
     * May hold an <code>int</code>-Array of those user identifiers the search should be restricted to, i.e. only contacts whose internal
     * user id matches one of them will be matched.
     */
    public static final String MATCH_USER_IDS = "match_user_ids";

	private final Map<String, Object> parameters = new HashMap<String, Object>();
	private List<OXException> warnings;

	private AutocompleteParameters() {
		super();
	}

	/**
	 * Creates a new, empty {@link AutocompleteParameters} instance.
	 */
	public static AutocompleteParameters newInstance() {
		return new AutocompleteParameters();
	}

	/**
	 * Gets the boolean value for the given key or the passed default value,
	 * if the parameter is not set or not of type {@link Boolean}.
	 *
	 * @param key The parameter key
	 * @param defaultValue The default value
	 * @return The parameters boolean value
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		Object object = get(key);
		if (object == null || !(object instanceof Boolean)) {
			return defaultValue;
		}

		return ((Boolean) object).booleanValue();
	}

	/**
	 * Gets the integer value for the given key or the passed default value,
	 * if the parameter is not set or not of type {@link Integer}.
	 *
	 * @param key The parameter key
	 * @param defaultValue The default value
	 * @return The parameters integer value
	 */
	public int getInteger(String key, int defaultValue) {
		Object object = get(key);
		if (object == null || !(object instanceof Integer)) {
			return defaultValue;
		}

		return ((Integer) object).intValue();
	}

	/**
	 * Gets the long value for the given key or the passed default value,
	 * if the parameter is not set or not of type {@link Long}.
	 *
	 * @param key The parameter key
	 * @param defaultValue The default value
	 * @return The parameters long value
	 */
	public long getLong(String key, long defaultValue) {
		Object object = get(key);
		if (object == null || !(object instanceof Long)) {
			return defaultValue;
		}

		return ((Long) object).longValue();
	}

	/**
	 * Gets the float value for the given key or the passed default value,
	 * if the parameter is not set or not of type {@link Float}.
	 *
	 * @param key The parameter key
	 * @param defaultValue The default value
	 * @return The parameters float value
	 */
	public float getFloat(String key, float defaultValue) {
		Object object = get(key);
		if (object == null || !(object instanceof Float)) {
			return defaultValue;
		}

		return ((Float) object).floatValue();
	}

	/**
	 * Gets the double value for the given key or the passed default value,
	 * if the parameter is not set or not of type {@link Double}.
	 *
	 * @param key The parameter key
	 * @param defaultValue The default value
	 * @return The parameters double value
	 */
	public double getDouble(String key, double defaultValue) {
		Object object = get(key);
		if (object == null || !(object instanceof Double)) {
			return defaultValue;
		}

		return ((Double) object).doubleValue();
	}

	/**
	 * Gets the string value for the given key or the passed default value,
	 * if the parameter is not set or not of type {@link String}.
	 *
	 * @param key The parameter key
	 * @param defaultValue The default value
	 * @return The parameters string value
	 */
	public String getString(String key, String defaultValue) {
		Object object = get(key);
		if (object == null || !(object instanceof String)) {
			return defaultValue;
		}

		return (String) object;
	}

    /**
     * Gets the warnings.
     *
     * @return The warnings, or <code>null</code> if there were none
     */
    public List<OXException> getWarnings() {
        return warnings;
    }

    /**
     * Adds a warning.
     *
     * @param warning The warning to add
     */
    public void addWarning(OXException warning) {
        if (null == warnings) {
            warnings = new ArrayList<OXException>();
        }
        warnings.add(warning);
    }

	/*
	 * Map implementation
	 */
	@Override
	public int size() {
		return parameters.size();
	}

	@Override
	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return parameters.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return parameters.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return parameters.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		return parameters.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return parameters.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		parameters.putAll(m);
	}

	@Override
	public void clear() {
		parameters.clear();
	}

	@Override
	public Set<String> keySet() {
		return parameters.keySet();
	}

	@Override
	public Collection<Object> values() {
		return parameters.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return parameters.entrySet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
            return true;
        }
		if (obj == null) {
            return false;
        }
		if (getClass() != obj.getClass()) {
            return false;
        }
		AutocompleteParameters other = (AutocompleteParameters) obj;
		if (parameters == null) {
			if (other.parameters != null) {
                return false;
            }
		} else if (!parameters.equals(other.parameters)) {
            return false;
        }
		return true;
	}

	@Override
	public String toString() {
		return "[AutocompleteParameters: " + parameters.toString() + "]";
	}

}
