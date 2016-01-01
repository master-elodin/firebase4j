package net.thegreshams.firebase4j.service;

import static net.thegreshams.firebase4j.service.Firebase.OBJECT_MAPPER;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.util.Strings;

@Slf4j
public class FirebaseJsonUtil {

	@SuppressWarnings("unchecked")
	public static Map<String, Object> convertJsonToMap(final String jsonData) {
		/*
		 * NOTE: @SuppressWarnings("unchecked") because Jackson docs state that
		 * a JSON-Object will always return as Map<String, Object>
		 * http://wiki.fasterxml.com/JacksonDataBinding
		 */
		Map<String, Object> jsonAsMap;
		if (Strings.isBlank(jsonData)) {
			log.warn("jsonResponse was null/empty, returning empty map; was: '{}'", jsonData);
			jsonAsMap = new HashMap<String, Object>();
		} else {
			try {
				jsonAsMap = OBJECT_MAPPER.readValue(jsonData.trim(), Map.class);
			} catch (IOException e) {
				log.error("Failed converting JSON to map: {}", jsonData);
				jsonAsMap = new HashMap<>();
			}
		}
		return jsonAsMap;
	}
}
