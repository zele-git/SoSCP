package mcir;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class IncidentSceneGui extends JFrame {


    IncidentSceneGui(IncidentScene a) {
        super(a.getLocalName());

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));

        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Add");

        p = new JPanel();
        p.setPreferredSize(new Dimension(640, 480));
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
//				myAgent.doDelete();
            }
        } );

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}
