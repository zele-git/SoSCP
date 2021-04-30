package mcir;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class SoSManagerMainMenu extends JFrame {
    private JFrame menu;
    private BridgeheadInfoGui bhGui;
    private PolicyGui pgui;
    private SoSManager manager;

    private JSplitPane splitV;
//    private JTextField locNameField, patientNumField, xField, yField, criticalPatientNumField;


    public SoSManagerMainMenu(SoSManager a) {
        super(a.getLocalName());

        manager = a;
//        setTitle("Managers Desk");

        bhGui = new BridgeheadInfoGui(a);
        pgui = new PolicyGui(a);
        menu = new JFrame();
        menu.setBounds(100, 100, 730, 200);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menu.getContentPane().setLayout(null);

        JButton btnBridgehead = new JButton("Manage Bridgehead");
        btnBridgehead.setBounds(150, 80, 200, 23);
        menu.getContentPane().add(btnBridgehead);

        JButton btnPolicy = new JButton("Manage Policy");
        btnPolicy.setBounds(400, 80, 200, 23);
        menu.getContentPane().add(btnPolicy);


        btnPolicy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                pgui.showPolicyGui();
            }
        });

        btnBridgehead.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try{
                    bhGui.showGui();
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        });
    }

    public void showManagerGui() {
        menu.setVisible(true);
    }
}
