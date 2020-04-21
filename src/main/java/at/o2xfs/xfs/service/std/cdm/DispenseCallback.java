package at.o2xfs.xfs.service.std.cdm;

import at.o2xfs.memory.core.MemorySystem;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.cdm.CdmMessage;
import at.o2xfs.xfs.service.api.cdm.event.DispenseListener;
import at.o2xfs.xfs.service.api.event.IntermediateListener;
import at.o2xfs.xfs.service.std.XfsResultFactory;

public class DispenseCallback extends CdmCallback implements IntermediateListener {

	private final DispenseListener listener;

	public DispenseCallback(MemorySystem memorySystem, XfsResultFactory factory, DispenseListener listener) {
		super(memorySystem, factory);
		this.listener = listener;
	}

	@Override
	public void onIntermediateEvent(WfsResult wfsResult) {
		CdmMessage msg = wfsResult.getEventId(CdmMessage.class);
		switch (msg) {
		case EXEE_DELAYEDDISPENSE:
			onDelayedDispense(listener, wfsResult);
			break;
		case EXEE_STARTDISPENSE:
			onStartDispense(listener, wfsResult);
			break;
		case EXEE_CASHUNITERROR:
			onCashUnitError(listener, wfsResult);
			break;
		case EXEE_PARTIALDISPENSE:
			onPartialDispense(listener, wfsResult);
			break;
		case EXEE_SUBDISPENSEOK:
			onSubDispenseOk(listener, wfsResult);
			break;
		case EXEE_INCOMPLETEDISPENSE:
			onIncompleteDispense(listener, wfsResult);
			break;
		case EXEE_NOTEERROR:
			onNoteError(listener, wfsResult);
			break;
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
