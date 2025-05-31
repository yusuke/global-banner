package one.cafebabe.globalbanner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for the Global Banner application.
 * Creates a global floating window displaying an image.
 */
public class Main {
    /**
     * Margin in pixels to keep from screen edges
     */
    private static final int SCREEN_EDGE_MARGIN = 50;

    /**
     * Flag to track if the window is currently at the bottom left corner
     */
    private static boolean isAtBottomLeft = false;

    /**
     * Timer for window movement animation
     */
    private static Timer animationTimer = null;

    /**
     * List of all available screens
     */
    private static List<GraphicsDevice> availableScreens = new ArrayList<>();

    /**
     * Currently selected screen
     */
    private static GraphicsDevice currentScreen = null;

    /**
     * The main application frame
     */
    private static JFrame frame = null;
    /**
     * Main entry point for the application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Global Banner Application");

        // Set the menu bar to appear in the macOS global menu bar
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        // Set the application name to appear in the macOS menu
        System.setProperty("apple.awt.application.name", "Global Banner");

        SwingUtilities.invokeLater(() -> {
            try {
                // Detect all available screens
                detectAvailableScreens();

                // Select the appropriate screen
                selectDefaultScreen();

                // Create the floating window
                createGlobalFloatingWindow();
            } catch (IOException e) {
                System.err.println("Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Detects all available screens in the system.
     */
    private static void detectAvailableScreens() {
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
    private static void selectDefaultScreen() {
        if (availableScreens.isEmpty()) {
            System.err.println("No screens detected. Using default screen.");
            currentScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        } else if (availableScreens.size() == 1) {
            System.out.println("Only one screen available. Using it.");
            currentScreen = availableScreens.get(0);
        } else {
            System.out.println("Multiple screens available. Using the second screen.");
            currentScreen = availableScreens.get(1); // Use the second screen (index 1)
        }

        System.out.println("Selected screen: " + currentScreen.getIDstring());
    }

    /**
     * Creates a global floating window that displays an image.
     * 
     * @throws IOException if the image cannot be loaded
     */
    private static void createGlobalFloatingWindow() throws IOException {
        // Load the image from resources
        InputStream imageStream = Main.class.getResourceAsStream("/image.png");
        if (imageStream == null) {
            throw new IOException("Image not found in resources");
        }

        BufferedImage image = ImageIO.read(imageStream);

        // Create a JFrame with no decorations
        frame = new JFrame("Global Banner");
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setType(Window.Type.UTILITY); // Makes it a floating utility window

        // Create and set the menu bar
        JMenuBar menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);

        // Set the frame to be transparent
        frame.setBackground(new Color(0, 0, 0, 0));

        // Make the content pane transparent
        Container contentPane = frame.getContentPane();
        contentPane.setBackground(new Color(0, 0, 0, 0));
        if (contentPane instanceof JComponent) {
            ((JComponent) contentPane).setOpaque(false);
        }

        // Create a panel to display the image with transparency
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to avoid filling the background
                if (g instanceof Graphics2D g2d) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                }
                g.drawImage(image, 0, 0, this);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };

        panel.setOpaque(false);
        panel.setBackground(new Color(0, 0, 0, 0));
        panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.add(panel);

        // Add a mouse listener to allow dragging the window
        MouseDragListener dragListener = new MouseDragListener(frame);
        panel.addMouseListener(dragListener);
        panel.addMouseMotionListener(dragListener);

        // Add mouse enter listener to detect when mouse hovers over the window
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Move window to the opposite corner when mouse hovers over it
                moveWindowToOppositeCorner(frame);
            }
        });

        // Create a popup menu for screen selection
        JPopupMenu popupMenu = createScreenSelectionMenu();

        // Add mouse right-click listener to show the popup menu
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            private void showPopupMenu(java.awt.event.MouseEvent e) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        // Add a key listener to close the application when Escape is pressed
        frame.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });

        // Enable window transparency
        frame.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", false);

        // Set the window to be translucent
        frame.setOpacity(1.0f);

        // Position the window in the bottom right corner of the screen with margin
        frame.pack();

        // Position the window on the selected screen
        positionWindowOnCurrentScreen();

        frame.setFocusable(true);
        frame.setVisible(true);
    }

    /**
     * Creates a menu bar with a File menu containing screen selection options.
     * 
     * @return the created menu bar
     */
    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Create File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Create Screen submenu
        JMenu screenMenu = new JMenu("Select Screen");
        fileMenu.add(screenMenu);

        // Add menu items for each available screen
        for (int i = 0; i < availableScreens.size(); i++) {
            GraphicsDevice screen = availableScreens.get(i);
            DisplayMode displayMode = screen.getDisplayMode();
            String screenName = "Screen " + (i + 1) + " (" + displayMode.getWidth() + " x " + displayMode.getHeight() + ")";

            // Add asterisk to indicate currently selected screen
            if (screen.equals(currentScreen)) {
                screenName += " *";
            }

            JMenuItem menuItem = new JMenuItem(screenName);
            final int screenIndex = i;

            menuItem.addActionListener(e -> {
                currentScreen = availableScreens.get(screenIndex);
                System.out.println("Switched to screen: " + currentScreen.getIDstring());
                positionWindowOnCurrentScreen();
                // Update menus to reflect the new screen selection
                if (frame != null) {
                    frame.setJMenuBar(createMenuBar());
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
     * @return the created popup menu
     */
    private static JPopupMenu createScreenSelectionMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenu screenMenu = new JMenu("Select Screen");
        menu.add(screenMenu);

        // Add menu items for each available screen
        for (int i = 0; i < availableScreens.size(); i++) {
            GraphicsDevice screen = availableScreens.get(i);
            DisplayMode displayMode = screen.getDisplayMode();
            String screenName = "Screen " + (i + 1) + " (" + displayMode.getWidth() + " x " + displayMode.getHeight() + ")";

            // Add asterisk to indicate currently selected screen
            if (screen.equals(currentScreen)) {
                screenName += " *";
            }

            JMenuItem menuItem = new JMenuItem(screenName);
            final int screenIndex = i;

            menuItem.addActionListener(e -> {
                currentScreen = availableScreens.get(screenIndex);
                System.out.println("Switched to screen: " + currentScreen.getIDstring());
                positionWindowOnCurrentScreen();
                // Update menus to reflect the new screen selection
                if (frame != null) {
                    frame.setJMenuBar(createMenuBar());
                    frame.revalidate();
                }
            });

            screenMenu.add(menuItem);
        }

        return menu;
    }

    /**
     * Positions the window on the currently selected screen.
     */
    private static void positionWindowOnCurrentScreen() {
        if (frame != null && currentScreen != null) {
            // Get the bounds of the selected screen
            Rectangle screenBounds = currentScreen.getDefaultConfiguration().getBounds();

            // Calculate position (bottom right with margin)
            int x = screenBounds.x + screenBounds.width - frame.getWidth() - SCREEN_EDGE_MARGIN;
            int y = screenBounds.y + screenBounds.height - frame.getHeight() - SCREEN_EDGE_MARGIN;

            // Set window location (initially at bottom right, isAtBottomLeft is false by default)
            frame.setLocation(x, y);
            isAtBottomLeft = false;
        }
    }

    /**
     * Moves the window to the opposite corner (bottom left or bottom right)
     * based on its current position.
     * 
     * @param frame the JFrame to reposition
     */
    private static void moveWindowToOppositeCorner(JFrame frame) {
        if (currentScreen == null) {
            return;
        }

        // Get the bounds of the selected screen
        Rectangle screenBounds = currentScreen.getDefaultConfiguration().getBounds();

        // Calculate new position based on current state
        int targetX;
        if (isAtBottomLeft) {
            // Move to bottom right
            targetX = screenBounds.x + screenBounds.width - frame.getWidth() - SCREEN_EDGE_MARGIN;
            isAtBottomLeft = false;
        } else {
            // Move to bottom left
            targetX = screenBounds.x + SCREEN_EDGE_MARGIN;
            isAtBottomLeft = true;
        }

        // Y position is always at the bottom with margin
        int targetY = screenBounds.y + screenBounds.height - frame.getHeight() - SCREEN_EDGE_MARGIN;

        // Get current position
        Point currentLocation = frame.getLocation();

        // Animate the window movement
        animateWindowMovement(frame, currentLocation.x, currentLocation.y, targetX, targetY);
    }

    /**
     * Animates the window movement from current position to target position.
     * The animation takes 0.5 seconds with easing (acceleration and deceleration).
     * 
     * @param frame the JFrame to animate
     * @param startX starting X coordinate
     * @param startY starting Y coordinate
     * @param endX ending X coordinate
     * @param endY ending Y coordinate
     */
    private static void animateWindowMovement(JFrame frame, int startX, int startY, int endX, int endY) {
        // Stop any existing animation
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        // Animation duration in milliseconds (0.5 seconds)
        final int DURATION = 150;

        // Animation update interval in milliseconds (smoother with smaller values)
        final int INTERVAL = 3; // ~60fps

        // Total number of steps in the animation
        final int STEPS = DURATION / INTERVAL;

        // Current step counter
        final int[] currentStep = {0};

        // Create new animation timer
        animationTimer = new Timer(INTERVAL, e -> {
            // Calculate progress (0.0 to 1.0)
            double progress = (double) currentStep[0] / STEPS;

            // Apply easing function (ease in-out)
            double easedProgress = easeInOutQuad(progress);

            // Calculate current position
            int x = startX + (int) (easedProgress * (endX - startX));
            int y = startY + (int) (easedProgress * (endY - startY));

            // Update window position
            frame.setLocation(x, y);

            // Increment step counter
            currentStep[0]++;

            // Stop timer when animation is complete
            if (currentStep[0] > STEPS) {
                // Ensure final position is exact
                frame.setLocation(endX, endY);
                animationTimer.stop();
            }
        });

        // Start the animation
        animationTimer.start();
    }

    /**
     * Easing function for smooth acceleration and deceleration.
     * This is a quadratic ease-in-out function.
     * 
     * @param t progress value between 0.0 and 1.0
     * @return eased value between 0.0 and 1.0
     */
    private static double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }

    /**
     * Helper class to allow dragging the window with the mouse.
     */
    private static class MouseDragListener extends java.awt.event.MouseAdapter {
        private final JFrame frame;
        private Point dragStart = null;

        public MouseDragListener(JFrame frame) {
            this.frame = frame;
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            dragStart = e.getPoint();
            // Cancel any ongoing animations when starting a new drag
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
        }

        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
            dragStart = null;
        }

        @Override
        public void mouseDragged(java.awt.event.MouseEvent e) {
            if (dragStart != null) {
                Point currentLocation = frame.getLocation();
                int newX = currentLocation.x + e.getX() - dragStart.x;
                int newY = currentLocation.y + e.getY() - dragStart.y;

                // During active dragging, we use immediate movement for responsive feel
                frame.setLocation(newX, newY);
            }
        }
    }
}
