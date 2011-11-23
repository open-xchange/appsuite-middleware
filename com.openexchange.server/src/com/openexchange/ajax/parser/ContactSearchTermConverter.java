package com.openexchange.ajax.parser;

import java.util.List;

import com.openexchange.search.SearchTerm;

public interface ContactSearchTermConverter {

	/**
	 * Takes a search term (basically an Abstract Syntax Tree, AST,
	 * which is usually created from JSON sent by the GUI) and forms
	 * a WHERE clause as used by SQL database queries.
	 *
	 */
	public abstract <T> void parse(SearchTerm<T> term);

	/**
	 * Returns the folders that where queried. These may still be part of
	 * the query string, they are not removed per definition. There are just
	 * returned here for easier access (checking access rights) or to
	 * add this data back in to contacts again in case it gets lost.
	 *
	 * @return A list of folders that are requested to be searched
	 */
	public List<String> getFolders();
}