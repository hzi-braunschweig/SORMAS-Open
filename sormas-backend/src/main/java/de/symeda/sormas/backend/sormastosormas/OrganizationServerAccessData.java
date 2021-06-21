package de.symeda.sormas.backend.sormastosormas;

import java.util.Objects;

import de.symeda.sormas.api.sormastosormas.ServerAccessDataReferenceDto;

public class OrganizationServerAccessData {

	private String id;
	private String name;
	private String hostName;

	public OrganizationServerAccessData() {
	}

	public OrganizationServerAccessData(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}



	public ServerAccessDataReferenceDto toReference() {
		return new ServerAccessDataReferenceDto(id, name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		OrganizationServerAccessData that = (OrganizationServerAccessData) o;
		return Objects.equals(id, that.id)
			&& Objects.equals(hostName, that.hostName)
			&& Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, hostName, name);
	}

	@Override
	public String toString() {
		return "ServerAccessDataDto{" + "id='" + id + '\'' + ", hostName='" + hostName + '\'' + ", organizationName='" + name + '\'' + '}';
	}
}
