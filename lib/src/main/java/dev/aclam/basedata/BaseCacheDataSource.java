package dev.aclam.basedata;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dev.aclam.basemodel.BaseModel;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * In-Memory cache implementation of {@link BaseDataSource} of type {@link M}
 */
public abstract class BaseCacheDataSource<M extends BaseModel> implements BaseDataSource<M> {

  @NonNull
  private final Map<String, M> mCache;

  protected BaseCacheDataSource() {
    mCache = new LinkedHashMap<>();
  }

  @Override
  public Completable add(M m) {
    return Completable.fromAction(() -> mCache.put(m.getUuid(), m));
  }

  @Override
  public Completable add(List<M> vals) {
    return Completable.fromAction(() -> {
      for (M m : vals){
        mCache.put(m.getUuid(),m);
      }
    });
  }

  @Override
  public Flowable<List<M>> getAll() {
    Collection<M> vals = mCache.values();
    if (vals.isEmpty()) {
      return Flowable.empty();
    } else {
      return Flowable.just(new LinkedList<>(vals));
    }
  }

  @Override
  public Flowable<M> get(String uuid) {
    @Nullable M m = mCache.get(uuid);
    if (m != null) {
      return Flowable.just(m);
    } else {
      return Flowable.empty();
    }
  }

  @Override
  public Completable update(M m) {
    return Completable.fromAction(() -> mCache.put(m.getUuid(), m));
  }

  @Override
  public Completable remove(M m) {
    return Completable.fromAction(() -> mCache.remove(m.getUuid()));
  }

  @Override
  public Completable removeAll() {
    return Completable.fromAction(mCache::clear);
  }

  @Override
  public Completable refresh() {
    return removeAll();
  }
}
