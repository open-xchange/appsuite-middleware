
package com.openexchange.test.fixtures.ajax;

import java.io.File;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.resource.Resource;
import com.openexchange.test.fixtures.AppointmentFixtureFactory;
import com.openexchange.test.fixtures.ContactFixtureFactory;
import com.openexchange.test.fixtures.CredentialFixtureFactory;
import com.openexchange.test.fixtures.Document;
import com.openexchange.test.fixtures.DocumentFixtureFactory;
import com.openexchange.test.fixtures.EMailFixtureFactory;
import com.openexchange.test.fixtures.FixtureLoader;
import com.openexchange.test.fixtures.FolderFixtureFactory;
import com.openexchange.test.fixtures.GroupFixtureFactory;
import com.openexchange.test.fixtures.InfoItem;
import com.openexchange.test.fixtures.InfoItemFixtureFactory;
import com.openexchange.test.fixtures.ResourceFixtureFactory;
import com.openexchange.test.fixtures.SimpleCredentials;
import com.openexchange.test.fixtures.TaskFixtureFactory;
import com.openexchange.test.fixtures.YAMLFixtureLoader;

public class FixtureLoaderFactory {

    public static FixtureLoader getLoader(AJAXClient client, File datapath) {//TODO add datapath to method signature
        final YAMLFixtureLoader loader = new YAMLFixtureLoader();

        AJAXGroupResolver groupResolver = new AJAXGroupResolver(client);
        AJAXContactFinder contactFinder = new AJAXContactFinder(client);
        AJAXUserConfigFactory userConfigFactory = new AJAXUserConfigFactory();

        loader.addFixtureFactory(new TaskFixtureFactory(groupResolver, loader), Task.class);
        loader.addFixtureFactory(new AppointmentFixtureFactory(groupResolver, loader), Appointment.class);
        loader.addFixtureFactory(new ContactFixtureFactory(loader), Contact.class);
        loader.addFixtureFactory(new InfoItemFixtureFactory(loader), InfoItem.class);
        loader.addFixtureFactory(new CredentialFixtureFactory(userConfigFactory, contactFinder, loader), SimpleCredentials.class);
        loader.addFixtureFactory(new GroupFixtureFactory(loader), Group.class);
        loader.addFixtureFactory(new ResourceFixtureFactory(loader), Resource.class);
        loader.addFixtureFactory(new EMailFixtureFactory(datapath, loader), MailMessage.class);
        loader.addFixtureFactory(new DocumentFixtureFactory(datapath, loader), Document.class);
        loader.addFixtureFactory(new FolderFixtureFactory(loader), FolderObject.class);
        return loader;
    }
}
