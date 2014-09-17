package tw.com.ischool.account.login;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.client.token.ITokenProvider;
import android.os.AsyncTask;

public class GreeningConnectionTask extends
		AsyncTask<String, Void, ContractConnection> {

	private OnReceiveListener<ContractConnection> _listener;
	private Exception _exception;

	public GreeningConnectionTask(OnReceiveListener<ContractConnection> listener) {
		_listener = listener;
	}

	@Override
	protected ContractConnection doInBackground(String... params) {
		try {
			String accessToken = params[0];

			ContractConnection cc = new ContractConnection(
					Constant.GREENING_URL, Constant.GREENING_CONTRACT);
			ITokenProvider tokenProvider = new AccessTokenProvider(accessToken);
			cc.connect(tokenProvider, true);

			return cc;
		} catch (Exception ex) {
			_exception = ex;
			return null;
		}
	}

	@Override
	protected void onPostExecute(ContractConnection result) {
		if (_listener == null)
			return;
		
		if (_exception == null) {
			_listener.onReceive(result);
		} else {
			_listener.onError(_exception);
		}
	}
}
