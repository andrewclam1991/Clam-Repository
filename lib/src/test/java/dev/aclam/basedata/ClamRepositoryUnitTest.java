package dev.aclam.basedata;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Tests for the implementation of the in-memory {@link Clam}
 * repository with cache
 */
@RunWith(JUnit4.class)
public class ClamRepositoryUnitTest {

  private static final List<Clam> CLAMS = new ArrayList<>();

  static {
    CLAMS.add(new Clam(UUID.randomUUID().toString()));
    CLAMS.add(new Clam(UUID.randomUUID().toString()));
  }

  private BaseDataSource<Clam> mCache;
  @Mock
  private BaseDataSource<Clam> mLocal;
  @Mock
  private BaseDataSource<Clam> mRemote;
  private BaseRepository<Clam> mRepository;

  @Before
  public void setupRepository() {
    // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
    // inject the mocks in the test the initMocks method needs to be called.
    MockitoAnnotations.initMocks(this);
    mCache = new ClamCacheDataSource();
    mRepository = new ClamRepository(mCache, mLocal, mRemote);
  }

  @After
  public void destroyRepository() {
    mRepository = null;
  }

  @Test
  public void getLinks_repoCachesAfterFirstSubscription_whenLinksAvailableInLocalStorage() {
    // Given that the local data source has data available
    setItemsAvailable(mLocal, CLAMS);
    // and that the remote data source doesn't have any data available
    setItemsNotAvailable(mRemote);

    // When two subscriptions are set
    TestSubscriber<List<Clam>> testSubscriber1 = new TestSubscriber<>();
    mRepository.getAll().subscribe(testSubscriber1);

    TestSubscriber<List<Clam>> testSubscriber2 = new TestSubscriber<>();
    mRepository.getAll().subscribe(testSubscriber2);

    // Then links were only requested once from local source
    verify(mLocal).getAll();
    //
    assertFalse(mRepository.mCacheIsDirty);
    testSubscriber1.assertValue(CLAMS);
    testSubscriber2.assertValue(CLAMS);
  }

  @Test
  public void getLinks_repoCachesAfterFirstSubscription_whenLinksAvailableInRemoteStorage() {
    // Given that the local data source doesn't have any data available
    setItemsNotAvailable(mLocal);
    // and that the remote data source has data available
    setItemsAvailable(mRemote, CLAMS);
    // and that save data to local succeeds
    setItemAddSucceeds(mLocal);

    // When two subscriptions are set
    TestSubscriber<List<Clam>> testSubscriber1 = new TestSubscriber<>();
    mRepository.getAll().subscribe(testSubscriber1);

    TestSubscriber<List<Clam>> testSubscriber2 = new TestSubscriber<>();
    mRepository.getAll().subscribe(testSubscriber2);

    // Then tasks were only requested once from remote source
    verify(mRemote).getAll();
    //
    assertFalse(mRepository.mCacheIsDirty);
    testSubscriber1.assertValue(CLAMS);
    testSubscriber1.assertValue(CLAMS);
  }

  @Test
  public void getLinks_fetchesFromRemote_whenForcesRefresh() {
    // Given that the remote data source has data available
    setItemsAvailable(mRemote, CLAMS);
    // and that save data to local succeeds
    setItemAddSucceeds(mLocal);
    // and that a subscription is set to force refresh
    TestObserver observer = new TestObserver();
    mRepository.refresh().subscribe(observer);
    observer.assertComplete();

    // When a subscription is set
    TestSubscriber<List<Clam>> testSubscriber = new TestSubscriber<>();
    mRepository.getAll().subscribe(testSubscriber);

    // Then links were only requested from remote source
    verify(mRemote).getAll();
    //
    assertFalse(mRepository.mCacheIsDirty);
    testSubscriber.assertValue(CLAMS);
  }

  @Test
  public void getLink_FetchesFromRemote_whenForcesRefresh() {
    // Given that the remote data source has data available
    Clam clam = CLAMS.get(0);
    setItemAvailable(mRemote, clam);
    // and that save data to local succeeds
    setItemAddSucceeds(mLocal);
    // and that a subscription is set to force refresh
    TestObserver observer = new TestObserver();
    mRepository.refresh().subscribe(observer);
    observer.assertComplete();

    // When a subscription is set
    TestSubscriber<Clam> testSubscriber = new TestSubscriber<>();
    mRepository.get(clam.getUuid()).subscribe(testSubscriber);

    // Then links were only requested from remote source
    verify(mRemote).get(clam.getUuid());
    //
    assertFalse(mRepository.mCacheIsDirty);
    testSubscriber.assertValue(clam);
  }

  @Test
  public void getLink_repoCachesAfterFirstSubscription_whenLinkAvailableInRemoteStorage() {
    // Given that the remote data source has data available
    Clam clam = CLAMS.get(0);
    setItemAvailable(mRemote, clam);
    // and that the local data source has no data available
    setItemsNotAvailable(mLocal);
    // and that save data to local succeeds
    setItemAddSucceeds(mLocal);
    // and that a subscription is set to force refresh
    TestObserver observer = new TestObserver();
    mRepository.refresh().subscribe(observer);
    observer.assertComplete();

    // When two subscriptions are set
    TestSubscriber<Clam> testSubscriber1 = new TestSubscriber<>();
    mRepository.get(clam.getUuid()).subscribe(testSubscriber1);

    TestSubscriber<Clam> testSubscriber2 = new TestSubscriber<>();
    mRepository.get(clam.getUuid()).subscribe(testSubscriber2);

    // Then links were only requested from remote once
    verify(mRemote).get(clam.getUuid());
    //
    assertFalse(mRepository.mCacheIsDirty);
    testSubscriber1.assertValue(clam);
    testSubscriber2.assertValue(clam);
  }

  @Test
  public void getLink_requestLinkFromLocalStorage() {
    // Given that the local data source has data available
    Clam clam = CLAMS.get(0);
    setItemAvailable(mLocal, CLAMS.get(0));
    // and the remote data source doesn't have any data available
    setItemsNotAvailable(mRemote);

    // When a subscription is set to get item
    TestSubscriber<Clam> testSubscriber = new TestSubscriber<>();
    mRepository.get(clam.getUuid()).subscribe(testSubscriber);

    // Then item were only requested from local data source
    verify(mLocal).get(clam.getUuid());
    //
    assertFalse(mRepository.mCacheIsDirty);
    testSubscriber.assertValue(clam);
  }

  @Test
  public void getLink_fetchesFromRemote_whenLinkNotAvailableInLocalStorage() {
    // Given that the local data source doesn't have the particular link
    Clam clam = CLAMS.get(0);
    setItemNotAvailable(mLocal, clam.getUuid());
    // and that the remote data source has the particular link
    setItemAvailable(mRemote, clam);
    // and that save data to local succeeds
    setItemAddSucceeds(mLocal);

    // When a subscription is set
    TestSubscriber<Clam> testSubscriber = new TestSubscriber<>();
    mRepository.get(clam.getUuid()).subscribe(testSubscriber);

    // Then the remote storage is called
    verify(mRemote).get(clam.getUuid());
    // and the value is the expected one
    testSubscriber.assertValue(clam);
  }

  @Test
  public void saveLink_savesLinkToRemoteStorage() {
    // Given a stub item
    Clam newClam = new Clam("uid1");
    // and that save data to local succeeds
    setItemAddSucceeds(mLocal);
    // and that save data to remote succeeds
    setItemAddSucceeds(mRemote);

    // When a stub item is saved to the links repository
    mRepository.add(newClam).subscribe();

    // Then the remote and local storage are called and the cache is updated
    verify(mLocal).add(newClam);
    verify(mRemote).add(newClam);

    // Subscribes to cache assert value is there
    TestSubscriber<Clam> testSubscriber = new TestSubscriber<>();
    mCache.get(newClam.getUuid()).subscribe(testSubscriber);
    testSubscriber.assertValue(newClam);
  }

  @Test
  public void updateLink_updatesLinkToRemoteStorage() {
    // Given a stub item
    Clam newLink = new Clam("uid1");
    // and that update data to local succeeds
    setItemUpdateSucceeds(mLocal);
    // and that update data to remote succeeds
    setItemUpdateSucceeds(mRemote);

    // When a stub item is updated to the links repository
    mRepository.update(newLink).subscribe();

    // Then the remote and local storage are called and the cache is updated
    verify(mLocal).update(newLink);
    verify(mRemote).update(newLink);

    // Subscribes to cache assert value is there
    TestSubscriber<Clam> testSubscriber = new TestSubscriber<>();
    mCache.get(newLink.getUuid()).subscribe(testSubscriber);
    testSubscriber.assertValue(newLink);
  }

  @Test
  public void removeLink_removesLinkFromRemoteStorage() {
    // Given a stub item
    Clam newLink = CLAMS.get(0);
    // and that delete data from local succeeds
    setItemsRemoveSucceeds(mLocal);
    // and that delete data from remote succeeds
    setItemsRemoveSucceeds(mRemote);

    // When a stub item is removed from the links repository
    mRepository.remove(newLink).subscribe();

    // Then the remote and local storage are called and the cache is removed
    verify(mLocal).remove(newLink);
    verify(mRemote).remove(newLink);

    // Subscribes to cache assert value is not there
    TestSubscriber<Clam> testSubscriber = new TestSubscriber<>();
    mCache.get(newLink.getUuid()).subscribe(testSubscriber);
    testSubscriber.assertValueCount(0);
    testSubscriber.assertComplete();
  }

  @Test
  public void removeAll_removesLinksFromRemoteStorage() {
    // Given that delete data from local succeeds
    setItemsRemoveSucceeds(mLocal);
    // and that delete data from remote succeeds
    setItemsRemoveSucceeds(mRemote);

    // When a subscription is called to remove all
    mRepository.removeAll().subscribe();

    // Then the remote and local storage are called and the cache is removed
    verify(mLocal).removeAll();
    verify(mRemote).removeAll();

    // Subscribes to cache assert nothing is there
    TestSubscriber<List<Clam>> testSubscriber = new TestSubscriber<>();
    mCache.getAll().subscribe(testSubscriber);
    testSubscriber.assertValueCount(0);
    testSubscriber.assertComplete();
  }


  private void setItemsNotAvailable(BaseDataSource<Clam> dataSource) {
    when(dataSource.getAll()).thenReturn(Flowable.just(Collections.emptyList()));
  }

  private void setItemsAvailable(BaseDataSource<Clam> dataSource, List<Clam> clams) {
    // don't allow the data sources to complete.
    when(dataSource.getAll()).thenReturn(Flowable.just(clams).concatWith(Flowable.never()));
  }

  private void setItemNotAvailable(BaseDataSource<Clam> dataSource, String uuid) {
    when(dataSource.get(eq(uuid))).thenReturn(Flowable.empty());
  }

  private void setItemAvailable(BaseDataSource<Clam> dataSource, Clam clam) {
    when(dataSource.get(ArgumentMatchers.eq(clam.getUuid()))).thenReturn(Flowable.just(clam).concatWith(Flowable.never()));
  }

  private void setItemAddSucceeds(BaseDataSource<Clam> dataSource) {
    when(dataSource.add(any(Clam.class))).thenReturn(Completable.complete());
  }

  private void setItemUpdateSucceeds(BaseDataSource<Clam> dataSource) {
    when(dataSource.update(any(Clam.class))).thenReturn(Completable.complete());
  }

  private void setItemsRemoveSucceeds(BaseDataSource<Clam> dataSource) {
    when(dataSource.remove(any(Clam.class))).thenReturn(Completable.complete());
    when(dataSource.removeAll()).thenReturn(Completable.complete());
  }
}
