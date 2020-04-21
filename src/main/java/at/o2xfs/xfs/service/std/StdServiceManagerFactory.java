package at.o2xfs.xfs.service.std;

import java.util.ServiceLoader;

import at.o2xfs.xfs.api.XfsApi;
import at.o2xfs.xfs.service.api.XfsServiceManager;
import at.o2xfs.xfs.service.api.XfsServiceManagerFactory;
import at.o2xfs.xfs.service.std.win32.Win32MessageLoop;

public class StdServiceManagerFactory extends XfsServiceManagerFactory {

	@Override
	public XfsServiceManager getServiceManager() {
		MessageLoop messageLoop = new Win32MessageLoop();
		XfsApi xfsApi = ServiceLoader.load(XfsApi.class).findFirst().orElse(null);
		return new StdXfsServiceManager(xfsApi, messageLoop);
	}
}
