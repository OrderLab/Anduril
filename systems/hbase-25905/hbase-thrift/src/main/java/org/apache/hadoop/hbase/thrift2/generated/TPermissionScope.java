/**
 * Autogenerated by Thrift Compiler (0.14.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.hadoop.hbase.thrift2.generated;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.14.1)", date = "2021-07-19")
public enum TPermissionScope implements org.apache.thrift.TEnum {
  TABLE(0),
  NAMESPACE(1);

  private final int value;

  private TPermissionScope(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  @org.apache.thrift.annotation.Nullable
  public static TPermissionScope findByValue(int value) { 
    switch (value) {
      case 0:
        return TABLE;
      case 1:
        return NAMESPACE;
      default:
        return null;
    }
  }
}
