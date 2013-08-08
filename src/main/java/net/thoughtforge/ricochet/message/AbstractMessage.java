package net.thoughtforge.ricochet.message;

import java.io.Serializable;

import net.thoughtforge.ricochet.Node;

public abstract class AbstractMessage implements Serializable {

	private static final long serialVersionUID = -7967785378107328836L;

	public abstract void handleMessage(Node node) throws Exception;
}
