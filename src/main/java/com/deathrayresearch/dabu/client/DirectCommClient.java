package com.deathrayresearch.dabu.client;

import com.deathrayresearch.dabu.server.DbServer;
import com.deathrayresearch.dabu.shared.msg.DeleteReply;
import com.deathrayresearch.dabu.shared.msg.DocDeleteRequest;
import com.deathrayresearch.dabu.shared.msg.DocWriteRequest;
import com.deathrayresearch.dabu.shared.msg.DocsDeleteRequest;
import com.deathrayresearch.dabu.shared.msg.DocsGetRequest;
import com.deathrayresearch.dabu.shared.msg.DocsWriteRequest;
import com.deathrayresearch.dabu.shared.msg.GetReply;
import com.deathrayresearch.dabu.shared.msg.DocGetRequest;
import com.deathrayresearch.dabu.shared.msg.Reply;
import com.deathrayresearch.dabu.shared.msg.Request;
import com.deathrayresearch.dabu.shared.msg.WriteReply;

/**
 * A communication client that communicates directly with an in-process db server
 */
public class DirectCommClient implements CommClient {

  private final DbServer dbServer;

  public DirectCommClient() {
    dbServer = DbServer.INSTANCE;
  }

  @Override
  public GetReply sendRequest(DocGetRequest request) {
    return (GetReply) dbServer.handleRequest(request);
  }

  @Override
  public GetReply sendRequest(DocsGetRequest request) {
    return (GetReply) dbServer.handleRequest(request);
  }

  @Override
  public DeleteReply sendRequest(DocDeleteRequest request) {
    return (DeleteReply) dbServer.handleRequest(request);
  }

  @Override
  public DeleteReply sendRequest(DocsDeleteRequest request) {
    return (DeleteReply) dbServer.handleRequest(request);
  }

  @Override
  public WriteReply sendRequest(DocWriteRequest request) {
    return (WriteReply) dbServer.handleRequest(request);
  }

  @Override
  public WriteReply sendRequest(DocsWriteRequest request) {
    return (WriteReply) dbServer.handleRequest(request);
  }
}
