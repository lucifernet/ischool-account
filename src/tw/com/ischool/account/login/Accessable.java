package tw.com.ischool.account.login;

import java.io.Serializable;

import org.w3c.dom.Element;

public class Accessable implements Serializable{	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String _accessPoint, _user, _domain, _title, _type;
	//private ConnectionHelper _connectionHelper;

	public Accessable(Element item) {
//		Accessable = connectionHelper2;
		_accessPoint = item.getAttribute("AccessPoint");
		_user = item.getAttribute("User");
		_type = item.getAttribute("Type");
		_domain = item.getAttribute("Domain");
		_title = item.getAttribute("Title");
		//_connectionHelper = connectionHelper;
	}

	public String getAccessPoint() {
		return _accessPoint;
	}

	public String getUser() {
		return _user;
	}

	public String getDomain() {
		return _domain;
	}

	public String getTitle() {
		return _title;
	}

	public String getType() {
		return _type;
	}

//	public void callService(String contract, String serviceName,
//			DSRequest request, OnReceiveListener<DSResponse> listener,
//			Cancelable cancelable) {
//		
//		Accessable.callService(this, contract, serviceName, request,
//				listener, cancelable);
//	}
}