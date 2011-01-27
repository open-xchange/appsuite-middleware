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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigCascadeException;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.tools.strings.StringParser;


/**
 * {@link CoercingComposedConfigProperty}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CoercingComposedConfigProperty<T> implements ComposedConfigProperty<T> {
    private ComposedConfigProperty<String> delegate;
    private StringParser stringParser;
    private Class<T> coerceTo;
    
    public CoercingComposedConfigProperty(Class<T> coerceTo, ComposedConfigProperty<String> delegate, StringParser stringParser) {
        this.stringParser = stringParser;
        this.coerceTo = coerceTo;
        this.stringParser = stringParser;
        initDelegate(delegate);
        
    }
 
    private void initDelegate(ComposedConfigProperty<String> d) {
        this.delegate = d;
    }

    public ComposedConfigProperty<T> precedence(String... scopes) throws ConfigCascadeException {
        initDelegate(delegate.precedence(scopes));
        return this;
    }
    
    public T get() throws ConfigCascadeException {
        String value = delegate.get();
        return parse(value, coerceTo);
    }

    private <S> S parse(String value, Class<S> s) throws ConfigCascadeException {
        if (value == null) {
            return null;
        }

        S parsed = stringParser.parse(value, s);
        if (parsed == null) {
            throw ConfigCascadeExceptionCodes.COULD_NOT_COERCE_VALUE.create(value, s.getName());
        }
        return parsed;
    }

    public String get(String metadataName) throws ConfigCascadeException {
        return delegate.get(metadataName);
    }

    public <M> M get(String metadataName, Class<M> m) throws ConfigCascadeException {
        return parse(delegate.get(metadataName), m);
    }

    public boolean isDefined() throws ConfigCascadeException {
        return delegate.isDefined();
    }

    public CoercingComposedConfigProperty<T> set(T value) throws ConfigCascadeException {
        delegate.set(value.toString()); // We assume good toString methods that allow reparsing
        return this;
    }

    public <M> CoercingComposedConfigProperty<T> set(String metadataName, M value) throws ConfigCascadeException {
        delegate.set(metadataName, value);
        return this;
    }

    public <M> ComposedConfigProperty<M> to(Class<M> otherType) throws ConfigCascadeException {
        return delegate.to(otherType);
    }

}
