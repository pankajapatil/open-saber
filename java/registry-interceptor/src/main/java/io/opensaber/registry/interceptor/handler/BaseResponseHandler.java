package io.opensaber.registry.interceptor.handler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.opensaber.pojos.ValidationResponse;
import io.opensaber.pojos.ValidationResponseSerializer;
import io.opensaber.registry.middleware.util.Constants;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.google.gson.Gson;

import io.opensaber.pojos.Response;
import io.opensaber.pojos.ResponseParams;

/**
 * 
 * @author jyotsna
 *
 */
public class BaseResponseHandler {
	
	protected HttpServletResponse response;
	protected ResponseWrapper responseWrapper;
	protected Response formattedResponse;

	public void setResponse(HttpServletResponse response) throws IOException {
		this.response = response;
		setResponseWrapper();
	}

	public void setResponseWrapper() throws IOException {
		responseWrapper = new ResponseWrapper(response);
	}

	public void writeResponseBody(String content) throws IOException {
		//setResponseWrapper();
		responseWrapper.writeResponseBody(content);
		response = (HttpServletResponse) responseWrapper.getResponse();
	}

	public HttpServletResponse getResponse(){
		return response;
	}

	public String getResponseContent() throws IOException {
		setResponseWrapper();
		return responseWrapper.getResponseContent();
	}
	
	public void setFormattedResponse(String json){
		JSONObject jsonObj = new JSONObject(json);
		formattedResponse.setResult(jsonObj.toMap());
	}
	
	public String getFormattedResponse(){
		Gson gson = new Gson();
		return gson.toJson(formattedResponse);
	}
	
	public Map<String, Object> getResponseBodyMap() throws IOException {
		Gson gson = new Gson();
		String responseBody = getResponseContent();
		formattedResponse = gson.fromJson(responseBody, Response.class);
		return formattedResponse.getResult();
	}

	public Map<String, Object> getResponseHeaderMap() throws IOException {
		//setResponseWrapper();
		Map<String, Object> responseHeaderMap = new HashMap<String, Object>();
		Collection<String> headerNames = responseWrapper.getHeaderNames();
		if (headerNames != null) {
			for (String header : headerNames) {
				responseHeaderMap.put(header, responseWrapper.getHeader(header));
			}
		}
		return responseHeaderMap;
	}

	public void writeResponseObj(Gson gson, String errMessage) throws IOException {
		responseWrapper.setStatus(HttpStatus.OK.value());
		responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
		writeResponseBody(gson.toJson(setErrorResponse(errMessage)));
	}

	public void writeResponseObj(String errMessage, Object responseObj) throws IOException {
		responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);

		ResponseParams responseParams = new ResponseParams();
		Response response = new Response(Response.API_ID.NONE, "OK", responseParams);
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ValidationResponse.class, new ValidationResponseSerializer());
		Gson gson = builder.create();
		String result = gson.toJson(responseObj);
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		response.setResult(gson.fromJson(result, type));
		responseParams.setStatus(Response.Status.UNSUCCESSFUL);
		responseParams.setErrmsg(errMessage);

		writeResponseBody(new Gson().toJson(response));
	}

	public Response setErrorResponse(String message) {
		ResponseParams responseParams = new ResponseParams();
		Response response = new Response(Response.API_ID.NONE, "OK", responseParams);
		responseParams.setStatus(Response.Status.UNSUCCESSFUL);
		responseParams.setErrmsg(message);
		return response;
	}
}
