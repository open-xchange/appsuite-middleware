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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.filestore.sproxyd;

import java.net.URI;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageFactoryCandidate;

/**
 * {@link S3FileStorageFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydFileStorageFactory implements FileStorageFactoryCandidate {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydFileStorageFactory.class);

    /**
     * The URI scheme identifying sproxyd file storages.
     */
    private static final String SPROXYD_SCHEME = "sproxyd";

    /**
     * The expected pattern for file store names - hard-coded at
     * com.openexchange.admin.storage.mysqlStorage.OXContextMySQLStorage.create(Context, User, UserModuleAccess) ,
     * so expect nothing else.
     */
    private static final Pattern CTX_STORE_PATTERN = Pattern.compile("(\\d+)_ctx_store");

    /**
     * The file storage's ranking compared to other sharing the same URL scheme.
     */
    private static final int RANKING = 547;

    private final ConfigurationService configService;

    /**
     * Initializes a new {@link SproxydFileStorageFactory}.
     *
     * @param configService The configuration service to use
     */
    public SproxydFileStorageFactory(ConfigurationService configService) {
        super();
        this.configService = configService;
    }

    @Override
    public SproxydFileStorage getFileStorage(URI uri) throws OXException {
        return null;
    }

    @Override
    public FileStorage getInternalFileStorage(URI uri) throws OXException {
        return getFileStorage(uri);
    }

    @Override
    public boolean supports(URI uri) {
         return null != uri && SPROXYD_SCHEME.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    public int getRanking() {
        return RANKING;
    }

}
