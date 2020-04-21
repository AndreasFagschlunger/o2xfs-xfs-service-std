package at.o2xfs.xfs.service.std;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.o2xfs.xfs.api.RequestId;
import at.o2xfs.xfs.api.ServiceId;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.api.XfsExceptionFactory;
import at.o2xfs.xfs.service.api.XfsFuture;

public class EmptyFuture implements XfsFuture<Void>, XfsEventNotification {

	private static final Logger LOG = LogManager.getLogger(EmptyFuture.class);

	private final StdXfsService service;
	private final RequestId requestId;
	private boolean complete = false;
	private XfsException exception = null;

	public EmptyFuture(StdXfsService service, RequestId requestId) {
		this.service = service;
		this.requestId = requestId;
	}

	@Override
	public void cancel() throws XfsException {
		service.cancelAsyncRequest(requestId);
	}

	@Override
	public Void get() throws InterruptedException, XfsException {
		synchronized (this) {
			while (!complete) {
				LOG.debug("Waiting for completion, RequestId: {}", requestId);
				wait();
			}
		}
		if (exception != null) {
			throw exception;
		}
		return null;
	}

	public RequestId getRequestId() {
		return requestId;
	}

	public ServiceId getServiceId() {
		return service.getId();
	}

	@Override
	public void fireIntermediateEvent(WfsResult wfsResult) {
		throw new UnsupportedOperationException(wfsResult.toString());
	}

	@Override
	public void fireOperationCompleteEvent(WfsResult wfsResult) {
		LOG.debug("wfsResult={}", wfsResult);
		if (wfsResult.getErrorCode() != 0) {
			exception = XfsExceptionFactory.create(wfsResult.getErrorCode());
		}
		synchronized (this) {
			complete = true;
			notifyAll();
		}
	}
}
