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

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Use this as the base class for your acceptance tests.
 *
 * @param <Request> The type of request passed to the {@link When}
 * @param <Response> The type of response produced by the {@link When}
 */
public abstract class FluentTest<Request, Response> implements WithTestState, WriteOnlyTestItems {

    /**
     * You should aim to never access these directly, but you might need to (e.g. global shared state).
     * Call {@link #addToGivens(String, Object)} when possible or make use of the {@link WriteOnlyTestItems} interface.
     */
    protected final InterestingGivens interestingGivens = new InterestingGivens();

    /**
     * You should aim to never access these directly , you shouldn't need to access these, but you might need to (e.g. sequence diagrams)
     * Call {@link #addToCapturedInputsAndOutputs(String, Object)} when possible or make use of the {@link WriteOnlyTestItems} interface.
     */
    protected final CapturedInputAndOutputs capturedInputAndOutputs = new CapturedInputAndOutputs();

    private final List<Given> givens = new ArrayList<>();

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
            if (stage != Stage.THEN ) {
                throw new IllegalStateException("Each test needs at least a 'when' and a 'then'");
            }
        }
    };

    @Override
    public TestState testState() {
        TestState testState = new TestState();
        testState.interestingGivens = interestingGivens;
        testState.capturedInputAndOutputs = capturedInputAndOutputs;
        return testState;
    }

    /**
     * Same as {@link #given(Given)} for all givens after the first one.
     */
    protected void and(Given given) {
        doGiven(given);
    }

    /**
     * Prime the first given immediately.
     *
     * @param given The first given in the acceptance test, which should be built up inside the brackets
     */
    protected void given(Given given) {
        if (stage != Stage.GIVEN) {
            throw new IllegalStateException("The 'given' steps must be specified before the 'when' and 'then' steps");
        }
        if (!givens.isEmpty()) {
            throw new IllegalStateException("All of the 'given' statements after the initial then should be 'and'");
        }
        doGiven(given);
    }

    private void doGiven(Given given) {
        boolean alreadyHadGiven = givens.contains(given);
        if (alreadyHadGiven) {
            throw new IllegalStateException(format("The dependency '%s' has already specified a 'given' step", given));
        }
        given.prime();
        givens.add(given);
    }

    /**
     * Invoke the system under test and store the response ready for the assertions.
     *
     * @param when the system under test, which should be built up inside the brackets
     * @param <T> The type of {@link When}
     */
    protected <T extends When<Request, Response>> void when(T when) {
        if (stage != Stage.GIVEN) {
            throw new IllegalStateException("There should only be one 'when', after the 'given' and before the 'then'");
        }
        Request request = when.request();
        if (request == null) {
            throw new IllegalStateException(format("'%s' request was null", when));
        }
        response = when.response(request);
        if (response == null) {
            throw new IllegalStateException(format("'%s' response was null", when));
        }
        stage = Stage.WHEN;
    }

    /**
     * Perform the first assertion. Assertions should be chained outside the brackets.
     *
     * @param thenFactory A {@link ThenFactory} that will produce a {@link Then} given the stored response
     * @param <Then> The type of fluent assertions that will be performed
     * @return The fluent assertions instance
     */
    protected <Then> Then then(ThenFactory<Then, Response> thenFactory) {
        if (stage == Stage.GIVEN) {
            throw new IllegalStateException("The initial 'then' should be after the 'when'");
        }
        if (stage == Stage.THEN) {
            throw new IllegalStateException("After the first 'then' you should use 'and'");
        }
        stage = Stage.THEN;
        return thenFactory.then(response);
    }

    /**
     * Same as {@link #then(ThenFactory)} but for all then steps after the first one.
     */
    protected <Then> Then and(ThenFactory<Then, Response> thenFactory) {
        if (stage != Stage.THEN) {
            throw new IllegalStateException("The first 'then' should be a 'then' and after that you should use 'and'");
        }
        return thenFactory.then(response);
    }

    @Override
    public void addToGivens(String key, Object instance) {
        interestingGivens.add(key, instance);
    }

    @Override
    public void addToCapturedInputsAndOutputs(String key, Object instance) {
        capturedInputAndOutputs.add(key, instance);
    }
}
