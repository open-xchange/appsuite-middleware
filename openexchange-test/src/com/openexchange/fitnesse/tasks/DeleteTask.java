package com.openexchange.fitnesse.tasks;

import java.util.List;
import com.openexchange.fitnesse.AbstractTableTable;
import com.openexchange.fitnesse.wrappers.FitnesseResult;


public class DeleteTask  extends AbstractTableTable {

    @Override
    public List doTable() throws Exception {
        return (new FitnesseResult(data, FitnesseResult.PASS)).toResult();
    }


}
