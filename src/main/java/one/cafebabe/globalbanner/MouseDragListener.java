package one.cafebabe.globalbanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Helper class to allow dragging the window with the mouse.
 */
public class MouseDragListener extends MouseAdapter {
    private final JFrame frame;
    private final WindowManager windowManager;
    private Point dragStart = null;

    /**
     * Creates a new MouseDragListener for the specified frame.
     * 
     * @param frame the frame to drag
     * @param windowManager the window manager to use
     */
    public MouseDragListener(JFrame frame, WindowManager windowManager) {
        this.frame = frame;
        this.windowManager = windowManager;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragStart = e.getPoint();
        // Cancel any ongoing animations when starting a new drag
        windowManager.stopAnimation();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragStart = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragStart != null) {
            Point currentLocation = frame.getLocation();
            int newX = currentLocation.x + e.getX() - dragStart.x;
            int newY = currentLocation.y + e.getY() - dragStart.y;

            // During active dragging, we use immediate movement for responsive feel
            frame.setLocation(newX, newY);
        }
    }
}