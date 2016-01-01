package net.thegreshams.firebase4j.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.nullToEmpty;
import static net.thegreshams.firebase4j.service.FirebaseJsonUtil.convertJsonToMap;
import static net.thegreshams.firebase4j.service.FirebaseRestMethod.DELETE;
import static net.thegreshams.firebase4j.service.FirebaseRestMethod.GET;
import static net.thegreshams.firebase4j.service.FirebaseRestMethod.PATCH;
import static net.thegreshams.firebase4j.service.FirebaseRestMethod.POST;
import static net.thegreshams.firebase4j.service.FirebaseRestMethod.PUT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.thegreshams.firebase4j.model.FirebaseResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.Strings;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;

@ToString(of = "baseUrl")
@Slf4j
public class Firebase {

	public static final String FIREBASE_API_JSON_EXTENSION = ".json";

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final HttpClient HTTP_CLIENT = HttpClients.createDefault();

	private final String baseUrl;

	private final Map<String, String> queryMap = new HashMap<>();

	private final Map<String, Object> dataMap = new HashMap<>();

	private final StringBuffer jsonData = new StringBuffer();

	public Firebase(final String baseUrl) {
		this(baseUrl, null);
	}

	public Firebase(final String baseUrl, final String secureToken) {
		checkArgument(Strings.isNotBlank(baseUrl), "baseUrl cannot be null or empty; was: '" + baseUrl + "'");
		if (secureToken != null) {
			queryMap.put("auth", secureToken);
		}
		this.baseUrl = baseUrl.trim() + (!baseUrl.trim().endsWith("/") ? "/" : "");
		log.debug("intialized with base URL: " + this.baseUrl);
	}

	/**
	 * Append a query to the request.
	 *
	 * @param query
	 *            Query string based on Firebase REST API
	 * @param parameter
	 *            Query parameter
	 * @return this Firebase object
	 * @throws UnsupportedEncodingException
	 *             if parameter cannot be encoded to UTF-8
	 */
	public Firebase addQuery(final String query, final String parameter) throws UnsupportedEncodingException {
		queryMap.put(query, URLEncoder.encode(parameter, "UTF-8"));
		return this;
	}

	/**
	 * Add data to the request
	 *
	 * @param key
	 *            Data key
	 * @param value
	 *            Data value
	 * @return this Firebase object
	 */
	public Firebase addData(final String key, final Object value) {
		dataMap.put(key, value);
		return this;
	}

	/**
	 * Add data to the request
	 *
	 * @param jsonData
	 *            Data to add as a JSON string
	 * @return this Firebase object
	 */
	public Firebase addData(final String jsonData) {
		this.jsonData.append(jsonData);
		return this;
	}

	/**
	 * GETs data from the base URL
	 *
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse get() throws IOException {
		return get(null);
	}

	/**
	 * GETs data from the provided path relative to the base URL
	 *
	 * @param path
	 *            Path relative to base URL. If null/empty, refers to the base
	 *            URL
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse get(final String path) throws IOException {
		return requestResponse(GET, path, new HttpGet());
	}

	/**
	 * PATCHs data to the URL
	 *
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse patch() throws IOException {
		return patch(null);
	}

	/**
	 *
	 * @param path
	 *            Path relative to base URL. If null/empty, refers to the base
	 *            URL
	 * @param jsonData
	 *            Data to PATCH to the URL
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse patch(final String path) throws IOException {
		return requestResponse(PATCH, path, new HttpPatch());
	}

	/**
	 * PUTs data to the base URL (ie: creates or overwrites). If there is
	 * already data at the base URL, this data overwrites it. If data is
	 * null/empty, any data existing at the base URL is deleted.
	 *
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse put() throws IOException {
		return put(null);
	}

	/**
	 * PUTs data to the provided-path relative to the base URL (ie: creates or
	 * overwrites). If there is already data at the path, this data overwrites
	 * it. If data is null/empty, any data existing at the path is deleted.
	 *
	 * @param path
	 *            Path relative to base URL. If null/empty, refers to the base
	 *            URL
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse put(final String path) throws IOException {
		return requestResponse(PUT, path, new HttpPut());
	}

	/**
	 * POSTs data to the base URL (ie: creates).
	 *
	 * NOTE: the Firebase API does not treat this method in the conventional
	 * way, but instead defines it as 'PUSH'; the API will insert this data
	 * under the base URL but associated with a Firebase- generated key; thus,
	 * every use of this method will result in a new insert even if the data
	 * already exists.
	 *
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse post() throws IOException {
		return post(null);
	}

	/**
	 * POSTs data to the provided-path relative to the base URL (ie: creates).
	 *
	 * NOTE: the Firebase API does not treat this method in the conventional
	 * way, but instead defines it as 'PUSH'; the API will insert this data
	 * under the provided path but associated with a Firebase- generated key;
	 * thus, every use of this method will result in a new insert even if the
	 * provided path and data already exist.
	 *
	 * @param path
	 *            Path relative to base URL. If null/empty, refers to the base
	 *            URL
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse post(final String path) throws IOException {
		return requestResponse(POST, path, new HttpPost());
	}

	/**
	 * DELETEs data from the provided-path relative to the base URL.
	 *
	 * @param path
	 *            Path relative to base URL. If null/empty, refers to the base
	 *            URL
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse delete() throws IOException {
		return delete(null);
	}

	/**
	 * DELETEs data from the provided-path relative to the base URL.
	 *
	 * @param path
	 *            Path relative to base URL. If null/empty, refers to the base
	 *            URL
	 * @return {@link FirebaseResponse}
	 * @throws IOException
	 *             if ClientProtocolException executing Rest request or
	 *             ParseException while converting response content to string
	 */
	public FirebaseResponse delete(final String path) throws IOException {
		return requestResponse(DELETE, path, new HttpDelete());
	}

	private FirebaseResponse requestResponse(final FirebaseRestMethod restMethod, final String path,
	        final HttpRequestBase requestWithEntity) throws IOException {
		String url = buildFullUrlFromRelativePath(path);
		requestWithEntity.setURI(URI.create(url));
		if (requestWithEntity instanceof HttpEntityEnclosingRequestBase) {
			combineData();
			((HttpEntityEnclosingRequestBase) requestWithEntity).setEntity(new StringEntity(jsonData.toString()));
		}
		return processResponse(restMethod, HTTP_CLIENT.execute(requestWithEntity), url);
	}

	private void combineData() throws IOException {
		dataMap.putAll(convertJsonToMap(jsonData.toString()));
		jsonData.setLength(0);
		jsonData.append(OBJECT_MAPPER.writeValueAsString(dataMap));
	}

	private String buildFullUrlFromRelativePath(final String path) {

		String tempPath = nullToEmpty(path).trim();
		if (tempPath.startsWith("/")) {
			tempPath = tempPath.substring(1);
		}
		String url = String.format("%s%s%s?%s", baseUrl, tempPath, FIREBASE_API_JSON_EXTENSION,
				Joiner.on("&").withKeyValueSeparator("=").join(queryMap));

		log.debug("built full url to '{}' using relative-path of '{}'", url, path);

		return url;
	}

	private FirebaseResponse processResponse(final FirebaseRestMethod method, final HttpResponse httpResponse,
	        final String url) throws IOException {
		int code = httpResponse.getStatusLine().getStatusCode();

		boolean success = false;
		switch (method) {
		case DELETE:
			if (code == 204) {
				success = true;
			}
			break;
		case PATCH:
		case PUT:
		case POST:
		case GET:
			if (code == 200) {
				success = true;
			}
			break;
		default:
			break;
		}

		// Clear all data
		queryMap.clear();
		dataMap.clear();
		jsonData.setLength(0);

		return new FirebaseResponse(success, code, EntityUtils.toString(httpResponse.getEntity()), url);
	}
}
