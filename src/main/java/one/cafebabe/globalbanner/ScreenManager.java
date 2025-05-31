package one.cafebabe.globalbanner;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages screen detection and selection for the Global Banner application.
 */
public class ScreenManager {
    /**
     * List of all available screens
     */
    private final List<GraphicsDevice> availableScreens = new ArrayList<>();

    /**
     * Currently selected screen
     */
    private GraphicsDevice currentScreen = null;

    /**
     * Detects all available screens in the system.
     */
    public void detectAvailableScreens() {
        availableScreens.clear();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        for (GraphicsDevice screen : screens) {
            availableScreens.add(screen);
            System.out.println("Detected screen: " + screen.getIDstring());
        }

        System.out.println("Total screens detected: " + availableScreens.size());
    }

    /**
     * Selects the default screen based on availability.
     * If only one screen is available, it selects that screen.
     * If multiple screens are available, it selects the second screen.
     */
    public void selectDefaultScreen() {
        if (availableScreens.isEmpty()) {
            System.err.println("No screens detected. Using default screen.");
            currentScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        } else if (availableScreens.size() == 1) {
            System.out.println("Only one screen available. Using it.");
            currentScreen = availableScreens.getFirst();
        } else {
            System.out.println("Multiple screens available. Using the second screen.");
            currentScreen = availableScreens.get(1); // Use the second screen (index 1)
        }

        System.out.println("Selected screen: " + currentScreen.getIDstring());
    }

    /**
     * Gets the currently selected screen.
     * 
     * @return the currently selected screen
     */
    public GraphicsDevice getCurrentScreen() {
        return currentScreen;
    }

    /**
     * Sets the current screen to the specified screen.
     * 
     * @param screenIndex the index of the screen to select
     */
    public void setCurrentScreen(int screenIndex) {
        if (screenIndex >= 0 && screenIndex < availableScreens.size()) {
            currentScreen = availableScreens.get(screenIndex);
            System.out.println("Switched to screen: " + currentScreen.getIDstring());
        }
    }

    /**
     * Gets the list of all available screens.
     * 
     * @return the list of available screens
     */
    public List<GraphicsDevice> getAvailableScreens() {
        return availableScreens;
    }
}