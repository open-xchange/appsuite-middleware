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

package com.openexchange.subscribe.crawler;

import java.io.File;
import java.io.InputStream;
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
        
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#addBundleListener(org.osgi.framework.BundleListener)
     */
    public void addBundleListener(BundleListener arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#addFrameworkListener(org.osgi.framework.FrameworkListener)
     */
    public void addFrameworkListener(FrameworkListener arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#addServiceListener(org.osgi.framework.ServiceListener)
     */
    public void addServiceListener(ServiceListener arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#addServiceListener(org.osgi.framework.ServiceListener, java.lang.String)
     */
    public void addServiceListener(ServiceListener arg0, String arg1) throws InvalidSyntaxException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#createFilter(java.lang.String)
     */
    public Filter createFilter(String arg0) throws InvalidSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#getAllServiceReferences(java.lang.String, java.lang.String)
     */
    public ServiceReference[] getAllServiceReferences(String arg0, String arg1) throws InvalidSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#getBundle()
     */
    public Bundle getBundle() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#getBundle(long)
     */
    public Bundle getBundle(long arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#getBundles()
     */
    public Bundle[] getBundles() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#getDataFile(java.lang.String)
     */
    public File getDataFile(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#getProperty(java.lang.String)
     */
    public String getProperty(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#getService(org.osgi.framework.ServiceReference)
     */
    public Object getService(ServiceReference arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#getServiceReference(java.lang.String)
     */
    public ServiceReference getServiceReference(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#getServiceReferences(java.lang.String, java.lang.String)
     */
    public ServiceReference[] getServiceReferences(String arg0, String arg1) throws InvalidSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#installBundle(java.lang.String)
     */
    public Bundle installBundle(String arg0) throws BundleException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#installBundle(java.lang.String, java.io.InputStream)
     */
    public Bundle installBundle(String arg0, InputStream arg1) throws BundleException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#registerService(java.lang.String[], java.lang.Object, java.util.Dictionary)
     */
    public ServiceRegistration registerService(String[] arg0, Object arg1, Dictionary arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#registerService(java.lang.String, java.lang.Object, java.util.Dictionary)
     */
    public ServiceRegistration registerService(String arg0, Object arg1, Dictionary arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#removeBundleListener(org.osgi.framework.BundleListener)
     */
    public void removeBundleListener(BundleListener arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#removeFrameworkListener(org.osgi.framework.FrameworkListener)
     */
    public void removeFrameworkListener(FrameworkListener arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#removeServiceListener(org.osgi.framework.ServiceListener)
     */
    public void removeServiceListener(ServiceListener arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleContext#ungetService(org.osgi.framework.ServiceReference)
     */
    public boolean ungetService(ServiceReference arg0) {
        // TODO Auto-generated method stub
        return false;
    }

}
