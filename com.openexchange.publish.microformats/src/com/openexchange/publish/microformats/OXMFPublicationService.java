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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.publish.microformats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.AbstractPublicationService;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationTarget;

/**
 * {@link OXMFPublicationService}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXMFPublicationService extends AbstractPublicationService {

    private static final String SECRET = "secret";

    private static final String PROTECTED = "protected";

    private static final String SITE = "siteName";

    private Random random = new Random();

    private String rootURL;

    private PublicationTarget target;


    
    public OXMFPublicationService() {
        super();
        target = buildTarget();
    }
    
    @Override
    public PublicationTarget getTarget() throws PublicationException {
        return target;
    }

    private PublicationTarget buildTarget() {
        PublicationTarget target = new PublicationTarget();
        
        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("siteName", "Site", true, null)).add(FormElement.checkbox("protected", "Hide with secret?"));
        
        target.setFormDescription(form);
        target.setPublicationService(this);
        
        return target;
    }

    public void setRootURL(String string) {
        rootURL = string;
    }
    
    public String getRootURL() {
        return rootURL;
    }
    
    public void setFolderType(String string) {
        target.setModule("folder:"+string);
    }
    
    public void setTargetId(String targetId) {
        target.setId(targetId);
    }
    
    public void setTargetDisplayName(String string) {
        target.setDisplayName(string);
    }


    @Override
    public void beforeCreate(Publication publication) throws PublicationException {
        super.beforeCreate(publication);
        addSecretIfNeeded(publication, null);
    }

    @Override
    public void beforeUpdate(Publication publication) throws PublicationException {
        super.beforeUpdate(publication);
        Publication oldPublication = load(publication.getContext(), publication.getId());
        addSecretIfNeeded(publication, oldPublication);
        removeSecretIfNeeded(publication);
    }

    @Override
    public void modifyOutgoing(Publication publication) throws PublicationException {
        super.modifyOutgoing(publication);

        Map<String, Object> configuration = publication.getConfiguration();

        StringBuilder urlBuilder = new StringBuilder(rootURL);
        urlBuilder.append('/').append(publication.getContext().getContextId()).append('/').append(configuration.get(SITE));

        if (configuration.containsKey(SECRET)) {
            urlBuilder.append("?secret=").append(configuration.get(SECRET));
        }

        publication.setUrl(urlBuilder.toString());

        publication.getConfiguration().remove(SECRET);

    }

    private boolean needsSecret(Publication publication) {
        Map<String, Object> configuration = publication.getConfiguration();
        return configuration.containsKey(PROTECTED) && (Boolean) configuration.get(PROTECTED);
    }

    private void removeSecretIfNeeded(Publication publication) {
        if (mustRemoveSecret(publication)) {
            publication.getConfiguration().put(SECRET, null);
        }
    }

    private boolean mustRemoveSecret(Publication publication) {
        Map<String, Object> configuration = publication.getConfiguration();
        return configuration.containsKey(PROTECTED) && !(Boolean) configuration.get(PROTECTED);
    }

    private void addSecretIfNeeded(Publication publication, Publication oldPublication) {
        if (needsSecret(publication)) {

            String secret = null;
            if(oldPublication != null) {
                secret = (String) oldPublication.getConfiguration().get(SECRET);
            }
            if (secret == null) {
                long l1 = random.nextLong();
                long l2 = random.nextLong();

                secret = Long.toHexString(l1) + Long.toHexString(l2);
            }

            publication.getConfiguration().put(SECRET, secret);

        }
    }

    public Publication getPublication(Context ctx, String site) throws PublicationException {
        Map<String,Object> query = new HashMap<String, Object>();
        query.put(SITE, site);
        
        Collection<Publication> result = getStorage().search(ctx, getTarget().getId(), query);
        if(result.isEmpty()) {
            return null;
        }
        
        return result.iterator().next();
    }


}
