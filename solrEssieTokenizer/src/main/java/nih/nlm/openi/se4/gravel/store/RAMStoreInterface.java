//--------------------------------------------------------------------------------------------------------
// RAMStoreInterface.java
//--------------------------------------------------------------------------------------------------------

package gravel.store;

//--------------------------------------------------------------------------------------------------------
// RAMStoreInterface
//--------------------------------------------------------------------------------------------------------

public interface RAMStoreInterface extends StoreInterface {

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(String inFilename, boolean inCompact);

  public void store(String inFilename);

}

