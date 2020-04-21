package at.o2xfs.xfs.service.std;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

import at.o2xfs.memory.core.Address;
import at.o2xfs.xfs.api.WfsResult;
import at.o2xfs.xfs.api.XfsMessage;

public final class XfsEvent {

	private final XfsMessage message;
	private final Address address;
	private final WfsResult wfsResult;

	private XfsEvent(XfsMessage message, Address address, WfsResult wfsResult) {
		this.message = Objects.requireNonNull(message);
		this.address = Objects.requireNonNull(address);
		this.wfsResult = Objects.requireNonNull(wfsResult);
	}

	public XfsMessage getMessage() {
		return message;
	}

	public Address getAddress() {
		return address;
	}

	public WfsResult getWfsResult() {
		return wfsResult;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("message", message).append("address", address)
				.append("wfsResult", wfsResult).toString();
	}

	public static XfsEvent build(XfsMessage message, Address address, WfsResult wfsResult) {
		return new XfsEvent(message, address, wfsResult);
	}
}
