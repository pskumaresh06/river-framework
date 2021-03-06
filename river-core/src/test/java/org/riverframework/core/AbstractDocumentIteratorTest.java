package org.riverframework.core;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.riverframework.Context;
import org.riverframework.RandomString;

public abstract class AbstractDocumentIteratorTest {
	protected Session session = null;
	protected Context context = null;
	protected Database database = null;

	final String TEST_FORM = "TestForm";

	@Before
	public void open() {
		// Opening the test context in the current package
		try {
			if (context == null) {
				String className = this.getClass().getPackage().getName() + ".Context";
				Class<?> clazz = Class.forName(className);
				if (org.riverframework.Context.class.isAssignableFrom(clazz)) {
					Constructor<?> constructor = clazz.getDeclaredConstructor();
					constructor.setAccessible(true);
					context = (Context) constructor.newInstance();
				}

				session = context.getSession();
				database = session.getDatabase(context.getTestDatabaseServer(), context.getTestDatabasePath());
				database.getAllDocuments().deleteAll();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@After
	public void close() {
		context.closeSession();
	}
	
	@Test
	public void testIterator() {
		assertTrue("The test database could not be instantiated.",
				database != null);
		assertTrue("The test database could not be opened.", database.isOpen());

		DocumentIterator col = null;
		col = database.getAllDocuments().deleteAll();

		RandomString rs = new RandomString(10);

		for (int i = 0; i < 10; i++) {
			database.createDocument().setField("Form", TEST_FORM)
					.setField("Value", rs.nextString()).save();
		}

		col = database.getAllDocuments();
		int j = 0;
		for (@SuppressWarnings("unused") Document doc : col) {
			j++;
		}
		assertTrue("The iterator does not returns the expected values.", j == 10);
	}
}
