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

package com.openexchange.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link SimpleMessagingMessage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimpleMessagingMessage implements MessagingMessage, MessagingBodyPart {

    private static final long serialVersionUID = -5015210482990734316L;

    private int colorLabel;

    private int flags;

    private String folder;

    private long receivedDate;

    private Collection<String> userFlags;

    private String disposition;

    private String fileName;

    private Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();

    private String sectionId;

    private transient MessagingContent content;

    private long size;

    private int threadLevel;

    private transient MultipartContent parent;

    private ContentType contentType;

    private String id;

    private String picture;

    private String url;

    @Override
    public int getColorLabel() {
        return colorLabel;
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public String getFolder() {
        return folder;
    }

    @Override
    public long getReceivedDate() {
        return receivedDate;
    }

    @Override
    public Collection<String> getUserFlags() {
        return userFlags;
    }

    @Override
    public MessagingContent getContent() throws OXException {
        return content;
    }

    @Override
    public String getDisposition() throws OXException {
        return disposition;
    }

    @Override
    public String getFileName() throws OXException {
        return fileName;
    }

    @Override
    public MessagingHeader getFirstHeader(final String name) throws OXException {
        final Collection<MessagingHeader> collection = headers.get(name);
        return null == collection ? null : (collection.isEmpty() ? null : collection.iterator().next());
    }

    @Override
    public Collection<MessagingHeader> getHeader(final String name) {
        return headers.get(name);
    }

    @Override
    public Map<String, Collection<MessagingHeader>> getHeaders() {
        return headers;
    }

    @Override
    public String getSectionId() {
        return sectionId;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int getThreadLevel() {
        return threadLevel;
    }

    @Override
    public void writeTo(final OutputStream os) throws IOException, OXException {
        throw new UnsupportedOperationException();
    }

    public void setColorLabel(final int colorLabel) {
        this.colorLabel = colorLabel;
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

    public void setFolder(final String folder) {
        this.folder = folder;
    }

    public void setReceivedDate(final long receivedDate) {
        this.receivedDate = receivedDate;
    }

    public void setUserFlags(final Collection<String> userFlags) {
        this.userFlags = userFlags;
    }

    public void setDisposition(final String disposition) {
        this.disposition = disposition;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setHeaders(final Map<String, Collection<MessagingHeader>> headers) {
        this.headers = headers;
    }

    public void putHeader(final MessagingHeader header) {
        if (headers.containsKey(header.getName())) {
            headers.get(header.getName()).add(header);
        } else {
            headers.put(header.getName(), new ArrayList<MessagingHeader>(Arrays.asList(header)));
        }

    }

    public void setSectionId(final String sectionId) {
        this.sectionId = sectionId;
    }

    public void setContent(final String content) {
        this.content = new StringContent(content);
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public void setThreadLevel(final int threadLevel) {
        this.threadLevel = threadLevel;
    }

    public void setContent(final byte[] bytes) {
        content = new ByteArrayContent(bytes);
    }

    public void setContent(final MessagingBodyPart... parts) {
        content = new MessagingPartArrayContent(parts);
    }

    @Override
    public MultipartContent getParent() throws OXException {
        return parent;
    }

    public void setParent(final MultipartContent parent) {
        this.parent = parent;
    }

    @Override
    public ContentType getContentType() throws OXException {
        return contentType;
    }

    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getPicture() {
        return picture;
    }

    public void setPicture(final String picture) {
        this.picture = picture;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public String getUrl() throws OXException {
      return url;
    }

    public void setContentReference(final String string) {
      content =  new ReferenceContent(string);
    }


}
