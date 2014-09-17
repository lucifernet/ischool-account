package tw.com.ischool.account.login;

import org.w3c.dom.Element;

import ischool.dsa.client.token.ITokenProvider;
import ischool.dsa.utility.XmlUtil;

public class AccessTokenProvider implements ITokenProvider {
	private Element _stt;
	
	
	public AccessTokenProvider(String accessToken) {
		_stt = XmlUtil.createElement("SecurityToken");
		_stt.setAttribute("Type", "PassportAccessToken");
		XmlUtil.addElement(_stt, "AccessToken", accessToken);
	}

	@Override
	public Element getSecurityToken() {
		return _stt;
	}

}
