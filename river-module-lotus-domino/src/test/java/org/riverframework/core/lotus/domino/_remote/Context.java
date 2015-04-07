package org.riverframework.core.lotus.domino._remote;

import org.riverframework.River;
import org.riverframework.Session;
import org.riverframework.core.Credentials;

public final class Context extends org.riverframework.core.AbstractContext {
	@Override
	public String getConfigurationFileName() {
		return "test-configuration-lotus-domino-remote";
	}

	@Override
	public Session getSession() {
		Session session = River.getInstance().getSession(
				River.MODULE_LOTUS_DOMINO,
				Credentials.getServer(),
				Credentials.getUsername(),
				Credentials.getPassword());
		return session;
	}

	@Override
	public void closeSession() {
		River.getInstance().closeSession(River.MODULE_LOTUS_DOMINO);
	}
}