package org.riverframework.core;

import java.util.Iterator;

//TODO: evaluate if this class can be simplified with the View class
/**
 * Exposes the methods for iterate a collection of documents from a NoSQL database.
 * 
 * @author mario.sotil@gmail.com
 *
 */
public interface DocumentIterator extends Iterator<org.riverframework.core.Document>, Iterable<org.riverframework.core.Document>, Base {
	/**
	 * Returns the database where the collection's documents belong
	 * 
	 * @return a Database object
	 */
	public Database getDatabase();

	/**
	 * Loops around all the documents and call the delete method from each one. It would make a hard deletion. Anyway, its behavior will
	 * depend on how the wrapper loaded is implemented.
	 * 
	 * @return the same DocumentIterator, for method chaining
	 */
	public DocumentIterator deleteAll();
}
