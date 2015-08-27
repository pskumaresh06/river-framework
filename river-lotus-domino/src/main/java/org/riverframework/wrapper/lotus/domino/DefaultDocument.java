package org.riverframework.wrapper.lotus.domino;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
// import java.util.logging.Logger;

import lotus.domino.Base;
import lotus.domino.DateTime;
import lotus.domino.Item;
import lotus.domino.NotesException;

import org.riverframework.River;
import org.riverframework.RiverException;
import org.riverframework.core.DefaultField;
import org.riverframework.core.Field;
import org.riverframework.utils.Converter;
import org.riverframework.wrapper.Document;
import org.riverframework.wrapper.Factory;

/**
 * Loads an IBM Notes document
 * <p>
 * This is a javadoc test
 * 
 * @author mario.sotil@gmail.com
 * @version 0.0.x
 */
class DefaultDocument extends AbstractBase<lotus.domino.Document> implements org.riverframework.wrapper.Document<lotus.domino.Document> {
	protected org.riverframework.wrapper.Session<lotus.domino.Session> _session = null;
	protected org.riverframework.wrapper.Factory<lotus.domino.Base> _factory = null;
	protected volatile lotus.domino.Document __doc = null;
	private String objectId = null;

	private final String FRAGMENTED_FIELD_ID = "{{RIVER_FRAGMENTED_FIELD}}";
	private final String FRAGMENT_FIELD_NAME_SEPARATOR = "$";
	private final int MAX_FIELD_SIZE = 32 * 1024 - 1;
	
	@SuppressWarnings("unchecked")
	protected DefaultDocument(org.riverframework.wrapper.Session<lotus.domino.Session> _s, lotus.domino.Document __d) {
		__doc = __d;
		_session = _s;
		_factory = (Factory<Base>) _session.getFactory();
		objectId = calcObjectId(__doc);
	}

	@Override
	public boolean isRecycled() {
		return isObjectRecycled(__doc);
	}

	String getDocumentId() {
		if (_factory.getIsRemoteSession()) {
			String id;
			try {
				id = __doc.getUniversalID();
			} catch (NotesException e) {
				throw new RiverException(e);
			}
			return id; 
		} else {
			return String.valueOf(getCpp(__doc));
		}
	}

	public String calcObjectId(lotus.domino.Document __doc) {
		String objectId = "";
		if (__doc != null) {
			String documentId = getDocumentId();

			if(isRecycled()) {
				throw new RiverException("The object " + documentId + " was recycled!");
			} else {
				try {
					lotus.domino.Database __database = __doc.getParentDatabase();

					StringBuilder sb = new StringBuilder(512);
					sb.append(__database.getServer());
					sb.append(River.ID_SEPARATOR);
					sb.append(__database.getFilePath());
					sb.append(River.ID_SEPARATOR);
					sb.append(documentId);

					objectId = sb.toString();

				} catch (NotesException e) {
					throw new RiverException(e);
				}
			}
		}

		return objectId;
	}

	@Override
	public Document<lotus.domino.Document> setTable(String table) {
		setField("Form", table);
		return this;
	}

	@Override
	public String getTable() {
		return getFieldAsString("Form");
	}
	
	@Override
	public lotus.domino.Document getNativeObject() {
		return __doc;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Document<lotus.domino.Document> setField(String field, Object value) {
		java.util.Vector temp = null;

		if (value instanceof String) {
			String str = (String) value;
			if (str.length() >= MAX_FIELD_SIZE) {
				// Fragment the text in parts with size less than MAX_FIELD_SIZE
				boolean finished = false;
				int size = str.length();
				int block = 0;
				boolean remote = _factory.getIsRemoteSession();
				
				while (!finished) {
					int start = block * (MAX_FIELD_SIZE);
					
					// For some reason, the replaceItemValue lost two bytes  
					// when it is a remote session
					if (remote && start > 0)  
						start -= block* 2;
					
					int end = start + (MAX_FIELD_SIZE);
					if (end > size) { 
						end = size;
						finished = true;
					}
					String substr = str.substring(start, end);
					block++;
					
					try {
						String fieldName = field + FRAGMENT_FIELD_NAME_SEPARATOR + block;
						Item __item = __doc.replaceItemValue(fieldName, substr);
						__item.setSummary(false);
						if(!_factory.getIsRemoteSession())  __item.recycle(); 
					} catch (NotesException e) {
						throw new RiverException(e);
					}
				}				
				
				// Saving the size as number of blocks 
				str = FRAGMENTED_FIELD_ID + "|" + block;		
			}
			
			temp = new Vector(1);
			temp.add(str);
			
		} else if (value instanceof java.util.Vector) {
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
				// Always save as lotus.domino.DateTime
				lotus.domino.Session __session;
				lotus.domino.DateTime __date;
				try {
					__session = _session.getNativeObject(); //	__doc.getParentDatabase().getParent();
					__date = __session.createDateTime((java.util.Date) temp.get(i));
				} catch (NotesException e) {
					throw new RiverException(e);
				}
				temp.set(i, __date);
			}
		}

		try {
			__doc.replaceItemValue(field, temp);
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		return this;
	}

	@Override
	public String getObjectId() {
		return objectId;
	}

	@Override
	public Document<lotus.domino.Document> recalc() {
		try {
			__doc.computeWithForm(true, false);
		} catch (NotesException e) {
			throw new RiverException(e);
		}
		return this;
	}

	@Override
	public Field getField(String field) {
		Field value = null;

		try {
			Vector<?> temp = null;
			temp = __doc.getItemValue(field);
			value = temp == null ? new DefaultField() : new DefaultField(temp);
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		if (!value.isEmpty()) {
			if (value.get(0) instanceof lotus.domino.DateTime) {
				for (int i = 0; i < value.size(); i++) {
					try {
						value.set(i, ((lotus.domino.DateTime) value.get(i)).toJavaDate());
					} catch (NotesException e) {
						throw new RiverException(e);
					}
				}
			}
		}

		return value;
	}

	@Override
	public String getFieldAsString(String field) {
		String result = null;
		try {			
			Vector<?> value = null;
			value = __doc.getItemValue(field);  // We must not use getItemValueString(field) because it does not converts from the other types to string 
			result = value.size() > 0 ? Converter.getAsString(value.get(0)) : "";

			if (result.startsWith(FRAGMENTED_FIELD_ID)) {
				String[] params = result.split("\\|");
				int blockNum = Integer.valueOf(params[1]);
				StringBuilder sb = new StringBuilder(MAX_FIELD_SIZE * blockNum);
				for(int i = 1; i <= blockNum; i++) {
					String fragment = getFieldAsString(field + FRAGMENT_FIELD_NAME_SEPARATOR + i );
					sb.append(fragment);
				}
				
				result = sb.toString();
			}
			
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		return result;
	}

	@Override
	public int getFieldAsInteger(String field) {
		int result = 0;
		try {
			Vector<?> value = null;
			value = __doc.getItemValue(field);
			result = value.size() > 0 ? Converter.getAsInteger(value.get(0)) : 0;
		} catch (NotesException e) {
			throw new RiverException(e);
		}
		return result;
	}

	@Override
	public long getFieldAsLong(String field) {
		long result = 0;
		try {
			Vector<?> value = null;
			value = __doc.getItemValue(field);
			result = value.size() > 0 ? Converter.getAsLong(value.get(0)) : 0;
		} catch (NotesException e) {
			throw new RiverException(e);
		}
		return result;
	}

	@Override
	public double getFieldAsDouble(String field) {
		double result;
		try {
			Vector<?> value = null;
			value = __doc.getItemValue(field);
			result = value.size() > 0 ? Converter.getAsDouble(value.get(0)) : 0;
		} catch (NotesException e) {
			throw new RiverException(e);
		}
		return result;
	}

	@Override
	public Date getFieldAsDate(String field) {
		Date result;
		try {
			Vector<?> value = null;
			value = __doc.getItemValue(field);
			Object temp = value.size() > 0 ? value.get(0) : null;  
			if (temp != null && temp.getClass().getName().endsWith("DateTime")) {
				temp = ((DateTime) temp).toJavaDate();
			}

			result = Converter.getAsDate(temp);
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		return result;
	}

	@Override
	public boolean isFieldEmpty(String field) {
		boolean result = true;
		try {
			if (__doc.hasItem(field)) {
				lotus.domino.Item __item = __doc.getFirstItem(field);
				if (__item != null) {
					if (__item.getType() == lotus.domino.Item.RICHTEXT) {
						if (!__doc.getEmbeddedObjects().isEmpty()) {
							for (@SuppressWarnings("unchecked")
							Iterator<lotus.domino.EmbeddedObject> i = __doc.getEmbeddedObjects()
							.iterator(); i.hasNext();) {
								lotus.domino.EmbeddedObject eo = i.next();
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
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		return result;
	}

	@Override
	public boolean hasField(String field) {
		boolean result;
		try {
			result = __doc.hasItem(field);
		} catch (NotesException e) {
			throw new RiverException(e);
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Field> getFields() {
		Map<String, Field> result = null;

		try {
			// logWrapper.debug("getFields: " + _doc.getUniversalID());
			// logWrapper.debug("getFields: loading items");

			Vector<lotus.domino.Item> items = __doc.getItems();

			// logWrapper.debug("getFields: found " + items.size());
			result = new HashMap<String, Field>();

			for (lotus.domino.Item __item : items) {
				String name = __item.getName();
				int type = __item.getType();
				// logWrapper.debug("getFields: item=" + name + ", type=" + type);

				Field values = null;

				if (type == Item.TEXT
						|| type == Item.NUMBERS
						|| type == Item.NAMES
						|| type == Item.DATETIMES
						|| type == Item.READERS
						|| type == Item.RICHTEXT) 
				{
					Vector<Object> temp = __item.getValues();
					values = temp == null ? new DefaultField() : new DefaultField(temp);
				} else 
				{
					values = new DefaultField();
				}

				// __item.recycle(); // <== Very bad idea? 

				if (values.isEmpty()) {
					// logWrapper.debug("getFields: it's empty");
					values.add("");
				}

				if (!values.isEmpty()) {
					if (values.get(0) instanceof lotus.domino.DateTime) {
						// logWrapper.debug("getFields: it's datetime");
						for (int i = 0; i < values.size(); i++) {
							values.set(i, ((lotus.domino.DateTime) values.get(i)).toJavaDate());
						}
					}
				}

				// logWrapper.debug("getFields: saving into the map");
				result.put(name, values);
			}
		} catch (NotesException e) {
			throw new RiverException(e);
		}

		return result;
	}

	@Override
	public boolean isOpen() {		
		return (__doc != null && !isRecycled()); 
	}

	@Override
	public boolean isNew() {
		boolean result;
		try {
			result = __doc.isNewNote();
		} catch (NotesException e) {
			throw new RiverException(e);
		}
		return result;
	}

	@Override
	public Document<lotus.domino.Document> delete() {
		// synchronized (_session){  <== necessary?
		if (__doc != null) {
			try {
				__doc.removePermanently(true);
			} catch (NotesException e) {
				throw new RiverException(e);
			} finally {
				__doc = null;
			}
		}

		return this;
		// }
	}

	@Override
	public Document<lotus.domino.Document> save() {
		try {
			__doc.save(true, false);
		} catch (NotesException e) {
			throw new RiverException(e);
		}
		return this;
	}

	@Override
	@Deprecated
	public void close() {
		// Don't recycle or close it. Let the server do that.
	}	

	@Override
	public String toString() {
		return getClass().getName() + "(" + objectId + ")";
	}
}
