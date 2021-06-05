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
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.exception.OXException;
import com.openexchange.tools.strings.StringParser;


/**
 * {@link CoercingComposedConfigProperty}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CoercingComposedConfigProperty<T> implements ComposedConfigProperty<T> {

    private final AtomicReference<ComposedConfigProperty<String>> delegate;
    private final StringParser stringParser;
    private final Class<T> coerceTo;

    public CoercingComposedConfigProperty(final Class<T> coerceTo, final ComposedConfigProperty<String> delegate, final StringParser stringParser) {
        super();
        this.delegate = new AtomicReference<ComposedConfigProperty<String>>(null);
        this.stringParser = stringParser;
        this.coerceTo = coerceTo;
        initDelegate(delegate);
    }

    private void initDelegate(final ComposedConfigProperty<String> d) {
        this.delegate.set(d);
    }

    @Override
    public ComposedConfigProperty<T> precedence(ConfigViewScope... scopes) throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        initDelegate(delegate.precedence(scopes));
        return this;
    }

    @Override
    public ComposedConfigProperty<T> precedence(final String... scopes) throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        initDelegate(delegate.precedence(scopes));
        return this;
    }

    @Override
    public T get() throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        final String value = delegate.get();
        return parse(value, coerceTo);
    }

    private <S> S parse(final String value, final Class<S> s) throws OXException {
        if (value == null) {
            return null;
        }

        final S parsed = stringParser.parse(value, s);
        if (parsed == null) {
            throw ConfigCascadeExceptionCodes.COULD_NOT_COERCE_VALUE.create(value, s.getName());
        }
        return parsed;
    }

    @Override
    public String get(final String metadataName) throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        return delegate.get(metadataName);
    }

    @Override
    public <M> M get(final String metadataName, final Class<M> m) throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        return parse(delegate.get(metadataName), m);
    }

    @Override
    public boolean isDefined() throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        return delegate.isDefined();
    }

    @Override
    public CoercingComposedConfigProperty<T> set(final T value) throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        delegate.set(null == value ? null : value.toString()); // We assume good toString methods that allow reparsing
        return this;
    }

    @Override
    public <M> CoercingComposedConfigProperty<T> set(final String metadataName, final M value) throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        delegate.set(metadataName, value);
        return this;
    }

    @Override
    public <M> ComposedConfigProperty<M> to(final Class<M> otherType) throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        return delegate.to(otherType);
    }

    @Override
    public List<String> getMetadataNames() throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
        return delegate.getMetadataNames();
    }

    @Override
    public String getScope() throws OXException {
        ComposedConfigProperty<String> delegate = this.delegate.get();
    	return delegate.getScope();
    }

}
