package at.o2xfs.xfs.service.std.cdm;

import java.util.Objects;

import at.o2xfs.memory.core.MemorySystem;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.cdm.CdmMessage;
import at.o2xfs.xfs.service.api.cdm.event.PresentListener;
import at.o2xfs.xfs.service.std.XfsResultFactory;

public class PresentCallback extends CdmCallback {

	private final PresentListener listener;

	public PresentCallback(MemorySystem memorySystem, XfsResultFactory factory, PresentListener listener) {
		super(memorySystem, factory);
		this.listener = Objects.requireNonNull(listener);
	}

	@Override
	public void onIntermediateEvent(WfsResult wfsResult) {
		CdmMessage msg = wfsResult.getEventId(CdmMessage.class);
		switch (msg) {
		case EXEE_INPUT_P6:
			listener.onInputP6();
			break;
		case EXEE_INFO_AVAILABLE:
			onInfoAvailable(listener, wfsResult);
			break;
		default:
			throw new IllegalArgumentException(msg.name());
		}
	}

}
