package dev.aclam.basemodel;


/**
 * Base class for Models
 */
public interface BaseModel {
  /**
   * Unique row identifier for a {@link BaseModel}
   *
   * @return a UUID
   */
  String getUuid();

  /**
   * UNIX timestamp for a {@link BaseModel}
   *
   * @return a unix timestamp
   */
  default long getTimestamp() {
    return -1;
  }
}
