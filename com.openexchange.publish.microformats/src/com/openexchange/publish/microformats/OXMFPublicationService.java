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

package com.openexchange.publish.microformats;

import static com.openexchange.publish.microformats.FormStrings.FORM_LABEL_LINK;
import static com.openexchange.publish.microformats.FormStrings.FORM_LABEL_PROTECTED;
import static com.openexchange.publish.microformats.FormStrings.FORM_LABEL_SITE;
import static com.openexchange.publish.microformats.FormStrings.FORM_LABEL_TEMPLATE;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationErrorMessage;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.helpers.AbstractPublicationService;
import com.openexchange.publish.helpers.SecurityStrategy;
import com.openexchange.publish.interfaces.UserSpecificPublicationTarget;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link OXMFPublicationService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXMFPublicationService extends AbstractPublicationService {

    private static final String SECRET = OXMFConstants.SECRET;

    private static final String PROTECTED = OXMFConstants.PROTECTED;

    private static final String SITE_NAME = OXMFConstants.SITE_NAME;

    private static final String URL = OXMFConstants.URL;

    private static final String TEMPLATE = OXMFConstants.TEMPLATE;

    private final Random random = new Random();

    private String rootURL;

    private final PublicationTarget target;

    private TemplateService templateService;

    private String defaultTemplateName;

    private FormElement templateChooser;

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXMFPublicationService.class);

    public OXMFPublicationService() {
        super();
        target = buildTarget();
    }

    @Override
    public PublicationTarget getTarget() throws OXException {
        return target;
    }

    private PublicationTarget buildTarget() {
        final DynamicFormDescription form = new DynamicFormDescription();
        final DynamicFormDescription withoutInfostore = new DynamicFormDescription();

        form.add(FormElement.input(SITE_NAME, FORM_LABEL_SITE, true, null));
        withoutInfostore.add(FormElement.input(SITE_NAME, FORM_LABEL_SITE, true, null));

        templateChooser = FormElement.custom("com.openexchange.templating.templateChooser",TEMPLATE, FORM_LABEL_TEMPLATE);
        form.add(templateChooser);

        // No templating without infostore

        form.add(FormElement.checkbox(PROTECTED, FORM_LABEL_PROTECTED, true, Boolean.TRUE));
        withoutInfostore.add(FormElement.checkbox(PROTECTED, FORM_LABEL_PROTECTED, true, Boolean.TRUE));

        form.add(FormElement.link(URL, FORM_LABEL_LINK, false, null));
        withoutInfostore.add(FormElement.link(URL, FORM_LABEL_LINK, false, null));


        final PublicationTarget target = new OptionalTemplatingTarget(withoutInfostore);

        target.setFormDescription(form);
        target.setPublicationService(this);

        return target;
    }

    public void setRootURL(final String string) {
        rootURL = string;
    }

    public String getRootURL() {
        return rootURL;
    }

    public void setFolderType(final String string) {
        templateChooser.setOption("only", "publish,"+string);
        target.setModule(string);
    }

    public void setTargetId(final String targetId) {
        target.setId(targetId);
    }

    public void setTargetDisplayName(final String string) {
        target.setDisplayName(string);
    }

    @Override
    public void beforeCreate(final Publication publication) throws OXException {
        super.beforeCreate(publication);
        publication.getConfiguration().remove(URL);
        addSecretIfNeeded(publication, null);
        loadTemplate(publication);
    }

    public OXTemplate loadTemplate(final Publication publication) throws OXException {
        final Object object = publication.getConfiguration().get(TEMPLATE);
        if (null == object || JSONObject.NULL.equals(object)) {
            return templateService.loadTemplate(defaultTemplateName);
        }
        final String templateName = object.toString();
        if (templateName == null || "".equals(templateName)) {
            return templateService.loadTemplate(defaultTemplateName);
        }
        final ServerSession serverSession = ServerSessionAdapter.valueOf(new PublicationSession(publication));
        return templateService.loadTemplate(templateName, defaultTemplateName, serverSession);
    }

    @Override
    public void beforeUpdate(final Publication publication) throws OXException {
        super.beforeUpdate(publication);
        final Publication oldPublication = loadInternally(publication.getContext(), publication.getId());
        publication.getConfiguration().remove(URL);
        addSecretIfNeeded(publication, oldPublication);
        removeSecretIfNeeded(publication);
    }

    @Override
    public void modifyOutgoing(final Publication publication) throws OXException {
        super.modifyOutgoing(publication);

        updateUrl(publication);

        publication.getConfiguration().remove(SECRET);

        publication.setDisplayName( (String) publication.getConfiguration().get(SITE_NAME));
    }

    private void updateUrl(final Publication publication) {
        final Map<String, Object> configuration = publication.getConfiguration();

        final StringBuilder urlBuilder = new StringBuilder(rootURL);
        urlBuilder.append('/').append(publication.getContext().getContextId()).append('/').append(saneSiteName((String) configuration.get(SITE_NAME)));

        if (configuration.containsKey(SECRET)) {
            urlBuilder.append("?secret=").append(configuration.get(SECRET));
        }

        publication.getConfiguration().put(URL, urlBuilder.toString());
    }

    private String saneSiteName(final String site) {
        if (isEmpty(site)) {
            return site;
        }
        return AJAXUtility.encodeUrl(site, true, false);
    }

    protected String normalizeSiteName(final String siteName) {
        final String[] path = siteName.split("/");
        final List<String> normalized = new ArrayList<String>(path.length);
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals("")) {
                normalized.add(path[i]);
            }
        }

        final String site = Strings.join(normalized, "/");
        return site;
    }

    @Override
    public void modifyIncoming(final Publication publication) throws OXException {
        String siteName = (String) publication.getConfiguration().get(SITE_NAME);

        if (siteName != null) {
            siteName = normalizeSiteName(siteName);
            final Publication oldPub = getPublication(publication.getContext(), siteName);
            if (oldPub != null && oldPub.getId() != publication.getId()) {
                throw PublicationErrorMessage.UNIQUENESS_CONSTRAINT_VIOLATION_EXCEPTION.create(SITE_NAME, siteName);
            }
            publication.getConfiguration().put(SITE_NAME, siteName);
        }
    }

    private boolean needsSecret(final Publication publication) {
        final Map<String, Object> configuration = publication.getConfiguration();
        return configuration.containsKey(PROTECTED) && ((Boolean) configuration.get(PROTECTED)).booleanValue();
    }

    private void removeSecretIfNeeded(final Publication publication) {
        if (mustRemoveSecret(publication)) {
            publication.getConfiguration().put(SECRET, null);
        }
    }

    private boolean mustRemoveSecret(final Publication publication) {
        final Map<String, Object> configuration = publication.getConfiguration();
        return configuration.containsKey(PROTECTED) && !((Boolean) configuration.get(PROTECTED)).booleanValue();
    }

    private void addSecretIfNeeded(final Publication publication, final Publication oldPublication) {
        if (needsSecret(publication)) {

            String secret = null;
            if (oldPublication != null) {
                secret = (String) oldPublication.getConfiguration().get(SECRET);
            }
            if (secret == null) {
                final long l1 = random.nextLong();
                final long l2 = random.nextLong();

                secret = Long.toHexString(l1) + Long.toHexString(l2);
            }

            publication.getConfiguration().put(SECRET, secret);

        }
    }

    public Publication getPublication(final Context ctx, final String site) throws OXException {
        final Map<String,Object> query = new HashMap<String, Object>();
        query.put(SITE_NAME, site);

        final Collection<Publication> result = getStorage().search(ctx, getTarget().getId(), query);
        if (result.isEmpty()) {
            return null;
        }

        return result.iterator().next();
    }

    public void setDefaultTemplateName(final String defaultTemplateName) {
        this.defaultTemplateName = defaultTemplateName;
    }


    public void setTemplateService(final TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    protected SecurityStrategy getSecurityStrategy() {
        return FOLDER_ADMIN_ONLY;
    }

    private static final class OptionalTemplatingTarget extends PublicationTarget implements UserSpecificPublicationTarget {

        private final DynamicFormDescription withoutInfostore;

        public OptionalTemplatingTarget(final DynamicFormDescription withoutInfostore) {
            this.withoutInfostore = withoutInfostore;
        }

        @Override
        public DynamicFormDescription getUserSpecificDescription(final User user, final UserPermissionBits permissionBits) {
            if (permissionBits.hasInfostore()) {
                return getFormDescription();
            }
            return withoutInfostore;
        }
    }

    @Override
    public Publication resolveUrl(final Context ctx, final String URL) throws OXException {
        final String tmpRootUrl = getRootURL();
        if (getRootURL() == null) {
            return null;
        }
        if(!URL.contains(tmpRootUrl)){
            return null;
        }
        final Pattern firstSplit = Pattern.compile(getRootURL());
        final Pattern SPLIT = Pattern.compile("/");
        //The Url is something like http://localhost/rootURL/[cid]/[siteName(this may contain /)]?secret=[secret]
        final String[] pathSplitByRootUrl = firstSplit.split(URL, 0);
        //We want to get everything behind rootUrl/
        final String[] path = SPLIT.split(pathSplitByRootUrl[1], 0);
        final List<String> normalized = new ArrayList<String>(path.length);
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals("")) {
                String tmpPath = path[i];
                if (tmpPath.contains("?secret")){
                    tmpPath = tmpPath.split("\\?secret",0)[0];
                }
                normalized.add(tmpPath);
            }
        }
        final String site = getSite(normalized);
        if (site == null) {
            return null;
        }
        return getPublication(ctx, site);
    }

    private String getSite(final List<String> normalized) {
        Pattern splittern = Pattern.compile("\\+");
        //We need to decode this path Element here
        String encoding = "UTF-8";
        try {
            return Strings.join(HelperClass.decodeList(normalized.subList(1, normalized.size()),encoding, splittern), "/");
        } catch (UnsupportedEncodingException e) {
            LOG.warn("", e);
        }
        return null;
    }

    @Override
    public String getInformation(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("Publication:").append(", ");
        sb.append("Context: " + publication.getContext().getContextId()).append(", ");
        sb.append("UserID:  " + publication.getUserId());
        return sb.toString();
    }

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }
}
