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

package com.openexchange.ajax.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FileHolder} - The basic {@link IFileHolder} implementation.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added some JavaDoc comments
 */
public class FileHolder implements IFileHolder {

    private InputStreamClosure isClosure;
    private InputStream is;
    private long length;
    private String contentType;
    private String name;
    private String disposition;
    private String delivery;

    /**
     * Initializes a new {@link FileHolder}.
     * 
     * @param is The input stream
     * @param length The stream length
     * @param contentType The stream's MIME type
     * @param name The stream's resource name
     */
    public FileHolder(final InputStream is, final long length, final String contentType, final String name) {
        super();
        this.is = is;
        this.length = length;
        this.contentType = contentType;
        this.name = name;
    }

    /**
     * Initializes a new {@link FileHolder}.
     * 
     * @param isClosure The input stream closure
     * @param length The stream length
     * @param contentType The stream's MIME type
     * @param name The stream's resource name
     */
    public FileHolder(final InputStreamClosure isClosure, final long length, final String contentType, final String name) {
        super();
        this.isClosure = isClosure;
        this.length = length;
        this.contentType = contentType;
        this.name = name;
    }
    
    public FileHolder(final File file, String contentType) {
        this.length = file.length();
        
        if (contentType == null){
            contentType = javax.activation.MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
        }
        
        this.contentType = contentType;
        
        this.name = file.getName();
        
        this.isClosure = new InputStreamClosure() {
            
            @Override
            public InputStream newStream() throws OXException, IOException {
                return new FileInputStream(file);
            }
        };
        
    }
    
    public FileHolder(final File file) {
        this(file, null);
    }

    @Override
    public boolean repetitive() {
        return null != isClosure;
    }

    @Override
    public void close() {
        // Nope
    }

    @Override
    public InputStream getStream() throws OXException {
        final InputStreamClosure isClosure = this.isClosure;
        if (null != isClosure) {
            try {
                return isClosure.newStream();
            } catch (IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
        // Return stream directly
        return is;
    }

    /**
     * Sets the input stream
     * 
     * @param is The input stream
     */
    public void setStream(final InputStream is) {
        Streams.close(this.is);
        this.is = is;
        this.isClosure = null;
    }

    @Override
    public long getLength() {
        return length;
    }

    /**
     * Sets the stream length
     * 
     * @param length The length
     */
    public void setLength(final long length) {
        this.length = length;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets stream's MIME type.
     * 
     * @param contentType The MIME type
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets stream's resource name.
     * 
     * @param name The resource name
     */
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDisposition() {
        return disposition;
    }

    /**
     * Sets the disposition.
     * 
     * @param disposition The disposition
     */
    public void setDisposition(final String disposition) {
        this.disposition = disposition;
    }

    /**
     * Sets the delivery
     * 
     * @param delivery The delivery to set
     */
    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    @Override
    public String getDelivery() {
        return delivery;
    }

}
