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

package com.openexchange.spamhandler.spamexperts.management;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.URLName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;
import com.openexchange.spamhandler.spamexperts.exceptions.SpamExpertsExceptionCode;
import com.openexchange.spamhandler.spamexperts.osgi.SpamExpertsServiceRegistry;


/**
 * {@link SpamExpertsConfig}
 */
public class SpamExpertsConfig implements Initialization {

    private static final SpamExpertsConfig instance = new SpamExpertsConfig();
    
    private final AtomicBoolean started;

    private static final Logger LOG = LoggerFactory.getLogger(SpamExpertsConfig.class);

    private static final String CONFIG_SP_IMAPURL = "com.openexchange.custom.spamexperts.imapurl";
    private static final String CONFIG_SP_IMAPUSER = "com.openexchange.custom.spamexperts.imapuser";
    private static final String CONFIG_SP_IMAPPASSWORD = "com.openexchange.custom.spamexperts.imappassword";
    private static final String CONFIG_SP_TRAIN_SPAM_FOLDER = "com.openexchange.custom.spamexperts.trainspamfolder";
    private static final String CONFIG_SP_TRAIN_HAM_FOLDER = "com.openexchange.custom.spamexperts.trainhamfolder";

    private URLName imapUrl;
    private String imapUser;
    private String imappassword;
    private String trainSpamFolder;
    private String trainHamFolder;
    
    public static SpamExpertsConfig getInstance() {
        return instance;
    }
    
    /**
     * Initializes a new {@link SpamExpertsConfig}.
     */
    private SpamExpertsConfig() {
        super();
        this.started = new AtomicBoolean();
    }

    /* (non-Javadoc)
     * @see com.openexchange.server.Initialization#start()
     */
    @Override
    public void start() throws OXException {
        if (false == started.compareAndSet(false, true)) {
            LOG.warn("Already started - aborting.");
            return;
        }
        /*
         * register properties
         */
        load(SpamExpertsServiceRegistry.getInstance().getService(ConfigurationService.class, true));
    }

    /* (non-Javadoc)
     * @see com.openexchange.server.Initialization#stop()
     */
    @Override
    public void stop() throws OXException {
        if (false == started.compareAndSet(true, false)) {
            LOG.warn("Not started - aborting.");
            return;
        }
    }

    
    public URLName getImapUrl() {
        return imapUrl;
    }

    
    public void setImapUrl(URLName imapUrl) {
        this.imapUrl = imapUrl;
    }

    
    public String getImapUser() {
        return imapUser;
    }

    
    public void setImapUser(String imapUser) {
        this.imapUser = imapUser;
    }

    
    public String getImappassword() {
        return imappassword;
    }

    
    public void setImappassword(String imappassword) {
        this.imappassword = imappassword;
    }

    
    public String getTrainSpamFolder() {
        return trainSpamFolder;
    }

    
    public void setTrainSpamFolder(String trainSpamFolder) {
        this.trainSpamFolder = trainSpamFolder;
    }

    
    public String getTrainHamFolder() {
        return trainHamFolder;
    }

    
    public void setTrainHamFolder(String trainHamFolder) {
        this.trainHamFolder = trainHamFolder;
    }

    /**
     * Loads all relevant properties from the configuration service.
     *
     * @param configService The configuration service
     * @throws OXException
     */
    private void load(ConfigurationService configService) throws OXException {
        final String iurl = configService.getProperty(CONFIG_SP_IMAPURL);
        if( null == iurl || iurl.length() == 0) {
            throw SpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_IMAPURL);
        }
        try {
            URI tmp = new URI(iurl);
            if( null == tmp.getHost() || tmp.getHost().length() == 0) {
                throw SpamExpertsExceptionCode.INVALID_IMAP_URL.create(iurl);
            }
            imapUrl = new URLName(iurl);
        } catch (URISyntaxException e) {
            LOG.error(e.getMessage(), e);
            throw SpamExpertsExceptionCode.INVALID_IMAP_URL.create(iurl);
        }
        
        imapUser = configService.getProperty(CONFIG_SP_IMAPUSER);
        if( null == imapUser || imapUser.length() == 0) {
            throw SpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_IMAPUSER);
        }
        imappassword = configService.getProperty(CONFIG_SP_IMAPPASSWORD);
        if( null == imappassword ) {
            throw SpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_IMAPPASSWORD);
        }
        trainHamFolder = configService.getProperty(CONFIG_SP_TRAIN_HAM_FOLDER);
        if( null == trainHamFolder || trainHamFolder.length() == 0) {
            throw SpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_TRAIN_HAM_FOLDER);
        }
        trainSpamFolder = configService.getProperty(CONFIG_SP_TRAIN_SPAM_FOLDER);
        if( null == trainSpamFolder || trainSpamFolder.length() == 0) {
            throw SpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_TRAIN_SPAM_FOLDER);
        }
    }
}
