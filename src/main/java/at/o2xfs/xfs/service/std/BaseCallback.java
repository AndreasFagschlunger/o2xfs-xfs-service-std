package at.o2xfs.xfs.service.std;

import at.o2xfs.memory.core.MemorySystem;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.service.api.event.IntermediateListener;

public abstract class BaseCallback implements IntermediateListener {

	private final MemorySystem memorySystem;
	private final XfsResultFactory factory;

	public BaseCallback(MemorySystem memorySystem, XfsResultFactory factory) {
		this.memorySystem = memorySystem;
		this.factory = factory;
	}

	protected <T> T read(WfsResult wfsResult, Class<T> valueType) {
		return factory.create(wfsResult.getBuffer(), valueType);
	}

}
