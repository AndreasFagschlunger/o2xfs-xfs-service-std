package at.o2xfs.xfs.service.std;

import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.o2xfs.memory.core.Address;
import at.o2xfs.xfs.api.VersionsRequired;
import at.o2xfs.xfs.api.XfsApi;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.api.XfsVersion;
import at.o2xfs.xfs.service.api.XfsFuture;
import at.o2xfs.xfs.service.api.XfsService;
import at.o2xfs.xfs.service.api.XfsServiceManager;

public class StdXfsServiceManager implements XfsServiceManager {

	private static final Logger LOG = LogManager.getLogger(StdXfsServiceManager.class);

	private final XfsApi xfsApi;
	private final MessageLoop messageLoop;
	private final EventDispatcher eventDispatcher;

	private Optional<Address> appHandle;

	public StdXfsServiceManager(XfsApi xfsApi, MessageLoop messageLoop) {
		this.xfsApi = Objects.requireNonNull(xfsApi);
		this.messageLoop = Objects.requireNonNull(messageLoop);
		eventDispatcher = new EventDispatcher(xfsApi);
		appHandle = Optional.empty();
	}

	private OpenParam buildOpenParam(String logicalName) {
		return new OpenParam.Builder(logicalName).appHandle(appHandle).appId(Optional.of(getClass().getSimpleName()))
				.srvcVersionsRequired(VersionsRequired.build(XfsVersion.V3_00, XfsVersion.V3_30)).build();
	}

	@Override
	public <E extends XfsService> XfsFuture<E> open(String logicalName, Class<E> serviceClass) throws XfsException {
		return eventDispatcher.open(buildOpenParam(logicalName), serviceClass);
	}

	@Override
	public void initialize() throws XfsException {
		xfsApi.startUp(XfsVersion.V3_00, XfsVersion.V3_30);
		appHandle = Optional.of(xfsApi.createAppHandle());
		messageLoop.start(eventDispatcher);
		eventDispatcher.setWindowHandle(messageLoop.getWindowHandle());
	}

	@Override
	public void shutdown() {
		try {
			eventDispatcher.stop();
		} catch (InterruptedException e) {
			LOG.error("Interrupted stopping EventDispatcher", e);
		}
		if (appHandle.isPresent()) {
			try {
				xfsApi.destroyAppHandle(appHandle.get());
				appHandle = Optional.empty();
			} catch (XfsException e) {
				LOG.error("WFSDestroyAppHandle: appHandle=" + appHandle, e);
			}
		}
		try {
			xfsApi.cleanUp();
		} catch (XfsException e) {
			LOG.error("WFSCleanUp failed", e);
		}
		messageLoop.stop();
	}
}
