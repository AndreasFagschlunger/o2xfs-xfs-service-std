package at.o2xfs.xfs.service.std.cdm;

import java.util.Objects;

import at.o2xfs.memory.core.Address;
import at.o2xfs.memory.core.MemorySystem;
import at.o2xfs.xfs.api.XfsVersion;
import at.o2xfs.xfs.service.std.XfsResultFactory;
import at.o2xfs.xfs.v3.cdm.Calibrate3;
import at.o2xfs.xfs.v3.cdm.Capabilities3;
import at.o2xfs.xfs.v3.cdm.CashUnit3;
import at.o2xfs.xfs.v3.cdm.CashUnitError3;
import at.o2xfs.xfs.v3.cdm.CashUnitInfo3;
import at.o2xfs.xfs.v3.cdm.Count3;
import at.o2xfs.xfs.v3.cdm.CountsChanged3;
import at.o2xfs.xfs.v3.cdm.CurrencyExp3;
import at.o2xfs.xfs.v3.cdm.Denomination3;
import at.o2xfs.xfs.v3.cdm.ItemPosition3;
import at.o2xfs.xfs.v3.cdm.MixTable3;
import at.o2xfs.xfs.v3.cdm.MixType3;
import at.o2xfs.xfs.v3.cdm.PresentStatus3;
import at.o2xfs.xfs.v3.cdm.Status3;
import at.o2xfs.xfs.v3.cdm.TellerDetails3;
import at.o2xfs.xfs.v3.cdm.TellerInfo3;
import at.o2xfs.xfs.v3_10.cdm.Capabilities310;
import at.o2xfs.xfs.v3_10.cdm.CashUnit310;
import at.o2xfs.xfs.v3_10.cdm.CashUnitError310;
import at.o2xfs.xfs.v3_10.cdm.CashUnitInfo310;
import at.o2xfs.xfs.v3_10.cdm.DevicePosition310;
import at.o2xfs.xfs.v3_10.cdm.PowerSaveChange310;
import at.o2xfs.xfs.v3_10.cdm.Status310;
import at.o2xfs.xfs.v3_20.cdm.Capabilities320;
import at.o2xfs.xfs.v3_20.cdm.ItemNumberList320;
import at.o2xfs.xfs.v3_20.cdm.Status320;
import at.o2xfs.xfs.v3_30.cdm.AllItemsInfo330;
import at.o2xfs.xfs.v3_30.cdm.Blacklist330;
import at.o2xfs.xfs.v3_30.cdm.Capabilities330;
import at.o2xfs.xfs.v3_30.cdm.IncompleteRetract330;
import at.o2xfs.xfs.v3_30.cdm.ItemInfo330;
import at.o2xfs.xfs.v3_30.cdm.ItemInfoSummary330;
import at.o2xfs.xfs.v3_30.cdm.ShutterStatusChanged330;

public class CdmFactory implements XfsResultFactory {

	private final MemorySystem memorySystem;
	private final XfsVersion version;

	public CdmFactory(MemorySystem memorySystem, XfsVersion version) {
		this.memorySystem = Objects.requireNonNull(memorySystem);
		this.version = Objects.requireNonNull(version);

	}

	@Override
	public <T> T create(Address address, Class<T> type) {
		Object result = null;
		if (AllItemsInfo330.class.equals(type)) {
			result = memorySystem.read(address, AllItemsInfo330.class);
		} else if (Blacklist330.class.equals(type)) {
			result = memorySystem.read(address, Blacklist330.class);
		} else if (Calibrate3.class.equals(type)) {
			result = memorySystem.read(address, Calibrate3.class);
		} else if (CashUnit3.class.equals(type)) {
			result = createCashUnit(address);
		} else if (CashUnitError3.class.equals(type)) {
			result = createCashUnitError(address);
		} else if (CashUnitInfo3.class.equals(type)) {
			result = createCashUnitInfo(address);
		} else if (Capabilities3.class.equals(type)) {
			result = createCapabilities(address);
		} else if (Count3.class.equals(type)) {
			result = memorySystem.read(address, Count3.class);
		} else if (CountsChanged3.class.equals(type)) {
			result = memorySystem.read(address, CountsChanged3.class);
		} else if (CurrencyExp3.class.equals(type)) {
			result = memorySystem.read(address, CurrencyExp3.class);
		} else if (Denomination3.class.equals(type)) {
			result = memorySystem.read(address, Denomination3.class);
		} else if (DevicePosition310.class.equals(type)) {
			result = memorySystem.read(address, DevicePosition310.class);
		} else if (IncompleteRetract330.class.equals(type)) {
			result = memorySystem.read(address, IncompleteRetract330.class);
		} else if (ItemInfo330.class.equals(type)) {
			result = memorySystem.read(address, ItemInfo330.class);
		} else if (ItemInfoSummary330.class.equals(type)) {
			result = memorySystem.read(address, ItemInfoSummary330.class);
		} else if (ItemNumberList320.class.equals(type)) {
			result = memorySystem.read(address, ItemNumberList320.class);
		} else if (ItemPosition3.class.equals(type)) {
			result = memorySystem.read(address, ItemPosition3.class);
		} else if (MixTable3.class.equals(type)) {
			result = memorySystem.read(address, MixTable3.class);
		} else if (MixType3.class.equals(type)) {
			result = memorySystem.read(address, MixType3.class);
		} else if (PowerSaveChange310.class.equals(type)) {
			result = memorySystem.read(address, PowerSaveChange310.class);
		} else if (PresentStatus3.class.equals(type)) {
			result = memorySystem.read(address, PresentStatus3.class);
		} else if (ShutterStatusChanged330.class.equals(type)) {
			result = memorySystem.read(address, ShutterStatusChanged330.class);
		} else if (Status3.class.equals(type)) {
			result = createStatus(address);
		} else if (TellerDetails3.class.equals(type)) {
			result = memorySystem.read(address, TellerDetails3.class);
		} else if (TellerInfo3.class.equals(type)) {
			result = memorySystem.read(address, TellerInfo3.class);
		} else {
			result = memorySystem.read(address, type);
		}
		return type.cast(result);
	}

	private CashUnit3 createCashUnit(Address address) {
		CashUnit3 result;
		if (version.compareTo(XfsVersion.V3_10) >= 0) {
			result = memorySystem.read(address, CashUnit310.class);
		} else {
			result = memorySystem.read(address, CashUnit3.class);
		}
		return result;
	}

	private CashUnitError3 createCashUnitError(Address address) {
		CashUnitError3 result;
		if (version.compareTo(XfsVersion.V3_10) >= 0) {
			result = memorySystem.read(address, CashUnitError310.class);
		} else {
			result = memorySystem.read(address, CashUnitError3.class);
		}
		return result;
	}

	private CashUnitInfo3 createCashUnitInfo(Address address) {
		CashUnitInfo3 result;
		if (version.compareTo(XfsVersion.V3_10) >= 0) {
			result = memorySystem.read(address, CashUnitInfo310.class);
		} else {
			result = memorySystem.read(address, CashUnitInfo3.class);
		}
		return result;
	}

	private Capabilities3 createCapabilities(Address address) {
		Capabilities3 result;
		if (version.compareTo(XfsVersion.V3_30) >= 0) {
			result = memorySystem.read(address, Capabilities330.class);
		} else if (version.compareTo(XfsVersion.V3_20) >= 0) {
			result = memorySystem.read(address, Capabilities320.class);
		} else if (version.compareTo(XfsVersion.V3_10) >= 0) {
			result = memorySystem.read(address, Capabilities310.class);
		} else {
			result = memorySystem.read(address, Capabilities3.class);
		}
		return result;
	}

	private Status3 createStatus(Address address) {
		Status3 result;
		if (version.compareTo(XfsVersion.V3_20) >= 0) {
			result = memorySystem.read(address, Status320.class);
		} else if (version.compareTo(XfsVersion.V3_10) >= 0) {
			result = memorySystem.read(address, Status310.class);
		} else {
			result = memorySystem.read(address, Status3.class);
		}
		return result;
	}
}
