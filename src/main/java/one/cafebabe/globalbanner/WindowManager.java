package one.cafebabe.globalbanner;

import javax.swing.*;
import java.awt.*;

/**
 * Manages window positioning and animation for the Global Banner application.
 */
public class WindowManager {
    /**
     * Margin in pixels to keep from screen edges
     */
    private static final int MARGIN = 50;

    /**
     * Flag to track if the window is currently at the bottom left corner
     */
    private boolean isAtBottomLeft = false;

    /**
     * Timer for window movement animation
     */
    private Timer animationTimer = null;

    /**
     * The main application frame
     */
    private JFrame frame;

    /**
     * The screen manager to use for screen-related operations
     */
    private final ScreenManager screenManager;

    /**
     * Creates a new WindowManager with the specified screen manager.
     * 
     * @param screenManager the screen manager to use
     */
    public WindowManager(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Sets the frame to be managed by this window manager.
     * 
     * @param frame the frame to manage
     */
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * Positions the window on the currently selected screen.
     */
    public void positionWindowOnCurrentScreen() {
        if (frame != null && screenManager.getCurrentScreen() != null) {
            // Get the bounds of the selected screen
            Rectangle screenBounds = screenManager.getCurrentScreen().getDefaultConfiguration().getBounds();

            // Calculate position (bottom right with margin)
            int x = screenBounds.x + screenBounds.width - frame.getWidth() - MARGIN;
            int y = screenBounds.y + screenBounds.height - frame.getHeight() - MARGIN;

            // Set window location (initially at bottom right, isAtBottomLeft is false by default)
            frame.setLocation(x, y);
            isAtBottomLeft = false;
        }
    }

    /**
     * Moves the window to the opposite corner (bottom left or bottom right)
     * based on its current position.
     */
    public void moveWindowToOppositeCorner() {
        if (screenManager.getCurrentScreen() == null || frame == null) {
            return;
        }

        // Get the bounds of the selected screen
        Rectangle screenBounds = screenManager.getCurrentScreen().getDefaultConfiguration().getBounds();

        // Calculate new position based on current state
        int targetX;
        if (isAtBottomLeft) {
            // Move to bottom right
            targetX = screenBounds.x + screenBounds.width - frame.getWidth() - MARGIN;
            isAtBottomLeft = false;
        } else {
            // Move to bottom left
            targetX = screenBounds.x + MARGIN;
            isAtBottomLeft = true;
        }

        // Y position is always at the bottom with margin
        int targetY = screenBounds.y + screenBounds.height - frame.getHeight() - MARGIN;

        // Get current position
        Point currentLocation = frame.getLocation();

        // Animate the window movement
        animateWindowMovement(currentLocation.x, currentLocation.y, targetX, targetY);
    }

    /**
     * Animates the window movement from current position to target position.
     * The animation takes 0.5 seconds with easing (acceleration and deceleration).
     * 
     * @param startX starting X coordinate
     * @param startY starting Y coordinate
     * @param endX ending X coordinate
     * @param endY ending Y coordinate
     */
    private void animateWindowMovement(int startX, int startY, int endX, int endY) {
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
    private double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }

    /**
     * Stops any ongoing animation.
     */
    public void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }
}