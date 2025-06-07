package one.cafebabe.globalbanner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Main class for the Global Banner application.
 * Creates a global floating window displaying an image.
 */
public class Main {
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
                // Create managers
                ScreenManager screenManager = new ScreenManager();
                WindowManager windowManager = new WindowManager(screenManager);
                UIFactory uiFactory = new UIFactory(screenManager, windowManager);

                // Detect all available screens
                screenManager.detectAvailableScreens();

                // Select the appropriate screen
                screenManager.selectDefaultScreen();

                // Create the floating window
                createGlobalFloatingBanner(windowManager, uiFactory);
            } catch (IOException e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        });
    }

    /**
     * Creates a global floating window that displays an image.
     * 
     * @param windowManager the window manager to use
     * @param uiFactory the UI factory to use
     * @throws IOException if the image cannot be loaded
     */
    private static void createGlobalFloatingBanner(
            WindowManager windowManager, 
            UIFactory uiFactory) throws IOException {
        // Load the image from resources
        InputStream imageStream = Main.class.getResourceAsStream("/image.png");
        if (imageStream == null) {
            throw new IOException("Image not found in resources");
        }

        BufferedImage image = ImageIO.read(imageStream);

        // Create a JFrame with no decorations
        JFrame frame = new JFrame("Global Banner");
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setType(Window.Type.UTILITY); // Makes it a floating utility window

        // Create and set the menu bar
        JMenuBar menuBar = uiFactory.createMenuBar(frame);
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
        JPanel panel = getJPanel(image);
        frame.add(panel);

        // Add a mouse listener to allow dragging the window
        MouseDragListener dragListener = new MouseDragListener(frame, windowManager);
        panel.addMouseListener(dragListener);
        panel.addMouseMotionListener(dragListener);

        // Add mouse enter listener to detect when mouse hovers over the window
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Move window to the opposite corner when mouse hovers over it
                windowManager.moveWindowToOppositeCorner();
            }
        });

        // Create a popup menu for screen selection
        JPopupMenu popupMenu = uiFactory.createScreenSelectionMenu(frame);

        // Add mouse right-click listener to show the popup menu
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e, popupMenu);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e, popupMenu);
                }
            }

            private void showPopupMenu(java.awt.event.MouseEvent e, JPopupMenu popupMenu) {
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

        // Set the frame in the window manager
        windowManager.setFrame(frame);

        // Position the window on the selected screen
        windowManager.positionWindowOnCurrentScreen();

        frame.setFocusable(true);
        frame.setVisible(true);
    }

    private static JPanel getJPanel(BufferedImage image) {
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
        return panel;
    }
}