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

package com.openexchange.groupware.container;

import java.io.InputStream;

/**
 * AttachmenObject
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class Attachment extends DataObject {

    private static final long serialVersionUID = 8381024871307327468L;

    protected String filename = null;

    protected String mimetype = null;

    protected int target_id = 0;

    protected int module = 0;

    protected InputStream is = null;

    protected boolean b_filename = false;

    protected boolean b_mimetype = false;

    protected boolean b_target_id = false;

    protected boolean b_module = false;

    protected boolean b_is = false;

    public Attachment() {

    }

    // GET METHODS
    public String getFilename() {
        return filename;
    }

    public String getMimeType() {
        return mimetype;
    }

    public int getTargetID() {
        return target_id;
    }

    public int getModule() {
        return module;
    }

    public InputStream getInputStream() {
        return is;
    }

    // SET METHODS
    public void setFilename(final String filename) {
        this.filename = filename;
        b_filename = true;
    }

    public void setMimeType(final String mimetype) {
        this.mimetype = mimetype;
        b_mimetype = true;
    }

    public void setTargetID(final int target_id) {
        this.target_id = target_id;
        b_target_id = true;
    }

    public void setModule(final int module) {
        this.module = module;
        b_module = true;
    }

    public void setInputStream(final InputStream is) {
        this.is = is;
        b_is = true;
    }

    // REMOVE METHODS
    public void removeFilename() {
        filename = null;
        b_filename = false;
    }

    public void removeMimeType() {
        mimetype = null;
        b_mimetype = false;
    }

    public void removeTargetID() {
        target_id = 0;
        b_target_id = false;
    }

    public void removeModule() {
        module = 0;
        b_module = false;
    }

    public void removeInputStream() {
        is = null;
        b_is = false;
    }

    // CONTAINS METHODS
    public boolean containsFilename() {
        return b_filename;
    }

    public boolean containsMimeType() {
        return b_mimetype;
    }

    public boolean containsTargetID() {
        return b_target_id;
    }

    public boolean containsModule() {
        return b_module;
    }

    public boolean containsInputStream() {
        return b_is;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + filename.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Attachment)) {
            return false;
        }
        final Attachment attachmentobject = (Attachment) o;

        return filename.equals(attachmentobject.getFilename());
    }

    @Override
    public void reset() {
        super.reset();

        filename = null;
        mimetype = null;
        is = null;

        b_filename = false;
        b_mimetype = false;
        b_is = false;
    }
}
