package dev.aclam.basedata;


import java.util.List;

import dev.aclam.basemodel.BaseModel;
import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Base class for data source
 * @param <M>
 */
public interface BaseDataSource<M extends BaseModel> {
  /**
   * Adds a {@link M}
   * @param item a {@link M}
   * @return a {@link Completable} emission
   */
  Completable add(M item);

  /**
   * Adds a list of {@link M}
   * @param items a list of {@link M}s
   * @return a {@link Completable} emission
   */
  Completable add(List<M> items);

  /**
   * Gets all the {@link M}s
   * @return a {@link Flowable} list of {@link M}s
   */
  Flowable<List<M>> getAll();

  /**
   * Gets a single {@link M} by id
   * @param uuid a {@link M}'s uuid
   * @return a {@link Flowable} {@link M}
   */
  Flowable<M> get(String uuid);

  /**
   * Updates a {@link M}
   * @param item a {@link M}
   * @return a {@link Completable} emission
   */
  Completable update(M item);

  /**
   * Removes a {@link M}
   * @param item a {@link M}
   * @return a {@link Completable} emission
   */
  Completable remove(M item);

  /**
   * Removes all the {@link M}s
   * @return a {@link Completable} emission
   */
  Completable removeAll();

  /**
   * Force refresh from datasources
   * @return a {@link Completable} emission
   */
  Completable refresh();
}
