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

package com.openexchange.config.cascade.impl;

import java.util.List;

import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.tools.strings.StringParser;


/**
 * {@link CoercingComposedConfigProperty}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CoercingComposedConfigProperty<T> implements ComposedConfigProperty<T> {
    private ComposedConfigProperty<String> delegate;
    private StringParser stringParser;
    private final Class<T> coerceTo;

    public CoercingComposedConfigProperty(final Class<T> coerceTo, final ComposedConfigProperty<String> delegate, final StringParser stringParser) {
        this.stringParser = stringParser;
        this.coerceTo = coerceTo;
        this.stringParser = stringParser;
        initDelegate(delegate);

    }

    private void initDelegate(final ComposedConfigProperty<String> d) {
        this.delegate = d;
    }

    @Override
    public ComposedConfigProperty<T> precedence(final String... scopes) throws OXException {
        initDelegate(delegate.precedence(scopes));
        return this;
    }

    @Override
    public T get() throws OXException {
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
    public CoercingComposedConfigProperty<T> set(final T value) throws OXException {
        delegate.set(value.toString()); // We assume good toString methods that allow reparsing
        return this;
    }

    @Override
    public <M> CoercingComposedConfigProperty<T> set(final String metadataName, final M value) throws OXException {
        delegate.set(metadataName, value);
        return this;
    }

    @Override
    public <M> ComposedConfigProperty<M> to(final Class<M> otherType) throws OXException {
        return delegate.to(otherType);
    }

    @Override
    public List<String> getMetadataNames() throws OXException {
        return delegate.getMetadataNames();
    }
    
    @Override
    public String getScope() throws OXException {
    	return delegate.getScope();
    }

}
