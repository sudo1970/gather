package com.cq.oauth2.server.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cq.commons.model.domain.ResultInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
//@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
public class OAuthControllerTest {
    @Autowired
    private WebApplicationContext context ;
    private MockMvc mockMvc ;

    @Autowired
    private Filter springSecurityFilterChain;

    /**
     * @Description 初始化
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
//        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();  //构造MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain).build();
    }

    @Test
    public void writeToken() throws Exception {
        String authorization = Base64Utils.encodeToString("appId:123456".getBytes());
        StringBuffer tokens = new StringBuffer();
        for (int i = 0; i < 2000; i++) {
            System.out.println(i + "***********************************");
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/oauth/token")
                    .header("Authorization", "Basic " + authorization)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "test" + i)
                    .param("password", "123456")
                    .param("grant_type", "password")
                    .param("scope", "api")
            ).andExpect(status().isOk())
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            ResultInfo resultInfo = JSONUtil.toBean(contentAsString, ResultInfo.class);
            JSONObject result = (JSONObject) resultInfo.getData();
            String token = result.getStr("accessToken");
            tokens.append(token).append("\r\n");
        }
        Files.write(Paths.get("tokens.txt"), tokens.toString().getBytes());
    }
}