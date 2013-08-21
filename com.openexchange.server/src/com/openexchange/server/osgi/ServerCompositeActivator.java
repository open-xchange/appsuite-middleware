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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleActivator;
import com.openexchange.ajax.requesthandler.converters.preview.cache.osgi.PreviewCacheActivator;
import com.openexchange.ajax.requesthandler.osgi.PrefixServiceActivator;
import com.openexchange.groupware.update.tasks.quota.QuotaGWActivator;
import com.openexchange.image.osgi.ImageActivator;
import com.openexchange.json.cache.impl.osgi.JsonCacheActivator;
import com.openexchange.osgi.CompositeBundleActivator;
import com.openexchange.report.osgi.ReportActivator;

/**
 * {@link ServerCompositeActivator} combines several activators in the server bundle that have been prepared to split up the server bundle into several
 * bundles. Currently this is not done to keep number of packages low.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ServerCompositeActivator extends CompositeBundleActivator {

    private final BundleActivator[] activators = {
    	new PrefixServiceActivator(),
    	new JsonCacheActivator(),
        new com.openexchange.tools.pipesnfilters.osgi.PipesAndFiltersActivator(),
        new com.openexchange.tools.file.osgi.DefaultFileStorageActivator(),
        new com.openexchange.tools.file.osgi.DBQuotaFileStorageActivator(),
        new com.openexchange.tools.file.osgi.FileStorageWrapperActivator(),
        new com.openexchange.groupware.filestore.osgi.FilestoreActivator(),
        new com.openexchange.context.osgi.ContextActivator(),
        new com.openexchange.groupware.update.osgi.Activator(),
        new com.openexchange.groupware.reminder.osgi.ReminderActivator(),
        new com.openexchange.systemname.osgi.SystemNameActivator(),
        new com.openexchange.groupware.notify.hostname.osgi.HostDataActivator(),
        new com.openexchange.server.osgi.ServerActivator(),
        new com.openexchange.ajax.requesthandler.osgi.DispatcherActivator(),
        new com.openexchange.groupware.attach.osgi.AttachmentActivator(),
        new com.openexchange.groupware.contact.osgi.ContactActivator(),
        new com.openexchange.groupware.infostore.osgi.InfostoreActivator(),
        new com.openexchange.groupware.links.osgi.LinkActivator(),
        new com.openexchange.groupware.importexport.osgi.ImportExportActivator(),
        new com.openexchange.consistency.osgi.ConsistencyActivator(),
        new com.openexchange.authorization.osgi.AuthorizationActivator(),
        new com.openexchange.authentication.service.osgi.AuthenticationActivator(),
        new com.openexchange.ajax.login.osgi.LoginActivator(),
        new com.openexchange.tools.images.osgi.ImageToolsActivator(),
        new com.openexchange.mail.json.osgi.MailJSONActivator(),
        new com.openexchange.filemanagement.json.osgi.ManagedFileJSONActivator(),
        new com.openexchange.group.json.osgi.GroupJSONActivator(),
        new com.openexchange.resource.json.osgi.ResourceJSONActivator(),
        new com.openexchange.quota.json.osgi.QuotaJSONActivator(),
        new com.openexchange.config.json.osgi.ConfigJSONActivator(),
        new com.openexchange.mailaccount.json.osgi.MailAccountJSONActivator(),
        new com.openexchange.contact.storage.osgi.ContactStorageActivator(),
        new com.openexchange.contact.osgi.ContactServiceActivator(),
        new com.openexchange.ajax.redirect.osgi.RedirectActivator(),
        new com.openexchange.groupware.tasks.osgi.TaskActivator(),
        new FolderUpdaterRegistryDependencyActivator(),
        new ImageActivator(), 
        new PreviewCacheActivator(),
        new ReportActivator(),
        new QuotaGWActivator(),
        new PingActivator(),
    };

    public ServerCompositeActivator() {
        super();
    }

    @Override
    protected BundleActivator[] getActivators() {
        return activators;
    }
}
