package dev.aclam.basedata;

import dev.aclam.basemodel.BaseModel;
import io.reactivex.annotations.NonNull;

public class Clam implements BaseModel {

  @NonNull
  private final String mUuid;
  private boolean mIsOpen;

  public Clam(@NonNull String uuid){
    mUuid = uuid;
  }

  public boolean isOpen() {
    return mIsOpen;
  }

  public void setOpen(boolean open) {
    mIsOpen = open;
  }

  @Override
  public String getUuid() {
    return mUuid;
  }
}
