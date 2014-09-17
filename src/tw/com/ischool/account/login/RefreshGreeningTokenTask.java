package tw.com.ischool.account.login;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.OnProgressListener;
import ischool.dsa.client.target.dsns.DSNSTargetURLProvider;
import ischool.dsa.utility.http.Cancelable;
import ischool.dsa.utility.http.HttpUtil;
import ischool.utilities.JSONUtil;

import org.json.JSONObject;

import tw.com.ischool.account.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;

public class RefreshGreeningTokenTask extends
		AsyncTask<String, String, ConnectionData> {
	/**
	 * 
	 */
	// private final LoginHelper mLoginHelper;
	private Exception _exception;
	private OnProgressListener<ConnectionData> _listener;
	private Account _account;
	private Context _context;
	private AccountManager _am;

	public RefreshGreeningTokenTask(Context context, AccountManager am,
			Account account, OnProgressListener<ConnectionData> listener) {
		// mLoginHelper = loginHelper;
		_listener = listener;
		_account = account;
		_context = context;
		_am = am;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		_listener.onProgressUpdate(values[0]);
	}

	@Override
	protected ConnectionData doInBackground(String... params) {
		// String username = params[0];
		// String password = params[1];
		String refreshToken = params[0];

		try {

			publishProgress(_context
					.getString(R.string.login_progress_getting_info));

			String url = Constant.getAccessTokenURL(refreshToken);

			String resultString = HttpUtil.getString(url, new Cancelable());

			publishProgress(_context.getString(R.string.login_progress_parse));

			JSONObject json = JSONUtil.parseJSON(resultString);

			String accessToken = JSONUtil.getString(json, "access_token");

			// int expireIn = JSONUtil.getInt(json, "expires_in");

			refreshToken = JSONUtil.getString(json, "refresh_token");

			publishProgress(_context
					.getString(R.string.login_progress_login_greening));

			DSNSTargetURLProvider provider = new DSNSTargetURLProvider(
					Constant.GREENING_URL);
			ContractConnection greening = new ContractConnection(provider,
					Constant.GREENING_CONTRACT);

			// TODO 這裡應該要改成用 ischool oauth token 去登入 greening
			// greening.connect(username, password, true);
			greening.connect(new AccessTokenProvider(accessToken), true);

			_am.setUserData(_account, AccountManager.KEY_AUTHTOKEN,
					refreshToken);

			ConnectionData data = new ConnectionData(_account.name, json);
			data.setGreeningConnection(greening);
			// ConnectionHelper ch = new ConnectionHelper(_am, _account,
			// refreshToken, accessToken, expireIn, greening);

			// 取得可連結處
			publishProgress(_context
					.getString(R.string.login_progress_get_accessables));
			return data;
		} catch (Exception ex) {
			_exception = ex;
			return null;
		}
	}

	@Override
	protected void onPostExecute(ConnectionData connectionData) {
		// TODO 這邊發生錯誤表示登入失敗, 請使用者重新輸入帳號密碼好了
		if (_exception == null && _listener != null) {
			_listener.onReceive(connectionData);
		} else if (_exception != null && _listener != null) {
			// mLoginHelper.openLoginActivity(_account, _listener);
			_listener.onError(_exception);
		}
	}
}