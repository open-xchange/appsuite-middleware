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
package org.xerial.snappy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi bundle entry point
 *
 * @author Ioannis Chouklis
 * @deprecated For OX use {@link com.openexchange.nosql.cassandra.osgi.CassandraActivator}
 */
@Deprecated
public class SnappyBundleActivator implements BundleActivator {

	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SnappyBundleActivator.class);

	@Override
    public void start(BundleContext context) throws Exception {
    	log.info("starting snappy bundle");
    	//System.loadLibrary("libsnappyjava");

    	String osName = System.getProperty("os.name");
    	String osArch = System.getProperty("os.arch");
    	String fileSeparator = System.getProperty("file.separator");
    	log.info("{} {}", osName, osArch);

    	/*if (osName.equals("Linux")) {
    		if (osArch.equals("amd64")) {
    			System.load("Linux/amd64/libsnappyjava.so");
    		} else if (osArch.equals("i386")) {
    			System.load("Linux/i386/libsnappyjava.so");
    		} else {
    			log.error("Unsupported system");
    		}
    	}*/

    	//System.load(osName + "/" + osArch + "/libsnappyjava.so");

    	//System.load("/home/isole/Downloads/snappy-java-1.0.4.1/target/snappy-1.0.4-Linux-amd64/libsnappyjava.so");
    	System.load(fileSeparator + "tmp" + fileSeparator + "SNAPPY" + fileSeparator + "native"  + fileSeparator + osName + fileSeparator + osArch + fileSeparator + "libsnappyjava.so");
    }
	@Override
	public void stop(BundleContext context) throws Exception {
		// Nothing to do

	}


}
