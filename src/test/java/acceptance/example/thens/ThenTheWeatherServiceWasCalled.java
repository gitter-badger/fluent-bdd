/*
 * Copyright 2016 Liam Williams <liam.williams@zoho.com>.
 *
 * This file is part of fluent-bdd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package acceptance.example.thens;

import acceptance.example.test.TestResult;
import io.github.theangrydev.fluentbdd.ThenVerification;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ThenTheWeatherServiceWasCalled implements ThenVerification<TestResult> {

    private String city;

    public ThenTheWeatherServiceWasCalled withCity(String city) {
        this.city = city;
        return this;
    }

    @Override
    public void verify(TestResult testResult) {
        testResult.verifyThat(getRequestedFor(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("q", equalTo(city)));
    }
}
