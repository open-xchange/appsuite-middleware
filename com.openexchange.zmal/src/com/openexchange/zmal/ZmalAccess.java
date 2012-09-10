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

package com.openexchange.zmal;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.zmal.config.MailAccountZmalProperties;
import com.openexchange.zmal.config.ZmalConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;

/**
 * {@link ZmalAccess} - Establishes an Zimbra mail access and provides access to storages.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalAccess extends MailAccess<ZmalFolderStorage, ZmalMessageStorage> {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -7518596764376433468L;

    /**
     * The logger instance for {@link ZmalAccess} class.
     */
    private static final transient org.apache.commons.logging.Log LOG = com.openexchange.log.Log.loggerFor(ZmalAccess.class);

    /*-
     * Member section
     */

    /**
     * The folder storage.
     */
    private transient ZmalFolderStorage folderStorage;

    /**
     * The message storage.
     */
    private transient ZmalMessageStorage messageStorage;

    /**
     * The mail logic tools.
     */
    private transient MailLogicTools logicTools;

    /**
     * The Zimbra mail configuration.
     */
    private volatile ZmalConfig zmalConfig;

    private ZmalSoapPerformer performer;

    private boolean connected;

    private boolean useJson;

    /**
     * Initializes a new {@link ZmalAccess IMAP access} for default IMAP account.
     * 
     * @param session The session providing needed user data
     */
    protected ZmalAccess(final Session session) {
        super(session);
        setMailProperties((Properties) System.getProperties().clone());
    }

    /**
     * Initializes a new {@link ZmalAccess IMAP access}.
     * 
     * @param session The session providing needed user data
     * @param accountId The account ID
     */
    protected ZmalAccess(final Session session, final int accountId) {
        super(session, accountId);
        setMailProperties((Properties) System.getProperties().clone());
    }

    /**
     * Sets whether to use JSON format.
     * 
     * @param useJson <code>true</code> to use JSON format; otherwise <code>false</code>
     */
    public void setUseJson(final boolean useJson) {
        this.useJson = useJson;
    }

    private void reset() {
        super.resetFields();
        connected = false;
        useJson = false;
        folderStorage = null;
        messageStorage = null;
        logicTools = null;
        performer = null;
    }

    @Override
    public void releaseResources() {
        // Release resources
    }

    

    @Override
    protected void closeInternal() {
        try {
            if (folderStorage != null) {
                try {
                    folderStorage.releaseResources();
                } catch (final OXException e) {
                    LOG.error("Error while closing IMAP folder storage,", e);
                }
            }
            if (null != messageStorage) {
                try {
                    messageStorage.releaseResources();
                } catch (final OXException e) {
                    LOG.error("Error while closing IMAP message storage.", e);
                }
            }
        } finally {
            reset();
        }
    }

    @Override
    protected MailConfig createNewMailConfig() {
        return new ZmalConfig(accountId);
    }

    @Override
    public MailConfig getMailConfig() throws OXException {
        ZmalConfig tmp = zmalConfig;
        if (null == tmp) {
            synchronized (this) {
                tmp = zmalConfig;
                if (null == tmp) {
                    zmalConfig = tmp = (ZmalConfig) super.getMailConfig();
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the Zimbra mail configuration.
     * 
     * @return The Zimbra mail configuration
     */
    public ZmalConfig getZmalConfig() {
        final ZmalConfig tmp = zmalConfig;
        if (null == tmp) {
            try {
                return (ZmalConfig) getMailConfig();
            } catch (final OXException e) {
                // Cannot occur
                return null;
            }
        }
        return tmp;
    }

    @Override
    protected void connectInternal() throws OXException {
        if (connected) {
            return;
        }
        /*-
         * $ zmsoap -m user1 -p test123 -u http://localhost:7070/service/soap --type account GetInfoRequest | head

            <GetInfoResponse xmlns="urn:zimbraAccount">
              <version>unknown unknown unknown unknown</version>
              <id>decf3d72-623c-44d1-be34-23df4d285fb1</id>
              <name>user1@server.zimbra.com</name>
              <crumb>dace2c2c21df2009dc657b8f9e94b1cc</crumb>
              <lifetime>172799977</lifetime>
              <rest>http://server.zimbra.com:7070/home/user1</rest>
              <used>10775433</used>
              <prevSession>1211496468000</prevSession>
         * 
         */
        try {
            final ZmalConfig config = getZmalConfig();
            checkFieldsBeforeConnect(config);
            final ZmalSoapPerformer performer = new ZmalSoapPerformer(config).setUseJson(useJson);
            performer.setContextId(session.getContextId()).setUserId(session.getUserId());
            performer.setSelect("GetInfoResponse");
            final ZmalSoapResponse soapResponse = performer.perform(ZmalType.ACCOUNT, "GetInfoRequest");
            
            
            String resultString = soapResponse.getResultString();
            final List<Element> results = soapResponse.getResults();
            if (resultString == null && results != null) {
                StringBuilder buf = new StringBuilder();
                boolean first = true;
                for (Element e : results) {
                    if (first) {
                        first = false; 
                    } else {
                        buf.append('\n');
                    }
                    buf.append(e.prettyPrint());
                }
                resultString = buf.toString();
            }
            if (resultString == null) {
                resultString = "";
            }
            config.initializeCapabilities();
            System.out.println(resultString);
            
            
            this.performer = performer;
            connected = true;
        } catch (ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (IOException e) {
            throw ZmalException.create(ZmalException.Code.IO_ERROR, e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public ZmalFolderStorage getFolderStorage() throws OXException {
        // connected = ((imapStore != null) && imapStore.isConnected());
        if (!connected) {
            throw ZmalException.create(ZmalException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        if (null == folderStorage) {
            folderStorage = new ZmalFolderStorage(performer.getAuthToken(), performer, this, session);
        }
        return folderStorage;
    }

    @Override
    public ZmalMessageStorage getMessageStorage() throws OXException {
        // connected = ((imapStore != null) && imapStore.isConnected());
        if (!connected) {
            throw ZmalException.create(ZmalException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        if (null == messageStorage) {
            messageStorage = new ZmalMessageStorage(performer.getAuthToken(), performer, this, session);
        }
        return messageStorage;
    }

    @Override
    public MailLogicTools getLogicTools() throws OXException {
        // connected = ((imapStore != null) && imapStore.isConnected());
        if (!connected) {
            throw ZmalException.create(ZmalException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        if (null == logicTools) {
            logicTools = new MailLogicTools(session, accountId);
        }
        return logicTools;
    }

    @Override
    public boolean isConnected() {
        /*-
         *
        if (!connected) {
            return false;
        }
        return (connected = ((imapStore != null) && imapStore.isConnected()));
         */
        return connected;
    }

    @Override
    public boolean isConnectedUnsafe() {
        return connected;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    protected void startup() throws OXException {
        // TODO:
    }

    @Override
    protected void shutdown() throws OXException {
        // TODO:
    }

    @Override
    protected boolean checkMailServerPort() {
        return true;
    }

    @Override
    protected IMailProperties createNewMailProperties() throws OXException {
        final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
        return new MailAccountZmalProperties(storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()));
    }

    @Override
    public String toString() {
        if (null != performer) {
            return performer.toString();
        }
        return "[not connected]";
    }

}
