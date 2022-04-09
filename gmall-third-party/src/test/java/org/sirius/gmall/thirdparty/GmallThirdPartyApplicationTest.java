package org.sirius.gmall.thirdparty;

import org.junit.jupiter.api.Test;
import org.sirius.gmall.thirdparty.componts.SmsComponent;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class GmallThirdPartyApplicationTest {

    @Resource
    private SmsComponent smsComponent;



    @Test
    public void testSendMsg(){
//        smsComponent.sendSms("18510237686","test");
        smsComponent.sendSms("15810656564","去苏州玩吧！GO");
    }
}