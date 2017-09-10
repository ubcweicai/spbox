package models;

import java.util.List;

import models.*;

import org.junit.*;

import com.avaje.ebean.Ebean;

import static org.junit.Assert.*;
import play.libs.Yaml;
import play.test.WithApplication;
import static play.test.Helpers.*;

public class ModelsTest extends WithApplication {
    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase()));
    }
    
    /*
    @Test
    public void createAndRetrieveUser() {
        new User("bob@gmail.com", "Bob", "secret").save();
        User bob = User.find.where().eq("email", "bob@gmail.com").findUnique();
        assertNotNull(bob);
        assertEquals("Bob", bob.name);
    }
    
    
    @Test
    public void tryAuthenticateUser() {
        new User("bob@gmail.com", "Bob", "secret").save();
        
        assertNotNull(User.authenticate("bob@gmail.com", "secret"));
        assertNull(User.authenticate("bob@gmail.com", "badpassword"));
        assertNull(User.authenticate("tom@gmail.com", "secret"));
    }
    
    @Test
    public void findProjectsInvolving() {
        new User("bob@gmail.com", "Bob", "secret").save();
        new User("jane@gmail.com", "Jane", "secret").save();

        Activity.create("Play 2", "play", "bob@gmail.com");
        Activity.create("Play 1", "play", "jane@gmail.com");

        List<Activity> results = Activity.findInvolving("bob@gmail.com");
        assertEquals(1, results.size());
        assertEquals("Play 2", results.get(0).name);
    }
    
    @Test
    public void findTodoTasksInvolving() {
        User bob = new User("bob@gmail.com", "Bob", "secret");
        bob.save();

        Activity project = Activity.create("Play 2", "play", "bob@gmail.com");
        Ticket t1 = new Ticket();
        t1.title = "Write tutorial";
        t1.assignedTo = bob;
        t1.done = true;
        t1.save();

        Ticket t2 = new Ticket();
        t2.title = "Release next version";
        t2.activity = project;
        t2.save();

        List<Ticket> results = Ticket.findTodoInvolving("bob@gmail.com");
        assertEquals(1, results.size());
        assertEquals("Release next version", results.get(0).title);
    }
    
    @Test
    public void fullTest() {
        Ebean.save((List) Yaml.load("test-data.yml"));

        // Count things
        assertEquals(3, User.find.findRowCount());
        assertEquals(7, Activity.find.findRowCount());
        assertEquals(5, Ticket.find.findRowCount());

        // Try to authenticate as users
        assertNotNull(User.authenticate("bob@example.com", "secret"));
        assertNotNull(User.authenticate("jane@example.com", "secret"));
        assertNull(User.authenticate("jeff@example.com", "badpassword"));
        assertNull(User.authenticate("tom@example.com", "secret"));

        // Find all Bob's projects
        List<Activity> bobsProjects = Activity.findInvolving("bob@example.com");
        assertEquals(5, bobsProjects.size());

        // Find all Bob's todo tasks
        List<Ticket> bobsTasks = Ticket.findTodoInvolving("bob@example.com");
        assertEquals(4, bobsTasks.size());
    }
    */
}