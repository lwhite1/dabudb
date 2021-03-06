package org.daboodb.daboo.server;

import org.daboodb.daboo.server.db.Db;
import org.daboodb.daboo.server.io.NullLog;
import org.daboodb.daboo.server.io.WriteAheadLog;
import org.daboodb.daboo.server.io.WriteLog;
import org.daboodb.daboo.shared.exceptions.StartupException;
import org.daboodb.daboo.shared.serialization.DocumentSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration settings for a Dabu server, or any Dabu server running in embedded mode
 */
public class ServerSettings {

  private DocumentSerializer documentSerializer;

  private Db db;

  private String databaseDirectory;

  private WriteAheadLog writeAheadLog;

  //private CommServer commServer = new DirectCommServer();
  private CommServer commServer;

  private static ServerSettings ourInstance;

  public static ServerSettings getInstance() {
    if (ourInstance == null) {
      ourInstance = loadServerSettings();
    }
    return ourInstance;
  }

  /**
   * Returns ServerSettings initialized from the given Properties object
   */
  public static ServerSettings create(Properties properties) {
    ourInstance = new ServerSettings(properties);
    return ourInstance;
  }

  /**
   * Returns ServerSettings initialized from the given Properties object
   */
  private ServerSettings(Properties properties) {

    setDocumentSerializer(properties);
    setDatabaseDirectory(properties);
    setDb(properties);
    setWriteAheadLog(properties);
    setCommServer(properties);
  }

  public DocumentSerializer getDocumentSerializer() {
    return documentSerializer;
  }

  public Db getDb() {
    return db;
  }

  WriteAheadLog getWriteAheadLog() {
    return writeAheadLog;
  }

  public CommServer getCommServer() {
    return commServer;
  }

  private void setDocumentSerializer(Properties properties) {
    try {
      this.documentSerializer =
          (DocumentSerializer)
              Class.forName(String.valueOf(properties.getProperty("document.serializer.class"))).newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      throw new RuntimeException("Unable to load DocumentSerializer as specified in server.properties", e);
    }
  }

  private void setDb(Properties properties) {
    try {
      this.db = (Db) Class.forName(String.valueOf(properties.getProperty("db.class"))).newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      throw new StartupException("Unable to load db as specified in server.properties", e);
    }
  }

  private void setWriteAheadLog(Properties properties) {

    String folderName = String.valueOf(properties.getProperty("db.write_ahead_log.folderName"));

    String writeAheadClassName = String.valueOf(properties.getProperty("db.write_ahead_log.class"));

    Path logFolderPath = Paths.get(getDatabaseDirectory(), folderName);
    File logFolder = logFolderPath.toFile();
    if (writeAheadClassName.equals(WriteLog.class.getCanonicalName())) {
      this.writeAheadLog = WriteLog.getInstance(logFolder);
    } else if (writeAheadClassName.equals(NullLog.class.getCanonicalName())) {
      this.writeAheadLog = NullLog.getInstance(logFolder);
    } else {
      throw new StartupException("The Write Ahead Log is not configured.");
    }
  }

  private void setCommServer(Properties properties) {
    try {
      this.commServer =
          (CommServer)
              Class.forName(String.valueOf(properties.getProperty("comm.server.class"))).newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      throw new StartupException("Unable to load CommServer as specified in server.properties", e);
    }
  }

  private void setDatabaseDirectory(Properties properties) {
    databaseDirectory = String.valueOf(properties.getProperty("db.folderName"));
  }

  String getDatabaseDirectory() {
    return databaseDirectory;
  }

  private static ServerSettings loadServerSettings() {
    Properties serverProperties = new Properties();
    try {
      FileInputStream inputStream = new FileInputStream("src/main/resources/server.properties");
      serverProperties.load(inputStream);
      inputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
      throw new StartupException("Unable to load server settings as Java properties", e);
    }
    return new ServerSettings(serverProperties);
  }
}
