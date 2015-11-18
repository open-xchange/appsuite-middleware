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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding;

import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.lang.Validate;

/**
 * {@link DefaultEntityPath} - The default {@code EntityPath} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DefaultEntityPath implements EntityPath {

    private final OnboardingConfiguration service;
    private final Platform platform;
    private final Collection<IdEntity> col;
    private final Module module;
    private final Device device;
    private final String compositeId;

    /**
     * Initializes a new {@link DefaultEntityPath}.
     */
    public DefaultEntityPath(OnboardingConfiguration service, Device device, Module module) {
        this(service, device.getPlatform(), device, module, null);
    }

    /**
     * Initializes a new {@link DefaultEntityPath}.
     */
    public DefaultEntityPath(OnboardingConfiguration service, Platform platform, Device device, Module module) {
        this(service, platform, device, module, null);
    }

    /**
     * Initializes a new {@link DefaultEntityPath}.
     */
    public DefaultEntityPath(OnboardingConfiguration service, Platform platform, Device device, Module module, Collection<IdEntity> col) {
        super();
        Validate.notNull(service, "Service must not be null.");
        Validate.notNull(platform, "Platform must not be null.");
        Validate.notNull(module, "Module must not be null.");
        Validate.notNull(device, "Device must not be null.");
        this.service = service;
        this.platform = platform;
        this.module = module;
        this.device = device;
        this.col = col;
        compositeId = new StringBuilder(16).append(device.getId()).append('/').append(module.getId()).append('/').append(service.getId()).toString();
    }

    @Override
    public boolean matches(Device device, Module module) {
        if (this.device != device) {
            return false;
        }
        if (this.module != module) {
            return false;
        }
        return true;
    }

    @Override
    public String getCompositeId() {
        return compositeId;
    }

    @Override
    public OnboardingConfiguration getService() {
        return service;
    }

    @Override
    public Platform getPlatform() {
        return platform;
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public Iterator<IdEntity> iterator() {
        return null == col ? null : new UnmodifiableIterator<IdEntity>(col.iterator());
    }

    @Override
    public String toString() {
        return compositeId;
    }

    // --------------------------------------------------------------------------------------------------------

    private static class UnmodifiableIterator<E> implements Iterator<E> {

        private final Iterator<E> iter;

        UnmodifiableIterator(Iterator<E> iter) {
            super();
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public E next() {
            return iter.next();
        }

        /**
         * Guaranteed to throw an exception and leave the underlying data unmodified.
         *
         * @throws UnsupportedOperationException always
         */
        @Override
        public final void remove() {
            throw new UnsupportedOperationException("Iterator.remove() not supported");
        }
    }

}
