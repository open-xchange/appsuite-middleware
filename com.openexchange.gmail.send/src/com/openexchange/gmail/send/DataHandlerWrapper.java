/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.gmail.send;

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
