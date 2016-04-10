package org.dabudb.dabu.shared.msg;

import org.dabudb.dabu.shared.compression.CompressionType;
import org.dabudb.dabu.shared.compression.CompressorDeCompressor;
import org.dabudb.dabu.shared.compression.CompressorFactory;
import org.dabudb.dabu.shared.encryption.EncryptorDecryptor;
import org.dabudb.dabu.shared.encryption.EncryptorFactory;
import org.dabudb.dabu.shared.msg.serialization.MessageSerializerFactory;
import org.dabudb.dabu.shared.msg.serialization.MsgSerializerDeserializer;
import org.dabudb.dabu.shared.msg.serialization.MsgSerializerType;

/**
 *
 */
public class MessagePipe {

  private EncryptorDecryptor encryptorDecryptor;
  private CompressorDeCompressor compressorDeCompressor;
  private MsgSerializerDeserializer serializerDeserializer;

  /**
   * Creates a messagePipe with no encryption, and the other filters as defined
   */
  public static MessagePipe create(CompressionType compressionType, MsgSerializerType serializerType) {
    CompressorDeCompressor compressor = CompressorFactory.get(compressionType);
    EncryptorDecryptor encryptor = EncryptorFactory.NONE;
    MsgSerializerDeserializer serializer = MessageSerializerFactory.get(serializerType);

    return new MessagePipe(
        compressor,
        encryptor,
        serializer);
  }


  public MessagePipe(
      CompressorDeCompressor compressorDeCompressor,
      EncryptorDecryptor encryptorDecryptor,
      MsgSerializerDeserializer serializerDeserializer) {
    this.encryptorDecryptor = encryptorDecryptor;
    this.compressorDeCompressor = compressorDeCompressor;
    this.serializerDeserializer = serializerDeserializer;
  }

  public byte[] messageToBytes(Message message) {
    return
        encryptorDecryptor.encrypt(
            compressorDeCompressor.compress(
                serializerDeserializer.serialize(message)));
  }

  public Message bytesToMessage(Class messageClass, byte[] contentAsBytes) {
    return
        serializerDeserializer.deserialize(
            messageClass,
            compressorDeCompressor.decompress(
                encryptorDecryptor.decrypt(contentAsBytes)));
  }

}