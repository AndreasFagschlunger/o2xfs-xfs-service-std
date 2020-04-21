package at.o2xfs.xfs.service.std;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import at.o2xfs.memory.core.Address;
import at.o2xfs.memory.core.MemorySystem;
import at.o2xfs.xfs.api.OpenRequest;
import at.o2xfs.xfs.api.OpenResponse;
import at.o2xfs.xfs.api.RequestId;
import at.o2xfs.xfs.api.ServiceId;
import at.o2xfs.xfs.api.VersionError;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.api.XfsApi;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.api.XfsMessage;
import at.o2xfs.xfs.api.XfsSystemEvent;
import at.o2xfs.xfs.databind.XfsEnum32Wrapper;
import at.o2xfs.xfs.service.api.OpenFuture;
import at.o2xfs.xfs.service.api.XfsService;

public class EventDispatcher implements WindowProcCallback {

	private static final Logger LOG = LogManager.getLogger(EventDispatcher.class);

	private final XfsApi xfsApi;

	private final MemorySystem memorySystem;

	private final Map<ServiceId, StdXfsService> services;

	private final Map<RequestId, XfsEventNotification> requests;

	private final List<XfsEvent> eventQueue;

	private Address hWnd;

	private Thread thread = null;

	public EventDispatcher(XfsApi xfsApi) {
		this.xfsApi = Objects.requireNonNull(xfsApi);
		memorySystem = Objects.requireNonNull(xfsApi.getMemorySystem());
		services = new HashMap<>();
		requests = new HashMap<>();
		eventQueue = new ArrayList<>();
	}

	private void closeAllServices() {
		for (StdXfsService service : services.values()) {
			try {
				LOG.info("Closing service {} ...", service.getId());
				service.close().get();
			} catch (InterruptedException | XfsException e) {
				LOG.error("Error closing service", e);
			}
		}
	}

	private void dispatch() throws InterruptedException {
		XfsEvent event;
		synchronized (eventQueue) {
			while (eventQueue.isEmpty()) {
				LOG.debug("Waiting for events...");
				eventQueue.wait();
			}
			event = eventQueue.remove(0);
		}
		LOG.info("{}", event);
		switch (event.getMessage()) {
		case OPEN_COMPLETE:
			notifyOperationComplete(event);
			break;
		case CLOSE_COMPLETE:
		case LOCK_COMPLETE:
		case UNLOCK_COMPLETE:
		case REGISTER_COMPLETE:
		case DEREGISTER_COMPLETE:
		case GETINFO_COMPLETE:
		case EXECUTE_COMPLETE:
		case EXECUTE_EVENT:
		case SERVICE_EVENT:
		case USER_EVENT:
			dispatchServiceEvent(event);
			break;
		case SYSTEM_EVENT:
			dispatchSystemEvent(event);
			break;
		case TIMER_EVENT:
			break;
		}
	}

	private void doRun() {
		try {
			while (!thread.isInterrupted()) {
				dispatch();
			}
		} catch (InterruptedException e) {
			LOG.debug("Stopped.", e);
		}
		LOG.info("Stopped.");
	}

	private void dispatchServiceEvent(XfsEvent event) {
		synchronized (services) {
			StdXfsService service = services.get(event.getWfsResult().getServiceId());
			if (service == null) {
				LOG.warn("Unknown service: msg={},wfsResult={}", event.getMessage(), event.getWfsResult());
				try {
					xfsApi.freeResult(event.getAddress());
				} catch (XfsException e) {
					LOG.error(new ParameterizedMessage("Error freeing WfsResult: {}", event.getAddress()), e);
				}
			} else {
				service.getDispatcher().offer(event);
			}
		}
	}

	private void dispatchSystemEvent(XfsEvent event) {
		XfsSystemEvent systemEvent = XfsEnum32Wrapper.of(event.getWfsResult().getEventId(), XfsSystemEvent.class);
		if (XfsSystemEvent.VERSION_ERROR.equals(systemEvent)) {
			try {
				VersionError versionError = memorySystem.read(event.getWfsResult().getBuffer(), VersionError.class);
				LOG.info(versionError);
			} finally {
				try {
					xfsApi.freeResult(event.getAddress());
				} catch (XfsException e) {
					LOG.error(new ParameterizedMessage("Error freeing WfsResult: {}", event.getAddress()), e);
				}
			}
		} else {
			dispatchServiceEvent(event);
		}
	}

	private void offer(XfsEvent event) {
		synchronized (eventQueue) {
			eventQueue.add(event);
			eventQueue.notifyAll();
			if (thread == null || !thread.isAlive()) {
				thread = new Thread(() -> doRun());
				thread.start();
			}
		}
	}

	private void notifyOperationComplete(XfsEvent event) {
		XfsEventNotification listener;
		LOG.debug("notifyOperationComplete: event={},requests={}", event, requests);
		try {
			synchronized (requests) {
				listener = requests.remove(event.getWfsResult().getRequestId());
			}
			LOG.debug("listener: {}", listener);
			listener.fireOperationCompleteEvent(event.getWfsResult());
		} finally {
			try {
				xfsApi.freeResult(event.getAddress());
			} catch (XfsException e) {
				LOG.error(new ParameterizedMessage("Error freeing WfsResult: {}", event), e);
			}
		}
		if (XfsMessage.CLOSE_COMPLETE.equals(event.getMessage())) {
			synchronized (services) {
				services.remove(event.getWfsResult().getServiceId());
			}
		}
	}

	public <E extends XfsService> OpenFuture<E> open(OpenParam request, Class<E> serviceClass) throws XfsException {
		StdOpenFuture<?> result = null;
		StdXfsService service;
		OpenResponse response;
		synchronized (requests) {
			response = xfsApi.asyncOpen(new OpenRequest.Builder(request.getLogicalName(), hWnd)
					.appHandle(request.getAppHandle()).appId(request.getAppId()).traceLevel(request.getTraceLevel())
					.timeOut(request.getTimeOut()).srvcVersionsRequired(request.getSrvcVersionsRequired()).build());
			service = new XfsServiceFactory(xfsApi, hWnd).create(request.getLogicalName(), serviceClass, response);
			result = new StdOpenFuture<>(service, response.getRequestId());
			requests.put(response.getRequestId(), result);
			requests.notifyAll();
			synchronized (services) {
				services.put(response.getServiceId(), service);
			}
		}
		return (OpenFuture<E>) result;
	}

	public void setWindowHandle(Address hWnd) {
		this.hWnd = hWnd;
	}

	public void stop() throws InterruptedException {
		closeAllServices();
		if (thread != null) {
			thread.interrupt();
			thread.join();
		}
	}

	@Override
	public boolean windowProc(int msg, byte[] lParam) {
		XfsMessage message = XfsEnum32Wrapper.of(msg, XfsMessage.class);
		Address address = Address.build(lParam);
		LOG.debug("message={},address={}", message, address);
		if (message == null || XfsMessage.TIMER_EVENT.equals(message)) {
			LOG.warn("Unexpected message: msg={},lParam={}", msg, lParam);
			return false;
		}
		WfsResult wfsResult = memorySystem.read(address, WfsResult.class);
		offer(XfsEvent.build(message, address, wfsResult));
		return true;
	}
}
