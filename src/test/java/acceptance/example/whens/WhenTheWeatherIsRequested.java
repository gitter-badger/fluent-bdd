package acceptance.example.whens;

import acceptance.example.test.TestInfrastructure;
import io.github.theangrydev.yatspecfluent.ReadOnlyTestItems;
import io.github.theangrydev.yatspecfluent.When;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.assertj.core.api.WithAssertions;

import static java.lang.String.format;


public class WhenTheWeatherIsRequested implements When<Request, Response>, WithAssertions {

    private final ReadOnlyTestItems readOnlyTestItems;
    private final TestInfrastructure testInfrastructure;
    private final String caller;

    private String city;

    public WhenTheWeatherIsRequested(ReadOnlyTestItems readOnlyTestItems, TestInfrastructure testInfrastructure, String caller) {
        this.readOnlyTestItems = readOnlyTestItems;
        this.testInfrastructure = testInfrastructure;
        this.caller = caller;
    }

    @Override
    public Request request() {
        Request request = weatherRequest(testInfrastructure.serverBaseUrl());
        readOnlyTestItems.addToCapturedInputsAndOutputs(format("Request from %s to %s", caller, systemName()), request);
        return request;
    }

    @Override
    public Response response(Request request) {
        Response response = testInfrastructure.execute(request);
        readOnlyTestItems.addToCapturedInputsAndOutputs(format("Response from %s to %s", systemName(), caller), response);
        return response;
    }

    private Request weatherRequest(String baseUrl) {
        HttpUrl weatherUrl = HttpUrl.parse(baseUrl).newBuilder().addPathSegment("weather").addQueryParameter("city", this.city).build();
        return new Request.Builder().url(weatherUrl).build();
    }

    public void forCity(String city) {
        this.city = city;
    }

    public WhenTheWeatherIsRequested requestsTheWeather() {
        return this;
    }

    private String systemName() {
        return "Weather Application";
    }
}
