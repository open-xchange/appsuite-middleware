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

package com.openexchange.mail.mime;

import java.util.Locale;
import javax.mail.Part;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;

/**
 * {@link ContentDisposition}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContentDisposition extends ParameterizedHeader {

    private static final long serialVersionUID = 310827213193290169L;

    private static final String INLINE = Part.INLINE;

    private static final String ATTACHMENT = Part.ATTACHMENT;

    private static final String PARAM_FILENAME = "filename";

    private String disposition;

    /**
     * Initializes a new {@link ContentDisposition}
     */
    public ContentDisposition() {
        super();
        disposition = INLINE;
        parameterList = new ParameterList();
    }

    /**
     * Initializes a new {@link ContentDisposition}
     *
     * @param contentDisposition The content disposition
     * @throws OXException If content disposition cannot be parsed
     */
    public ContentDisposition(String contentDisposition) throws OXException {
        super();
        parseContentDisp(contentDisposition);
    }

    @Override
    public int compareTo(ParameterizedHeader other) {
        if (this == other) {
            return 0;
        }
        if (ContentDisposition.class.isInstance(other)) {
            final int dispComp = getDisposition().compareToIgnoreCase(((ContentDisposition) other).getDisposition());
            if (dispComp != 0) {
                return dispComp;
            }
        }
        return super.compareTo(other);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((disposition == null) ? 0 : disposition.toLowerCase(Locale.US).hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContentDisposition other = (ContentDisposition) obj;
        if (disposition == null) {
            if (other.disposition != null) {
                return false;
            }
        } else if (!disposition.equalsIgnoreCase(other.disposition)) {
            return false;
        }
        return true;
    }

    private void parseContentDisp(String contentDisposition) throws OXException {
        parseContentDisp(contentDisposition, true);
    }

    private void parseContentDisp(String contentDisposition, boolean paramList) throws OXException {
        if ((null == contentDisposition) || (contentDisposition.length() == 0)) {
            /*
             * Nothing to parse
             */
            disposition = INLINE;
            parameterList = new ParameterList();
            return;
        }
        try {
            final String contentDisp = prepareParameterizedHeader(contentDisposition);
            int semicolonPos = contentDisp.indexOf(';');
            String disp = (semicolonPos < 0 ? contentDisp : contentDisp.substring(0, semicolonPos)).trim();
            if (disp.indexOf('%') >= 0) {
                // Possibly encoded
                disp = decodeUrl(disp);
            }
            disposition = Strings.toLowerCase(disp);
            if (paramList) {
                if (semicolonPos >= 0) {
                    parameterList = semicolonPos < contentDisp.length() ? new ParameterList(contentDisp.substring(semicolonPos + 1)) : new ParameterList();
                } else {
                    // Assume no parameters
                    parameterList = new ParameterList();
                }
            }
        } catch (RuntimeException e) {
            throw MailExceptionCode.INVALID_CONTENT_DISPOSITION.create(e, contentDisposition);
        }
    }

    /**
     * Applies given content disposition to this content disposition
     *
     * @param contentDisp The content disposition to apply
     */
    public void setContentDisposition(ContentDisposition contentDisp) {
        if (contentDisp == this) {
            return;
        }
        disposition = contentDisp.disposition;
        parameterList = (ParameterList) contentDisp.parameterList.clone();
    }

    /**
     * @return disposition
     */
    public String getDisposition() {
        return disposition;
    }

    /**
     * Sets disposition
     */
    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    /**
     * Sets the disposition to <code>"inline"</code>.
     */
    public void setInline() {
        disposition = INLINE;
    }

    /**
     * Sets the disposition to <code>"attachment"</code>.
     */
    public void setAttachment() {
        disposition = ATTACHMENT;
    }

    /**
     * Sets filename parameter
     */
    public void setFilenameParameter(String filename) {
        setParameter(PARAM_FILENAME, filename);
    }

    /**
     * @return the filename value or <code>null</code> if not present
     */
    public String getFilenameParameter() {
        return getParameter(PARAM_FILENAME);
    }

    /**
     * @return <code>true</code> if filename parameter is present, <code>false</code> otherwise
     */
    public boolean containsFilenameParameter() {
        return containsParameter(PARAM_FILENAME);
    }

    /**
     * Sets Content-Disposition
     */
    public void setContentDisposition(String contentDisp) throws OXException {
        parseContentDisp(contentDisp);
    }

    /**
     * Checks if disposition is inline
     *
     * @return <code>true</code> if disposition is inline; otherwise <code>false</code>
     */
    public boolean isInline() {
        return Part.INLINE.equalsIgnoreCase(disposition);
    }

    /**
     * Checks if disposition is attachment
     *
     * @return <code>true</code> if disposition is attachment; otherwise <code>false</code>
     */
    public boolean isAttachment() {
        return Part.ATTACHMENT.equalsIgnoreCase(disposition);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Returns a RFC2045 style (ASCII-only) string representation of this content disposition.
     *
     * @param skipEmptyParams <code>true</code> to skip empty parameters; otherwise <code>false</code>
     * @return A RFC2045 style (ASCII-only) string representation of this content disposition
     */
    public String toString(boolean skipEmptyParams) {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(disposition);
        if (null != parameterList) {
            parameterList.appendRFC2045String(sb, skipEmptyParams);
        }
        return sb.toString();
    }

}
