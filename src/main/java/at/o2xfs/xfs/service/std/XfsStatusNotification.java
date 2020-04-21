package at.o2xfs.xfs.service.std;

import at.o2xfs.xfs.api.WfsResult;

public interface XfsStatusNotification {

	public void fireServiceEvent(WfsResult wfsResult);

	public void fireUserEvent(WfsResult wfsResult);
}
