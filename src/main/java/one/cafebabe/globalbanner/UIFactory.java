package one.cafebabe.globalbanner;

import javax.swing.*;
import java.awt.*;

/**
 * Factory class for creating UI components for the Global Banner application.
 */
public class UIFactory {
    private final ScreenManager screenManager;
    private final WindowManager windowManager;

    /**
     * Creates a new UIFactory with the specified screen manager and window manager.
     * 
     * @param screenManager the screen manager to use
     * @param windowManager the window manager to use
     */
    public UIFactory(ScreenManager screenManager, WindowManager windowManager) {
        this.screenManager = screenManager;
        this.windowManager = windowManager;
    }

    /**
     * Creates a menu bar with a File menu containing screen selection options.
     * 
     * @param frame the JFrame to update when screen selection changes
     * @return the created menu bar
     */
    public JMenuBar createMenuBar(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();

        // Create File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Create Screen submenu
        JMenu screenMenu = new JMenu("Select Screen");
        fileMenu.add(screenMenu);

        // Add menu items for each available screen
        for (int i = 0; i < screenManager.getAvailableScreens().size(); i++) {
            GraphicsDevice screen = screenManager.getAvailableScreens().get(i);
            DisplayMode displayMode = screen.getDisplayMode();
            String screenName = "Screen " + (i + 1) + " (" + displayMode.getWidth() + " x " + displayMode.getHeight() + ")";

            // Add asterisk to indicate currently selected screen
            if (screen.equals(screenManager.getCurrentScreen())) {
                screenName += " *";
            }

            JMenuItem menuItem = new JMenuItem(screenName);
            final int screenIndex = i;

            menuItem.addActionListener(e -> {
                screenManager.setCurrentScreen(screenIndex);
                windowManager.positionWindowOnCurrentScreen();
                // Update menus to reflect the new screen selection
                if (frame != null) {
                    frame.setJMenuBar(createMenuBar(frame));
                    frame.revalidate();
                }
            });

            screenMenu.add(menuItem);
        }

        // Add exit option
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        return menuBar;
    }

    /**
     * Creates a popup menu for screen selection.
     * 
     * @param frame the JFrame to update when screen selection changes
     * @return the created popup menu
     */
    public JPopupMenu createScreenSelectionMenu(JFrame frame) {
        JPopupMenu menu = new JPopupMenu();

        JMenu screenMenu = new JMenu("Select Screen");
        menu.add(screenMenu);

        // Add menu items for each available screen
        for (int i = 0; i < screenManager.getAvailableScreens().size(); i++) {
            GraphicsDevice screen = screenManager.getAvailableScreens().get(i);
            DisplayMode displayMode = screen.getDisplayMode();
            String screenName = "Screen " + (i + 1) + " (" + displayMode.getWidth() + " x " + displayMode.getHeight() + ")";

            // Add asterisk to indicate currently selected screen
            if (screen.equals(screenManager.getCurrentScreen())) {
                screenName += " *";
            }

            JMenuItem menuItem = new JMenuItem(screenName);
            final int screenIndex = i;

            menuItem.addActionListener(e -> {
                screenManager.setCurrentScreen(screenIndex);
                windowManager.positionWindowOnCurrentScreen();
                // Update menus to reflect the new screen selection
                if (frame != null) {
                    frame.setJMenuBar(createMenuBar(frame));
                    frame.revalidate();
                }
            });

            screenMenu.add(menuItem);
        }

        return menu;
    }
}
