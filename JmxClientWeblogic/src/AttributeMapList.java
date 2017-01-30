/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.management.Attribute;
import java.util.List;

/**
 *
 * @author pestupinan
 */
public class AttributeMapList {

    private String name, serverName;
    private List<Attribute> attributeList;

    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public List<Attribute> getAttributeList() {
		return attributeList;
	}
	public void setAttributeList(List<Attribute> attributeList) {
		this.attributeList = attributeList;
	}
}
