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

package com.openexchange.config.cascade.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.exception.OXException;


/**
 * {@link InMemoryConfigProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InMemoryConfigProvider implements ConfigProviderService{

    ConcurrentHashMap<String, String> values = new ConcurrentHashMap<String, String>();
    ConcurrentHashMap<String, ConcurrentHashMap<String, String>> metadata = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();


    @Override
    public BasicProperty get(final String propertyName, final int contextId, final int userId) throws OXException {
        return new BasicProperty() {

            @Override
            public String get() throws OXException {
                return values.get(propertyName);
            }

            @Override
            public void set(String value) throws OXException {
                values.put(propertyName, value);
            }

            @Override
            public String get(String metadataName) throws OXException {
                return getMetadata().get(metadataName);
            }

            @Override
            public void set(String metadataName, String value) throws OXException {
                getMetadata().put(metadataName, value);
            }

            private ConcurrentHashMap<String, String> getMetadata() {
                ConcurrentHashMap<String, String> retval = metadata.get(propertyName);
                if (retval == null) {
                    retval = metadata.putIfAbsent(propertyName, new ConcurrentHashMap<String, String>());
                    if (retval == null) {
                        retval = metadata.get(propertyName);
                    }
                }
                return retval;
            }

            @Override
            public boolean isDefined() throws OXException {
                return values.contains(propertyName);
            }

            @Override
            public List<String> getMetadataNames() throws OXException {
                return Collections.emptyList();
            }

        };
    }
    
    @Override
    public String getScope() {
    	return "inMemory";
    }


    @Override
    public Collection<String> getAllPropertyNames(int contextId, int userId) throws OXException {
        return values.keySet();
    }




}
