package org.daboodb.daboo.client;

import org.daboodb.daboo.shared.Document;
import org.daboodb.daboo.testutil.BasicTest;
import org.daboodb.daboo.testutil.Company;
import org.daboodb.daboo.testutil.Person;
import com.google.common.base.Stopwatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 *
 */
public class DbClientTest extends BasicTest {

  private final DbClient client = DbClient.get();

  @Override
  @Before
  public void setUp() throws Exception {
    super.tearDown();
    super.setUp();
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testWriteDocument() throws Exception {
    Company company = new Company("Drr");
    Stopwatch stopwatch = Stopwatch.createStarted();
    client.write(company);

    byte[] key = company.getKey();

    Document document1 = client.get(key);
    assertTrue(document1 != null);
    Company company1 = (Company) document1;
    assertEquals(company, company1);
    System.out.println("Round trip: " + stopwatch.elapsed(TimeUnit.MICROSECONDS));
    System.out.println(company.toString());
  }

  @Test
  public void testWriteDocument2() throws Exception {
    Company company = new Company("Drr");
    client.write(company);
  }

  @Test
  public void testWriteDocuments() throws Exception {
    List<Person> people = Person.createPeoples(6_000);
    List<Document> peopleDocs = new ArrayList<>();
    for (Person person : people) {
      Document document = person;
      peopleDocs.add(document);
    }

    Stopwatch stopwatch = Stopwatch.createStarted();
    for (Document document : peopleDocs) {
      client.write(document);
    }
    System.out.println("Wrote " + 6_000 + " objects in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
  }

  @Test
  public void testWriteDocuments2() throws Exception {
    int count = 6_000;
    List<Person> people = Person.createPeoples(count);
    List<Document> peopleDocs = new ArrayList<>();
    for (Person person : people) {
      peopleDocs.add(person);
    }

    Stopwatch stopwatch = Stopwatch.createStarted();
    client.write(peopleDocs);
    System.out.println("Wrote " + count + " objects in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
  }

  @Test
  public void testGet() throws Exception {
    DbClient client = DbClient.get();
    int testCount = 1_000;

    List<Person> people = Person.createPeoples(testCount);
    List<Document> peopleDocs = new ArrayList<>();
    List<byte[]> keys = new ArrayList<>();
    for (Person person : people) {
      peopleDocs.add(person);
      keys.add(person.getKey());
    }

    System.out.println("Test data created");
    // Write
    Stopwatch stopwatch = Stopwatch.createStarted();
    for (Document document : peopleDocs) {
      client.write(document);
    }
    System.out.println("Wrote " + testCount + " objects in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");

    stopwatch.reset().start();
    List<Document> documents = client.get(keys);
    assertEquals(testCount, documents.size());
    System.out.println("Read " + testCount + " objects in 1 batch " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
  }

  @Test
  public void testDelete() throws Exception {
    DbClient client = DbClient.get();
    int testCount = 1_000;

    List<Person> people = Person.createPeoples(testCount);
    List<Document> peopleDocs = new ArrayList<>();
    List<byte[]> keys = new ArrayList<>();
    for (Person person : people) {
      peopleDocs.add(person);
      keys.add(person.getKey());
    }

    System.out.println("Test data created");
    // Write
    Stopwatch stopwatch = Stopwatch.createStarted();
    for (Document document : peopleDocs) {
      client.write(document);
    }
    System.out.println("Wrote " + testCount + " objects in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");

    stopwatch.reset().start();
    client.delete(peopleDocs);
    System.out.println("Deleted "
        + testCount + " objects in 1 batch in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");

    stopwatch.reset().start();
    List<Document> documents = client.get(keys);
    assertEquals(0, documents.size());
    System.out.println("Read " + testCount + " objects in 1 batch in "
        + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
  }

  /**
   * Tests deletion of individual document
   */
  @Test
  public void testDelete2() throws Exception {
    DbClient client = DbClient.get();
    int testCount = 100;

    List<Person> people = Person.createPeoples(testCount);

    Document deleteDoc = null;
    int count = 0;
    List<Document> peopleDocs = new ArrayList<>();
    List<byte[]> keys = new ArrayList<>();

    for (Person person : people) {
      peopleDocs.add(person);
      keys.add(person.getKey());
      if (count == 49) {
        deleteDoc = person;
      }
      count++;
    }
    // Write
    client.write(peopleDocs);

    // Delete
    assertNotNull(deleteDoc);
    client.delete(deleteDoc);

    List<Document> documents = client.get(keys);
    assertEquals(99, documents.size());

    Document missing = client.get(deleteDoc.getKey());
    assertNull(missing);
  }
}