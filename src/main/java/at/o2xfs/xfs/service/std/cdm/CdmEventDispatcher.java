package at.o2xfs.xfs.service.std.cdm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.o2xfs.memory.core.MemorySystem;
import at.o2xfs.memory.databind.win32.UShortWrapper;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.cdm.CdmMessage;
import at.o2xfs.xfs.cdm.Position;
import at.o2xfs.xfs.databind.XfsEnum16Wrapper;
import at.o2xfs.xfs.databind.XfsEnum32Wrapper;
import at.o2xfs.xfs.service.api.cdm.CdmServiceListener;
import at.o2xfs.xfs.service.std.XfsResultFactory;
import at.o2xfs.xfs.service.std.XfsStatusNotification;
import at.o2xfs.xfs.v3.cdm.CashUnit3;
import at.o2xfs.xfs.v3.cdm.CountsChanged3;
import at.o2xfs.xfs.v3.cdm.ItemPosition3;
import at.o2xfs.xfs.v3_10.cdm.DevicePosition310;
import at.o2xfs.xfs.v3_10.cdm.PowerSaveChange310;
import at.o2xfs.xfs.v3_30.cdm.ShutterStatusChanged330;

public class CdmEventDispatcher implements XfsStatusNotification {

	private static final Logger LOG = LogManager.getLogger(CdmEventDispatcher.class);

	private final MemorySystem memorySystem;
	private final XfsResultFactory factory;

	private final List<CdmServiceListener> serviceListeners;

	public CdmEventDispatcher(MemorySystem memorySystem, XfsResultFactory factory) {
		this.memorySystem = Objects.requireNonNull(memorySystem);
		this.factory = Objects.requireNonNull(factory);
		serviceListeners = new ArrayList<>();
	}

	private void fireSafeDoorOpen() {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireSafeDoorOpen()", "");
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onSafeDoorOpen();
		}
	}

	private void fireSafeDoorClosed() {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireSafeDoorClosed()", "");
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onSafeDoorClosed();
		}
	}

	private void fireCashUnitThreshold(CashUnit3 cashUnit) {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireCashUnitThreshold(CashUnit3)", cashUnit);
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onCashUnitThreshold(cashUnit);
		}
	}

	private void fireCashUnitInfoChanged(CashUnit3 cashUnit) {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireCashUnitInfoChanged(CashUnit3)", cashUnit);
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onCashUnitInfoChanged(cashUnit);
		}
	}

	private void fireTellerInfoChanged(int tellerId) {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireTellerInfoChanged(int)", tellerId);
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onTellerInfoChanged(tellerId);
		}
	}

	private void fireItemsTaken(Position position) {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireItemsTaken(Position)", position);
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onItemsTaken(position);
		}
	}

	private void fireCountsChanged(CountsChanged3 countsChanged) {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireCountsChanged(CountsChanged3)", countsChanged);
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onCountsChanged(countsChanged);
		}
	}

	private void fireItemsPresented() {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireItemsPresented()", "");
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onItemsPresented();
		}
	}

	private void fireMediaDetected(Optional<ItemPosition3> itemPosition) {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireMediaDetected(Optional<ItemPosition3>)", itemPosition);
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onMediaDetected(itemPosition);
		}
	}

	private void fireDevicePosition(DevicePosition310 devicePosition) {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireDevicePosition(DevicePosition310)", devicePosition);
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onDevicePosition(devicePosition);
		}
	}

	private void firePowerSaveChange(PowerSaveChange310 powerSaveChange) {
		if (LOG.isInfoEnabled()) {
			LOG.info("firePowerSaveChange(PowerSaveChange310)", powerSaveChange);
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onPowerSaveChange(powerSaveChange);
		}
	}

	private void fireShutterStatusChanged(ShutterStatusChanged330 shutterStatusChanged) {
		if (LOG.isInfoEnabled()) {
			LOG.info("fireShutterStatusChanged(ShutterStatusChanged330)", shutterStatusChanged);
		}
		for (CdmServiceListener each : serviceListeners) {
			each.onShutterStatusChanged(shutterStatusChanged);
		}
	}

	public void addServiceListener(CdmServiceListener listener) {
		serviceListeners.add(listener);
	}

	@Override
	public void fireServiceEvent(WfsResult wfsResult) {
		CdmMessage message = XfsEnum32Wrapper.of(wfsResult.getEventId(), CdmMessage.class);
		switch (message) {
		case SRVE_CASHUNITINFOCHANGED:
			fireCashUnitInfoChanged(factory.create(wfsResult.getBuffer(), CashUnit3.class));
			break;
		case SRVE_COUNTS_CHANGED:
			fireCountsChanged(factory.create(wfsResult.getBuffer(), CountsChanged3.class));
			break;
		case SRVE_DEVICEPOSITION:
			fireDevicePosition(factory.create(wfsResult.getBuffer(), DevicePosition310.class));
			break;
		case SRVE_ITEMSPRESENTED:
			fireItemsPresented();
			break;
		case SRVE_ITEMSTAKEN:
			fireItemsTaken(memorySystem.read(wfsResult.getBuffer(), XfsEnum16Wrapper.class).get(Position.class));
			break;
		case SRVE_MEDIADETECTED:
			Optional<ItemPosition3> itemPosition = Optional
					.ofNullable(factory.create(wfsResult.getBuffer(), ItemPosition3.class));
			fireMediaDetected(itemPosition);
			break;
		case SRVE_POWER_SAVE_CHANGE:
			firePowerSaveChange(factory.create(wfsResult.getBuffer(), PowerSaveChange310.class));
			break;
		case SRVE_SAFEDOORCLOSED:
			fireSafeDoorClosed();
			break;
		case SRVE_SAFEDOOROPEN:
			fireSafeDoorOpen();
			break;
		case SRVE_SHUTTERSTATUSCHANGED:
			fireShutterStatusChanged(factory.create(wfsResult.getBuffer(), ShutterStatusChanged330.class));
			break;
		case SRVE_TELLERINFOCHANGED:
			fireTellerInfoChanged(memorySystem.read(wfsResult.getBuffer(), UShortWrapper.class).getValue());
			break;
		default:
			throw new IllegalArgumentException(message.name());
		}
	}

	@Override
	public void fireUserEvent(WfsResult wfsResult) {
		CdmMessage message = XfsEnum32Wrapper.of(wfsResult.getEventId(), CdmMessage.class);
		switch (message) {
		case USRE_CASHUNITTHRESHOLD:
			fireCashUnitThreshold(factory.create(wfsResult.getBuffer(), CashUnit3.class));
			break;
		default:
			throw new IllegalArgumentException(message.name());
		}
	}

	public void removeServiceListener(CdmServiceListener listener) {
		serviceListeners.remove(listener);
	}
}
