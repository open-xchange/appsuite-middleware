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

package com.openexchange.snippet.mime;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.mail.mime.utils.ImageMatcher;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.session.Session;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultAttachment.InputStreamProvider;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Snippet;

/**
 * {@link MimeSnippetProcessor}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MimeSnippetProcessor {

    private static class InputStreamProviderImpl implements InputStreamProvider {

        private final ManagedFile managedFile;

        /**
         * Initializes a new {@link InputStreamProviderImpl}.
         *
         * @param mf
         */
        protected InputStreamProviderImpl(ManagedFile managedFile) {
            super();
            this.managedFile = managedFile;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return managedFile.getInputStream();
            } catch (OXException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw new IOException(e);
            }
        }
    }

    private final Session session;
    private final Pattern pattern;

    /**
     * Initializes a new {@link MimeSnippetProcessor}.
     *
     * @param session
     * @param ctx
     */
    public MimeSnippetProcessor(Session session) {
        super();
        this.session = session;
        pattern = Pattern.compile("(?i)src=\"[^\"]*\"");
    }

    /**
     * Process the image in the snippet, extract it and convert it to an attachment
     *
     * @param snippet
     * @throws OXException
     */
    public void processImage(final Snippet snippet) throws OXException {
        String content = snippet.getContent();
        if (isEmpty(content)) {
            return;
        }

        final ImageMatcher m = ImageMatcher.matcher(content);
        if (m.find()) {
            final ManagedFileManagement mfm = Services.getService(ManagedFileManagement.class);
            final String imageTag = m.group();
            if (MimeMessageUtility.isValidImageUri(imageTag) && !imageTag.contains("picture?uid")) {
                final String id = m.getManagedFileId();
                final ManagedFile mf = mfm.getByID(id);

                DefaultAttachment att = new DefaultAttachment();
                att.setContentDisposition(mf.getContentDisposition());
                att.setContentType(mf.getContentType());
                att.setId(mf.getID());
                att.setSize(mf.getSize());
                att.setStreamProvider(new InputStreamProviderImpl(mf));

                DefaultSnippet ds = (DefaultSnippet) snippet;
                ds.addAttachment(att);

                //final String url = SnippetImageDataSource.getInstance().generateUrl(new ImageLocation.Builder(id).id(snippet.getId()).build(), session);
                final String url = mf.constructURL(session);
                content = pattern.matcher(content).replaceAll(Matcher.quoteReplacement("src=\"" + url + "\""));
                ds.setContent(content);
            }
        }
    }

}
