package at.o2xfs.xfs.service.std;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.o2xfs.memory.core.Address;
import at.o2xfs.memory.core.MemorySystem;
import at.o2xfs.xfs.api.AppDisconnect;
import at.o2xfs.xfs.api.DeviceStatus;
import at.o2xfs.xfs.api.HardwareError;
import at.o2xfs.xfs.api.RequestId;
import at.o2xfs.xfs.api.ServiceId;
import at.o2xfs.xfs.api.UndeliverableMessage;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.api.WfsVersion;
import at.o2xfs.xfs.api.XfsApi;
import at.o2xfs.xfs.api.XfsEventClass;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.service.api.ExecuteCommand;
import at.o2xfs.xfs.service.api.InfoCommand;
import at.o2xfs.xfs.service.api.XfsFuture;
import at.o2xfs.xfs.service.api.XfsService;
import at.o2xfs.xfs.service.api.event.IntermediateListener;
import at.o2xfs.xfs.service.api.event.SystemEventListener;

public abstract class StdXfsService implements XfsService {

	private static final Logger LOG = LogManager.getLogger(StdXfsService.class);

	private final XfsApi xfsApi;
	private final MemorySystem memorySystem;
	private final XfsResultFactory resultFactory;
	private final ServiceDispatcher dispatcher;
	private final ServiceId serviceId;
	private final String logicalName;
	private final WfsVersion version;
	private final WfsVersion spiVersion;
	private final List<SystemEventListener> systemEventListeners;

	public StdXfsService(XfsApi xfsApi, XfsResultFactory resultFactory, Address hWnd, ServiceId serviceId,
			String logicalName, WfsVersion version, WfsVersion spiVersion) {
		this.xfsApi = Objects.requireNonNull(xfsApi);
		this.memorySystem = xfsApi.getMemorySystem();
		this.resultFactory = Objects.requireNonNull(resultFactory);
		this.serviceId = Objects.requireNonNull(serviceId);
		this.logicalName = Objects.requireNonNull(logicalName);
		this.version = Objects.requireNonNull(version);
		this.spiVersion = Objects.requireNonNull(spiVersion);
		dispatcher = new ServiceDispatcher(xfsApi, hWnd, this);
		systemEventListeners = new ArrayList<>();
	}

	void fireAppDisconnect(AppDisconnect appDisconnect) {
		LOG.info(appDisconnect);
		synchronized (systemEventListeners) {
			for (SystemEventListener each : systemEventListeners) {
				each.onAppDisconnect(appDisconnect);
			}
		}
	}

	void fireDeviceStatus(DeviceStatus deviceStatus) {
		LOG.info(deviceStatus);
		synchronized (systemEventListeners) {
			for (SystemEventListener each : systemEventListeners) {
				each.onDeviceStatus(deviceStatus);
			}
		}
	}

	void fireFraudAttempt(HardwareError hardwareError) {
		LOG.info(hardwareError);
		synchronized (systemEventListeners) {
			for (SystemEventListener each : systemEventListeners) {
				each.onFraudAttempt(hardwareError);
			}
		}
	}

	void fireHardwareError(HardwareError hardwareError) {
		LOG.info(hardwareError);
		synchronized (systemEventListeners) {
			for (SystemEventListener each : systemEventListeners) {
				each.onHardwareError(hardwareError);
			}
		}
	}

	void fireLockRequested() {
		synchronized (systemEventListeners) {
			for (SystemEventListener each : systemEventListeners) {
				each.onLockRequested();
			}
		}
	}

	void fireSoftwareError(HardwareError hardwareError) {
		LOG.info(hardwareError);
		synchronized (systemEventListeners) {
			for (SystemEventListener each : systemEventListeners) {
				each.onSoftwareError(hardwareError);
			}
		}
	}

	void fireUserError(HardwareError hardwareError) {
		LOG.info(hardwareError);
		synchronized (systemEventListeners) {
			for (SystemEventListener each : systemEventListeners) {
				each.onUserError(hardwareError);
			}
		}
	}

	void fireUndeliverableMessage(UndeliverableMessage undeliverableMessage) {
		LOG.info(undeliverableMessage);
		synchronized (systemEventListeners) {
			for (SystemEventListener each : systemEventListeners) {
				each.onUndeliverableMessage(undeliverableMessage);
			}
		}
	}

	ServiceDispatcher getDispatcher() {
		return dispatcher;
	}

	protected MemorySystem getMemorySystem() {
		return memorySystem;
	}

	public void cancelAsyncRequest(RequestId requestId) throws XfsException {
		xfsApi.cancelAsyncRequest(serviceId, requestId);
	}

	@Override
	public XfsFuture<Void> close() throws XfsException {
		return dispatcher.deregisterAndClose();
	}

	@Override
	public void addSystemEventListener(SystemEventListener listener) {
		systemEventListeners.add(listener);
	}

	@Override
	public <T> XfsFuture<T> execute(ExecuteCommand<?> command, IntermediateListener listener, Class<T> valueType)
			throws XfsException {
		return dispatcher.execute(command, listener, valueType);
	}

	abstract public void fireServiceEvent(WfsResult wfsResult);

	abstract public void fireUserEvent(WfsResult wfsResult);

	@Override
	public <T> XfsFuture<T> getInfo(InfoCommand<?> command, Class<T> valueType) throws XfsException {
		return dispatcher.getInfo(command, valueType);
	}

	@Override
	public String getLogicalName() {
		return logicalName;
	}

	@Override
	public ServiceId getId() {
		return serviceId;
	}

	public XfsResultFactory getResultFactory() {
		return resultFactory;
	}

	@Override
	public WfsVersion getSpiVersion() {
		return spiVersion;
	}

	@Override
	public WfsVersion getVersion() {
		return version;
	}

	@Override
	public XfsFuture<Void> lock(OptionalInt timeOut) throws XfsException {
		return dispatcher.lock(timeOut);
	}

	public XfsFuture<Void> register(Set<XfsEventClass> eventClasses) throws XfsException {
		return dispatcher.register(eventClasses);
	}

	@Override
	public void removeSystemEventListener(SystemEventListener listener) {
		systemEventListeners.remove(listener);
	}

	@Override
	public XfsFuture<Void> unlock() throws XfsException {
		return dispatcher.unlock();
	}
}
