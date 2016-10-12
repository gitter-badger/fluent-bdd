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
package io.github.theangrydev.yatspecfluent;

import org.junit.After;

import static java.lang.String.format;

/**
 * Use this as the base class for your acceptance tests.
 *
 * @param <Request> The type of request passed to the {@link When}
 * @param <Response> The type of response produced by the {@link When}
 */
public interface FluentTest<Request, Response> extends WriteOnlyTestItems {


//    @Rule
//    TestWatcher makeSureThenIsUsed = new TestWatcher() {
//        @Override
//        protected void succeeded(Description description) {
//            if (stage != Stage.THEN ) {
//                throw new IllegalStateException("Each test needs at least a 'when' and a 'then'");
//            }
//        }
//    };

    @After
    default void check() {
        if (fluentTestState().stage != Stage.THEN) {
            throw new IllegalStateException("Each test needs at least a 'when' and a 'then'");
        }
    }

    /**
     * This should be implemented as a field in the class that implements this interface.
     * You should aim to never access the state directly, but you might need to (e.g. global shared state).
     * Call {@link #addToGivens(String, Object)} when possible or make use of the {@link WriteOnlyTestItems} interface.
     * Call {@link #addToCapturedInputsAndOutputs(String, Object)} when possible or make use of the {@link WriteOnlyTestItems} interface.
     */
    FluentTestState<Response> fluentTestState();

    /**
     * Same as {@link #given(Given)}.
     */
    default void and(Given given) {
        given(given);
    }

    /**
     * Prime the given immediately.
     *
     * @param given The first given in the acceptance test, which should be built up inside the brackets
     */
    default void given(Given given) {
        if (fluentTestState().stage != Stage.GIVEN) {
            throw new IllegalStateException("The 'given' steps must be specified before the 'when' and 'then' steps");
        }
        fluentTestState().stage = Stage.GIVEN;
        given.prime();
    }

    /**
     * Invoke the system under test and store the response ready for the assertions.
     *
     * @param when The system under test, which should be built up inside the brackets
     * @param <T> The type of {@link When}
     */
    default <T extends When<Request, Response>> void when(T when) {
        if (fluentTestState().stage != Stage.GIVEN) {
            throw new IllegalStateException("There should only be one 'when', after the 'given' and before the 'then'");
        }
        Request request = when.request();
        if (request == null) {
            throw new IllegalStateException(format("'%s' request was null", when));
        }
        fluentTestState().response = when.response(request);
        if (fluentTestState().response == null) {
            throw new IllegalStateException(format("'%s' response was null", when));
        }
        fluentTestState().stage = Stage.WHEN;
    }

    /**
     * Adapt the 'when' to a 'given'. This is a common pattern when e.g. calling an endpoint changes some state in the database.
     * This is the equivalent of {@link #given(Given)}.
     *
     * @param when The 'when' to adapt to a 'given'
     */
    default void given(When<Request, Response> when) {
        given(() -> when.response(when.request()));
    }

    /**
     * Same as {@link #given(When)}.
     * This is the equivalent of {@link #and(Given)}.
     */
    default void and(When<Request, Response> when) {
        given(when);
    }

    /**
     * Perform the assertion. Assertions should be chained outside the brackets.
     *
     * @param thenFactory A {@link ThenFactory} that will produce a {@link Then} given the stored response
     * @param <Then> The type of fluent assertions that will be performed
     * @return The fluent assertions instance
     */
    default <Then> Then then(ThenFactory<Then, Response> thenFactory) {
        if (fluentTestState().stage.compareTo(Stage.WHEN) < 0) {
            throw new IllegalStateException("The 'then' steps should be after the 'when'");
        }
        fluentTestState().stage = Stage.THEN;
        return thenFactory.then(fluentTestState().response);
    }

    /**
     * Same as {@link #then(ThenFactory)}.
     */
    default <Then> Then and(ThenFactory<Then, Response> thenFactory) {
        return then(thenFactory);
    }

    @Override
    default void addToGivens(String key, Object instance) {
        fluentTestState().interestingGivens.add(key, instance);
    }

    @Override
    default void addToCapturedInputsAndOutputs(String key, Object instance) {
        fluentTestState().capturedInputAndOutputs.add(key, instance);
    }
}
