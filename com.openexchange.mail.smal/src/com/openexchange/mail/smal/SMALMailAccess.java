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

package com.openexchange.mail.smal;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.session.Session;

/**
 * {@link SMALMailAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMALMailAccess extends MailAccess<SMALFolderStorage, SMALMessageStorage> {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(SMALMailAccess.class));

    private static final long serialVersionUID = 3887048765113161340L;

    private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> realMailAccess;

    /**
     * Initializes a new {@link SMALMailAccess}.
     * 
     * @param session The session
     * @param accountId The account identifier
     * @throws OXException If initialization fails
     */
    public SMALMailAccess(final Session session, final int accountId) throws OXException {
        super(session, accountId);
        this.realMailAccess = SMALMailProviderRegistry.getMailProviderBySession(session, accountId).createNewMailAccess(session, accountId);
    }

    @Override
    protected void connectInternal() throws OXException {
        realMailAccess.delegateConnectInternal();
    }

    @Override
    protected MailConfig createNewMailConfig() {
        
        
        
        final MailConfig realMailConfig = realMailAccess.delegateCreateNewMailConfig();
        
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#createNewMailProperties()
     */
    @Override
    protected IMailProperties createNewMailProperties() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#checkMailServerPort()
     */
    @Override
    protected boolean checkMailServerPort() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#releaseResources()
     */
    @Override
    protected void releaseResources() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#closeInternal()
     */
    @Override
    protected void closeInternal() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#getFolderStorage()
     */
    @Override
    public SMALFolderStorage getFolderStorage() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#getMessageStorage()
     */
    @Override
    public SMALMessageStorage getMessageStorage() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#getLogicTools()
     */
    @Override
    public MailLogicTools getLogicTools() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#isConnected()
     */
    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#isConnectedUnsafe()
     */
    @Override
    public boolean isConnectedUnsafe() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#startup()
     */
    @Override
    protected void startup() throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.api.MailAccess#shutdown()
     */
    @Override
    protected void shutdown() throws OXException {
        // TODO Auto-generated method stub

    }

}
