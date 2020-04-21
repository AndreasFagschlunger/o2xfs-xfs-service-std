package at.o2xfs.xfs.service.std;

import java.util.Objects;

import at.o2xfs.memory.core.Address;
import at.o2xfs.xfs.api.OpenResponse;
import at.o2xfs.xfs.api.XfsApi;
import at.o2xfs.xfs.service.api.XfsService;
import at.o2xfs.xfs.service.api.cdm.CdmService;
import at.o2xfs.xfs.service.std.cdm.StdCdmService;

public class XfsServiceFactory {

	private final XfsApi xfsApi;
	private final Address hWnd;

	public XfsServiceFactory(XfsApi xfsApi, Address hWnd) {
		this.xfsApi = Objects.requireNonNull(xfsApi);
		this.hWnd = Objects.requireNonNull(hWnd);
	}

	public <E extends XfsService> StdXfsService create(String logicalName, Class<E> serviceClass,
			OpenResponse response) {
		StdXfsService result;
		if (CdmService.class.equals(serviceClass)) {
			result = new StdCdmService(xfsApi, hWnd, response.getServiceId(), logicalName, response.getSrvcVersion(),
					response.getSpiVersion());
		} else {
			throw new IllegalArgumentException(serviceClass.toString());
		}
		return result;
	}
}
