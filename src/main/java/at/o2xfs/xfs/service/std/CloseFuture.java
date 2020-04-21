package at.o2xfs.xfs.service.std;

import java.util.Objects;

import at.o2xfs.xfs.api.RequestId;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.api.XfsExceptionFactory;
import at.o2xfs.xfs.service.api.XfsFuture;

public class CloseFuture implements XfsFuture<Void>, XfsEventNotification {

	private final StdXfsService service;
	private RequestId requestId;
	private XfsFuture<Void> closeFuture;
	private XfsException exception;

	public CloseFuture(StdXfsService service, RequestId requestId) {
		this.service = Objects.requireNonNull(service);
		this.requestId = Objects.requireNonNull(requestId);
	}

	@Override
	public void cancel() throws XfsException {
		service.cancelAsyncRequest(requestId);
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
				closeFuture = service.getDispatcher().close();
			} catch (XfsException e) {
				exception = e;
			}
		}
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public Void get() throws InterruptedException, XfsException {
		synchronized (this) {
			if (exception == null && closeFuture == null) {
				wait();
			}
		}
		if (exception != null) {
			throw exception;
		}
		return closeFuture.get();
	}
}
