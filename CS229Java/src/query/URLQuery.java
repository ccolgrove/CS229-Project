package query;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class URLQuery {
	
	public final String baseUrl;
	
	public URLQuery(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public URL withQueryParams(Map<String,String> queryParams) {
		StringBuilder builder = new StringBuilder(baseUrl);
		builder.append("?");
		int count = 0;
		for (String param: queryParams.keySet()) {
			if (count > 0) {
				builder.append("&");
			}
			builder.append(param);
			builder.append("=");
			builder.append(queryParams.get(param));
			count++;
		}
		
		try {
			return new URL(builder.toString());
		} catch (MalformedURLException ex) {
			return null;
		}
	}
}
