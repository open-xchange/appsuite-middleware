
package com.openexchange.fitnesse.tasks;

import java.util.List;
import com.openexchange.ajax.kata.tasks.TaskCreateStep;
import com.openexchange.fitnesse.AbstractTableTable;
import com.openexchange.fitnesse.FitnesseEnvironment;
import com.openexchange.fitnesse.wrappers.FitnesseResult;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link CreateTask} - a wrapper to use TaskCreateStep via FitNesse
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CreateTask extends AbstractTableTable {

    public CreateTask() {
        super();
        environment = FitnesseEnvironment.getInstance();
    }

    @Override
    public List doTable() throws Exception {
        final String fixtureName = data.getFixtureName();
        Task task = createTask(fixtureName, data);

        TaskCreateStep taskCreateStep = new TaskCreateStep(task, "create task step", data.getExpectedError());
        taskCreateStep.perform(environment.getClientForUser1());
        environment.registerStep(taskCreateStep);
        environment.registerSymbol(fixtureName, taskCreateStep);
        return (new FitnesseResult(data, FitnesseResult.PASS)).toResult();
    }
}
