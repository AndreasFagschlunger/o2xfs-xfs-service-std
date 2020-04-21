package at.o2xfs.xfs.service.std;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import at.o2xfs.memory.core.Address;
import at.o2xfs.xfs.api.VersionsRequired;
import at.o2xfs.xfs.api.XfsVersion;

public final class OpenParam {

	public static class Builder {

		private final String logicalName;
		private Optional<Address> appHandle;
		private Optional<String> appId;
		private int traceLevel;
		private int timeOut;
		private VersionsRequired srvcVersionsRequired;

		public Builder(String logicalName) {
			this.logicalName = Objects.requireNonNull(logicalName);
			appHandle = Optional.empty();
			appId = Optional.empty();
			srvcVersionsRequired = VersionsRequired.build(XfsVersion.V3_00, XfsVersion.V3_30);
		}


		public Builder appHandle(Optional<Address> appHandle) {
			this.appHandle = appHandle;
			return this;
		}

		public Builder appId(Optional<String> appId) {
			this.appId = appId;
			return this;
		}

		public Builder traceLevel(int traceLevel) {
			this.traceLevel = traceLevel;
			return this;
		}

		public Builder timeOut(int timeOut) {
			this.timeOut = timeOut;
			return this;
		}

		public Builder srvcVersionsRequired(VersionsRequired srvcVersionsRequired) {
			this.srvcVersionsRequired = srvcVersionsRequired;
			return this;
		}

		public OpenParam build() {
			return new OpenParam(this);
		}

	}

	private final String logicalName;
	private final Optional<Address> appHandle;
	private final Optional<String> appId;
	private final int traceLevel;
	private final int timeOut;
	private final VersionsRequired srvcVersionsRequired;

	private OpenParam(Builder builder) {
		logicalName = builder.logicalName;
		appHandle = builder.appHandle;
		appId = builder.appId;
		traceLevel = builder.traceLevel;
		timeOut = builder.timeOut;
		srvcVersionsRequired = builder.srvcVersionsRequired;
	}

	public String getLogicalName() {
		return logicalName;
	}

	public Optional<Address> getAppHandle() {
		return appHandle;
	}

	public Optional<String> getAppId() {
		return appId;
	}

	public int getTraceLevel() {
		return traceLevel;
	}

	public int getTimeOut() {
		return timeOut;
	}

	public VersionsRequired getSrvcVersionsRequired() {
		return srvcVersionsRequired;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(logicalName).append(appHandle).append(appId).append(traceLevel)
				.append(timeOut).append(srvcVersionsRequired).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OpenParam) {
			OpenParam request = (OpenParam) obj;
			return new EqualsBuilder().append(logicalName, request.logicalName).append(appHandle, request.appHandle)
					.append(appId, request.appId).append(traceLevel, request.traceLevel)
					.append(timeOut, request.timeOut).append(srvcVersionsRequired, request.srvcVersionsRequired)
					.isEquals();
		}
		return false;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("logicalName", logicalName).append("appHandle", appHandle)
				.append("appId", appId).append("traceLevel", traceLevel).append("timeOut", timeOut)
				.append("srvcVersionsRequired", srvcVersionsRequired).toString();
	}
}
