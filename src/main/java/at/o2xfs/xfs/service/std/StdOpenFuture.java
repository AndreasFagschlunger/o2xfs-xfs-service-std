package at.o2xfs.xfs.service.std;

import java.util.EnumSet;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.o2xfs.xfs.api.RequestId;
import at.o2xfs.xfs.api.ServiceId;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.api.WfsVersion;
import at.o2xfs.xfs.api.XfsEventClass;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.api.XfsExceptionFactory;
import at.o2xfs.xfs.service.api.OpenFuture;
import at.o2xfs.xfs.service.api.XfsFuture;

public class StdOpenFuture<T extends StdXfsService> implements OpenFuture<T>, XfsEventNotification {

	private static final Logger LOG = LogManager.getLogger(StdOpenFuture.class);

	private final T service;
	private final RequestId requestId;

	private XfsFuture<Void> registerFuture = null;
	private XfsException exception = null;

	public StdOpenFuture(T service, RequestId requestId) {
		this.service = Objects.requireNonNull(service);
		this.requestId = Objects.requireNonNull(requestId);
	}

	@Override
	public void cancel() throws XfsException {
		if (registerFuture != null) {
			registerFuture.cancel();
		} else {
			service.cancelAsyncRequest(requestId);
		}
	}

	@Override
	public void fireIntermediateEvent(WfsResult wfsResult) {
		throw new UnsupportedOperationException(wfsResult.toString());
	}

	@Override
	public void fireOperationCompleteEvent(WfsResult wfsResult) {
		if (wfsResult.getErrorCode() != 0) {
			exception = XfsExceptionFactory.create(wfsResult.getErrorCode());
		} else {
			try {
				registerFuture = service.register(EnumSet.allOf(XfsEventClass.class));
			} catch (XfsException e) {
				exception = e;
			}
		}
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public T get() throws InterruptedException, XfsException {
		synchronized (this) {
			while (registerFuture == null && exception == null) {
				LOG.debug("Waiting for completion, RequestId: {}", requestId);
				wait();
			}
		}
		if (exception != null) {
			throw exception;
		}
		registerFuture.get();
		return service;
	}

	public RequestId getRequestId() {
		return requestId;
	}

	public ServiceId getServiceId() {
		return service.getId();
	}

	@Override
	public WfsVersion getServiceVersion() {
		return service.getVersion();
	}

	@Override
	public WfsVersion getSpiVersion() {
		return service.getSpiVersion();
	}
}
