package efs.task.todoapp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Base64.Decoder;

public class Base64Utils {

    public static String encode(final String value){
        //Konwersja do byte
        final byte[] valuerBytes = value.getBytes(StandardCharsets.ISO_8859_1);

        //Pobieranie encodera
        final Encoder base64Encoded = Base64.getEncoder();

        //Konwersja przy pomocy "encodeToString"
        final String base64EncodedValue = base64Encoded.encodeToString(valuerBytes);

        return base64EncodedValue;
    }

    public static String decode(final String valueInBase64){
        //Konwersja pobieranie dekodera
        final Decoder base64Decoder = Base64.getDecoder();

        //Dekodowanie
        final byte[] decodeValuesByte = base64Decoder.decode(valueInBase64);

        //konwertowanie bin -> tekst
        return new String(decodeValuesByte, StandardCharsets.ISO_8859_1);
    }



}
