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

import java.util.List;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.exception.OXException;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link CoercingConfigProperty}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CoercingConfigProperty<T> implements ConfigProperty<T> {

    private final Class<T> type;

    private final BasicProperty delegate;

    private final StringParser parser;

    public CoercingConfigProperty(final Class<T> type, final BasicProperty delegate, final StringParser parser) {
        super();
        this.type = type;
        this.delegate = delegate;
        this.parser = parser;
    }

    @Override
    public T get() throws OXException {
        final String value = delegate.get();
        return parse(value, type);
    }

    private <S> S parse(final String value, final Class<S> s) throws OXException {
        if (value == null) {
            return null;
        }

        final S parsed = parser.parse(value, s);
        if (parsed == null) {
            throw ConfigCascadeExceptionCodes.COULD_NOT_COERCE_VALUE.create(value, s.getName());
        }
        return parsed;
    }

    @Override
    public String get(final String metadataName) throws OXException {
        return delegate.get(metadataName);
    }

    @Override
    public <M> M get(final String metadataName, final Class<M> m) throws OXException {
        return parse(delegate.get(metadataName), m);
    }

    @Override
    public boolean isDefined() throws OXException {
        return delegate.isDefined();
    }

    @Override
    public CoercingConfigProperty<T> set(final T value) throws OXException {
        delegate.set(null == value ? null : value.toString()); // We assume good toString methods that allow reparsing
        return this;
    }

    @Override
    public <M> CoercingConfigProperty<T> set(final String metadataName, final M value) throws OXException {
        delegate.set(metadataName, value.toString());
        return this;
    }

    @Override
    public <M> ConfigProperty<M> to(final Class<M> otherType) throws OXException {
        return new CoercingConfigProperty<M>(otherType, delegate, parser);
    }

    @Override
    public List<String> getMetadataNames() throws OXException {
        return delegate.getMetadataNames();
    }

}
