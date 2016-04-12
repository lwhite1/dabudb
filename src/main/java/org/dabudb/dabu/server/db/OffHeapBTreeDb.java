package org.dabudb.dabu.server.db;

import com.google.protobuf.ByteString;
import org.dabudb.dabu.shared.protobufs.Request;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An in-memory implementation of a Db
 */
public class OffHeapBTreeDb implements Db {

  private final long ALLOCATED_SIZE_IN_BYTES = 1024 * 1024 * 1024;  // 1 GB

  private final DB db = DBMaker
      .memoryDB()
      .allocateStartSize(ALLOCATED_SIZE_IN_BYTES)
      .make();

  private final BTreeMap<byte[],byte[]> store = db
      .treeMap("treemap", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY)
      .make();

  @Override
  public void write(List<Request.Document> documentList) {
    Map<byte[], byte[]> documentMap = new HashMap<>();
    for (Request.Document doc : documentList) {
      documentMap.put(doc.getKey().toByteArray(), doc.toByteArray());
    }
    store.putAll(documentMap);
  }

  @Override
  public void delete(List<Request.Document> documents) {
    for (Request.Document doc : documents) {
      store.remove(doc.getKey().toByteArray());
    }
  }

  @Override
  public List<ByteString> get(List<ByteString> keys) {
    List<ByteString> docs = new ArrayList<>();
    for (ByteString key : keys) {

      byte[] result = store.get(key.toByteArray());
      if (result != null) {
        docs.add(ByteString.copyFrom(result));
      }
    }
    return docs;
  }

  @Override
  public Iterator<Map.Entry<byte[], byte[]>> iterator() {
    return store.entrySet().iterator();
  }
}