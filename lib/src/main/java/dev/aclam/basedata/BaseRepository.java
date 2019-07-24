package dev.aclam.basedata;

import java.util.List;

import dev.aclam.annotation.Cache;
import dev.aclam.annotation.Local;
import dev.aclam.annotation.Remote;
import dev.aclam.basemodel.BaseModel;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;

/**
 * Repository, a facade of {@link BaseDataSource}s
 */
public abstract class BaseRepository<M extends BaseModel> implements BaseDataSource<M> {

  @NonNull
  private final BaseDataSource<M> mCache;

  @NonNull
  private final BaseDataSource<M> mLocal;

  @NonNull
  private final BaseDataSource<M> mRemote;

  /**
   * Allows client to mark cache as dirty and forces an update the next time data is requested
   */
  boolean mCacheIsDirty = false;

  protected BaseRepository(@NonNull @Cache BaseDataSource<M> cache,
                           @NonNull @Local BaseDataSource<M> local,
                           @NonNull @Remote BaseDataSource<M> remote) {
    mCache = cache;
    mLocal = local;
    mRemote = remote;
  }

  @Override
  public Completable add(M m) {
    return mCache.add(m).andThen(mLocal.add(m)).andThen(mRemote.add(m));
  }

  @Override
  public Completable add(List<M> ms) {
    return mCache.add(ms).andThen(mLocal.add(ms)).andThen(mRemote.add(ms));
  }

  @Override
  public Flowable<List<M>> getAll() {
    if (mCacheIsDirty) {
      return getAndCacheRemoteModels();
    } else {
      return mCache.getAll().switchIfEmpty(
          Flowable.concat(getAndCacheLocalModels(), getAndCacheRemoteModels())
              .filter(ms -> !ms.isEmpty())
              .firstOrError()
              .toFlowable()
      );
    }
  }

  @Override
  public Flowable<M> get(String uid) {
    if (mCacheIsDirty) {
      return getAndCacheRemoteModel(uid);
    } else {
      return mCache.get(uid).switchIfEmpty(
          Flowable.concat(getAndCacheLocalModel(uid), getAndCacheRemoteModel(uid))
              .firstOrError()
              .toFlowable()
      );
    }
  }

  @Override
  public Completable update(M m) {
    return mCache.update(m).andThen(mLocal.update(m)).andThen(mRemote.update(m));
  }

  @Override
  public Completable remove(M m) {
    return mCache.remove(m).andThen(mLocal.remove(m)).andThen(mRemote.remove(m));
  }

  @Override
  public Completable removeAll() {
    return mCache.removeAll().andThen(mLocal.removeAll()).andThen(mRemote.removeAll());
  }

  @Override
  public Completable refresh() {
    return Completable.fromAction(() -> mCacheIsDirty = true).andThen(mCache.refresh());
  }

  private Flowable<M> getAndCacheLocalModel(String uid) {
    return Flowable.defer(() -> mLocal.get(uid).flatMap(m -> mCache.add(m)
        .andThen(Flowable.just(m))));
  }

  private Flowable<M> getAndCacheRemoteModel(String uid) {
    return Flowable.defer(() -> mRemote.get(uid).flatMap(m -> mCache.add(m)
        .andThen(mLocal.add(m))
        .andThen(Flowable.just(m)))
        .doOnNext(link -> mCacheIsDirty = false)
    );
  }


  private Flowable<List<M>> getAndCacheLocalModels() {
    return Flowable.defer(() -> mLocal.getAll().flatMap(ms -> Flowable.fromIterable(ms)
        .flatMap(m -> mCache.add(m).andThen(Flowable.just(m)))
        .toList()
        .toFlowable())
    );
  }

  private Flowable<List<M>> getAndCacheRemoteModels() {
    return Flowable.defer(() -> mRemote.getAll().flatMap(ms -> Flowable.fromIterable(ms)
        .flatMap(m -> mCache.add(m)
            .andThen(mLocal.add(m))
            .andThen(Flowable.just(m)))
        .toList()
        .toFlowable())
        .doOnNext(links -> mCacheIsDirty = false)
    );
  }
}
