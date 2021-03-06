package org.daboodb.daboo.server;

import java.io.File;

/**
 * A database administer
 */
interface DatabaseAdmin {

  /**
   * Clears the primary database storage and the WAL
   */
  void clear();

  /**
   * Exports the entire serialized of the database to the given backup file
   */
  void export(File backupFile);

  /**
   * Exports the entire serialized of the database to the given backup file,
   * and clears the WAL to the point of backup
   */
  void backup(File backupFile);

  /**
   * Reads all the data from the backupFile into the database
   */
  void recoverFromBackup(File backupFile);
}
