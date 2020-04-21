package at.o2xfs.xfs.service.std.cdm;

import at.o2xfs.memory.core.MemorySystem;
import at.o2xfs.memory.databind.win32.ULongWrapper;
import at.o2xfs.memory.databind.win32.UShortWrapper;
import at.o2xfs.xfs.api.RequestId;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.cdm.NoteErrorReason;
import at.o2xfs.xfs.databind.XfsEnum16Wrapper;
import at.o2xfs.xfs.service.api.cdm.event.CashUnitErrorListener;
import at.o2xfs.xfs.service.api.cdm.event.DispenseListener;
import at.o2xfs.xfs.service.api.cdm.event.IncompleteRetractListener;
import at.o2xfs.xfs.service.api.cdm.event.InfoAvailableListener;
import at.o2xfs.xfs.service.std.BaseCallback;
import at.o2xfs.xfs.service.std.XfsResultFactory;
import at.o2xfs.xfs.v3.cdm.CashUnitError3;
import at.o2xfs.xfs.v3.cdm.Denomination3;
import at.o2xfs.xfs.v3_30.cdm.IncompleteRetract330;
import at.o2xfs.xfs.v3_30.cdm.ItemInfoSummary330;

public abstract class CdmCallback extends BaseCallback {

	public CdmCallback(MemorySystem memorySystem, XfsResultFactory factory) {
		super(memorySystem, factory);
	}

	protected void onCashUnitError(CashUnitErrorListener listener, WfsResult wfsResult) {
		listener.onCashUnitError(read(wfsResult, CashUnitError3.class));
	}

	protected void onDelayedDispense(DispenseListener listener, WfsResult wfsResult) {
		listener.onDelayedDispense(read(wfsResult, ULongWrapper.class).getValue());
	}

	protected void onIncompleteDispense(DispenseListener listener, WfsResult wfsResult) {
		listener.onIncompleteDispense(read(wfsResult, Denomination3.class));
	}

	protected void onIncompleteRetract(IncompleteRetractListener listener, WfsResult wfsResult) {
		listener.onIncompleteRetract(read(wfsResult, IncompleteRetract330.class));
	}

	protected void onInfoAvailable(InfoAvailableListener listener, WfsResult wfsResult) {
		listener.onInfoAvailable(read(wfsResult, ItemInfoSummary330.class));
	}

	protected void onNoteError(DispenseListener listener, WfsResult wfsResult) {
		listener.onNoteError(read(wfsResult, XfsEnum16Wrapper.class).get(NoteErrorReason.class));
	}

	protected void onPartialDispense(DispenseListener listener, WfsResult wfsResult) {
		listener.onPartialDispense(read(wfsResult, UShortWrapper.class).getValue());
	}

	protected void onStartDispense(DispenseListener listener, WfsResult wfsResult) {
		listener.onStartDispense(read(wfsResult, RequestId.class));
	}

	protected void onSubDispenseOk(DispenseListener listener, WfsResult wfsResult) {
		listener.onSubDispenseOk(read(wfsResult, Denomination3.class));
	}
}
