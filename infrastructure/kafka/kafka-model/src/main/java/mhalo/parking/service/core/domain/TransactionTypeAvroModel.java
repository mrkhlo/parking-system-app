/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package mhalo.parking.service.core.domain;
@org.apache.avro.specific.AvroGenerated
public enum TransactionTypeAvroModel implements org.apache.avro.generic.GenericEnumSymbol<TransactionTypeAvroModel> {
  DEBIT, CREDIT  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"TransactionTypeAvroModel\",\"namespace\":\"mhalo.parking.service.core.domain\",\"symbols\":[\"DEBIT\",\"CREDIT\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
}