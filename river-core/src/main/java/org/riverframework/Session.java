package org.riverframework;

public interface Session extends Base {
	public static final String OBJECT_PREFIX = "RIVER_";
	public static final String FIELD_PREFIX = "RIVER_";

	public <U extends Database> U getDatabase(String... parameters);

	public <U extends Database> U getDatabase(Class<U> type, String... parameters);

	public boolean isOpen();

	public String getUserName();

	public void close();

	public Session open(org.riverframework.wrapper.Session s);

}
