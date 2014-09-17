package tw.com.ischool.account.login;

public interface OnLoginListener {
	void onLoginCompleted(ConnectionHelper connectionHelper);

	void onMessageChanged(String message);

	void onLoginStart();
	
	void onLoginError(Exception ex);
}
