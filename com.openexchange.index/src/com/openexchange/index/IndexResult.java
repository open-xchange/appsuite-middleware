package com.openexchange.index;

import java.util.List;

public interface IndexResult {
	
	long getNumFound();
	
	List<IndexDocument> getResults();

}
