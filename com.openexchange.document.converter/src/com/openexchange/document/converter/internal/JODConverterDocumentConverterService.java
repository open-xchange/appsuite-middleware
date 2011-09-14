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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.document.converter.internal;

import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeConnectionProtocol;
import org.artofsolving.jodconverter.office.OfficeManager;
import com.openexchange.document.converter.DocumentContent;
import com.openexchange.document.converter.DocumentConverterService;
import com.openexchange.exception.OXException;

/**
 * {@link JODConverterDocumentConverterService} - The {@link DocumentConverterService} implementation based on <a
 * href="http://code.google.com/p/jodconverter/">JODConverter</a>.
 * <p>
 * <a href="http://shervinasgari.blogspot.com/2010/08/migrating-from-jodconverter-2-to.html">Example 1</a>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JODConverterDocumentConverterService implements DocumentConverterService {

    private final OfficeManager officeManager;

    /**
     * Initializes a new {@link JODConverterDocumentConverterService}.
     */
    public JODConverterDocumentConverterService() {
        super();
        /*
         * Start-up JODConverter
         */
        final DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
        configuration.setOfficeHome("/usr/lib/openoffice");
        configuration.setConnectionProtocol(OfficeConnectionProtocol.PIPE);
        configuration.setPipeNames("office1", "office2");
        configuration.setTaskExecutionTimeout(240000L); // 4 minutes
        configuration.setTaskQueueTimeout(60000L); // 1 minute
        officeManager = configuration.buildOfficeManager();
    }

    /**
     * Starts-up this {@link JODConverterDocumentConverterService} instance.
     * 
     * @return This <i>started</i> {@link JODConverterDocumentConverterService}
     */
    public JODConverterDocumentConverterService startUp() {
        officeManager.start();
        return this;
    }

    /**
     * Shuts down this {@link JODConverterDocumentConverterService} instance.
     */
    public void shutDown() {
        officeManager.stop();
    }

    @Override
    public DocumentContent convert(final DocumentContent inputContent, final String extension) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
