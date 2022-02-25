package efs.task.todoapp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base64UtilsTest {

    @Test
    public void shouldEncodeTextToBase64(){
        //given
        final String userName = "Lak Grzegorz";

        //when
        final String encoded = Base64Utils.encode(userName);

        //then
        assertEquals("TGFrIEdyemVnb3J6", encoded);
    }

    @Test
    public void shouldDecodeTextFromBase64(){
        //given
        final String encodeUserName = "TGFrIEdyemVnb3J6";

        //when
        final String decodeUserName = Base64Utils.decode(encodeUserName);

        //then
        assertEquals("Lak Grzegorz",decodeUserName);
    }

}
