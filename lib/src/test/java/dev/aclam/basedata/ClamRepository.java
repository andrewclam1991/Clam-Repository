package dev.aclam.basedata;

import dev.aclam.annotation.Cache;
import dev.aclam.annotation.Local;
import dev.aclam.annotation.Remote;

public class ClamRepository extends BaseRepository<Clam> {

  public ClamRepository(@Cache BaseDataSource<Clam> cache,
                        @Local BaseDataSource<Clam> local,
                        @Remote BaseDataSource<Clam> remote) {
    super(cache, local, remote);
  }
}
