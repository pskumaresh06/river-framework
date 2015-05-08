package org.riverframework.wrapper.org.openntf.domino;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.riverframework.wrapper.Document;
import org.riverframework.wrapper.DocumentIterator;
import org.riverframework.wrapper.DocumentList;

class DefaultDocumentList extends ArrayList<Document> implements DocumentList {
	private static final long serialVersionUID = 5182350124794069975L;

	@SuppressWarnings("deprecation")
	public DefaultDocumentList(org.openntf.domino.DocumentCollection _col) {
		org.openntf.domino.Document _doc = null;

		clear();
		_doc = _col.getFirstDocument();
		while (_doc != null) {
			Document doc = new DefaultDocument(_doc);
			this.add(doc);
			_doc = _col.getNextDocument(_doc);
		}
	}

	public DefaultDocumentList(org.openntf.domino.View _view) {
		org.openntf.domino.Document _doc = null;

		clear();
		_doc = _view.getFirstDocument();
		while (_doc != null) {
			DefaultDocument doc = new DefaultDocument(_doc);
			this.add(doc);
			_doc = _view.getNextDocument(_doc);
		}
	}

	@SuppressWarnings("deprecation")
	public DefaultDocumentList(org.openntf.domino.ViewEntryCollection _col) {
		org.openntf.domino.ViewEntry _entry = null;

		clear();
		_entry = _col.getFirstEntry();
		while (_entry != null) {
			DefaultDocument doc = new DefaultDocument(_entry.getDocument());
			this.add(doc);
			_entry = _col.getNextEntry(_entry);
		}
	}

	@Override
	public DocumentIterator iterator() {
		return null;
	}
	
	@Override
	public DocumentList deleteAll() {
		for (Document doc : this) {
			doc.delete();
		}
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
