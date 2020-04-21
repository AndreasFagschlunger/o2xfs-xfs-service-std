package at.o2xfs.xfs.service.std;

import at.o2xfs.memory.core.Address;

public interface MessageLoop {

	void start(WindowProcCallback callback);

	void stop();

	Address getWindowHandle();
}
