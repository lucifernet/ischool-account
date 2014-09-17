package tw.com.ischool.account.login;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.target.dsns.DSNSTargetURLProvider;
import ischool.dsa.utility.http.Cancelable;
import ischool.dsa.utility.http.HttpUtil;
import ischool.utilities.JSONUtil;

import org.json.JSONObject;

import tw.com.ischool.account.R;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AccountAuthenticatorActivity {

	private static OnLoginCompletedListener sLoginCompletedListener;

	public static final int RESULT_OK = 0;
	public static final int RESULT_FAIL = -1;

	private EditText mTxtUsername;
	private EditText mTxtPassword;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_login);
		final Button done = (Button) findViewById(R.id.btnLogin);
		mTxtUsername = (EditText) findViewById(R.id.txtUserName);
		mTxtPassword = (EditText) findViewById(R.id.txtPassword);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String account = bundle.getString(LoginHelper.LOGIN_ACCOUNT);
		mTxtUsername.setText(account);
		
		done.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mTxtUsername.setError(null);
				mTxtPassword.setError(null);

				boolean error = false;
				if (mTxtUsername.length() == 0) {
					mTxtUsername.setError(LoginActivity.this
							.getString(R.string.error_empty));
				}

				if (mTxtPassword.length() == 0) {
					mTxtUsername.setError(LoginActivity.this
							.getString(R.string.error_empty));
				}

				if (error) {
					return;
				}

				String url = Constant.getPasswordURL(mTxtUsername.getText()
						.toString(), mTxtPassword.getText().toString());

				LoginTask task = new LoginTask();
				task.execute(url);

			}
		});
	}

	public static void setOnLoginCompletedListener(
			OnLoginCompletedListener listener) {
		sLoginCompletedListener = listener;
	}

	class LoginTask extends AsyncTask<String, Void, JSONObject> {

		private Exception _exception;		
		//private ProgressDialog _dialog;
		private ContractConnection _connection;
		
		public LoginTask() {
			
		}

		@Override
		protected void onPreExecute() {
//			String title = _context.getString(R.string.login_progress_title);
//			String message = _context
//					.getString(R.string.login_progress_message);
//
//			_dialog = ProgressDialog.show(_context, title, message);
			if(sLoginCompletedListener != null)
				sLoginCompletedListener.onLoginStart();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			String url = params[0];

			try {
				String username = mTxtUsername.getText().toString();
				String password = mTxtPassword.getText().toString();
				
				DSNSTargetURLProvider provider = new DSNSTargetURLProvider(Constant.GREENING_URL);				
				_connection = new ContractConnection(provider, Constant.GREENING_CONTRACT);
				_connection.connect(username, password, true);
				
				String resultString = HttpUtil.getString(url, new Cancelable());

				JSONObject json = JSONUtil.parseJSON(resultString);
				return json;
			} catch (Exception ex) {
				_exception = ex;
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject json) {

			if (_exception != null) {
				mTxtUsername.setError(LoginActivity.this
						.getString(R.string.error_login));
				
				//_dialog.dismiss();
				return;
			}

			String refreshToken = JSONUtil.getString(json, "refresh_token");
			String accessToken = JSONUtil.getString(json, "access_token");
			int expireIn = JSONUtil.getInt(json, "expires_in");
			
			// Account
			Account account = new Account(mTxtUsername.getText().toString(),
					LoginHelper.ACCOUNT_TYPE);

			Bundle userdata = new Bundle();
			userdata.putString(AccountManager.KEY_PASSWORD, mTxtPassword
					.getText().toString());
			userdata.putString(AccountManager.KEY_AUTHTOKEN, refreshToken);

			// AccountManager
			AccountManager am = AccountManager.get(LoginActivity.this);

			// 這邊如果該帳號已存在時會傳回 false
			if (am.addAccountExplicitly(account, mTxtPassword.getText()
					.toString(), userdata)) {
				Bundle result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, mTxtUsername
						.getText().toString());
				result.putString(AccountManager.KEY_AUTHENTICATOR_TYPES,
						LoginHelper.ACCOUNT_TYPE);
				result.putString(AccountManager.KEY_AUTHTOKEN, accessToken);
				setAccountAuthenticatorResult(result);

				am.setUserData(account, AccountManager.KEY_AUTHTOKEN,
						refreshToken);

				if (sLoginCompletedListener != null)
					sLoginCompletedListener.onLoginCompleted(account, refreshToken, accessToken, expireIn, _connection);
					
				// Intent data = new Intent();
				// data.putExtras(result);
				// setResult(RESULT_OK, data);
			} else {
				// 帳號已存在, 把新的 refresh token 存進帳號中
				am.setUserData(account, AccountManager.KEY_AUTHTOKEN, refreshToken);
				if (sLoginCompletedListener != null)
					sLoginCompletedListener.onLoginCompleted(account, refreshToken, accessToken, expireIn, _connection);
			}

			//_dialog.dismiss();

			finish();

		}
	}

	public interface OnLoginCompletedListener {
		void onLoginStart();
		void onLoginCompleted(Account account, String refreshToken,
				String accessToken, int expireIn, ContractConnection greening);

	}
}
