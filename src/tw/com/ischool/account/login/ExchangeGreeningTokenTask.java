package tw.com.ischool.account.login;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.http.Cancelable;
import ischool.dsa.utility.http.HttpUtil;
import android.os.AsyncTask;

public class ExchangeGreeningTokenTask extends AsyncTask<String, Void, String>{

	private OnReceiveListener<String> mListener;
	private Exception mException;
	private String mClientID;
	private String mClientSec;
	
	public ExchangeGreeningTokenTask(String clientID, String clientSec){
		mClientID = clientID;
		mClientSec = clientSec;		
	}
	
	public void setListener(OnReceiveListener<String> onReceiveListener){
		mListener = onReceiveListener;
	}
	
	@Override
	protected String doInBackground(String... params) {
		String googleToken = params[0];
		String urlString = "https://auth.ischool.com.tw/c/servicem/gtoken.php?client_id=%s&client_secret=%s&gtoken=%s";
		urlString = String.format(urlString, mClientID, mClientSec, googleToken);
		String result = HttpUtil.getString(urlString, new Cancelable());
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			mListener.onReceive(result);
		}
		if(mException != null) {
			mListener.onError(mException);
		}
	}

}
