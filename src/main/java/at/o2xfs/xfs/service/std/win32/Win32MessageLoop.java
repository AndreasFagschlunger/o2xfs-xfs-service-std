package at.o2xfs.xfs.service.std.win32;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.o2xfs.common.Library;
import at.o2xfs.memory.core.Address;
import at.o2xfs.xfs.service.std.MessageLoop;
import at.o2xfs.xfs.service.std.WindowProcCallback;

public class Win32MessageLoop implements MessageLoop {

	static {
		Library.loadLibrary("o2xfs-xfs-service-std");
	}

	private static final Logger LOG = LogManager.getLogger(Win32MessageLoop.class);

	private Thread thread = null;
	private Address hWnd = null;

	private native byte[] createWindow();

	private native void runLoop(WindowProcCallback callback);

	private native void close(byte[] hWnd);

	void onWindowCreated(byte[] hWnd) {
		synchronized (this) {
			this.hWnd = Address.build(hWnd);
			LOG.info("Window created: hWnd={}", hWnd);
			notifyAll();
		}
	}

	@Override
	public Address getWindowHandle() {
		return hWnd;
	}

	@Override
	public void start(WindowProcCallback callback) {
		synchronized (this) {
			if (thread == null || !thread.isAlive()) {
				thread = new Thread(() -> runLoop(callback));
				thread.start();
				hWnd = null;
			}
			try {
				while (hWnd == null) {
					wait();
				}
			} catch (InterruptedException e) {
				LOG.error("Start interrupted", e);
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void stop() {
		synchronized (this) {
			if (thread != null && thread.isAlive()) {
				close(hWnd.getValue());
				try {
					thread.join();
				} catch (InterruptedException e) {
					LOG.error("Stop interrupted", e);
					throw new RuntimeException(e);
				}
				hWnd = null;
			}
		}
	}
}
