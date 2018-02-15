package com.greytip.birtreport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dibyendu on 12/19/2017.
 */
@Service
public class CougarService {

private static final Logger logger = LoggerFactory.getLogger(CougarService.class);

    @Autowired
    private RestTemplate restTemplate;


    public boolean processTask(String domainName, Map<String, String> result, String authKey) {
        String url = String.format("%s%s", domainName, "/internal-api/report/processTask");
        logger.info("process task url is {} ", url);

        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypeList = new ArrayList();
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypeList);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-ghr-internal-authorization", authKey);

        HttpEntity<?> requestEntity = new HttpEntity(result,headers);
        ResponseEntity<Object> response = this.restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Object.class);

        return response != null &&  HttpStatus.OK.equals(response.getStatusCode());
    }

    public boolean completeTask(String domainName, Map<String, String> result, String authKey) {
        String url = String.format("%s%s", domainName, "/internal-api/report/CompleteTask");
        logger.info("complete task url is {} ", url);

        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypeList = new ArrayList();
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypeList);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-ghr-internal-authorization", authKey);

        HttpEntity<?> requestEntity = new HttpEntity(result,headers);
        ResponseEntity<Object> response = this.restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Object.class);

        return response != null &&  HttpStatus.OK.equals(response.getStatusCode());
    }

}
