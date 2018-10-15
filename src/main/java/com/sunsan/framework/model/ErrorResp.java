package com.sunsan.framework.model;


import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ErrorResp {
	
    static public void respErrorApi(HttpServletResponse response, ApiException e, int respStatus) {
        Resp resp = new Resp();
        response.setStatus(respStatus);
        resp.setCode(e.getCode());
        resp.setMsg(e.getMessage());
        try {
            JsonGenerator jsonGenerator = new ObjectMapper().getFactory().createGenerator(response.getOutputStream(), JsonEncoding.UTF8);
            jsonGenerator.writeObject(resp);
            jsonGenerator.flush();
            jsonGenerator.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
