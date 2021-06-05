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

package com.openexchange.importexport.formats.csv;

import java.util.Map;
import java.util.Properties;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.groupware.contact.helpers.ContactField;


/**
 * {@link PropertyDrivenMapper}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class PropertyDrivenMapper extends AbstractOutlookMapper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertyDrivenMapper.class);

    private String encoding;
    private final String name;

    /**
     * Initializes a new {@link PropertyDrivenMapper}.
     *
     * @param props The properties holding the mappings
     * @param name The mapper's name
     */
	public PropertyDrivenMapper(Properties props, String name) {
	    super();
	    this.name = name;
        this.encoding = "UTF-8";
        for (Map.Entry<Object, Object>  entry : props.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());
            if ("encoding".equals(key)) {
                LOG.debug("Using encoding: {}", value);
                this.encoding = value;
            } else {
                ContactField mappedField = ContactMapper.getInstance().getMappedField(key);
                if (null == mappedField) {
                    LOG.debug("No contact field found for: \"{}\"", key);
                } else {
                    store(mappedField, value);
                }
            }
        }
    }

	@Override
	public String getEncoding() {
		return encoding;
	}

	public String getName() {
	    return name;
	}

    @Override
    public String toString() {
        return "PropertyDrivenMapper [encoding=" + encoding + ", name=" + name + "]";
    }

}
