package com.openexchange.test.fitnesse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.kata.tasks.TaskCreateStep;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.fixtures.Fixture;
import com.openexchange.test.fixtures.Fixtures;
import com.openexchange.test.fixtures.TaskFixtureFactory;


public class CreateTask extends AbstractTableTable {
    
    public CreateTask()  {
        super();
        try {
            System.setProperty("test.propfile", "/Users/development/workspace/openexchange-test/conf/test.properties");
            AJAXConfig.init();
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 
    @Override
    public List doTable(List<List<String>> table) throws Exception {
        Map<String, String> readAsMap = readAsMap(table);
        TaskFixtureFactory taskFixtureFactory = new TaskFixtureFactory(null,null);
        Map<String, Map<String,String>> map = new HashMap<String, Map<String,String>>();
        map.put("task", readAsMap);
        Fixtures<Task> fixtures = taskFixtureFactory.createFixture("beispielname", map);
        Fixture<Task> entry = fixtures.getEntry("task");
        Task task = entry.getEntry();
        TaskCreateStep taskCreateStep = new TaskCreateStep(task, "create task step", null);
        AJAXSession session = new AJAXSession();
        session.setId(LoginTools.login(session, new LoginRequest("thorben","netline")).getSessionId());
        taskCreateStep.perform( new AJAXClient( session ) );
        return new LinkedList();
    }

}
