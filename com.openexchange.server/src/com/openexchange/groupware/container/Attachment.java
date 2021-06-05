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
