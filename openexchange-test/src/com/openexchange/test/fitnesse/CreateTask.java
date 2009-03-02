package com.openexchange.test.fitnesse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.kata.tasks.TaskCreateStep;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.fixtures.Fixture;
import com.openexchange.test.fixtures.Fixtures;
import com.openexchange.test.fixtures.TaskFixtureFactory;


public class CreateTask extends AbstractTableTable {
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
        taskCreateStep.perform( new AJAXClient( User.User1 ) );
        return new LinkedList();
    }

}
