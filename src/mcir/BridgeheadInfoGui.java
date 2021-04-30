package mcir;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BridgeheadInfoGui extends JFrame implements ActionListener {

    private SoSManager sosmanager;

    private JTextField locNameField, patientNumField, xField,
            yField, criticalPatientNumField, policyCondField, policyActionField;

    BridgeheadInfoGui(SoSManager a) {
        super(a.getLocalName());
        sosmanager = a;


        JLabel bhname = new JLabel("Bridgehead name :");
        bhname.setBounds(30,70,200,20);
        locNameField = new JTextField(10);
        locNameField.setBounds(200,70,200,20);

        JLabel xcoor = new JLabel("X-coordinate value :");
        xcoor.setBounds(30,90,200,20);
        xField = new JTextField(10);
        xField.setBounds(200,90,200,20);

        JLabel ycoor = new JLabel("Y-coordinate value :");
        ycoor.setBounds(30,110,200,20);
        yField = new JTextField(10);
        yField.setBounds(200,110,200,20);

        JLabel pnum = new JLabel("Number of patients :");
        pnum.setBounds(30,130,200,20);
        patientNumField = new JTextField(10);
        patientNumField.setBounds(200,130,200,20);

        JLabel cnum = new JLabel("Number of critical patients :");
        cnum.setBounds(30,150,200,20);
        criticalPatientNumField = new JTextField(10);
        criticalPatientNumField.setBounds(200,150,200,20);

        JLabel pcon = new JLabel("Policy conditions :");
        pcon.setBounds(30,170,200,20);
        policyCondField = new JTextField(10);
        policyCondField.setBounds(200,170,200,20);

        JLabel paction = new JLabel("Policy actions :");
        paction.setBounds(30,190,200,20);
        policyActionField = new JTextField(10);
        policyActionField.setBounds(200,190,200,20);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String locname = locNameField.getText().trim();
                    String coordvalue = "(" + xField.getText().trim() + "," + yField.getText().trim() + ")";
                    String pnumbers = patientNumField.getText().trim();
                    String cpnumbers = criticalPatientNumField.getText().trim();
                    String polCond = policyCondField.getText().trim();
                    String polAction = policyActionField.getText().trim();
                    sosmanager.updatePatientList(locname, coordvalue,
                            Integer.parseInt(pnumbers), Integer.parseInt(cpnumbers), polCond, polAction);
                    locNameField.setText("");
                    xField.setText("");
                    yField.setText("");
                    patientNumField.setText("");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BridgeheadInfoGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        addButton.setBounds(30,220,100,20);

        add(bhname);add(locNameField); add(xcoor); add(xField); add(ycoor); add(yField );
        add(pnum ); add(patientNumField); add(cnum); add(criticalPatientNumField);
        add(pcon); add(policyCondField); add(paction); add(policyActionField);


        add(addButton);

        setSize(600,400);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    public void actionPerformed(ActionEvent e){

    }

    public void showGui() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        setVisible(true);
    }
}
