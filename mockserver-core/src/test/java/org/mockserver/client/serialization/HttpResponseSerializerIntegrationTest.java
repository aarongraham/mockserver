package org.mockserver.client.serialization;

import org.junit.Test;
import org.mockserver.client.serialization.model.*;
import org.mockserver.model.*;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.model.Delay.minutes;
import static org.mockserver.model.Delay.seconds;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.JsonSchemaBody.jsonSchema;
import static org.mockserver.model.NottableString.string;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.RegexBody.regex;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.model.XPathBody.xpath;
import static org.mockserver.model.XmlBody.xml;

/**
 * @author jamesdbloom
 */
public class HttpResponseSerializerIntegrationTest {


    @Test
    public void shouldIgnoreExtraFields() throws IOException {
        // given
        String requestBytes = "{" + NEW_LINE +
                "    \"statusCode\": \"123\"," + NEW_LINE +
                "    \"extra_field\": \"extra_value\"" + NEW_LINE +
                "}";

        // when
        HttpResponse httpResponse = new HttpResponseSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpResponseDTO()
                .setStatusCode(123)
                .buildObject(), httpResponse);
    }

    @Test
    public void shouldDeserializeCompleteObject() throws IOException {
        // given
        String requestBytes = "{" + NEW_LINE +
                "  \"statusCode\" : \"123\"," + NEW_LINE +
                "  \"delay\" : {" + NEW_LINE +
                "    \"timeUnit\" : \"SECONDS\"," + NEW_LINE +
                "    \"value\" : 5" + NEW_LINE +
                "  }," + NEW_LINE +
                "  \"body\" : {" + NEW_LINE +
                "    \"type\" : \"STRING\"," + NEW_LINE +
                "    \"value\" : \"somebody\"" + NEW_LINE +
                "  }," + NEW_LINE +
                "  \"cookies\" : [ {" + NEW_LINE +
                "    \"name\" : \"someCookieName\"," + NEW_LINE +
                "    \"value\" : \"someCookieValue\"" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"headers\" : [ {" + NEW_LINE +
                "    \"name\" : \"someHeaderName\"," + NEW_LINE +
                "    \"values\" : [ \"someHeaderValue\" ]" + NEW_LINE +
                "  } ]" + NEW_LINE +
                "}";

        // when
        HttpResponse httpResponse = new HttpResponseSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpResponseDTO()
                .setStatusCode(123)
                .setBody(BodyDTO.createDTO(new StringBody("somebody")))
                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                .setDelay(new DelayDTO(seconds(5)))
                .buildObject(), httpResponse);
    }

    @Test
    public void shouldDeserializeStringBodyShorthand() throws IOException {
        // given
        String requestBytes = "{" + NEW_LINE +
                "  \"body\" : \"somebody\"" + NEW_LINE +
                "}";

        // when
        HttpResponse httpResponse = new HttpResponseSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpResponseDTO()
                .setBody(BodyDTO.createDTO(exact("somebody")))
                .buildObject(), httpResponse);
    }

    @Test
    public void shouldDeserializeStringBodyWithType() throws IOException {
        // given
        String requestBytes = "{" + NEW_LINE +
                "  \"body\" : {" + NEW_LINE +
                "    \"type\" : \"STRING\"," + NEW_LINE +
                "    \"value\" : \"somebody\"" + NEW_LINE +
                "  }" + NEW_LINE +
                "}";

        // when
        HttpResponse httpResponse = new HttpResponseSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpResponseDTO()
                .setBody(BodyDTO.createDTO(exact("somebody")))
                .buildObject(), httpResponse);
    }

    @Test
    public void shouldDeserializeJsonBody() throws IOException {
        // given
        String requestBytes = "{" + NEW_LINE +
                "  \"body\" : {" + NEW_LINE +
                "    \"type\" : \"JSON\"," + NEW_LINE +
                "    \"value\" : \"{ \\\"key\\\": \\\"value\\\" }\"" + NEW_LINE +
                "  }" + NEW_LINE +
                "}";

        // when
        HttpResponse httpResponse = new HttpResponseSerializer().deserialize(requestBytes);

        // then
        HttpResponse expected = new HttpResponseDTO()
                .setBody(BodyDTO.createDTO(json("{ \"key\": \"value\" }")))
                .buildObject();
        assertEquals(expected, httpResponse);
    }

    @Test
    public void shouldDeserializeParameterBody() throws IOException {
        // given
        String requestBytes = "{" + NEW_LINE +
                "  \"body\" : {" + NEW_LINE +
                "    \"type\" : \"PARAMETERS\"," + NEW_LINE +
                "    \"parameters\" : [ {" + NEW_LINE +
                "      \"name\" : \"nameOne\"," + NEW_LINE +
                "      \"values\" : [ \"valueOne\" ]" + NEW_LINE +
                "    }, {" + NEW_LINE +
                "      \"name\" : \"nameTwo\"," + NEW_LINE +
                "      \"values\" : [ \"valueTwo_One\", \"valueTwo_Two\" ]" + NEW_LINE +
                "    } ]" + NEW_LINE +
                "  }" + NEW_LINE +
                "}";

        // when
        HttpResponse httpResponse = new HttpResponseSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpResponseDTO()
                .setBody(BodyDTO.createDTO(params(
                        new Parameter("nameOne", "valueOne"),
                        new Parameter("nameTwo", "valueTwo_One", "valueTwo_Two")
                )))
                .buildObject(), httpResponse);
    }

    @Test
    public void shouldDeserializePartialObject() throws IOException {
        // given
        String requestBytes = "{" + NEW_LINE +
                "    \"statusCode\": \"123\"" + NEW_LINE +
                "}";

        // when
        HttpResponse httpResponse = new HttpResponseSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpResponseDTO()
                .setStatusCode(123)
                .buildObject(), httpResponse);
    }

    @Test
    public void shouldDeserializeAsHttpResponseField() throws IOException {
        // given
        String requestBytes = "{" + NEW_LINE +
                "    \"httpResponse\": {" + NEW_LINE +
                "        \"statusCode\": \"123\"," + NEW_LINE +
                "        \"body\" : {" + NEW_LINE +
                "          \"type\" : \"STRING\"," + NEW_LINE +
                "          \"value\" : \"somebody\"" + NEW_LINE +
                "        }" + NEW_LINE +
                "    }" + NEW_LINE +
                "}";

        // when
        HttpResponse httpResponse = new HttpResponseSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpResponseDTO()
                .setStatusCode(123)
                .setBody(new StringBodyDTO(exact("somebody")))
                .buildObject(), httpResponse);
    }

    @Test
    public void shouldSerializeCompleteObject() throws IOException {
        // when
        String jsonHttpResponse = new HttpResponseSerializer().serialize(
                new HttpResponseDTO()
                        .setStatusCode(123)
                        .setBody(BodyDTO.createDTO(new StringBody("somebody")))
                        .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                        .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                        .setDelay(new DelayDTO(minutes(1)))
                        .buildObject()
        );

        // then
        assertEquals("{" + NEW_LINE +
                "  \"statusCode\" : 123," + NEW_LINE +
                "  \"headers\" : [ {" + NEW_LINE +
                "    \"name\" : \"someHeaderName\"," + NEW_LINE +
                "    \"values\" : [ \"someHeaderValue\" ]" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"cookies\" : [ {" + NEW_LINE +
                "    \"name\" : \"someCookieName\"," + NEW_LINE +
                "    \"value\" : \"someCookieValue\"" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"body\" : \"somebody\"," + NEW_LINE +
                "  \"delay\" : {" + NEW_LINE +
                "    \"timeUnit\" : \"MINUTES\"," + NEW_LINE +
                "    \"value\" : 1" + NEW_LINE +
                "  }" + NEW_LINE +
                "}", jsonHttpResponse);
    }

    @Test
    public void shouldSerializeArray() throws IOException {
        // when
        String jsonHttpResponse = new HttpResponseSerializer().serialize(
                new HttpResponse[]{
                        new HttpResponseDTO()
                                .setStatusCode(123)
                                .setBody(BodyDTO.createDTO(new StringBody("somebody_one")))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                                .setDelay(new DelayDTO(minutes(1)))
                                .buildObject(),
                        new HttpResponseDTO()
                                .setStatusCode(456)
                                .setBody(BodyDTO.createDTO(new StringBody("somebody_two")))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                                .setDelay(new DelayDTO(minutes(1)))
                                .buildObject()
                }
        );

        // then
        assertEquals("[ {" + NEW_LINE +
                "  \"statusCode\" : 123," + NEW_LINE +
                "  \"headers\" : [ {" + NEW_LINE +
                "    \"name\" : \"someHeaderName\"," + NEW_LINE +
                "    \"values\" : [ \"someHeaderValue\" ]" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"cookies\" : [ {" + NEW_LINE +
                "    \"name\" : \"someCookieName\"," + NEW_LINE +
                "    \"value\" : \"someCookieValue\"" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"body\" : \"somebody_one\"," + NEW_LINE +
                "  \"delay\" : {" + NEW_LINE +
                "    \"timeUnit\" : \"MINUTES\"," + NEW_LINE +
                "    \"value\" : 1" + NEW_LINE +
                "  }" + NEW_LINE +
                "}, {" + NEW_LINE +
                "  \"statusCode\" : 456," + NEW_LINE +
                "  \"headers\" : [ {" + NEW_LINE +
                "    \"name\" : \"someHeaderName\"," + NEW_LINE +
                "    \"values\" : [ \"someHeaderValue\" ]" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"cookies\" : [ {" + NEW_LINE +
                "    \"name\" : \"someCookieName\"," + NEW_LINE +
                "    \"value\" : \"someCookieValue\"" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"body\" : \"somebody_two\"," + NEW_LINE +
                "  \"delay\" : {" + NEW_LINE +
                "    \"timeUnit\" : \"MINUTES\"," + NEW_LINE +
                "    \"value\" : 1" + NEW_LINE +
                "  }" + NEW_LINE +
                "} ]", jsonHttpResponse);
    }

    @Test
    public void shouldSerializeList() throws IOException {
        // when
        String jsonHttpResponse = new HttpResponseSerializer().serialize(
                Arrays.asList(
                        new HttpResponseDTO()
                                .setStatusCode(123)
                                .setBody(BodyDTO.createDTO(new StringBody("somebody_one")))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                                .setDelay(new DelayDTO(minutes(1)))
                                .buildObject(),
                        new HttpResponseDTO()
                                .setStatusCode(456)
                                .setBody(BodyDTO.createDTO(new StringBody("somebody_two")))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                                .setDelay(new DelayDTO(minutes(1)))
                                .buildObject()
                )
        );

        // then
        assertEquals("[ {" + NEW_LINE +
                "  \"statusCode\" : 123," + NEW_LINE +
                "  \"headers\" : [ {" + NEW_LINE +
                "    \"name\" : \"someHeaderName\"," + NEW_LINE +
                "    \"values\" : [ \"someHeaderValue\" ]" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"cookies\" : [ {" + NEW_LINE +
                "    \"name\" : \"someCookieName\"," + NEW_LINE +
                "    \"value\" : \"someCookieValue\"" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"body\" : \"somebody_one\"," + NEW_LINE +
                "  \"delay\" : {" + NEW_LINE +
                "    \"timeUnit\" : \"MINUTES\"," + NEW_LINE +
                "    \"value\" : 1" + NEW_LINE +
                "  }" + NEW_LINE +
                "}, {" + NEW_LINE +
                "  \"statusCode\" : 456," + NEW_LINE +
                "  \"headers\" : [ {" + NEW_LINE +
                "    \"name\" : \"someHeaderName\"," + NEW_LINE +
                "    \"values\" : [ \"someHeaderValue\" ]" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"cookies\" : [ {" + NEW_LINE +
                "    \"name\" : \"someCookieName\"," + NEW_LINE +
                "    \"value\" : \"someCookieValue\"" + NEW_LINE +
                "  } ]," + NEW_LINE +
                "  \"body\" : \"somebody_two\"," + NEW_LINE +
                "  \"delay\" : {" + NEW_LINE +
                "    \"timeUnit\" : \"MINUTES\"," + NEW_LINE +
                "    \"value\" : 1" + NEW_LINE +
                "  }" + NEW_LINE +
                "} ]", jsonHttpResponse);
    }

    @Test
    public void shouldSerializeStringBody() throws IOException {
        // when
        String jsonHttpResponse = new HttpResponseSerializer().serialize(
                new HttpResponseDTO()
                        .setBody(BodyDTO.createDTO(exact("somebody")))
                        .buildObject()
        );

        // then
        assertEquals("{" + NEW_LINE +
                "  \"body\" : \"somebody\"" + NEW_LINE +
                "}", jsonHttpResponse);
    }

    @Test
    public void shouldSerializeJsonBody() throws IOException {
        // when
        String jsonHttpResponse = new HttpResponseSerializer().serialize(
                new HttpResponseDTO()
                        .setBody(BodyDTO.createDTO(json("{ \"key\": \"value\" }")))
                        .buildObject()
        );

        // then
        assertEquals("{" + NEW_LINE +
                "  \"body\" : \"{ \\\"key\\\": \\\"value\\\" }\"" + NEW_LINE +
                "}", jsonHttpResponse);
    }

    @Test
    public void shouldSerializeJsonSchemaBody() throws IOException {
        // when
        String jsonHttpResponse = new HttpResponseSerializer().serialize(
                new HttpResponseDTO()
                        .setBody(BodyDTO.createDTO(jsonSchema("{ \"key\": \"value\" }")))
                        .buildObject()
        );

        // then
        assertEquals("{" + NEW_LINE +
                "  \"body\" : \"{ \\\"key\\\": \\\"value\\\" }\"" + NEW_LINE +
                "}", jsonHttpResponse);
    }

    @Test
    public void shouldSerializeXmlBody() throws IOException {
        // when
        String jsonHttpResponse = new HttpResponseSerializer().serialize(
                new HttpResponseDTO()
                        .setBody(BodyDTO.createDTO(xml("<some><xml></xml></some>")))
                        .buildObject()
        );

        // then
        assertEquals("{" + NEW_LINE +
                "  \"body\" : \"<some><xml></xml></some>\"" + NEW_LINE +
                "}", jsonHttpResponse);
    }

    @Test
    public void shouldSerializeParameterBody() throws IOException {
        // when
        String jsonHttpResponse = new HttpResponseSerializer().serialize(
                new HttpResponseDTO()
                        .setBody(BodyDTO.createDTO(params(
                                new Parameter("nameOne", "valueOne"),
                                new Parameter("nameTwo", "valueTwo_One", "valueTwo_Two")
                        )))
                        .buildObject()
        );

        // then
        assertEquals("{" + NEW_LINE +
                "  \"body\" : \"nameOne=valueOne&nameTwo=valueTwo_One&nameTwo=valueTwo_Two\"" + NEW_LINE +
                "}", jsonHttpResponse);
    }

    @Test
    public void shouldSerializePartialHttpResponse() throws IOException {
        // when
        String jsonHttpResponse = new HttpResponseSerializer().serialize(new HttpResponseDTO()
                        .setStatusCode(123)
                        .buildObject()
        );

        // then
        assertEquals("{" + NEW_LINE +
                "  \"statusCode\" : 123" + NEW_LINE +
                "}", jsonHttpResponse);
    }
}
