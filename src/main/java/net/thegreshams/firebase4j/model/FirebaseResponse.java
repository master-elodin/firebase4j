package net.thegreshams.firebase4j.model;

import static com.google.common.base.Strings.nullToEmpty;
import static net.thegreshams.firebase4j.service.FirebaseJsonUtil.convertJsonToMap;

import java.util.Map;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FirebaseResponse {

	private final boolean success;

	private final int code;

	private final String rawBody;

	private final String url;

	private Map<String, Object> body;

	public FirebaseResponse(final boolean success, final int code, final String rawBody, final String url) {
		this.success = success;
		this.code = code;
		this.rawBody = nullToEmpty(rawBody).trim();
		body = convertJsonToMap(rawBody);
		this.url = url;
	}
}
