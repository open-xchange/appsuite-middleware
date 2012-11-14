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

package com.openexchange.aws.s3.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.amazonaws.services.s3.AmazonS3;
import com.openexchange.aws.s3.AWSS3Configuration;
import com.openexchange.aws.s3.AWSS3FileStorageFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.file.external.FileStorageFactoryCandidate;

/**
 * {@link AWSS3Activator}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSS3Activator extends HousekeepingActivator {

    private final static Log LOG = LogFactory.getLog(AWSS3Activator.class);

    /**
     * Initializes a new {@link AWSS3Activator}.
     */
    public AWSS3Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, AmazonS3.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.aws.s3");
        AmazonS3 s3Client = getService(AmazonS3.class);
        ConfigurationService configService = getService(ConfigurationService.class);
        String defaultBucket = configService.getProperty("com.openexchange.aws.s3.defaultBucket");
        boolean versioningEnabled = configService.getBoolProperty("com.openexchange.s3.versioning", true);
        AWSS3Configuration config = new AWSS3Configuration(defaultBucket, versioningEnabled);
        AWSS3FileStorageFactory fileStorage = new AWSS3FileStorageFactory(s3Client, config);
        registerService(FileStorageFactoryCandidate.class, fileStorage);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.aws.s3");
        unregisterServices();
        cleanUp();
    }

}
