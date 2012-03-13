package com.openexchange.index;

import java.util.Collection;

import com.openexchange.exception.OXException;

public interface IndexAccess {
	
	void addEnvelopeData(IndexDocument document) throws OXException;
	
	void addEnvelopeData(Collection<IndexDocument> documents) throws OXException;
	
	void addContent(IndexDocument document) throws OXException;
	
	void addContent(Collection<IndexDocument> documents) throws OXException;
	
	void addAttachments(IndexDocument document) throws OXException;
	
	void addAttachments(Collection<IndexDocument> documents) throws OXException;
	
	void deleteById(String id) throws OXException;
	
	void deleteByQuery(String query) throws OXException;
		
	IndexResult query(QueryParameters parameters) throws OXException;
	
	TriggerType getTriggerType();

}
