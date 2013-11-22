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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.subscribe.crawler;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * {@link SimBundleContext}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class SimBundleContext implements BundleContext {

    public SimBundleContext(){
        super();
    }

    @Override
    public void addBundleListener(final BundleListener arg0) {
        // Nothing to do
    }

    @Override
    public void addFrameworkListener(final FrameworkListener arg0) {
        // Nothing to do
    }

    @Override
    public void addServiceListener(final ServiceListener arg0) {
        // Nothing to do
    }

    @Override
    public void addServiceListener(final ServiceListener arg0, final String arg1) throws InvalidSyntaxException {
        // Nothing to do
    }

    @Override
    public Filter createFilter(final String arg0) throws InvalidSyntaxException {
        // Nothing to do
        return null;
    }

    @Override
    public ServiceReference<?>[] getAllServiceReferences(final String arg0, final String arg1) throws InvalidSyntaxException {
        // Nothing to do
        return null;
    }

    @Override
    public Bundle getBundle() {
        // Nothing to do
        return null;
    }

    @Override
    public Bundle getBundle(final long arg0) {
        // Nothing to do
        return null;
    }

    @Override
    public Bundle[] getBundles() {
        // Nothing to do
        return null;
    }

    @Override
    public File getDataFile(final String arg0) {
        // Nothing to do
        return null;
    }

    @Override
    public String getProperty(final String arg0) {
        // Nothing to do
        return null;
    }

    @Override
    public ServiceReference<?> getServiceReference(final String arg0) {
        // Nothing to do
        return null;
    }

    @Override
    public ServiceReference<?>[] getServiceReferences(final String arg0, final String arg1) throws InvalidSyntaxException {
        // Nothing to do
        return null;
    }

    @Override
    public Bundle installBundle(final String arg0) throws BundleException {
        // Nothing to do
        return null;
    }

    @Override
    public Bundle installBundle(final String arg0, final InputStream arg1) throws BundleException {
        // Nothing to do
        return null;
    }

    @Override
    public void removeBundleListener(final BundleListener arg0) {
        // Nothing to do
    }

    @Override
    public void removeFrameworkListener(final FrameworkListener arg0) {
        // Nothing to do
    }

    @Override
    public void removeServiceListener(final ServiceListener arg0) {
        // Nothing to do
    }

    @Override
    public ServiceRegistration<?> registerService(final String[] clazzes, final Object service, final Dictionary<String, ?> properties) {
        // Nothing to do
        return null;
    }

    @Override
    public ServiceRegistration<?> registerService(final String clazz, final Object service, final Dictionary<String, ?> properties) {
        // Nothing to do
        return null;
    }

    @Override
    public <S> ServiceRegistration<S> registerService(final Class<S> clazz, final S service, final Dictionary<String, ?> properties) {
        // Nothing to do
        return null;
    }

    @Override
    public <S> ServiceReference<S> getServiceReference(final Class<S> clazz) {
        // Nothing to do
        return null;
    }

    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(final Class<S> clazz, final String filter) throws InvalidSyntaxException {
        // Nothing to do
        return null;
    }

    @Override
    public <S> S getService(final ServiceReference<S> reference) {
        // Nothing to do
        return null;
    }

    @Override
    public boolean ungetService(final ServiceReference<?> reference) {
        // Nothing to do
        return false;
    }

    @Override
    public Bundle getBundle(final String location) {
        // Nothing to do
        return null;
    }
}
