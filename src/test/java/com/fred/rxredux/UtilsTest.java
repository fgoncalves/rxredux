package com.fred.rxredux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;

public class UtilsTest {
  @Test
  public void reduceRight_shouldThrowIllegalArgumentIfListIsEmpty() {
    try {
      Utils.reduceRight(new ArrayList<Object>(), mock(Utils.ReduceCallbacks.class));
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Cannot reduce empty list");
    }
  }

  @Test
  public void reduceRight_shouldReturnTheFirstElementIfListHasOnlyOneValue() {
    List<Integer> integers = new ArrayList<Integer>(Collections.singletonList(1));

    Integer result = Utils.reduceRight(integers, mock(Utils.ReduceCallbacks.class));

    assertThat(result).isEqualTo(1);
  }

  @Test
  public void reduceRight_shouldReduceFromRightToLeft() {
    List<Integer> integers = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5));

    Integer result = Utils.reduceRight(integers, new Utils.ReduceCallbacks<Integer>() {
      public Integer reduce(Integer previous, Integer current) {
        return previous - current;
      }
    });

    assertThat(result).isEqualTo(-5);
  }

  @Test
  public void map_shouldReturnAnEmptyListIfAnEmptyListIsGiven() {
    List<Object> result = Utils.map(new ArrayList<Object>(), mock(Utils.MapCallbacks.class));

    assertThat(result).isEmpty();
  }

  @Test
  public void map_shouldMapToTheCorrectList() {
    List<Integer> integers = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5));

    List<Integer> result = Utils.map(integers, new Utils.MapCallbacks<Integer, Integer>() {
      public Integer map(Integer object) {
        return object * object;
      }
    });

    assertThat(result).containsExactly(1, 4, 9, 16, 25);
  }
}
