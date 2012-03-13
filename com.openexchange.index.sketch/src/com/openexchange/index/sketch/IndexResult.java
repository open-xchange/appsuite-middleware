package com.openexchange.index.sketch;

import java.util.List;

public interface IndexResult {
	
	long getNumFound();
	
	List<IndexDocument> getResults();

}
