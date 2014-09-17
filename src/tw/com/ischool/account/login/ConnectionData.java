package tw.com.ischool.account.login;

import ischool.dsa.client.ContractConnection;
import ischool.utilities.JSONUtil;

import java.io.Serializable;

import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class ConnectionData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String _ischoolAccountName;
	private String _accessToken;
	private String _refreshToken;
	private ContractConnection _greening;
	private int _expiredIn;

	public ConnectionData(String ischoolAccountName, JSONObject greeningToken) {
		_ischoolAccountName = ischoolAccountName;

		_refreshToken = JSONUtil.getString(greeningToken, "refresh_token");
		_accessToken = JSONUtil.getString(greeningToken, "access_token");
		_expiredIn = JSONUtil.getInt(greeningToken, "expires_in");
	}

	public String getIschoolAccountName() {
		return _ischoolAccountName;
	}

	public String getAccessToken() {
		return _accessToken;
	}

	public String getRefreshToken() {
		return _refreshToken;
	}

	public int getExpiredIn() {
		return _expiredIn;
	}

	public void setGreeningConnection(ContractConnection connection) {
		_greening = connection;
	}

	public ContractConnection getGreeningConnection() {
		return _greening;
	}

	public ConnectionHelper createConnectionHelper(Context context) {
		AccountManager am = AccountManager.get(context);

		final Account[] ischoolAccounts = am
				.getAccountsByType(LoginHelper.ACCOUNT_TYPE);

		ConnectionHelper chelper = null;
		for (Account account : ischoolAccounts) {
			if (!account.name.equals(_ischoolAccountName))
				continue;

			chelper = new ConnectionHelper(am, account, _refreshToken,
					_accessToken, _expiredIn, _greening);
			break;
		}
		return chelper;
	}

}
