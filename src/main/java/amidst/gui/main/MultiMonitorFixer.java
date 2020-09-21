package amidst.gui.main;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import amidst.logging.AmidstLogger;

/**
 * Listener that patches AWT bugs with multiple monitors.
 */
public class MultiMonitorFixer implements ComponentListener {
	private final JFrame frame;
	private final Method setGCMethod;
	private boolean errorPrinted = false;
	
	public MultiMonitorFixer(JFrame frame) {
		this.frame = frame;
		this.setGCMethod = getGCMethod();
	}
	
	private static Method getGCMethod() {
		try {
			Method m1 = Window.class.getDeclaredMethod("setGraphicsConfiguration", GraphicsConfiguration.class);
			m1.setAccessible(true);
			return m1;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void componentResized(ComponentEvent e) {
		updateGC();
	}
	
	public void componentMoved(ComponentEvent e) {
		updateGC();
	}
	
	public void componentShown(ComponentEvent e) {
		updateGC();
	}
	
	public void componentHidden(ComponentEvent e) {
		updateGC();
	}
	
	private void updateGC() {
		try {
			for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
				GraphicsConfiguration defaultConfig = gd.getDefaultConfiguration();
				if (!frame.getGraphicsConfiguration().equals(defaultConfig)) {
					if (frame.getLocation().getX() >= defaultConfig.getBounds().getMinX()
							&& frame.getLocation().getX() < defaultConfig.getBounds().getMaxX()
							&& frame.getLocation().getY() >= defaultConfig.getBounds().getMinY()
							&& frame.getLocation().getY() < defaultConfig.getBounds().getMaxY()) {
						setGCMethod.invoke(frame, defaultConfig);
					}
				}
			}
		} catch (Throwable t) {
			if (!errorPrinted) {
				AmidstLogger.error(t, "Unable to set GraphicsConfiguration");
				errorPrinted = true;
			}
		}
	}
	
}
