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

package com.openexchange.ajax.container;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link ModifyableFileHolder} - A delegating file holder providing possibility to set MIME type, name, disposition and delivery
 * information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ModifyableFileHolder implements IFileHolder {

    private final IFileHolder fileHolder;
    private String name;
    private String disposition;
    private String contentType;
    private String delivery;

    /**
     * Initializes a new {@link ModifyableFileHolder}.
     *
     * @param fileHolder The delegate file holder
     */
    public ModifyableFileHolder(final IFileHolder fileHolder) {
        super();
        this.fileHolder = fileHolder;
    }

    @Override
    public List<Runnable> getPostProcessingTasks() {
        return fileHolder.getPostProcessingTasks();
    }

    @Override
    public void addPostProcessingTask(Runnable task) {
        fileHolder.addPostProcessingTask(task);
    }

    @Override
    public boolean repetitive() {
        return fileHolder.repetitive();
    }

    @Override
    public void close() throws IOException {
        fileHolder.close();
    }

    @Override
    public InputStream getStream() throws OXException {
        return fileHolder.getStream();
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        return fileHolder.getRandomAccess();
    }

    @Override
    public long getLength() {
        return fileHolder.getLength();
    }

    @Override
    public String getContentType() {
        return null == contentType ? fileHolder.getContentType() : contentType;
    }

    @Override
    public String getName() {
        return null == name ? fileHolder.getName() : name;
    }

    @Override
    public String getDisposition() {
        return null == disposition ? fileHolder.getDisposition() : disposition;
    }

    @Override
    public String getDelivery() {
        return delivery == null ? fileHolder.getDelivery() : delivery;
    }

    /**
     * Sets the MIME type
     *
     * @param contentType The MIME type to set
     * @return This file holder with new argument applied
     */
    public ModifyableFileHolder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Sets the delivery
     *
     * @param delivery The delivery to set
     * @return This file holder with new argument applied
     */
    public ModifyableFileHolder setDelivery(String delivery) {
        this.delivery = delivery;
        return this;
    }

    /**
     * Sets the disposition
     *
     * @param disposition The disposition to set
     * @return This file holder with new argument applied
     */
    public ModifyableFileHolder setDisposition(String disposition) {
        this.disposition = disposition;
        return this;
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     * @return This file holder with new argument applied
     */
    public ModifyableFileHolder setName(String name) {
        this.name = name;
        return this;
    }

}
