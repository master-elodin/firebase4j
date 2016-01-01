package net.thegreshams.firebase4j.service;

import static net.thegreshams.firebase4j.service.FirebaseRestMethod.DELETE;
import static net.thegreshams.firebase4j.service.FirebaseRestMethod.GET;
import static net.thegreshams.firebase4j.service.FirebaseRestMethod.PATCH;
import static net.thegreshams.firebase4j.service.FirebaseRestMethod.POST;
import static net.thegreshams.firebase4j.service.FirebaseRestMethod.PUT;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import net.thegreshams.firebase4j.model.FirebaseResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseTest {

	@Mock
	HttpResponse httpResponse;

	@Mock
	HttpEntity httpEntity;

	@Mock
	StatusLine statusLine;

	@Mock
	InputStream contentInputStream;

	@Before
	public void setup() throws Exception {
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(httpResponse.getEntity()).thenReturn(httpEntity);

		httpEntity = new HttpEntity() {

			@Override
			public void writeTo(final OutputStream outstream) throws IOException {
			}

			@Override
			public boolean isStreaming() {
				return false;
			}

			@Override
			public boolean isRepeatable() {
				return false;
			}

			@Override
			public boolean isChunked() {
				return false;
			}

			@Override
			public Header getContentType() {
				return null;
			}

			@Override
			public long getContentLength() {
				return 0;
			}

			@Override
			public Header getContentEncoding() {
				return null;
			}

			@Override
			public InputStream getContent() throws IOException, UnsupportedOperationException {
				return null;
			}

			@Override
			public void consumeContent() throws IOException {
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Test
	public void constructor_shouldAddSecureTokenAsParam_ifNotNull() throws Exception {
		String token = "token123";
		Firebase firebase = new Firebase("http://localhost", token);

		Map<String, String> query = (Map<String, String>) getField(firebase, "queryMap");
		assertThat(query.get("auth"), is(token));
	}

	@Test
	public void processResponse_sucessShouldBeTrue_ifDeleteAndStatusCode204() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(204);

		assertThat(processResponse(DELETE).isSuccess(), is(true));
	}

	@Test
	public void processResponse_sucessShouldBeFalse_ifDeleteAndStatusCodeNot204() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(200);

		assertThat(processResponse(DELETE).isSuccess(), is(false));
	}

	@Test
	public void processResponse_sucessShouldBeTrue_ifPatchAndStatusCode200() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(200);

		assertThat(processResponse(PATCH).isSuccess(), is(true));
	}

	@Test
	public void processResponse_sucessShouldBeFalse_ifPatchAndStatusCodeNot200() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(500);

		assertThat(processResponse(PATCH).isSuccess(), is(false));
	}

	@Test
	public void processResponse_sucessShouldBeTrue_ifPutAndStatusCode200() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(200);

		assertThat(processResponse(PUT).isSuccess(), is(true));
	}

	@Test
	public void processResponse_sucessShouldBeFalse_ifPutAndStatusCodeNot200() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(500);

		assertThat(processResponse(PUT).isSuccess(), is(false));
	}

	@Test
	public void processResponse_sucessShouldBeTrue_ifPostAndStatusCode200() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(200);

		assertThat(processResponse(POST).isSuccess(), is(true));
	}

	@Test
	public void processResponse_sucessShouldBeFalse_ifPostAndStatusCodeNot200() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(500);

		assertThat(processResponse(POST).isSuccess(), is(false));
	}

	@Test
	public void processResponse_sucessShouldBeTrue_ifGetAndStatusCode200() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(200);

		assertThat(processResponse(GET).isSuccess(), is(true));
	}

	@Test
	public void processResponse_sucessShouldBeFalse_ifGetAndStatusCodeNot200() throws Exception {
		when(statusLine.getStatusCode()).thenReturn(500);

		assertThat(processResponse(GET).isSuccess(), is(false));
	}

	@Test
	public void buildFullUrlFromRelativePath_shouldUseAllQueryParams() throws Exception {
		Firebase firebase = new Firebase("http://localhost");

		firebase.addQuery("query1", "parameter1");
		firebase.addQuery("query2", "parameter2");

		String fullUrl = invokeMethod(firebase, "buildFullUrlFromRelativePath", (String) null);

		assertThat(fullUrl, is("http://localhost.json?query1=parameter1&query2=parameter2"));
	}

	@Test
	public void buildFullUrlFromRelativePath_shouldSetSecureToken_withQueryParam_ifTokenNotNull() throws Exception {
		Firebase firebase = new Firebase("http://localhost", "token123");

		firebase.addQuery("query1", "parameter1");

		String fullUrl = invokeMethod(firebase, "buildFullUrlFromRelativePath", (String) null);

		assertThat(fullUrl, is("http://localhost.json?auth=token123&query1=parameter1"));
	}

	@Test
	public void buildFullUrlFromRelativePath_shouldSetSecureToken_withoutQueryParam_ifTokenNotNull() throws Exception {
		Firebase firebase = new Firebase("http://localhost", "token123");

		String fullUrl = invokeMethod(firebase, "buildFullUrlFromRelativePath", (String) null);

		assertThat(fullUrl, is("http://localhost.json?auth=token123"));
	}

	private FirebaseResponse processResponse(final FirebaseRestMethod method) {
		Firebase firebase = new Firebase("http://localhost");
		return ReflectionTestUtils.invokeMethod(firebase, "processResponse", method, httpResponse);
	}
}
