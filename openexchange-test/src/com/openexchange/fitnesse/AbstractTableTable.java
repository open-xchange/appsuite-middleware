package com.openexchange.fitnesse;

import java.util.List;
import com.openexchange.fitnesse.wrappers.FixtureDataWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.fixtures.AppointmentFixtureFactory;
import com.openexchange.test.fixtures.ContactFixtureFactory;
import com.openexchange.test.fixtures.Fixture;
import com.openexchange.test.fixtures.FixtureException;
import com.openexchange.test.fixtures.Fixtures;
import com.openexchange.test.fixtures.TaskFixtureFactory;

/**
 * 
 * {@link AbstractTableTable} - The superclass for all TableTables
 * used by FitNesse / Slim
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public abstract class AbstractTableTable implements SlimTableTable{
    
    public FitnesseEnvironment environment;
    public FixtureDataWrapper data;

    public AbstractTableTable() {
        super();
        environment = FitnesseEnvironment.getInstance();
    }

    /**
     * Required method for FitNesse calls.
     */
    public final List doTable(List<List<String>> table) throws Exception {
        data = new FixtureDataWrapper(table) ;
        return doTable();
    }
    
    /**
     * Works with the internal FixtureDataWrapper <code>data</code>
     * @return
     * @throws Exception
     */
    public abstract List doTable() throws Exception ;
    
    /**
     * Creates a task via TaskFixtureFactory
     */
    public Task createTask(String fixtureName, FixtureDataWrapper data) throws FixtureException{
        TaskFixtureFactory taskFixtureFactory = new TaskFixtureFactory(null, null);
        Fixtures<Task> fixtures = taskFixtureFactory.createFixture(fixtureName, data.asFixtureMap("task"));
        Fixture<Task> entry = fixtures.getEntry("task");
        return entry.getEntry();
    }
    
    /**
     * Creates an appointment via AppointmentFixtureFactory
     */
    public AppointmentObject createAppointment(String fixtureName, FixtureDataWrapper data) throws FixtureException{
        AppointmentFixtureFactory appointmentFixtureFactory = new AppointmentFixtureFactory(null, null);
        Fixtures<AppointmentObject> fixtures = appointmentFixtureFactory.createFixture(fixtureName, data.asFixtureMap("appointment"));
        Fixture<AppointmentObject> entry = fixtures.getEntry("appointment");
        return entry.getEntry();
    }
    
    /**
     * Creates a contact via ContactFixtureFactory
     */
    public ContactObject createContact(String fixtureName, FixtureDataWrapper data) throws FixtureException{
        ContactFixtureFactory contactFixtureFactory = new ContactFixtureFactory(null);
        Fixtures<ContactObject> fixtures = contactFixtureFactory.createFixture(fixtureName, data.asFixtureMap("contact"));
        Fixture<ContactObject> entry = fixtures.getEntry("contact");
        return entry.getEntry();
    }
}
