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

package com.openexchange.mail.mime.converters;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.CommandInfo;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;

/**
 * {@link DataHandlerWrapper} - A simple wrapper for a {@link DataHandler data handler} to apply a custom content type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DataHandlerWrapper extends DataHandler {

    private final DataHandler delegatee;

    private final String contentType;

    /**
     * Initializes a new {@link DataHandlerWrapper}. The content type argument may be <code>null</code> to return delagatee's content type
     * on {@link #getContentType()}.
     *
     * @param dataHandler The delegatee data handler
     * @param contentType The (optional) content type
     */
    public DataHandlerWrapper(final DataHandler dataHandler, final String contentType) {
        super(dataHandler.getDataSource());
        delegatee = dataHandler;
        this.contentType = contentType;
    }

    @Override
    public boolean equals(final Object obj) {
        return delegatee.equals(obj);
    }

    @Override
    public CommandInfo[] getAllCommands() {
        return delegatee.getAllCommands();
    }

    @Override
    public Object getBean(final CommandInfo cmdinfo) {
        return delegatee.getBean(cmdinfo);
    }

    @Override
    public CommandInfo getCommand(final String cmdName) {
        return delegatee.getCommand(cmdName);
    }

    @Override
    public Object getContent() throws IOException {
        return delegatee.getContent();
    }

    @Override
    public String getContentType() {
        return contentType == null ? delegatee.getContentType() : contentType;
    }

    @Override
    public DataSource getDataSource() {
        return delegatee.getDataSource();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegatee.getInputStream();
    }

    @Override
    public String getName() {
        return delegatee.getName();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return delegatee.getOutputStream();
    }

    @Override
    public CommandInfo[] getPreferredCommands() {
        return delegatee.getPreferredCommands();
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return delegatee.getTransferData(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return delegatee.getTransferDataFlavors();
    }

    @Override
    public int hashCode() {
        return delegatee.hashCode();
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return delegatee.isDataFlavorSupported(flavor);
    }

    @Override
    public void setCommandMap(final CommandMap commandMap) {
        delegatee.setCommandMap(commandMap);
    }

    @Override
    public String toString() {
        return delegatee.toString();
    }

    @Override
    public void writeTo(final OutputStream os) throws IOException {
        delegatee.writeTo(os);
    }

}
