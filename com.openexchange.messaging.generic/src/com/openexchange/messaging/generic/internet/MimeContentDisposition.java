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

package com.openexchange.messaging.generic.internet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.ContentDisposition;
import com.openexchange.messaging.generic.internal.ParameterizedHeader;

/**
 * {@link MimeContentDisposition} - The MIME content disposition.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class MimeContentDisposition extends ParameterizedHeader implements ContentDisposition {

    private static final long serialVersionUID = -1040187672540142351L;

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * Gets the <i>Content-Disposition</i> name.
     *
     * @return The <i>Content-Disposition</i> name
     */
    public static String getContentDispositionName() {
        return CONTENT_DISPOSITION;
    }

    private final com.openexchange.mail.mime.ContentDisposition cdo;

    /**
     * Initializes a new {@link MimeContentDisposition}
     */
    public MimeContentDisposition() {
        super(new com.openexchange.mail.mime.ContentDisposition());
        cdo = (com.openexchange.mail.mime.ContentDisposition) delegate;
    }

    /**
     * Initializes a new {@link MimeContentDisposition}
     *
     * @param contentDisp The content disposition
     * @throws OXException If content disposition cannot be parsed
     */
    public MimeContentDisposition(final String contentDisp) throws OXException {
        super(toContentDisposition(contentDisp));
        cdo = (com.openexchange.mail.mime.ContentDisposition) delegate;
    }

    private static com.openexchange.mail.mime.ContentDisposition toContentDisposition(final String contentDisposition) throws OXException {
        return new com.openexchange.mail.mime.ContentDisposition(contentDisposition);
    }

    @Override
    public int compareTo(final ParameterizedHeader other) {
        if (this == other) {
            return 0;
        }
        if (MimeContentDisposition.class.isInstance(other)) {
            final int dispComp = getDisposition().compareToIgnoreCase(((MimeContentDisposition) other).getDisposition());
            if (dispComp != 0) {
                return dispComp;
            }
        }
        return super.compareTo(other);
    }

    @Override
    public int hashCode() {
        return cdo.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MimeContentDisposition other = (MimeContentDisposition) obj;
        if (cdo == null) {
            if (other.cdo != null) {
                return false;
            }
        } else if (!cdo.equals(other.cdo)) {
            return false;
        }
        return true;
    }

    /**
     * Applies given content disposition to this content disposition
     *
     * @param contentDisp The content disposition to apply
     */
    public void setContentDisposition(final MimeContentDisposition contentDisp) {
        if (contentDisp == this) {
            return;
        }
        cdo.setContentDisposition(contentDisp.cdo);
    }

    @Override
    public HeaderType getHeaderType() {
        return HeaderType.PARAMETERIZED;
    }

    /**
     * @return disposition
     */
    @Override
    public String getDisposition() {
        return cdo.getDisposition();
    }

    /**
     * Sets disposition
     */
    @Override
    public void setDisposition(final String disposition) {
        cdo.setDisposition(disposition);
    }

    /**
     * Sets filename parameter
     */
    @Override
    public void setFilenameParameter(final String filename) {
        cdo.setFilenameParameter(filename);
    }

    /**
     * @return the filename value or <code>null</code> if not present
     */
    @Override
    public String getFilenameParameter() {
        return cdo.getFilenameParameter();
    }

    /**
     * @return <code>true</code> if filename parameter is present, <code>false</code> otherwise
     */
    @Override
    public boolean containsFilenameParameter() {
        return cdo.containsFilenameParameter();
    }

    /**
     * Sets Content-Disposition
     */
    @Override
    public void setContentDisposition(final String contentDisp) throws OXException {
        cdo.setContentDisposition(contentDisp);
    }

    /**
     * Checks if disposition is inline
     *
     * @return <code>true</code> if disposition is inline; otherwise <code>false</code>
     */
    @Override
    public boolean isInline() {
        return cdo.isInline();
    }

    /**
     * Checks if disposition is attachment
     *
     * @return <code>true</code> if disposition is attachment; otherwise <code>false</code>
     */
    @Override
    public boolean isAttachment() {
        return cdo.isAttachment();
    }

    @Override
    public String toString() {
        return cdo.toString();
    }

    /**
     * Returns a RFC2045 style (ASCII-only) string representation of this content disposition.
     *
     * @param skipEmptyParams <code>true</code> to skip empty parameters; otherwise <code>false</code>
     * @return A RFC2045 style (ASCII-only) string representation of this content disposition
     */
    public String toString(final boolean skipEmptyParams) {
        return cdo.toString(skipEmptyParams);
    }

    @Override
    public void setContentDispositio(final ContentDisposition contentDisp) {
        if (contentDisp == this) {
            return;
        }
        if (contentDisp instanceof MimeContentDisposition) {
            cdo.setContentDisposition(((MimeContentDisposition) contentDisp).cdo);
        } else {
            cdo.setDisposition(contentDisp.getDisposition());
            {
                final List<String> tmp = new ArrayList<String>(4);
                for (final Iterator<String> it = cdo.getParameterNames(); it.hasNext();) {
                    tmp.add(it.next());
                }
                for (final String name : tmp) {
                    cdo.removeParameter(name);
                }
            }
            for (final Iterator<String> it = contentDisp.getParameterNames(); it.hasNext();) {
                final String name = it.next();
                cdo.addParameter(name, contentDisp.getParameter(name));
            }
        }
    }

    @Override
    public String getName() {
        return CONTENT_DISPOSITION;
    }

    @Override
    public String getValue() {
        return toString();
    }

}
