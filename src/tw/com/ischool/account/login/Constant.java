package tw.com.ischool.account.login;

public class Constant {
	public static final String ISCHOOL_OAUTH_ACCESS_TOKEN_URL = "https://auth.ischool.com.tw/oauth/token.php";
	public static final String CLIENT_ID = "e6228b759e6ca00c620a1f9a1171745d";
	public static final String CLIENT_SECRET = "070575826e01ae4396d244b2ebb463491c447634068657eb3cb20d01a3b96fdd";
	public static final String GREENING_URL = "https://auth.ischool.com.tw:8443/dsa/greening";
	public static final String GREENING_CONTRACT = "user";
	public static final String GREENING_SERVICE_GET_APPLICATION_LIST = "GetApplicationListRef";
	
	
	public static String getPasswordURL(String username, String password) {
		String url = "%s?grant_type=password&client_id=%s&client_secret=%s&username=%s&password=%s";

		return String.format(url, ISCHOOL_OAUTH_ACCESS_TOKEN_URL, CLIENT_ID,
				CLIENT_SECRET, username, password);
	}

	public static String getAccessTokenURL(String refreshToken) {
		String url = "%s?grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s";

		return String.format(url, ISCHOOL_OAUTH_ACCESS_TOKEN_URL, CLIENT_ID,
				CLIENT_SECRET, refreshToken);
	}
	
	public static String getExchangeTokenURL(String googleToken) {
		String url = "https://auth.ischool.com.tw/c/servicem/gtoken.php?client_id=%s&client_secret=%s&gtoken=%s";

		return String.format(url, CLIENT_ID,
				CLIENT_SECRET, googleToken);
	}
}
