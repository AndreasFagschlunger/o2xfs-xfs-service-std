package at.o2xfs.xfs.service.std.cdm;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.o2xfs.memory.core.Address;
import at.o2xfs.xfs.api.ServiceId;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.api.WfsVersion;
import at.o2xfs.xfs.api.XfsApi;
import at.o2xfs.xfs.api.XfsException;
import at.o2xfs.xfs.cdm.CdmExecuteCommand;
import at.o2xfs.xfs.cdm.CdmInfoCommand;
import at.o2xfs.xfs.cdm.Position;
import at.o2xfs.xfs.service.api.ExecuteCommand;
import at.o2xfs.xfs.service.api.InfoCommand;
import at.o2xfs.xfs.service.api.XfsFuture;
import at.o2xfs.xfs.service.api.cdm.CdmService;
import at.o2xfs.xfs.service.api.cdm.CdmServiceListener;
import at.o2xfs.xfs.service.api.cdm.event.DispenseListener;
import at.o2xfs.xfs.service.api.cdm.event.PresentListener;
import at.o2xfs.xfs.service.api.cdm.event.ResetListener;
import at.o2xfs.xfs.service.std.StdXfsService;
import at.o2xfs.xfs.v3.cdm.Capabilities3;
import at.o2xfs.xfs.v3.cdm.CashUnitInfo3;
import at.o2xfs.xfs.v3.cdm.CurrencyExp3;
import at.o2xfs.xfs.v3.cdm.Denomination3;
import at.o2xfs.xfs.v3.cdm.Dispense3;
import at.o2xfs.xfs.v3.cdm.ItemPosition3;
import at.o2xfs.xfs.v3.cdm.Status3;

public class StdCdmService extends StdXfsService implements CdmService {

	private static final Logger LOG = LogManager.getLogger(StdCdmService.class);

	private final CdmEventDispatcher eventDispatcher;

	public StdCdmService(XfsApi xfsApi, Address hWnd, ServiceId serviceId, String logicalName, WfsVersion version,
			WfsVersion spiVersion) {
		super(xfsApi, new CdmFactory(xfsApi.getMemorySystem(), version.getVersion()), hWnd, serviceId, logicalName,
				version, spiVersion);
		eventDispatcher = new CdmEventDispatcher(xfsApi.getMemorySystem(), getResultFactory());
	}

	@Override
	public void addServiceListener(CdmServiceListener listener) {
		eventDispatcher.addServiceListener(listener);
	}

	@Override
	public XfsFuture<? extends Denomination3> dispense(Dispense3 dispense, DispenseListener listener)
			throws XfsException {
		return execute(new ExecuteCommand.Builder<>(CdmExecuteCommand.DISPENSE).cmdData(dispense).build(),
				new DispenseCallback(getMemorySystem(), getResultFactory(), listener), Denomination3.class);
	}

	@Override
	public void fireServiceEvent(WfsResult wfsResult) {
		eventDispatcher.fireServiceEvent(wfsResult);
	}

	@Override
	public void fireUserEvent(WfsResult wfsResult) {
		eventDispatcher.fireUserEvent(wfsResult);
	}

	@Override
	public XfsFuture<? extends Capabilities3> getCapabilities() throws XfsException {
		return getInfo(InfoCommand.build(CdmInfoCommand.CAPABILITIES), Capabilities3.class);
	}

	@Override
	public XfsFuture<? extends CashUnitInfo3> getCashUnitInfo() throws XfsException {
		return getInfo(InfoCommand.build(CdmInfoCommand.CASH_UNIT_INFO), CashUnitInfo3.class);
	}

	@Override
	public XfsFuture<List<? extends CurrencyExp3>> getCurrencyExponents() throws XfsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XfsFuture<? extends Status3> getStatus() throws XfsException {
		return getInfo(InfoCommand.build(CdmInfoCommand.STATUS), Status3.class);
	}

	@Override
	public XfsFuture<Void> present(Position position, PresentListener listener) throws XfsException {
		return execute(new ExecuteCommand.Builder<>(CdmExecuteCommand.PRESENT).cmdData(position).build(),
				new PresentCallback(getMemorySystem(), getResultFactory(), listener), null);
	}

	@Override
	public XfsFuture<Void> reset(ResetListener listener, Optional<ItemPosition3> resetIn) throws XfsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeServiceListener(CdmServiceListener listener) {
		eventDispatcher.removeServiceListener(listener);
	}
}
