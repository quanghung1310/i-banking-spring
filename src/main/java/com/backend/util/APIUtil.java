package com.backend.util;

import com.backend.Application;
import com.backend.dto.APIInfor;
import com.backend.dto.APIParameter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.codec.BodyCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIUtil {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String CONTENT_TYPE_XXX_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private static final String JAVA_LANG_OBJECT = "java.lang.Object";

    private static final int PARAM_GET_FROM_API = -1;
    private static final int PARAM_HEADER = 0;
    private static final int PARAM_NORMAL = 1;
    private static final int PARAM_LINK_VALUE = 2;
    private static final int PARAM_LINK_QUERY = 3;
    private static final int RETURN_MAPPING = 4;
    private static final int PARAM_FILE = 5;
    private static final int PARAM_BASIC_AUTH = 6;

    /** application/json **/
    private static final int BODY_TYPE_APPLICATION_JSON = 0;
    /** application/x-www-form-urlencoded **/
    private static final int BODY_TYPE_XXX_FORM_URLENCODED = 1;
    /** text **/
    private static final int BODY_TYPE_TEXT = 2;
    /** multipart **/
    private static final int BODY_TYPE_MULTIPART = 3;
    /** application/json no mapping **/
    private static final int BODY_TYPE_APPLICATION_JSON_NO_MAPPING = 4;
    /** text no mapping **/
    private static final int BODY_TYPE_TEXT_NO_MAPPING = 5;

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    private String callPostAPI(APIInfor api, Object data) {
        try {
            APIInfor tempApi;
            String jsonInString = null;
            if (api == null) {
                return null;
            }
            if (data instanceof List) {
                return null;
            } else {
                Map<String, Object> mpData = mapper.convertValue(data, Map.class);
                // https://partner.com/api
                StringBuilder sbAPILink = new StringBuilder(api.getApiLink());
                List<APIParameter> lstParamMapping = api.getParams(PARAM_LINK_VALUE, null);
                for (APIParameter item : lstParamMapping) {
                    // https://partner.com/api/param1/param2
                    sbAPILink.append("/").append(mpData.get(item.getKey()));
                }
                lstParamMapping = api.getParams(PARAM_LINK_QUERY, null);
                if (lstParamMapping != null && !lstParamMapping.isEmpty()) {
                    sbAPILink.append("?");
                    for (APIParameter item : lstParamMapping) {
                        if (StringUtils.isNotBlank(item.getValue())) {
                            sbAPILink.append(item.getInputKey()).append("=")
                                    .append(URLEncoder.encode(item.getValue(), StandardCharsets.UTF_8.name())).append("&");
                        } else {
                            if (mpData.get(item.getKey()) != null) {
                                sbAPILink.append(item.getInputKey()).append("=")
                                        .append(URLEncoder.encode(String.valueOf(mpData.get(item.getKey())), StandardCharsets.UTF_8.name())).append("&");
                            }
                        }
                    }
                    // https://partner.com/api/param1/param2?key1=value1&key2=value2
                    sbAPILink.deleteCharAt(sbAPILink.length() - 1);
                }
                if (StringUtils.isNotBlank(api.getLinkPrefix())) {
                    // https://partner.com/api.json
                    sbAPILink.append(api.getLinkPrefix());
                }
                MultiMap headers = MultiMap.caseInsensitiveMultiMap();
                lstParamMapping = api.getParams(PARAM_HEADER, null);
                for (APIParameter item : lstParamMapping) {
                    if (StringUtils.isNotBlank(item.getValue())) {
                        if (item.getParentId() == null || item.getParentId() != PARAM_GET_FROM_API) {
                            headers.add(item.getInputKey(), item.getValue());
                        } else {
//                            tempApi = apiRepo.findByApilinkAndCompanyid(item.getValue(), companyId);
                            // TODO
                            tempApi = new APIInfor();
                            if (tempApi != null) {
                                String strData = null;
                                if ("POST".equals(tempApi.getType())) {
                                    strData = callPostAPI(tempApi, new HashMap<String, Object>());
                                } else if ("GET".equals(tempApi.getType())) {
                                    strData = callGetAPI(tempApi, new HashMap<String, Object>());
                                }
                                strData = (String) mappingResponse(tempApi, strData);
                                headers.add(item.getInputKey(), strData);
                            }
                        }
                    } else {
                        Object obj = mpData.get(item.getKey());
                        if (obj != null) {
                            headers.add(item.getInputKey(), obj.toString());
                        }
                    }
                }

                HttpRequest<Buffer> request = Application.webClient
                        .postAbs(sbAPILink.toString())
                        .putHeaders(headers)
                        .timeout(60000);

                // set username (InputKey) and pass (Value)
                lstParamMapping = api.getParams(PARAM_BASIC_AUTH, null);
                if (!lstParamMapping.isEmpty()) {
                    request.basicAuthentication(lstParamMapping.get(0).getInputKey(), lstParamMapping.get(0).getValue());
                }

                Map<String, Object> mpInputData = buildRequestBody(api, mpData);
                Object requestBody;
                String contentType = CONTENT_TYPE_APPLICATION_JSON;
                // TODO
                switch (api.getBodyDatatype()) {
                    case BODY_TYPE_APPLICATION_JSON:
                        if (StringUtils.isNotBlank(api.getParamKey())) {
                            Map<String, Object> mpWrapper = new HashMap<>();
                            mpWrapper.put(api.getParamKey(), mpInputData);
                            jsonInString = mapper.writeValueAsString(mpWrapper);
                        } else {
                            jsonInString = mapper.writeValueAsString(mpInputData);
                        }
//                        method.setEntity(new StringEntity(jsonInString, ContentType.create(APPLICATION_JSON, UTF_8)));
                        requestBody = new JsonObject(jsonInString);
                        break;
                    case BODY_TYPE_XXX_FORM_URLENCODED:
//                        List<BasicNameValuePair> params = new ArrayList<>();
                        MultiMap params = MultiMap.caseInsensitiveMultiMap();
                        for (String key : mpInputData.keySet()) {
                            Object obj = mpInputData.get(key);
                            if (obj instanceof Map || obj instanceof List) {
                                params.add(key, mapper.writeValueAsString(obj));
//                                params.add(new BasicNameValuePair(key, mapper.writeValueAsString(obj)));
                            } else {
                                params.add(key, String.valueOf(obj));
//                                params.add(new BasicNameValuePair(key, String.valueOf(obj)));
                            }
                        }
//                        method.setEntity(new UrlEncodedFormEntity(params, UTF_8));
                        requestBody = params;
                        contentType = CONTENT_TYPE_XXX_FORM_URLENCODED;
                        break;
                    case BODY_TYPE_TEXT:
//                        MultipartEntityBuilder entity = MultipartEntityBuilder.create();
//                        Charset chars = Charset.forName(UTF_8);
//                        entity.setCharset(chars);
                        if (StringUtils.isNotBlank(api.getParamKey())) {
                            Map<String, Object> mpWrapper = new HashMap<>();
                            mpWrapper.put(api.getParamKey(), mpInputData);
                            jsonInString = mapper.writeValueAsString(mpWrapper);
                        } else {
                            jsonInString = mapper.writeValueAsString(mpInputData);
                        }
//                        method.setEntity(new StringEntity(jsonInString, ContentType.create(TEXT_PLAIN, UTF_8)));
                        requestBody = jsonInString;
                        contentType = CONTENT_TYPE_TEXT_PLAIN;
                        break;
                    case BODY_TYPE_MULTIPART:
                        // TODO not supported
//                        MultipartEntityBuilder entity = MultipartEntityBuilder.create();
//                        Charset chars = Charset.forName(UTF_8);
//                        entity.setCharset(chars);
//                        entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                        lstParamMapping = api.getParams(PARAM_FILE, null);
                        for (APIParameter item : lstParamMapping) {
                            if (StringUtils.isNotBlank(item.getValue())) {
//                                entity.addBinaryBody(item.getInputkey(), new File(item.getValue()));
                            }
                        }
                        while (mpInputData.keySet().iterator().hasNext()) {
                            String key = mpInputData.keySet().iterator().next();
//                            entity.addPart(key, new StringBody(mapper.writeValueAsString(mpInputData.get(key)), ContentType.TEXT_PLAIN));
                        }
//                        method.setEntity(entity.build());
                        contentType = CONTENT_TYPE_MULTIPART_FORM_DATA;
                        break;
                    case BODY_TYPE_APPLICATION_JSON_NO_MAPPING:
//                        mpInputData.putAll(mpData);
                        if (StringUtils.isNotBlank(api.getParamKey())) {
                            Map<String, Object> mpWrapper = new HashMap<>();
                            mpWrapper.put(api.getParamKey(), mpData);
                            jsonInString = mapper.writeValueAsString(mpWrapper);
                        } else {
                            jsonInString = mapper.writeValueAsString(mpData);
                        }
//                        method.setEntity(new StringEntity(jsonInString, ContentType.create(APPLICATION_JSON, UTF_8)));
                        requestBody = new JsonObject(jsonInString);
                        break;
                    case BODY_TYPE_TEXT_NO_MAPPING:
//                        mpInputData.putAll(mpData);
//                        MultipartEntityBuilder entity = MultipartEntityBuilder.create();
//                        Charset chars = Charset.forName(StandardCharsets.UTF_8.name());
//                        entity.setCharset(chars);
                        if (StringUtils.isNotBlank(api.getParamKey())) {
                            Map<String, Object> mpWrapper = new HashMap<>();
                            mpWrapper.put(api.getParamKey(), mpData);
                            jsonInString = mapper.writeValueAsString(mpWrapper);
                        } else {
                            jsonInString = mapper.writeValueAsString(mpData);
                        }
//                        method.setEntity(new StringEntity(jsonInString, ContentType.create(TEXT_PLAIN, UTF_8)));
                        requestBody = jsonInString;
                        contentType = CONTENT_TYPE_TEXT_PLAIN;
                        break;
                }

                try {
                    LOGGER.info("CALL COMMON POST API, URL: {}", sbAPILink.toString());
                    StringBuilder headerStr = new StringBuilder();
                    for (String header : headers.names()) {
                        headerStr.append("(").append(header).append(":").append(headers.get(header)).append(")");
                    }
                    LOGGER.info("HEADERS: {}", headerStr.toString());
                    LOGGER.info("JSON STRING: {}", jsonInString);
                } catch (Exception e) {
                    LOGGER.error("Write log error!", e);
                }

//                HttpResponse response = client.execute(method);
//                String result = EntityUtils.toString(response.getEntity());
//
//                logger.info("JSON STRING OUT: " + result);
//                return result;
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }

    private String callGetAPI(APIInfor api, Object data) {
//        HttpClient client = HttpClientBuilder.create().build();
        APIInfor tempApi;
        try {
            if (api == null) {
                return null;
            }
            if (data instanceof List) {
                return null;
            }
            Map<String, Object> mpData = mapper.convertValue(data, Map.class);
            StringBuilder sbAPILink = new StringBuilder(api.getApiLink());
            List<APIParameter> lstParamMapping = api.getParams(PARAM_LINK_VALUE, null);
            for (APIParameter item : lstParamMapping) {
                sbAPILink.append("/").append(mpData.get(item.getKey()));
            }
            lstParamMapping = api.getParams(PARAM_LINK_QUERY, null);
            if (lstParamMapping != null && !lstParamMapping.isEmpty()) {
                sbAPILink.append("?");
                for (APIParameter item : lstParamMapping) {
                    if (item.getValue() != null && !item.getValue().isEmpty()) {
                        if (item.getParentId() != null && item.getParentId() == PARAM_GET_FROM_API) {
//                            tempApi = apiRepo.findByApilinkAndCompanyid(item.getValue(), companyId);
                            tempApi = new APIInfor();
                            if (tempApi != null) {
                                String strData = null;
                                if ("POST".equals(tempApi.getType())) {
                                    strData = callPostAPI(tempApi, new HashMap<String, Object>());
                                } else if ("GET".equals(tempApi.getType())) {
                                    strData = callGetAPI(tempApi, new HashMap<String, Object>());
                                }
                                strData = (String) mappingResponse(tempApi, strData);
                                sbAPILink.append(item.getInputKey()).append("=")
                                        .append(URLEncoder.encode(strData, StandardCharsets.UTF_8.name())).append("&");
                            }
                        } else {
                            sbAPILink.append(item.getInputKey()).append("=")
                                    .append(URLEncoder.encode(item.getValue(), StandardCharsets.UTF_8.name())).append("&");
                        }
                    } else {
                        if (mpData.get(item.getKey()) != null) {
                            sbAPILink.append(item.getInputKey()).append("=")
                                    .append(URLEncoder.encode(String.valueOf(mpData.get(item.getKey())), StandardCharsets.UTF_8.name())).append("&");
                        }
                    }
                }
                sbAPILink.deleteCharAt(sbAPILink.length() - 1);
            }
            if (StringUtils.isNotBlank(api.getLinkPrefix())) {
                sbAPILink.append(api.getLinkPrefix());
            }

//            HttpGet method = new HttpGet(sbAPILink.toString());
            MultiMap headers = MultiMap.caseInsensitiveMultiMap();
            lstParamMapping = api.getParams(PARAM_HEADER, null);
            for (APIParameter item : lstParamMapping) {
                if (item.getValue() != null && !item.getValue().isEmpty()) {
                    if (item.getParentId() == null || item.getParentId() != PARAM_GET_FROM_API) {
//                        method.addHeader(item.getInputkey(), item.getValue());
                        headers.add(item.getInputKey(), item.getValue());
                    } else {
//                        tempApi = apiRepo.findByApilinkAndCompanyid(item.getValue(), companyId);
                        tempApi = new APIInfor();
                        if (tempApi != null) {
                            String strData = null;
                            if ("POST".equals(tempApi.getType())) {
                                strData = callPostAPI(tempApi, new HashMap<String, Object>());
                            } else if ("GET".equals(tempApi.getType())) {
                                strData = callGetAPI(tempApi, new HashMap<String, Object>());
                            }
                            strData = (String) mappingResponse(tempApi, strData);
//                            method.addHeader(item.getInputkey(), strData);
                            headers.add(item.getInputKey(), strData);
                        }
                    }
                } else {
                    Object obj = mpData.get(item.getKey());
                    if (obj != null) {
//                        method.addHeader(item.getInputkey(), obj.toString());
                        headers.add(item.getInputKey(), obj.toString());
                    }
                }
            }
            HttpRequest<JsonObject> request = Application.webClient
                    .getAbs(sbAPILink.toString())
                    .putHeaders(headers)
                    .as(BodyCodec.jsonObject());

            try {
                LOGGER.info("CALL COMMON GET API, URL: {}", sbAPILink.toString());
                for (String header : headers.names()) {
                    LOGGER.info("HEADER: {} - {}", header, headers.get(header));
                }
            } catch (Exception e) {
                LOGGER.error("Write log error!", e);
            }
//            HttpResponse response = client.execute(method);
//            String result = EntityUtils.toString(response.getEntity());
//
//            logger.info("JSON STRING OUT: " + result);
//            return result;
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }

    public Object mappingResponse(APIInfor apiInfor, String data) {
        try {
            if ("OBJECT".equalsIgnoreCase(apiInfor.getReturnType())) {
                try {
                    return mapper.readValue(data, Map.class);
                } catch (Exception e) {
                    LOGGER.error(e);
                    return mapper.readValue(data, List.class);
                }
            }
            Object obj;
            Map<String, Object> mpData = new HashMap<>();
            if (!"LIST".equalsIgnoreCase(apiInfor.getReturnType())) {
                mpData = mapper.readValue(data, Map.class);
            }
            List<APIParameter> lstParamMapping = apiInfor.getMappingOut(null);
            if (lstParamMapping.isEmpty()) {
                return null;
            }
            APIParameter paramListKey = lstParamMapping.get(0);
            if ("BEAN".equals(apiInfor.getReturnType())) {
                Map<String, Object> mpDataOut = new HashMap<>();
                Map<String, Object> mpSubBeanIn;
                if (StringUtils.isNotBlank(paramListKey.getKey())) {
                    mpSubBeanIn = (Map<String, Object>) mpData.get(paramListKey.getKey());
                } else {
                    mpSubBeanIn = mpData;
                }
                List<APIParameter> lstSubParamMapping = apiInfor.getMappingOut(paramListKey.getId());
                for (APIParameter subItem : lstSubParamMapping) {
                    if ("Bean".equalsIgnoreCase(subItem.getDataType()) || "List".equalsIgnoreCase(subItem.getDataType())) {
                        throw new Exception("Mapping not support 3 layer bean type!");
                    }
                    if (StringUtils.isNotBlank(paramListKey.getKey())) {
                        obj = mpSubBeanIn.get(subItem.getKey());
                    } else {
                        obj = mpData.get(subItem.getKey());
                    }
                    mapDataObject(obj, mpDataOut, subItem);
                }
                return mpDataOut;
            } else if ("LIST".equals(apiInfor.getReturnType())) {
                Map<String, Object> mpSubBean;
                Map<String, Object> mpDataOut;
                Map<String, Object> mpSubS2BeanIn;
                List<Map<String, Object>> lstMpDataOut;
                List<Map<String, Object>> lstMpSubDataIn;
                List<Map<String, Object>> lstMpDataIn;
                List<Map<String, Object>> lstMpSubBean = new ArrayList<>();
                List<APIParameter> lstSubBeanMapping;
                List<APIParameter> lstBeanMappings = apiInfor.getMappingOut(paramListKey.getId());
                try {
                    mpData = mapper.readValue(data, Map.class);
                    lstMpDataIn = (List<Map<String, Object>>) mpData.get(paramListKey.getKey());
                } catch (Exception e) {
                    LOGGER.error(e);
                    lstMpDataIn = mapper.readValue(data, List.class);
                }
                for (Map<String, Object> mpDataIn : lstMpDataIn) {
                    mpSubBean = new HashMap<>();
                    for (APIParameter beanMapping : lstBeanMappings) {
                        lstSubBeanMapping = apiInfor.getMappingOut(beanMapping.getId());
                        if ("Bean".equalsIgnoreCase(beanMapping.getDataType())) {
                            mpDataOut = new HashMap<>();
                            for (APIParameter subBeanMapping : lstSubBeanMapping) {
                                if ("Bean".equals(subBeanMapping.getDataType()) || "List".equals(subBeanMapping.getDataType())) {
                                    throw new Exception("Mapping not support 3 layer bean type!");
                                }
                                if (StringUtils.isNotBlank(subBeanMapping.getKey())) {
                                    mpSubS2BeanIn = (Map<String, Object>) mpDataIn.get(beanMapping.getKey());
                                    obj = mpSubS2BeanIn.get(subBeanMapping.getKey());
                                    mapDataObject(obj, mpDataOut, subBeanMapping);
                                }
                            }
                            if (!mpDataOut.keySet().isEmpty()) {
                                mpSubBean.put(beanMapping.getInputKey(), mpDataOut);
                            }
                        } else if ("List".equalsIgnoreCase(beanMapping.getDataType())) {
                            lstMpDataOut = new ArrayList<>();
                            lstMpSubDataIn = (List<Map<String, Object>>) mpDataIn.get(beanMapping.getKey());
                            for (Map<String, Object> mpSubDataIn : lstMpSubDataIn) {
                                mpDataOut = new HashMap<>();
                                for (APIParameter subBeanMapping : lstSubBeanMapping) {
                                    if ("Bean".equals(subBeanMapping.getDataType()) || "List".equals(subBeanMapping.getDataType())) {
                                        if (StringUtils.isNotBlank(subBeanMapping.getKey())) {
                                            mpDataOut.put(subBeanMapping.getInputKey(), mpSubDataIn.get(subBeanMapping.getKey()));
                                        }
                                        continue;
                                    }
                                    if (StringUtils.isNotBlank(subBeanMapping.getKey())) {
                                        obj = mpSubDataIn.get(subBeanMapping.getKey());
                                        mapDataObject(obj, mpDataOut, subBeanMapping);
                                    }
                                }
                                if (!mpDataOut.keySet().isEmpty()) {
                                    lstMpDataOut.add(mpDataOut);
                                }
                            }
                            mpSubBean.put(beanMapping.getInputKey(), lstMpDataOut);
                        } else {
                            obj = mpDataIn.get(beanMapping.getKey());
                            mapDataObject(obj, mpSubBean, beanMapping);
                        }
                    }
                    lstMpSubBean.add(mpSubBean);
                }
                return lstMpSubBean;
            } else {
                if (StringUtils.isNotBlank(paramListKey.getKey())) {
                    if (!JAVA_LANG_OBJECT.equals(paramListKey.getDataType())) {
                        Class<?> cls = Class.forName(paramListKey.getDataType());
                        obj = mpData.get(paramListKey.getKey());
                        return obj == null ? null : cls.getConstructor(String.class).newInstance(obj.toString());
                    } else {
                        return mpData.get(paramListKey.getKey());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }

    private void mapDataObject(Object obj, Map<String, Object> mpDataOut, APIParameter subItem) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        if (obj != null) {
            Class<?> cls;
            Constructor<?> construct;
            if (!JAVA_LANG_OBJECT.equals(subItem.getDataType())) {
                cls = Class.forName(subItem.getDataType());
                construct = cls.getConstructor(String.class);
                mpDataOut.put(subItem.getInputKey(), construct.newInstance(obj.toString()));
            } else {
                mpDataOut.put(subItem.getInputKey(), obj);
            }
        }
    }

    private Map<String, Object> buildRequestBody(APIInfor api, Map<String, Object> data) throws Exception {
        if (api == null) {
            return null;
        }
        Map<String, Object> mpData = new HashMap<>();
        Map<String, Object> mpSubBean;
        Map<String, Object> mpSubBeanIn = new HashMap<>();
        List<Map<String, Object>> lstMpSubBeanIn;
        List<APIParameter> lstParamMapping;
        List<APIParameter> lstSubParamMapping;
        Class<?> cls;
        Constructor<?> construct;
        try {
            lstParamMapping = api.getParams(PARAM_NORMAL, null);
            for (APIParameter item : lstParamMapping) {
                if ("Bean".equalsIgnoreCase(item.getDataType())) {
                    if (StringUtils.isNotBlank(item.getKey())) {
                        mpSubBeanIn = (Map<String, Object>) data.get(item.getKey());
                        if (mpSubBeanIn == null) {
                            continue;
                        }
                    }
                    mpSubBean = new HashMap<>();
                    lstSubParamMapping = api.getParams(PARAM_NORMAL, item.getId());
                    Object obj;
                    for (APIParameter subItem : lstSubParamMapping) {
                        if ("Bean".equals(subItem.getDataType()) || "List".equals(subItem.getDataType())) {
                            if (StringUtils.isNotBlank(item.getKey())) {
                                obj = mpSubBeanIn.get(subItem.getKey());
                            } else {
                                obj = data.get(subItem.getKey());
                            }
                            mpSubBean.put(subItem.getInputKey(), obj);
                            continue;
                        }
                        cls = Class.forName(subItem.getDataType());
                        construct = cls.getConstructor(String.class);
                        if (StringUtils.isNotBlank(subItem.getValue())) {
                            mpSubBean.put(subItem.getInputKey(), construct.newInstance(subItem.getValue()));
                        } else {
                            if (StringUtils.isNotBlank(item.getKey())) {
                                obj = mpSubBeanIn.get(subItem.getKey());
                            } else {
                                obj = data.get(subItem.getKey());
                            }

                            if (obj != null) {
                                mpSubBean.put(subItem.getInputKey(), construct.newInstance(obj.toString()));
                            }
                        }
                    }
                    mpData.put(item.getInputKey(), mpSubBean);
                } else if ("List".equalsIgnoreCase(item.getDataType())) {
                    lstMpSubBeanIn = (List<Map<String, Object>>) data.get(item.getKey());
                    List<Object> lstObjSubBean = new ArrayList<>();
                    lstSubParamMapping = api.getParams(PARAM_NORMAL, item.getId());
                    Object obj;
                    for (Object mpItemIn : lstMpSubBeanIn) {
                        mpSubBean = new HashMap<>();
                        if (mpItemIn instanceof Map) {
                            Map<String, Object> mpItem = (Map<String, Object>) mpItemIn;
                            for (APIParameter subItem : lstSubParamMapping) {
                                if ("Bean".equals(subItem.getDataType()) || "List".equals(subItem.getDataType())) {
                                    obj = mpItem.get(subItem.getKey());
                                    mpSubBean.put(subItem.getInputKey(), obj);
                                    continue;
                                }
                                cls = Class.forName(subItem.getDataType());
                                construct = cls.getConstructor(String.class);
                                if (StringUtils.isNotBlank(subItem.getValue())) {
                                    mpSubBean.put(subItem.getInputKey(), construct.newInstance(subItem.getValue()));
                                } else {
                                    obj = mpItem.get(subItem.getKey());
                                    if (obj != null) {
                                        mpSubBean.put(subItem.getInputKey(), construct.newInstance(obj.toString()));
                                    }
                                }
                            }
                            lstObjSubBean.add(mpSubBean);
                        } else {
                            lstObjSubBean.add(mpItemIn);
                        }
                    }
                    mpData.put(item.getInputKey(), lstObjSubBean);
                } else {
                    cls = Class.forName(item.getDataType());
                    construct = cls.getConstructor(String.class);
                    if (StringUtils.isNotBlank(item.getValue())) {
                        mpData.put(item.getInputKey(), construct.newInstance(item.getValue()));
                    } else {
                        Object obj = data.get(item.getKey());
                        if (obj != null) {
                            mpData.put(item.getInputKey(), construct.newInstance(obj.toString()));
                        }
                    }
                }
            }
            return mpData;
        } catch (Exception e) {
            LOGGER.error(e);
            throw new Exception("Can not mapping API data!");
        }
    }
}

