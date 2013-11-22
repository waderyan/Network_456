package model;

import java.util.UUID;

public abstract class AbstractNtkEntity {
	
	public abstract String toString();
	
	protected final UUID id = UUID.randomUUID();
	
	public UUID getId () {
		return id;
	}
}
