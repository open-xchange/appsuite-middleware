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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleActivator;
import com.openexchange.osgi.CompositeBundleActivator;

/**
 * {@link ServerCompositeActivator} combines several activators in the server bundle that have been prepared to split up the server bundle into several
 * bundles. Currently this is not done to keep number of packages low.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ServerCompositeActivator extends CompositeBundleActivator {

    private final BundleActivator[] activators = {
        new com.openexchange.ajax.requesthandler.osgi.PrefixServiceActivator(),
        new com.openexchange.json.cache.impl.osgi.JsonCacheActivator(),
        new com.openexchange.tools.pipesnfilters.osgi.PipesAndFiltersActivator(),
        new com.openexchange.tools.file.osgi.FileStorageWrapperActivator(),
        new com.openexchange.groupware.filestore.osgi.FilestoreActivator(),
        new com.openexchange.context.osgi.ContextActivator(),
        new com.openexchange.groupware.update.osgi.Activator(),
        new com.openexchange.groupware.reminder.osgi.ReminderActivator(),
        new com.openexchange.systemname.osgi.SystemNameActivator(),
        new com.openexchange.groupware.notify.hostname.osgi.HostDataActivator(),
        new com.openexchange.ajax.version.osgi.VersionActionActivator(),
        new com.openexchange.filemanagement.osgi.ManagedFileManagementActivator(),
        new com.openexchange.server.osgi.ServerActivator(),
        new com.openexchange.ajax.requesthandler.osgi.DispatcherActivator(),
        new com.openexchange.login.osgi.LoginRampUpActivator(),
        new com.openexchange.ajax.ipcheck.osgi.IPCheckActivator(),
        new com.openexchange.groupware.settings.impl.osgi.SettingsActivator(),
        new com.openexchange.ajax.printing.osgi.AJAXPrintingActivator(),
        new com.openexchange.groupware.attach.osgi.AttachmentActivator(),
        new com.openexchange.groupware.contact.osgi.ContactActivator(),
        new com.openexchange.groupware.infostore.osgi.InfostoreActivator(),
        new com.openexchange.groupware.infostore.media.osgi.MediaMetadataActivator(),
        new com.openexchange.groupware.importexport.osgi.ImportExportActivator(),
        new com.openexchange.consistency.osgi.ConsistencyActivator(),
        new com.openexchange.authorization.osgi.AuthorizationActivator(),
        new com.openexchange.authentication.service.osgi.AuthenticationActivator(),
        new com.openexchange.ajax.osgi.SessionServletInterceptorActivator(),
        new com.openexchange.ajax.login.osgi.LoginActivator(),
        new com.openexchange.tools.images.osgi.ImageToolsActivator(),
        new com.openexchange.mail.json.osgi.MailJSONActivator(),
        new com.openexchange.mail.json.compose.share.osgi.ShareComposeActivator(),
        new com.openexchange.mail.osgi.UserSettingMailActivator(),
        new com.openexchange.filemanagement.json.osgi.ManagedFileJSONActivator(),
        new com.openexchange.group.internal.osgi.GroupActivator(),
        new com.openexchange.group.json.osgi.GroupJSONActivator(),
        new com.openexchange.resource.internal.osgi.ResourceActivator(),
        new com.openexchange.resource.json.osgi.ResourceJSONActivator(),
        new com.openexchange.quota.json.osgi.QuotaJSONActivator(),
        new com.openexchange.config.json.osgi.ConfigJSONActivator(),
        new com.openexchange.config.admin.osgi.ConfigAdminActivator(),
        new com.openexchange.mailaccount.json.osgi.MailAccountJSONActivator(),
        new com.openexchange.mailaccount.osgi.ExternalMailAccountProviderActivator(),
        new com.openexchange.mail.compose.osgi.CompositionSpaceServiceActivator(),
        new com.openexchange.contact.storage.osgi.ContactStorageActivator(),
        new com.openexchange.contact.osgi.ContactServiceActivator(),
        new com.openexchange.ajax.redirect.osgi.RedirectActivator(),
        new com.openexchange.ajax.noop.osgi.NoopActivator(),
        new com.openexchange.groupware.tasks.osgi.TaskActivator(),
        new FolderUpdaterRegistryDependencyActivator(),
        new com.openexchange.image.osgi.ImageActivator(),
        new com.openexchange.ajax.requesthandler.converters.preview.cache.osgi.ResourceCacheActivator(),
        new com.openexchange.report.osgi.ReportActivator(),
        new com.openexchange.groupware.update.tasks.quota.QuotaGWActivator(),
        new com.openexchange.server.osgi.PingActivator(),
        new com.openexchange.passwordchange.osgi.PasswordChangeActivator(),
        new com.openexchange.ajax.anonymizer.osgi.AnonymizerActivator(),
        new com.openexchange.server.osgi.inspector.SessionInspectorChainActivator(),
        new com.openexchange.groupware.upload.osgi.UploadActivator(),
        new com.openexchange.net.ssl.management.osgi.SSLCertificateManagementActivator(),
        new com.openexchange.net.ssl.management.json.osgi.SSLCertificateManagementJSONActivator(),
        new com.openexchange.tools.oxfolder.property.osgi.FolderUserPropertyActivator(),
        new com.openexchange.groupware.datahandler.osgi.DataHandlerActivator(),
        new com.openexchange.diagnostics.osgi.DiagnosticsActivator(),
        new com.openexchange.groupware.upgrade.osgi.SegmentedUpgradeActivator(),
        new com.openexchange.groupware.update.tasks.objectusagecount.UseCountTableActivator()
    };

    public ServerCompositeActivator() {
        super();
    }

    @Override
    protected BundleActivator[] getActivators() {
        return activators;
    }
}
