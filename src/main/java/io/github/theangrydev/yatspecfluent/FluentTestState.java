package io.github.theangrydev.yatspecfluent;

import com.googlecode.yatspec.state.givenwhenthen.TestState;

public class FluentTestState<Response> extends TestState {
    Stage stage = Stage.GIVEN;
    Response response;
}
