package at.o2xfs.xfs.service.std;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import at.o2xfs.memory.core.Address;
import at.o2xfs.xfs.api.AppDisconnect;
import at.o2xfs.xfs.api.DeviceStatus;
import at.o2xfs.xfs.api.HardwareError;
import at.o2xfs.xfs.api.RequestId;
import at.o2xfs.xfs.api.UndeliverableMessage;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.api.XfsApi;
import at.o2xfs.xfs.api.XfsConstant;
import at.o2xfs.xfs.api.XfsEventClass;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.api.XfsMessage;
import at.o2xfs.xfs.api.XfsSystemEvent;
import at.o2xfs.xfs.databind.XfsEnum32Wrapper;
import at.o2xfs.xfs.service.api.ExecuteCommand;
import at.o2xfs.xfs.service.api.InfoCommand;
import at.o2xfs.xfs.service.api.XfsFuture;
import at.o2xfs.xfs.service.api.event.IntermediateListener;

public class ServiceDispatcher {

	private static final Logger LOG = LogManager.getLogger(ServiceDispatcher.class);

	private final XfsApi xfsApi;
	private final Address hWnd;
	private final StdXfsService service;

	private final List<XfsEvent> eventQueue;

	private final Map<RequestId, XfsEventNotification> requests;

	private Thread thread = null;

	public ServiceDispatcher(XfsApi xfsApi, Address hWnd, StdXfsService service) {
		this.xfsApi = Objects.requireNonNull(xfsApi);
		this.hWnd = Objects.requireNonNull(hWnd);
		this.service = Objects.requireNonNull(service);
		eventQueue = new ArrayList<>();
		requests = new HashMap<>();
	}

	private void notifyCloseComplete(WfsResult wfsResult) {
		notifyOperationCompleteEvent(wfsResult);
		thread.interrupt();
	}

	private void dispatchExecuteEvent(XfsMessage msg, WfsResult wfsResult) {
		synchronized (requests) {
			XfsEventNotification notification = requests.get(wfsResult.getRequestId());
			notification.fireIntermediateEvent(wfsResult);
		}
	}

	private void dispatchSystemEvent(WfsResult wfsResult) {
		XfsSystemEvent message = XfsEnum32Wrapper.of(wfsResult.getEventId(), XfsSystemEvent.class);
		switch (message) {
		case UNDELIVERABLE_MSG:
			UndeliverableMessage undeliverableMessage = xfsApi.getMemorySystem().read(wfsResult.getBuffer(),
					UndeliverableMessage.class);
			service.fireUndeliverableMessage(undeliverableMessage);
			break;
		case HARDWARE_ERROR:
			service.fireHardwareError(read(wfsResult.getBuffer(), HardwareError.class));
			break;
		case DEVICE_STATUS:
			service.fireDeviceStatus(read(wfsResult.getBuffer(), DeviceStatus.class));
			break;
		case APP_DISCONNECT:
			service.fireAppDisconnect(read(wfsResult.getBuffer(), AppDisconnect.class));
			break;
		case SOFTWARE_ERROR:
			service.fireSoftwareError(read(wfsResult.getBuffer(), HardwareError.class));
			break;
		case USER_ERROR:
			service.fireUserError(read(wfsResult.getBuffer(), HardwareError.class));
			break;
		case LOCK_REQUESTED:
			service.fireLockRequested();
			break;
		case FRAUD_ATTEMPT:
			service.fireFraudAttempt(read(wfsResult.getBuffer(), HardwareError.class));
			break;
		default:
			throw new IllegalArgumentException(message.name());
		}
	}

	private void doRun() {
		try {
			while (!thread.isInterrupted()) {
				XfsEvent event;
				synchronized (eventQueue) {
					while (eventQueue.isEmpty()) {
						LOG.debug("Waiting...");
						eventQueue.wait();
					}
					event = eventQueue.remove(0);
				}
				LOG.info("Processing: {}", event);
				try {
					XfsMessage msg = event.getMessage();
					switch (msg) {
					case CLOSE_COMPLETE:
						notifyCloseComplete(event.getWfsResult());
						break;
					case LOCK_COMPLETE:
					case UNLOCK_COMPLETE:
					case REGISTER_COMPLETE:
					case DEREGISTER_COMPLETE:
					case GETINFO_COMPLETE:
					case EXECUTE_COMPLETE:
						notifyOperationCompleteEvent(event.getWfsResult());
						break;
					case EXECUTE_EVENT:
						dispatchExecuteEvent(msg, event.getWfsResult());
						break;
					case SERVICE_EVENT:
						service.fireServiceEvent(event.getWfsResult());
						break;
					case USER_EVENT:
						service.fireUserEvent(event.getWfsResult());
						break;
					case SYSTEM_EVENT:
						dispatchSystemEvent(event.getWfsResult());
						break;
					default:
						throw new IllegalArgumentException(msg.name());
					}
				} finally {
					try {
						xfsApi.freeResult(event.getAddress());
					} catch (XfsException e) {
						LOG.error(new ParameterizedMessage("Error freeing WfsResult: {}", event.getAddress()), e);
					}
				}
			}
			LOG.info("Stopped.");
		} catch (InterruptedException e) {
			LOG.info("Interrupted.");
		}
	}

	private void notifyOperationCompleteEvent(WfsResult wfsResult) {
		synchronized (requests) {
			XfsEventNotification notification = requests.remove(wfsResult.getRequestId());
			notification.fireOperationCompleteEvent(wfsResult);
		}
	}

	private <T> T read(Address address, Class<T> type) {
		return xfsApi.getMemorySystem().read(address, type);
	}

	private void start() {
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(() -> doRun());
			thread.start();
		}
	}

	public XfsFuture<Void> close() throws XfsException {
		EmptyFuture result;
		synchronized (requests) {
			RequestId requestId = xfsApi.asyncClose(service.getId(), hWnd);
			result = new EmptyFuture(service, requestId);
			requests.put(requestId, result);
		}
		return result;
	}

	public XfsFuture<Void> deregisterAndClose() throws XfsException {
		CloseFuture result;
		synchronized (requests) {
			RequestId requestId = xfsApi.asyncDeregister(service.getId(), EnumSet.noneOf(XfsEventClass.class), hWnd,
					hWnd);
			result = new CloseFuture(service, requestId);
			requests.put(requestId, result);
		}
		return result;
	}

	public <E extends Enum<E> & XfsConstant, T> XfsFuture<T> execute(ExecuteCommand<E> command,
			IntermediateListener intermediateListener, Class<T> valueType) throws XfsException {
		StdXfsFuture<T> result;
		synchronized (requests) {
			RequestId requestId = xfsApi.asyncExecute(service.getId(), command.getCommand(), command.getCmdData(),
					command.getTimeOut(), hWnd);
			result = new StdXfsFuture<>(service, requestId, intermediateListener, valueType);
			requests.put(requestId, result);
		}
		return result;
	}

	public <E extends Enum<E> & XfsConstant, T> XfsFuture<T> getInfo(InfoCommand<E> command, Class<T> valueType)
			throws XfsException {
		StdXfsFuture<T> result = null;
		synchronized (requests) {
			RequestId requestId = xfsApi.asyncGetInfo(service.getId(), command.getCategory(), command.getQueryDetails(),
					command.getTimeOut(), hWnd);
			result = new StdXfsFuture<>(service, requestId, null, valueType);
			requests.put(requestId, result);
		}
		return result;
	}

	public XfsFuture<Void> lock(OptionalInt timeOut) throws XfsException {
		EmptyFuture result;
		synchronized (requests) {
			RequestId requestId = xfsApi.asyncLock(service.getId(), timeOut, hWnd);
			result = new EmptyFuture(service, requestId);
			requests.put(requestId, result);
		}
		return result;
	}

	public XfsFuture<Void> register(Set<XfsEventClass> eventClasses) throws XfsException {
		EmptyFuture result;
		synchronized (requests) {
			RequestId requestId = xfsApi.asyncRegister(service.getId(), eventClasses, hWnd, hWnd);
			result = new EmptyFuture(service, requestId);
			requests.put(requestId, result);
		}
		return result;
	}

	public void offer(XfsEvent event) {
		synchronized (eventQueue) {
			eventQueue.add(event);
			eventQueue.notifyAll();
			start();
		}
	}

	public XfsFuture<Void> unlock() throws XfsException {
		EmptyFuture result;
		synchronized (requests) {
			RequestId requestId = xfsApi.asyncUnlock(service.getId(), hWnd);
			result = new EmptyFuture(service, requestId);
			requests.put(requestId, result);
		}
		return result;
	}
}
