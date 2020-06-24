package com.billkuker.rocketry.motorsim.gui.visual.workbench;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class About extends JDialog {
    private static final long serialVersionUID = 1L;
    private final static URI webpage;

    static {
        try {
            webpage = new URI(
                    "http://content.billkuker.com/projects/rocketry/software");
        } catch (URISyntaxException e) {
            throw new Error(e);
        }
    }

    private final JFrame f;

    public About(final JFrame f) throws IOException, XmlPullParserException {
        super(f, "About " + MotorWorkbench.getFullName(), true);
        this.f = f;
        setSize(400, 250);

        setIconImage(f.getIconImage());

        setLayout(new BorderLayout());
        add(new JLabel("<html>" + MotorWorkbench.getFullName()
                + " &copy;2010 Bill Kuker</html>"), BorderLayout.NORTH);
        JTextArea text;
        add(new JScrollPane(text = new JTextArea()), BorderLayout.CENTER);
        text.setEditable(false);
        StringBuilder lic = new StringBuilder();
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream("license.txt");
            assert is != null;
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = in.readLine()) != null) {
                lic.append(line);
                lic.append("\n");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        text.setText(lic.toString());
        JLabel link = new JLabel(
                "<html><u>Visit MotorSim on the Web</u></html>");
        link.setForeground(Color.BLUE);
        add(link, BorderLayout.SOUTH);
        link.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(webpage);
                    } catch (IOException ignored) {

                    }
                }

            }
        });
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setSize(1024, 768);
        f.setVisible(true);
        About s = null;
        try {
            s = new About(f);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        About finalS = s;
        SwingUtilities.invokeLater(() -> finalS.setVisible(true));

    }

    @Override
    public void setVisible(boolean v) {
        int x = f.getLocation().x + f.getWidth() / 2 - getWidth() / 2;
        int y = f.getLocation().y + f.getHeight() / 2 - getHeight() / 2;
        setLocation(x, y);
        super.setVisible(v);
    }
}
