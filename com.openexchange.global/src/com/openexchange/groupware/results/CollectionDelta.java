package com.openexchange.groupware.results;

import java.util.ArrayList;
import java.util.List;

public class CollectionDelta<T> {

	public interface DeltaVisitor<T> {
		public void newOrModified(T thing);
		public void deleted(T thing);
	}

	private final List<T> newAndModified = new ArrayList<T>();
	private final List<T> deleted = new ArrayList<T>();

	public void addNewOrModified(T thing) {
		newAndModified.add(thing);
	}

	public void addDeleted(T thing) {
		deleted.add(thing);
	}

	public List<T> getNewAndModified() {
		return newAndModified;
	}

	public List<T> getDeleted() {
		return deleted;
	}

	public void visitAll(DeltaVisitor<T> visitor) {
		for(T thing : newAndModified) {
			visitor.newOrModified(thing);
		}

		for(T thing : deleted) {
			visitor.deleted(thing);
		}
	}
}
