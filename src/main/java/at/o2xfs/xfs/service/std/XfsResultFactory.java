package at.o2xfs.xfs.service.std;

import at.o2xfs.memory.core.Address;

public interface XfsResultFactory {

	<T> T create(Address buffer, Class<T> valueType);
}
