package tw.com.ischool.account.login;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.client.target.dsns.DSNSTargetURLProvider;
import ischool.dsa.client.token.ITokenProvider;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlHelper;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.dsa.utility.http.HttpUtil;
import ischool.utilities.JSONUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.w3c.dom.Element;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.AsyncTask;
import android.util.Log;

public class ConnectionHelper implements ITokenProvider {
	private String mRefreshToken;
	private String mAccessToken;
	private ContractConnection mGreening;
	private List<Accessable> mAccessables;
	private Map<String, ContractConnection> mConnections;
	private Calendar mExpireIn;
	private AccountManager mAccountManager;
	private Account mAccount;

	public ConnectionHelper(AccountManager am, Account account,
			String refreshToken, String accessToken, int expireIn,
			ContractConnection greening) {
		mAccountManager = am;
		mAccount = account;
		mConnections = new HashMap<String, ContractConnection>();
		mExpireIn = Calendar.getInstance(Locale.getDefault());
		mExpireIn.add(Calendar.SECOND, expireIn - 60);

		mRefreshToken = refreshToken;
		mAccessToken = accessToken;
		mGreening = greening;
	}

	/**
	 * 呼叫 Greening 的 service
	 * 
	 * @param serviceName
	 *            : 服務名稱
	 * @param request
	 *            : DSRequest
	 * @param listener
	 *            : 事件
	 * @param cancelable
	 *            : 取消
	 * **/
	public void callGreening(String serviceName, DSRequest request,
			OnReceiveListener<DSResponse> listener, Cancelable cancelable) {

		mGreening.sendAsyncRequest(serviceName, request, listener, cancelable);
	}

	/**
	 * 取得可連結學校
	 * 
	 * @param listener
	 *            :
	 * **/
	public void getAccessables(
			final OnReceiveListener<List<Accessable>> listener) {
		if (mAccessables != null && mAccessables.size() > 0) {
			listener.onReceive(mAccessables);
		}

		final DSRequest request = new DSRequest();
		Element content = XmlUtil.createElement("Request");
		XmlUtil.addElement(content, "Type", "dynpkg");
		request.setContent(content);
		callGreening("GetApplicationListRef", request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse response) {

						Element content = response.getContent();

						String contentString = XmlHelper.convertToString(
								content, true);
						Log.d("ConnectionHelper", "response : \n"
								+ contentString);

						mAccessables = new ArrayList<Accessable>();

						HashSet<String> set = new HashSet<String>();

						Element element = XmlUtil.selectElement(content,
								"Domain");
						for (Element item : XmlUtil.selectElements(element,
								"App")) {
							Accessable accessable = new Accessable(item);
							if (set.contains(accessable.getAccessPoint()))
								continue;

							mAccessables.add(accessable);
						}

						element = XmlUtil.selectElement(content, "User");
						for (Element item : XmlUtil.selectElements(element,
								"App")) {
							Accessable accessable = new Accessable(item);
							if (set.contains(accessable.getAccessPoint()))
								continue;

							mAccessables.add(accessable);
						}

						listener.onReceive(mAccessables);
					}

					@Override
					public void onError(Exception ex) {
						listener.onError(ex);
					}
				}, new Cancelable());
	}

	/**
	 * 準備可連結處, 這是在同步執行
	 * **/
	public void prepareAccessables() {
		final DSRequest request = new DSRequest();
		Element content = XmlUtil.createElement("Request");
		XmlUtil.addElement(content, "Type", "dynpkg");
		request.setContent(content);

		DSResponse response = mGreening.sendRequest(
				Constant.GREENING_SERVICE_GET_APPLICATION_LIST,
				new Cancelable());

		content = response.getContent();

		mAccessables = new ArrayList<Accessable>();

		HashSet<String> set = new HashSet<String>();

		Element element = XmlUtil.selectElement(content, "Domain");
		for (Element item : XmlUtil.selectElements(element, "App")) {
			Accessable accessable = new Accessable(item);
			if (set.contains(accessable.getAccessPoint()))
				continue;

			mAccessables.add(accessable);
		}

		element = XmlUtil.selectElement(content, "User");
		for (Element item : XmlUtil.selectElements(element, "App")) {
			Accessable accessable = new Accessable(item);
			if (set.contains(accessable.getAccessPoint()))
				continue;

			mAccessables.add(accessable);
		}

	}
	
	public List<Accessable> getAccessables(){
		return mAccessables;
	}

	public Account getAccount() {
		return mAccount;
	}

	@Override
	public Element getSecurityToken() {
		Element stt = XmlUtil.createElement("SecurityToken");
		stt.setAttribute("Type", "PassportAccessToken");
		XmlUtil.addElement(stt, "AccessToken", mAccessToken);

		return stt;
	}

	public void callService(final Accessable accessable, final String contract,
			final String serviceName, final DSRequest request,
			final OnReceiveListener<DSResponse> listener,
			final Cancelable cancelable) {

		callService(accessable.getAccessPoint(), contract, serviceName, request, listener, cancelable);
	}

	public ContractConnection getGreening(){
		return mGreening;
	}
	
	public void callService(final String accessable, final String contract,
			final String serviceName, final DSRequest request,
			final OnReceiveListener<DSResponse> listener,
			final Cancelable cancelable) {

		// 要呼叫 service, 先檢查一下 access token 是否過期
		if (Calendar.getInstance().after(mExpireIn)) {
			renewAccessToken(new OnReceiveListener<String>() {

				@Override
				public void onReceive(String result) {
					connectAndCall(accessable, contract, serviceName, request,
							listener, cancelable);
				}

				@Override
				public void onError(Exception ex) {
					// TODO Auto-generated method stub

				}
			});
		} else {
			connectAndCall(accessable, contract, serviceName, request,
					listener, cancelable);
		}
	}
	
	/**
	 * 更新已過期之 Access Token
	 * **/
	private void renewAccessToken(OnReceiveListener<String> listener) {
		RefreshTask task = new RefreshTask(listener);
		task.execute();
	}

	private void connectAndCall(final String accessable,
			final String contract, final String serviceName,
			final DSRequest request,
			final OnReceiveListener<DSResponse> listener,
			final Cancelable cancelable) {

		ContractConnection connection = mConnections.get(accessable);
		if (connection == null) {
			ConnectTask task = new ConnectTask(accessable, contract,
					new OnReceiveListener<ContractConnection>() {

						@Override
						public void onReceive(ContractConnection result) {
							mConnections.put(accessable, result);
							callService(result, contract, serviceName, request,
									listener, cancelable);
						}

						@Override
						public void onError(Exception ex) {
							// TODO 如果 AccessToken 過期, 應該要在這裡處理
							listener.onError(ex);

						}
					});
			task.execute(connection);
		} else {
			callService(connection, contract, serviceName, request, listener,
					cancelable);
		}
	}

	private void callService(ContractConnection connection, String contract,
			String service, DSRequest request,
			OnReceiveListener<DSResponse> listener, Cancelable cancelable) {

		// TODO 如果有 session 過期, 應該要在此處理, 所以獨立出來
		connection.sendAsyncRequest(service, request, listener, cancelable);

	}

	private void setExpireIn(int expire) {
		mExpireIn = Calendar.getInstance(Locale.getDefault());
		mExpireIn.add(Calendar.SECOND, expire - 60);
	}

	public interface OnConnectionReadyListener {
		void onFail(Exception exception);

		void onConnectionReady(ContractConnection connection);
	}

	private class ConnectTask extends
			AsyncTask<ContractConnection, Void, ContractConnection> {

		private OnReceiveListener<ContractConnection> _listener;
		private String _accesspoint;
		private String _contract;
		private Exception _exception;

		public ConnectTask(String accesspoint, String contract,
				OnReceiveListener<ContractConnection> listener) {
			_listener = listener;
			_accesspoint = accesspoint;
			_contract = contract;
		}

		@Override
		protected ContractConnection doInBackground(
				ContractConnection... params) {

			DSNSTargetURLProvider urlProvider = new DSNSTargetURLProvider(
					_accesspoint);

			ContractConnection cc = new ContractConnection(urlProvider,
					_contract);
			try {
				cc.connect(ConnectionHelper.this, true);
				return cc;
			} catch (Exception ex) {
				// 這裡應該有 502
				// "User doesn't exist"和"Server returned HTTP response code: 401 for URL"
				// 兩種認證錯誤法
				// 另外也可能發生 Code 532 Contract Not Found.
				_exception = ex;
				return null;
			}
		}

		@Override
		protected void onPostExecute(ContractConnection result) {
			if (_exception != null) {
				_listener.onError(_exception);
				return;
			}
			_listener.onReceive(result);
		}
	}

	private class RefreshTask extends AsyncTask<Void, Void, String> {
		private Exception _exception;
		private OnReceiveListener<String> _listener;

		public RefreshTask(OnReceiveListener<String> listener) {
			_listener = listener;
		}

		@Override
		protected String doInBackground(Void... params) {

			String refreshToken = mRefreshToken;

			try {

				String url = Constant.getAccessTokenURL(refreshToken);

				String resultString = HttpUtil.getString(url, new Cancelable());

				JSONObject json = JSONUtil.parseJSON(resultString);

				mAccessToken = JSONUtil.getString(json, "access_token");

				mRefreshToken = JSONUtil.getString(json, "refresh_token");

				int expire_in = JSONUtil.getInt(json, "expires_in");

				setExpireIn(expire_in);

				return mRefreshToken;
			} catch (Exception ex) {
				_exception = ex;
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (_exception != null && _listener != null) {
				_listener.onError(_exception);
				return;
			}
			_listener.onReceive(result);

			// 這邊要把 RefreshToken 寫回帳號備用
			mAccountManager.setUserData(mAccount, AccountManager.KEY_AUTHTOKEN,
					result);
		}
	}
}
