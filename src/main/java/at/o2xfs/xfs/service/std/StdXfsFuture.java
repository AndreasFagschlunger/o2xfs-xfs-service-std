package at.o2xfs.xfs.service.std;

import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.o2xfs.xfs.api.RequestId;
import at.o2xfs.xfs.api.ServiceId;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.api.XfsExceptionFactory;
import at.o2xfs.xfs.service.api.XfsFuture;
import at.o2xfs.xfs.service.api.event.IntermediateListener;

public class StdXfsFuture<T> implements XfsFuture<T>, XfsEventNotification {

	private static final Logger LOG = LogManager.getLogger(StdXfsFuture.class);

	private final StdXfsService service;
	private final RequestId requestId;
	private final Optional<IntermediateListener> intermediateListener;
	private final Class<T> valueType;
	private boolean done = false;
	private T result = null;
	private XfsException exception = null;

	public StdXfsFuture(StdXfsService service, RequestId requestId, IntermediateListener intermediateListener,
			Class<T> valueType) {
		this.service = Objects.requireNonNull(service);
		this.requestId = requestId;
		this.intermediateListener = Optional.ofNullable(intermediateListener);
		this.valueType = valueType;
	}

	@Override
	public void cancel() throws XfsException {
		service.cancelAsyncRequest(requestId);
	}

	@Override
	public void fireIntermediateEvent(WfsResult wfsResult) {
		if (intermediateListener.isPresent()) {
			intermediateListener.get().onIntermediateEvent(wfsResult);
		}
	}

	@Override
	public void fireOperationCompleteEvent(WfsResult wfsResult) {
		synchronized (this) {
			if (wfsResult.getErrorCode() == 0) {
				if (valueType != null) {
					result = service.getResultFactory().create(wfsResult.getBuffer(), valueType);
					LOG.info("fireOperationCompleteEvent: wfsResult={},result={}", wfsResult, result);
				}
			} else {
				exception = XfsExceptionFactory.create(wfsResult.getErrorCode());
			}
			done = true;
			notifyAll();
		}
	}

	@Override
	public T get() throws InterruptedException, XfsException {
		synchronized (this) {
			if (!done) {
				wait();
			}
		}
		if (exception != null) {
			throw exception;
		}
		return result;
	}

	public RequestId getRequestId() {
		return requestId;
	}

	public ServiceId getServiceId() {
		return service.getId();
	}
}
