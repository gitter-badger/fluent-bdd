/*
 * Copyright 2016 Liam Williams <liam.williams@zoho.com>.
 *
 * This file is part of yatspec-fluent.
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
package acceptance.example.test;

import io.github.theangrydev.yatspecfluent.FluentTest;
import io.github.theangrydev.yatspecfluent.FluentTestState;
import org.assertj.core.api.WithAssertions;
import org.junit.After;
import org.junit.Before;

public abstract class AcceptanceTest<Request, Response> implements FluentTest<Request, Response>, WithAssertions {

    protected final TestInfrastructure testInfrastructure = new TestInfrastructure();

    private final FluentTestState<Response> fluentTestState = new FluentTestState<>();

    @Override
    public FluentTestState<Response> fluentTestState() {
        return fluentTestState;
    }

    @Before
    public void setUp() {
        testInfrastructure.setUp();
    }

    @After
    public void tearDown() {
        testInfrastructure.tearDown();
    }
}
