package org.riverframework.wrapper.org.openntf.domino;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
// import java.util.logging.Logger;


import org.openntf.domino.DateTime;
import org.openntf.domino.Item;
import org.riverframework.core.Field;
import org.riverframework.River;
import org.riverframework.RiverException;
import org.riverframework.core.DefaultField;
import org.riverframework.utils.Converter;
import org.riverframework.wrapper.Document;

/**
 * Loads an IBM Notes document
 * <p>
 * This is a javadoc test
 * 
 * @author mario.sotil@gmail.com
 * @version 0.0.x
 */
class DefaultDocument extends DefaultBase<org.openntf.domino.Document> implements org.riverframework.wrapper.Document<org.openntf.domino.Document> {
	// private static final Logger log = River.LOG_WRAPPER_ORG_OPENNTF_DOMINO;
	protected org.riverframework.wrapper.Session<org.openntf.domino.Session> _session = null;
	protected volatile org.openntf.domino.Document __doc = null;
	private String objectId = null;

	protected DefaultDocument(org.riverframework.wrapper.Session<org.openntf.domino.Session> s, org.openntf.domino.Document d) {
		__doc = d;
		_session = s;
		objectId = calcObjectId(__doc);
	}

	public static String calcObjectId(org.openntf.domino.Document __doc) {
		String objectId = "";
		if (__doc != null) {
			org.openntf.domino.Database __database = __doc.getParentDatabase();

			StringBuilder sb = new StringBuilder();
			sb.append(__database.getServer());
			sb.append(River.ID_SEPARATOR);
			sb.append(__database.getFilePath());
			sb.append(River.ID_SEPARATOR);
			sb.append(__doc.getUniversalID());

			objectId = sb.toString();
		}

		return objectId;
	}

	@Override
	public Document<org.openntf.domino.Document> setTable(String table) {
		__doc.replaceItemValue("Form", table);
		return this;
	}

	@Override
	public String getTable() {
		return __doc.getItemValueString("Form");
	}

	@Override
	public org.openntf.domino.Document getNativeObject() {
		return __doc;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Document setField(String field, Object value) {
		java.util.Vector temp = null;

		if (value instanceof java.util.Vector) {
			temp = (Vector) ((java.util.Vector) value).clone();
		} else if (value instanceof java.util.Collection) {
			temp = new Vector((java.util.Collection) value);
		} else if (value instanceof String[]) {
			temp = new Vector(Arrays.asList((Object[]) value));
		} else {
			temp = new Vector(1);
			temp.add(value);
		}

		if (temp.get(0) instanceof java.util.Date) {
			for (int i = 0; i < temp.size(); i++) {
				// Always save as org.openntf.domino.DateTime
				org.openntf.domino.Session __session;
				org.openntf.domino.DateTime _date;

				__session = _session.getNativeObject(); //	__doc.getParentDatabase().getParent();
				_date = __session.createDateTime((java.util.Date) temp.get(i));

				temp.set(i, _date);
			}
		}

		__doc.replaceItemValue(field, temp);

		return this;
	}

	@Override
	public String getObjectId() {
		return objectId;
	}

	@Override
	public Document<org.openntf.domino.Document> recalc() {
		__doc.computeWithForm(true, false);
		return this;
	}

	@Override
	public Field getField(String field) {
		Vector<?> temp = __doc.getItemValue(field);
		Field value = temp == null ? new DefaultField() : new DefaultField(temp);

		if (!value.isEmpty()) {
			if (value.get(0) instanceof org.openntf.domino.DateTime) {
				for (int i = 0; i < value.size(); i++) {
					value.set(i, ((org.openntf.domino.DateTime) value.get(i)).toJavaDate());
				}
			}
		}

		return value;
	}

	@Override
	public String getFieldAsString(String field) {
		Vector<?> value = __doc.getItemValue(field);
		String result = value.size() > 0 ? Converter.getAsString(value.get(0)) : "";

		return result;
	}

	@Override
	public int getFieldAsInteger(String field) {
		Vector<?> value = __doc.getItemValue(field);
		int result = value.size() > 0 ? Converter.getAsInteger(value.get(0)) : 0;

		return result;
	}

	@Override
	public long getFieldAsLong(String field) {
		Vector<?> value = __doc.getItemValue(field);
		long result = value.size() > 0 ? Converter.getAsLong(value.get(0)) : 0;

		return result;
	}

	@Override
	public double getFieldAsDouble(String field) {
		Vector<?> value = __doc.getItemValue(field);
		double result = value.size() > 0 ? Converter.getAsDouble(value.get(0)) : 0;

		return result;
	}

	@Override
	public Date getFieldAsDate(String field) {
		Date result;

		Vector<?> value = null;
		value = __doc.getItemValue(field);
		Object temp = value.size() > 0 ? value.get(0) : null;  
		if (temp != null && temp.getClass().getName().endsWith("DateTime")) {
			temp = ((DateTime) temp).toJavaDate();
		}

		result = Converter.getAsDate(temp);

		return result;
	}

	@Override
	public boolean isFieldEmpty(String field) {
		boolean result = true;
		if (__doc.hasItem(field)) {
			org.openntf.domino.Item __item = __doc.getFirstItem(field);
			if (__item != null) {
				if (__item.getType() == org.openntf.domino.Item.RICHTEXT) {
					if (!__doc.getEmbeddedObjects().isEmpty()) {
						for (@SuppressWarnings("unchecked")
						Iterator<org.openntf.domino.EmbeddedObject> i = __doc.getEmbeddedObjects()
						.iterator(); i.hasNext();) {
							org.openntf.domino.EmbeddedObject eo = i.next();
							if (eo.getType() != 0) {
								result = false;
								break;
							}
						}
					}
				}

				if (result && __item.getText() != "")
					result = false;

				try {
					// __item.recycle(); <== Very bad idea? 
				} catch (Exception e) {
					throw new RiverException(e);
				} finally {
					__item = null;
				}
			}
		}

		return result;
	}

	@Override
	public boolean hasField(String field) {
		boolean result;

		result = __doc.hasItem(field);

		return result;
	}

	@Override
	public Map<String, Field> getFields() {
		Map<String, Field> result = null;

		// logWrapper.debug("getFields: " + _doc.getUniversalID());
		// logWrapper.debug("getFields: loading items");

		Vector<org.openntf.domino.Item> items = null;
		items = __doc.getItems();

		// logWrapper.debug("getFields: found " + items.size());
		result = new HashMap<String, Field>(items.size());

		for (org.openntf.domino.Item __item : items) {
			String name = __item.getName();
			int type = __item.getType();
			// logWrapper.debug("getFields: item=" + name + ", type=" + type);

			Field values = null;

			if (type == Item.DATETIMES
					|| type == Item.NAMES
					|| type == Item.NUMBERS
					|| type == Item.READERS
					|| type == Item.RICHTEXT
					|| type == Item.TEXT) {
				Vector<Object> temp = __item.getValues();
				values = temp == null ? new DefaultField() : new DefaultField(temp);
			} else {
				values = new DefaultField();
			}

			// __item.recycle(); <== Very bad idea? 

			if (values.isEmpty()) {
				// logWrapper.debug("getFields: it's empty");
				values.add("");
			}

			if (!values.isEmpty()) {
				if (values.get(0) instanceof org.openntf.domino.DateTime) {
					// logWrapper.debug("getFields: it's datetime");
					for (int i = 0; i < values.size(); i++) {
						values.set(i, ((org.openntf.domino.DateTime) values.get(i)).toJavaDate());
					}
				}
			}

			// logWrapper.debug("getFields: saving into the map");
			result.put(name, values);
		}

		return result;
	}

	@Override
	public boolean isOpen() {		
		return (__doc != null);
	}

	@Override
	public boolean isNew() {
		boolean result;

		result = __doc.isNewNote();

		return result;
	}

	@Override
	public Document<org.openntf.domino.Document> delete() {
		if (__doc != null) {
			__doc.removePermanently(true);
			__doc = null;
		}

		return this;
	}

	@Override
	public Document<org.openntf.domino.Document> save() {
		__doc.save(true, false);

		return this;
	}

	@Override
	public void close() {
		__doc = null;
	}	

	@Override
	public String toString() {
		return getClass().getName() + "(" + objectId + ")";
	}

}
