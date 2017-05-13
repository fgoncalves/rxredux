package com.fred.rxredux;

import com.fred.rxredux.testhelpers.ImmediateToImmediateScheduler;
import io.reactivex.observers.TestObserver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyAction;
import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyDispatch;
import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyState;
import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyStore;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StoreImplTest {
  @Mock
  Reducer<State, Action<Integer>> rootReducer;
  @Mock
  Middleware<Action<Integer>, State> middleware;

  private TestObserver<State> testObserver;
  private Store<State, Action<Integer>> store;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    store =
        new StoreImpl<State, Action<Integer>>(rootReducer, mock(State.class),
            new ImmediateToImmediateScheduler(), Collections.singletonList(middleware));
    testObserver = new TestObserver<State>();
    store.subscribe(testObserver);
  }

  @After
  public void tearDown() throws Exception {
    testObserver.dispose();
  }

  @Test
  public void dispatch_shouldNotForwardEventsIfMiddlewareCompletesTheStream() throws Exception {
    when(middleware.apply(anyStore(), anyAction(), anyDispatch())).then(new Answer<Integer>() {
      public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
        // avoid calling the next middleware
        return null;
      }
    });
    store.dispatch(mock(Action.class));

    testObserver.assertNoValues();
  }

  @Test
  public void dispatch_shouldForwardActionsToReducerAfterMiddlewareRan() throws Exception {
    when(middleware.apply(anyStore(), anyAction(), anyDispatch())).then(new Answer<Integer>() {
      public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
        ((Dispatch) invocationOnMock.getArguments()[2]).apply(new Action<Integer>(1));
        return null;
      }
    });

    store.dispatch(new Action<Integer>(2));

    verify(rootReducer).apply(anyAction(), anyState());
  }

  @Test
  public void dispatch_shouldStillForwardActionsToRootReducerIfThereAreNoMiddlewares()
      throws Exception {
    State state = mock(State.class);
    store =
        new StoreImpl<State, Action<Integer>>(rootReducer, state,
            new ImmediateToImmediateScheduler(),
            new ArrayList<Middleware<Action<Integer>, State>>());

    Action<Integer> action = mock(Action.class);
    store.dispatch(action);

    verify(rootReducer).apply(action, state);
  }

  @Test
  public void create_shouldCreateAStoreWithTheGivenInitialState() {
    State initialState = new State();
    Store<State, Action<Integer>> store =
        StoreImpl.create(rootReducer, initialState,
            new ImmediateToImmediateScheduler());

    assertThat(store.state()).isEqualTo(initialState);

    store =
        StoreImpl.create(rootReducer, initialState,
            new ImmediateToImmediateScheduler(),
            Collections.singletonList(middleware));

    assertThat(store.state()).isEqualTo(initialState);
  }

  @Test
  public void dispatch_shouldInvokeMiddlewaresInOrderOfAdditionAndThenTheRootReducer()
      throws Exception {
    final Action<Integer> action = mock(Action.class);
    Middleware<Action<Integer>, State> one = new Middleware<Action<Integer>, State>() {
      public State apply(Store<State, Action<Integer>> stateActionStore,
          Action<Integer> integerAction,
          Dispatch<Action<Integer>, State> next) throws Exception {
        action.setType(123);
        next.apply(integerAction);
        return null;
      }
    };
    Middleware<Action<Integer>, State> two = new Middleware<Action<Integer>, State>() {
      public State apply(Store<State, Action<Integer>> stateActionStore,
          Action<Integer> integerAction,
          Dispatch<Action<Integer>, State> next) throws Exception {
        action.setType(456);
        next.apply(integerAction);
        return null;
      }
    };
    rootReducer = new Reducer<State, Action<Integer>>() {
      public State apply(Action<Integer> integerAction, State state) {
        action.setType(789);
        return state;
      }
    };

    store =
        new StoreImpl<State, Action<Integer>>(rootReducer, mock(State.class),
            new ImmediateToImmediateScheduler(), Arrays.asList(one, two));

    store.dispatch(action);

    InOrder inOrder = inOrder(action);

    inOrder.verify(action).setType(123);
    inOrder.verify(action).setType(456);
    inOrder.verify(action).setType(789);
  }
}
