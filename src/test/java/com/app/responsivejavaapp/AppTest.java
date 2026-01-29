package com.app.responsivejavaapp;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import com.app.responsivejavaapp.view.MainWindow;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

/**
 * Unit tests for Responsive Java App.
 */
public class AppTest {

    private MainWindow window;

    @Before
    public void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            window = new MainWindow();
        });
    }

    @After
    public void tearDown() throws Exception {
        if (window != null) {
            SwingUtilities.invokeAndWait(() -> {
                window.dispose();
            });
        }
    }

    @Test
    public void testMainWindowInitialization() throws Exception {
        assertNotNull("MainWindow should be created", window);
        assertNotNull("Status label should be initialized", window.getStatusLabel());
        assertNotNull("Action button should be initialized", window.getActionButton());
        assertEquals("Window title should match", "Responsive Java App", window.getTitle());
    }

    @Test
    public void testButtonClickUpdatesStatus() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            window.getActionButton().doClick();
        });

        // Wait for UI update
        Thread.sleep(100);

        SwingUtilities.invokeAndWait(() -> {
            String statusText = window.getStatusLabel().getText();
            assertEquals("Status should update after button click", "Action performed!", statusText);
        });
    }

    @Test
    public void testBasicAssertions() {
        assertTrue("This should always pass", true);
        assertEquals("Basic math should work", 2, 1 + 1);
        assertNotNull("Strings should not be null", "test");
    }
}
