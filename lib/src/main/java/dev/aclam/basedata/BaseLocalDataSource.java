package dev.aclam.basedata;

import java.util.List;

import dev.aclam.basemodel.BaseModel;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;

/**
 * Concrete implementation of a {@link BaseDataSource} of type {@link M} on local
 */
public abstract class BaseLocalDataSource<M extends BaseModel, D extends BaseDataSource<M>>
    implements BaseDataSource<M> {

  @NonNull
  private final D mDao;

  protected BaseLocalDataSource(@NonNull D dao) {
    mDao = dao;
  }

  @Override
  public Completable add(M m) {
    return mDao.add(m);
  }

  @Override
  public Completable add(List<M> ms) {
    return mDao.add(ms);
  }

  @Override
  public Flowable<List<M>> getAll() {
    return mDao.getAll();
  }

  @Override
  public Flowable<M> get(String uuid) {
    return mDao.get(uuid);
  }

  @Override
  public Completable update(M m) {
    return mDao.update(m);
  }

  @Override
  public Completable remove(M m) {
    return mDao.remove(m);
  }

  @Override
  public Completable removeAll() {
    return mDao.removeAll();
  }

  @Override
  public Completable refresh() {
    return mDao.refresh();
  }
}
