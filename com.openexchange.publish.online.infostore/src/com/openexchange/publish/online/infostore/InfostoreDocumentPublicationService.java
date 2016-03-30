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

package com.openexchange.publish.online.infostore;

import static com.openexchange.publish.online.infostore.FormStrings.FORM_LABEL_URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.helpers.AbstractPublicationService;
import com.openexchange.publish.helpers.SecurityStrategy;
import com.openexchange.publish.online.infostore.util.InfostorePublicationUtils;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link InfostoreDocumentPublicationService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InfostoreDocumentPublicationService extends AbstractPublicationService {

    private static final String SECRET = "secret";

    public static final String PREFIX = "/publications/documents";

    private static final String URL = "url";

    private final Random random = new Random();

    private volatile IDBasedFileAccessFactory fileAccessFactory;

    public InfostoreDocumentPublicationService(IDBasedFileAccessFactory fileAccessFactory) {
        this.fileAccessFactory = fileAccessFactory;
    }

    private PublicationTarget buildTarget() {
        PublicationTarget target = new PublicationTarget();
        target.setDisplayName(FormStrings.TARGET_NAME_INFOSTORE_DOCUMENT);
        target.setId("com.openexchange.publish.online.infostore.document");
        target.setModule("infostore/object");
        target.setPublicationService(this);

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.link("url", FORM_LABEL_URL, false, null));

        target.setFormDescription(form);

        return target;
    }

    private PublicationTarget target;

    @Override
    public PublicationTarget getTarget() throws OXException {
        if(target == null) {
            return target = buildTarget();
        }
        return target;
    }

    @Override
    public void beforeCreate(Publication publication) throws OXException {
        super.beforeCreate(publication);
        publication.getConfiguration().remove(URL);
        String secret = null;
        Context ctx = publication.getContext();
        while(true) {
            secret = generateSecret();
            if(getPublication(ctx, secret) == null) {
                break;
            }
        }
        publication.getConfiguration().put(SECRET, secret);
    }
    @Override
    public void beforeUpdate(Publication publication) throws OXException {
        super.beforeUpdate(publication);
        publication.getConfiguration().remove(URL);
    }

    private String generateSecret() {
        long l1 = random.nextLong();
        long l2 = random.nextLong();
        return Long.toHexString(l1)+Long.toHexString(l2);
    }

    @Override
    public void modifyOutgoing(Publication publication) throws OXException {
        super.modifyOutgoing(publication);
        publication.getConfiguration().put(URL, PREFIX+"/"+publication.getContext().getContextId()+"/"+publication.getConfiguration().get(SECRET));
        publication.getConfiguration().remove(SECRET);

        if (null != publication.getEntityId()) {
            // Valid entity identifier needed in order to load associated document's meta-data
            DocumentMetadata metadata = InfostorePublicationUtils.loadDocumentMetadata(publication, this.fileAccessFactory);
            publication.setDisplayName((metadata.getTitle() == null) ? metadata.getFileName() : metadata.getTitle());
            publication.setEntityId(IDMangler.mangle(Integer.toString(metadata.getId()), Long.toString(metadata.getFolderId())));
        }
    }

    public Publication getPublication(Context ctx, String secret) throws OXException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(SECRET, secret);
        Collection<Publication> result = getDefaultStorage().search(ctx, getTarget().getId(), query);
        if(result == null || result.isEmpty()) {
            return null;
        }
        return result.iterator().next();
    }

    @Override
    protected SecurityStrategy getSecurityStrategy() {
        return ALLOW_ALL;
    }

    private static final Pattern SPLIT = Pattern.compile("/");

    @Override
    public Publication resolveUrl(final Context ctx, final String URL) throws OXException {
        if(!URL.contains(PREFIX)){
            return null;
        }
        final String[] path = SPLIT.split(URL, 0);
        final String secret = getSecret(path);
        if (secret == null) {
            return null;
        }
        return getPublication(ctx, secret);
    }

    private String getSecret(final String[] path) {
        return path[path.length-1];
    }

    @Override
    public String getInformation(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("Publication:").append("/n");
        sb.append("Context: " + publication.getContext().getContextId()).append("/n");
        sb.append("UserID:  " + publication.getUserId()).append("/n");
        return sb.toString();
    }
}
