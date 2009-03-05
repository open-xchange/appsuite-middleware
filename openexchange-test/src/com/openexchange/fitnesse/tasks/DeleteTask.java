package com.openexchange.fitnesse.tasks;

import java.util.List;
import com.openexchange.ajax.kata.tasks.TaskDeleteStep;
import com.openexchange.fitnesse.AbstractTableTable;
import com.openexchange.fitnesse.wrappers.FitnesseResult;
import com.openexchange.groupware.tasks.Task;


public class DeleteTask extends AbstractTableTable {

    @Override
    public List doTable() throws Exception {
        final String fixtureName = data.getFixtureName();
        
        Task task = createTask(fixtureName, data);
        
        TaskDeleteStep taskStep = new TaskDeleteStep( task, data.getFixtureName(), data.expectedError() );
        taskStep.setIdentitySource( environment.getSymbol( fixtureName ) );
        environment.registerStep(taskStep);
        
        FitnesseResult returnValues = new FitnesseResult(data, FitnesseResult.PASS);
        
        taskStep.perform(environment.getClientForUser1());
        
        return returnValues.toResult();
    }


}
