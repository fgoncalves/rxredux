package com.fred.rxredux;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionTest {
  @Test
  public void shouldBuildTheCorrectlyAnAction() {
    Action<Integer> action = new Action<Integer>(123);

    assertThat(action.getType()).isEqualTo(123);
    action.setType(456);
    assertThat(action.getType()).isEqualTo(456);
  }
}
