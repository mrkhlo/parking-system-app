/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package mhalo.parking.service.core.domain;

import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.SchemaStore;
import org.apache.avro.specific.SpecificData;

@org.apache.avro.specific.AvroGenerated
public class ParkingApprovedEventAvroModel extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -6538389769750112864L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ParkingApprovedEventAvroModel\",\"namespace\":\"mhalo.parking.service.core.domain\",\"fields\":[{\"name\":\"parkingId\",\"type\":{\"type\":\"string\",\"logicalType\":\"uuid\"}},{\"name\":\"customerId\",\"type\":{\"type\":\"string\",\"logicalType\":\"uuid\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();
  static {
    MODEL$.addLogicalTypeConversion(new org.apache.avro.Conversions.UUIDConversion());
  }

  private static final BinaryMessageEncoder<ParkingApprovedEventAvroModel> ENCODER =
      new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<ParkingApprovedEventAvroModel> DECODER =
      new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<ParkingApprovedEventAvroModel> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<ParkingApprovedEventAvroModel> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<ParkingApprovedEventAvroModel> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this ParkingApprovedEventAvroModel to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a ParkingApprovedEventAvroModel from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a ParkingApprovedEventAvroModel instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static ParkingApprovedEventAvroModel fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private java.util.UUID parkingId;
  private java.util.UUID customerId;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public ParkingApprovedEventAvroModel() {}

  /**
   * All-args constructor.
   * @param parkingId The new value for parkingId
   * @param customerId The new value for customerId
   */
  public ParkingApprovedEventAvroModel(java.util.UUID parkingId, java.util.UUID customerId) {
    this.parkingId = parkingId;
    this.customerId = customerId;
  }

  @Override
  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }

  // Used by DatumWriter.  Applications should not call.
  @Override
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return parkingId;
    case 1: return customerId;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  private static final org.apache.avro.Conversion<?>[] conversions =
      new org.apache.avro.Conversion<?>[] {
      new org.apache.avro.Conversions.UUIDConversion(),
      new org.apache.avro.Conversions.UUIDConversion(),
      null
  };

  @Override
  public org.apache.avro.Conversion<?> getConversion(int field) {
    return conversions[field];
  }

  // Used by DatumReader.  Applications should not call.
  @Override
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: parkingId = (java.util.UUID)value$; break;
    case 1: customerId = (java.util.UUID)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'parkingId' field.
   * @return The value of the 'parkingId' field.
   */
  public java.util.UUID getParkingId() {
    return parkingId;
  }


  /**
   * Sets the value of the 'parkingId' field.
   * @param value the value to set.
   */
  public void setParkingId(java.util.UUID value) {
    this.parkingId = value;
  }

  /**
   * Gets the value of the 'customerId' field.
   * @return The value of the 'customerId' field.
   */
  public java.util.UUID getCustomerId() {
    return customerId;
  }


  /**
   * Sets the value of the 'customerId' field.
   * @param value the value to set.
   */
  public void setCustomerId(java.util.UUID value) {
    this.customerId = value;
  }

  /**
   * Creates a new ParkingApprovedEventAvroModel RecordBuilder.
   * @return A new ParkingApprovedEventAvroModel RecordBuilder
   */
  public static mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder newBuilder() {
    return new mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder();
  }

  /**
   * Creates a new ParkingApprovedEventAvroModel RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new ParkingApprovedEventAvroModel RecordBuilder
   */
  public static mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder newBuilder(mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder other) {
    if (other == null) {
      return new mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder();
    } else {
      return new mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder(other);
    }
  }

  /**
   * Creates a new ParkingApprovedEventAvroModel RecordBuilder by copying an existing ParkingApprovedEventAvroModel instance.
   * @param other The existing instance to copy.
   * @return A new ParkingApprovedEventAvroModel RecordBuilder
   */
  public static mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder newBuilder(mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel other) {
    if (other == null) {
      return new mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder();
    } else {
      return new mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder(other);
    }
  }

  /**
   * RecordBuilder for ParkingApprovedEventAvroModel instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ParkingApprovedEventAvroModel>
    implements org.apache.avro.data.RecordBuilder<ParkingApprovedEventAvroModel> {

    private java.util.UUID parkingId;
    private java.util.UUID customerId;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.parkingId)) {
        this.parkingId = data().deepCopy(fields()[0].schema(), other.parkingId);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.customerId)) {
        this.customerId = data().deepCopy(fields()[1].schema(), other.customerId);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
    }

    /**
     * Creates a Builder by copying an existing ParkingApprovedEventAvroModel instance
     * @param other The existing instance to copy.
     */
    private Builder(mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.parkingId)) {
        this.parkingId = data().deepCopy(fields()[0].schema(), other.parkingId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.customerId)) {
        this.customerId = data().deepCopy(fields()[1].schema(), other.customerId);
        fieldSetFlags()[1] = true;
      }
    }

    /**
      * Gets the value of the 'parkingId' field.
      * @return The value.
      */
    public java.util.UUID getParkingId() {
      return parkingId;
    }


    /**
      * Sets the value of the 'parkingId' field.
      * @param value The value of 'parkingId'.
      * @return This builder.
      */
    public mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder setParkingId(java.util.UUID value) {
      validate(fields()[0], value);
      this.parkingId = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'parkingId' field has been set.
      * @return True if the 'parkingId' field has been set, false otherwise.
      */
    public boolean hasParkingId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'parkingId' field.
      * @return This builder.
      */
    public mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder clearParkingId() {
      parkingId = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'customerId' field.
      * @return The value.
      */
    public java.util.UUID getCustomerId() {
      return customerId;
    }


    /**
      * Sets the value of the 'customerId' field.
      * @param value The value of 'customerId'.
      * @return This builder.
      */
    public mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder setCustomerId(java.util.UUID value) {
      validate(fields()[1], value);
      this.customerId = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'customerId' field has been set.
      * @return True if the 'customerId' field has been set, false otherwise.
      */
    public boolean hasCustomerId() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'customerId' field.
      * @return This builder.
      */
    public mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel.Builder clearCustomerId() {
      customerId = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ParkingApprovedEventAvroModel build() {
      try {
        ParkingApprovedEventAvroModel record = new ParkingApprovedEventAvroModel();
        record.parkingId = fieldSetFlags()[0] ? this.parkingId : (java.util.UUID) defaultValue(fields()[0]);
        record.customerId = fieldSetFlags()[1] ? this.customerId : (java.util.UUID) defaultValue(fields()[1]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<ParkingApprovedEventAvroModel>
    WRITER$ = (org.apache.avro.io.DatumWriter<ParkingApprovedEventAvroModel>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<ParkingApprovedEventAvroModel>
    READER$ = (org.apache.avro.io.DatumReader<ParkingApprovedEventAvroModel>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}










