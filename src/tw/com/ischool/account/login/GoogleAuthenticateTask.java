package tw.com.ischool.account.login;

import ischool.dsa.client.OnReceiveListener;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class GoogleAuthenticateTask extends AsyncTask<String, String, String> {

	/**
	 * 
	 */
	private final Activity mActivity;

	/**
	 * @param loginHelper
	 */
	public GoogleAuthenticateTask(Activity activity) {
		mActivity = activity;
	}

	private GoogleOAuthTokenListener _listener;
	private Exception _exception;

	public void setListener(GoogleOAuthTokenListener onReceiveListener) {
		_listener = onReceiveListener;
	}

	@Override
	protected String doInBackground(String... arg0) {
		String email = arg0[0];
		String token = null;
		try {
			// https://www.googleapis.com/auth/userinfo.email

			token = GoogleAuthUtil.getToken(mActivity, email,
					"oauth2:https://www.googleapis.com/auth/userinfo.email");
		} catch (IOException transientEx) {
			_exception = transientEx;
			Log.e("IOException", transientEx.toString());
		} catch (UserRecoverableAuthException e) {
			// Recover (with e.getIntent())
			_exception = e;
			Log.e("AuthException", e.toString());
		} catch (GoogleAuthException authEx) {
			_exception = authEx;
			// The call is not ever expected to succeed
			// assuming you have already verified that
			// Google Play services is installed.
			Log.e("GoogleAuthException", authEx.toString());
		}
		return token;
	}

	@Override
	protected void onPostExecute(String token) {
		if (token != null) {
			_listener.onReceive(token);
		}
		if (_exception != null) {
			if (_exception instanceof UserRecoverableAuthException) {
				Intent intent = ((UserRecoverableAuthException) _exception).getIntent();
						
				_listener.onUserRecoverable(intent);
			} else {
				_listener.onError(_exception);
			}
		}
	}

	public interface GoogleOAuthTokenListener extends OnReceiveListener<String> {
		void onUserRecoverable(Intent intent);
	}
}