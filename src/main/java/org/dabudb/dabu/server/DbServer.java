package org.dabudb.dabu.server;

import com.google.common.base.Stopwatch;
import com.google.protobuf.ByteString;
import org.dabudb.dabu.server.db.Db;
import org.dabudb.dabu.server.io.WriteAheadLog;

import org.dabudb.dabu.shared.protobufs.Request;
import org.dabudb.dabu.shared.protobufs.Request.GetReply;
import org.dabudb.dabu.shared.protobufs.Request.WriteReply;
import org.dabudb.dabu.shared.protobufs.Request.WriteRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The primary controller for the db. It receives input from a CommServer and forwards to a WAL (log) and db
 */
class DbServer {

  private static DbServer INSTANCE;

  private Db db;
  private WriteAheadLog writeAheadLog;

  public static DbServer get() {

    if (INSTANCE == null) {
      INSTANCE = new DbServer();
    }
    return INSTANCE;
  }

  private DbServer() {
    loadExistingDataFromWAL();
  }

  /**
   * Initialize fields with values that are determined by ServerSettings
   */
  private void init() {
    ServerSettings serverSettings = ServerSettings.getInstance();
    db = serverSettings.getDb();
    writeAheadLog = serverSettings.getWriteAheadLog();
  }

  private Db db() {
    if (db == null) {
      init();
    }
    return db;
  }

  private WriteAheadLog writeLog() {
    if (writeAheadLog == null) {
      init();
    }
    return writeAheadLog;
  }


  Request.WriteReply handleRequest(WriteRequest request, byte[] requestBytes) {
    try {
      writeLog().log(requestBytes);
      if (!request.getIsDelete()) {
        List<Request.DocumentKeyValue> documents = request.getWriteBody().getDocumentKeyValueList();
        Map<byte[], byte[]> documentMap = new HashMap<>();
        for (Request.DocumentKeyValue keyValue : documents) {
          documentMap.put(keyValue.getKey().toByteArray(), keyValue.getValue().toByteArray());
        }
        db().write(documentMap);
      } else {
        List<Request.Document> documents = request.getDeleteBody().getDocumentList();
        db().delete(documents);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    return WriteReply.newBuilder()
        .setRequestId(request.getHeader().getId())
        .setTimestamp(Instant.now().toEpochMilli())
        .build();
  }

  public Request.GetReply handleRequest(Request.GetRequest request) {
    List<ByteString> result = db().get(request.getBody().getKeyList());
    return GetReply.newBuilder()
        .setRequestId(request.getHeader().getId())
        .setTimestamp(Instant.now().toEpochMilli())
        .addAllDocumentBytes(result)
        .build();
  }

  /**
   * Replay the writeAheadLog, on startup, to initialize the in-memory data structures
   * <p>
   * TODO(lwhite): Review. This probably needs to lock the files or otherwise fully synchronize
   */
  private synchronized void loadExistingDataFromWAL() {

    int count = 0;
    Stopwatch stopwatch = Stopwatch.createStarted();
    while (writeLog().hasNext()) {

      byte[] requestBytes = writeLog().next();

      try {
        WriteRequest request = WriteRequest.parseFrom(requestBytes);
        if (!request.getIsDelete()) {
          List<Request.DocumentKeyValue> documents = request.getWriteBody().getDocumentKeyValueList();
          Map<byte[], byte[]> documentMap = new HashMap<>();
          for (Request.DocumentKeyValue keyValue : documents) {
            documentMap.put(keyValue.getKey().toByteArray(), keyValue.getValue().toByteArray());
          }
          db().write(documentMap);
        } else {
          List<Request.Document> documents = request.getDeleteBody().getDocumentList();
          Map<byte[], byte[]> documentMap = new HashMap<>();
          for (Request.Document document : documents) {
            documentMap.put(document.getKey().toByteArray(), document.toByteArray());
          }
          db().write(documentMap);
        }
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      count++;
      if (count % 100_000 == 0) {
        System.out.println("Loaded " + count + " documents ");
      }
    }
    System.out.println("Loaded " + count + " documents in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
  }

  private Request.ErrorCondition noErrorCondition() {
    return Request.ErrorCondition.newBuilder()
        .setErrorType(Request.ErrorType.NONE)
        .setDescription("")
        .build();
  }
}
