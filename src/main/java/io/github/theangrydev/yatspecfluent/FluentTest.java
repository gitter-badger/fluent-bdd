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

import com.googlecode.yatspec.state.givenwhenthen.CapturedInputAndOutputs;
import com.googlecode.yatspec.state.givenwhenthen.InterestingGivens;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.googlecode.yatspec.state.givenwhenthen.WithTestState;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static java.lang.String.format;

/**
 * Use this as the base class for your acceptance tests.
 *
 * @param <Response> The type of response produced by the {@link When}
 */
@SuppressWarnings("PMD.TooManyMethods") // Maybe I will refactor this one day...
public abstract class FluentTest<Response> implements WithTestState, WriteOnlyTestItems {

    /**
     * You should aim to never access these directly, but you might need to (e.g. global shared state).
     * Call {@link #addToGivens(String, Object)} when possible or make use of the {@link WriteOnlyTestItems} interface.
     */
    protected final InterestingGivens doNotUseTheInterestingGivens = new InterestingGivens();

    /**
     * You should aim to never access these directly, but you might need to (e.g. sequence diagrams)
     * Call {@link #addToCapturedInputsAndOutputs(String, Object)} when possible or make use of the {@link WriteOnlyTestItems} interface.
     */
    protected final CapturedInputAndOutputs doNotUseTheCapturedInputAndOutputs = new CapturedInputAndOutputs();

    private Stage stage = Stage.GIVEN;
    private Response response;

    private enum Stage {
        GIVEN,
        WHEN,
        THEN
    }

    @Rule
    public TestWatcher makeSureThenIsUsed = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            if (stage != Stage.THEN) {
                throw new IllegalStateException("Each test needs at least a 'when' and a 'then'");
            }
        }
    };

    @Override
    public TestState testState() {
        TestState testState = new TestState();
        testState.interestingGivens = doNotUseTheInterestingGivens;
        testState.capturedInputAndOutputs = doNotUseTheCapturedInputAndOutputs;
        return testState;
    }

    /**
     * Same as {@link #given(Given)}.
     * <p>
     * Prime the given immediately.
     *
     * @param given The first given in the acceptance test, which should be built up inside the brackets
     */
    protected void and(Given given) {
        given(given);
    }

    /**
     * Prime the given immediately.
     *
     * @param given The first given in the acceptance test, which should be built up inside the brackets
     */
    protected void given(Given given) {
        if (stage != Stage.GIVEN) {
            throw new IllegalStateException("The 'given' steps must be specified before the 'when' and 'then' steps");
        }
        stage = Stage.GIVEN;
        given.prime();
    }

    /**
     * Invoke the system under test and store the response ready for the assertions.
     *
     * @param when The system under test, which should be built up inside the brackets
     * @param <T>  The type of {@link When}
     */
    protected <T extends When<Response>> void when(T when) {
        if (stage != Stage.GIVEN) {
            throw new IllegalStateException("There should only be one 'when', after the 'given' and before the 'then'");
        }
        response = when.execute();
        if (response == null) {
            throw new IllegalStateException(format("'%s' response was null", when));
        }
        stage = Stage.WHEN;
    }

    /**
     * Adapt the 'when' to a 'given'. This is a common pattern when e.g. calling an endpoint changes some state in the database.
     * This is the equivalent of {@link #given(Given)}.
     *
     * @param when The 'when' to adapt to a 'given'
     */
    public void given(When<Response> when) {
        given((Given) when::execute);
    }

    /**
     * Same as {@link #given(When)}.
     * <p>
     * Adapt the 'when' to a 'given'. This is a common pattern when e.g. calling an endpoint changes some state in the database.
     * This is the equivalent of {@link #given(Given)}.
     *
     * @param when The 'when' to adapt to a 'given'
     */
    public void and(When<Response> when) {
        given(when);
    }

    /**
     * Perform the assertion. Assertions should be chained outside the brackets.
     *
     * @param thenFactory A {@link ThenFactory} that will produce a {@link Then} given the stored response
     * @param <Then>      The type of fluent assertions that will be performed
     * @return The fluent assertions instance
     */
    protected <Then> Then then(ThenFactory<Then, Response> thenFactory) {
        if (stage.compareTo(Stage.WHEN) < 0) {
            throw new IllegalStateException("The 'then' steps should be after the 'when'");
        }
        stage = Stage.THEN;
        return thenFactory.then(response);
    }

    /**
     * Same as {@link #then(ThenFactory)}.
     * <p>
     * Perform the assertion. Assertions should be chained outside the brackets.
     *
     * @param thenFactory A {@link ThenFactory} that will produce a {@link Then} given the stored response
     * @param <Then>      The type of fluent assertions that will be performed
     * @return The fluent assertions instance
     */
    protected <Then> Then and(ThenFactory<Then, Response> thenFactory) {
        return then(thenFactory);
    }

    @Override
    public void addToGivens(String key, Object instance) {
        doNotUseTheInterestingGivens.add(key, instance);
    }

    @Override
    public void addToCapturedInputsAndOutputs(String key, Object instance) {
        doNotUseTheCapturedInputAndOutputs.add(key, instance);
    }
}
