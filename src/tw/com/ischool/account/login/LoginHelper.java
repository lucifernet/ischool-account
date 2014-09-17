package tw.com.ischool.account.login;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.OnReceiveListener;
import ischool.utilities.StringUtil;

import java.util.ArrayList;
import java.util.List;

import tw.com.ischool.account.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class LoginHelper {
	public static final String ACCOUNT_TYPE = "tw.com.ischool.account";
	public static final String LOGIN_ACCOUNT = "Login Account";

	Activity mActivity;
	Context mAppContext;
	private Dialog mDialog;
	private String mClientID;
	private String mClientSec;

	public LoginHelper(Activity activity, Context appContext, String clientID,
			String clientSec) {
		mActivity = activity;
		mAppContext = appContext;
		mClientID = clientID;
		mClientSec = clientSec;
	}

	public void login(final OnLoginListener listener) {
		final AccountManager am = AccountManager.get(mActivity);
		final Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);

		if (accounts.length == 1) {
			Account account = accounts[0];
			listener.onLoginStart();
//			onAccountReady(am, account, listener);
		} else {
			final AlertDialog.Builder adb = new AlertDialog.Builder(mActivity);
			adb.setTitle(mActivity.getString(R.string.login_dialog_title));

			ArrayList<String> names = new ArrayList<String>();
			for (Account account : accounts) {
				names.add(account.name);
			}

			String[] items = new String[names.size()];
			items = names.toArray(items);

			adb.setSingleChoiceItems(items, 0, new OnClickListener() {

				@Override
				public void onClick(DialogInterface d, int n) {
					if (mDialog != null)
						mDialog.dismiss();

					listener.onLoginStart();

					Account account = accounts[n];
//					onAccountReady(am, account, new OnLoginListener() {
//
//						@Override
//						public void onLoginError(Exception ex) {
//							listener.onLoginError(ex);
//						}
//
//						@Override
//						public void onLoginCompleted(
//								ConnectionHelper connectionHelper) {
//							listener.onLoginCompleted(connectionHelper);
//						}
//
//						@Override
//						public void onMessageChanged(String message) {
//							listener.onMessageChanged(message);
//						}
//
//						@Override
//						public void onLoginStart() {
//
//						}
//					});
				}

			});

			mDialog = adb.show();
		}
	}

	// 拿到 Google token 後向 greening 交換 token
	private void exchangeGreeningToken(String googleToken,
			final OnLoginListener listener) {
		ExchangeGreeningTokenTask task = new ExchangeGreeningTokenTask(
				mClientID, mClientSec);
		task.setListener(new OnReceiveListener<String>() {

			@Override
			public void onReceive(String result) {
				// TODO Auto-generated method stub
				listener.onMessageChanged(mActivity
						.getString(R.string.login_progress_exchange_token));
				Toast.makeText(mActivity, result, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(Exception ex) {
				// TODO Auto-generated method stub

			}
		});
		task.execute(googleToken);
	}

	

	void openLoginActivity(final Account account, final OnLoginListener listener) {
		LoginActivity
				.setOnLoginCompletedListener(new LoginActivity.OnLoginCompletedListener() {

					@Override
					public void onLoginStart() {
						if (listener instanceof OnLoginListener) {
							OnLoginListener ocListener = (OnLoginListener) listener;
							ocListener.onMessageChanged(mActivity
									.getString(R.string.login_progress_start));
						}

					}

					@Override
					public void onLoginCompleted(Account account,
							String refreshToken, String accessToken,
							int expireIn, ContractConnection greening) {

						AccountManager am = AccountManager.get(mAppContext);

						final ConnectionHelper ch = new ConnectionHelper(am,
								account, refreshToken, accessToken, expireIn,
								greening);

						listener.onMessageChanged(mActivity
								.getString(R.string.login_progress_get_accessables));

						ch.getAccessables(new OnReceiveListener<List<Accessable>>() {

							@Override
							public void onReceive(List<Accessable> result) {
								listener.onLoginCompleted(ch);
							}

							@Override
							public void onError(Exception ex) {
								listener.onLoginCompleted(ch);
							}
						});

					}
				});

		Intent intent = new Intent(mActivity, LoginActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString(LOGIN_ACCOUNT, account == null ? StringUtil.EMPTY
				: account.name);
		intent.putExtras(bundle);
		mActivity.startActivity(intent);
	}
}
