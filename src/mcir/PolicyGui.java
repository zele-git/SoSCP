package mcir;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PolicyGui extends JFrame implements ActionListener{

    private SoSManager ffAgent;
    private JCheckBox pol1, pol2,pol3;
    private JLabel desc;
    private  JButton activate;

    PolicyGui(SoSManager a) {
        super(a.getLocalName());
        ffAgent = a;

        desc = new JLabel("Policy List");
        desc.setBounds(20,50,300,20);
        pol1 = new JCheckBox("Priority for bridgeheads having maximum number of critical patients");
        pol1.setBounds(30,70,550,20);
        pol2 =new JCheckBox("Priority for bridgeheads having maximum number of patients");
        pol2.setBounds(30,90,550,20);
        pol3=new JCheckBox("Decide based on distance between bridgehead location and arriving Ambulances");
        pol3.setBounds(30,110,550,20);
        activate=new JButton("Activate");
        activate.setBounds(60,150,80,30);
        activate.addActionListener(this);
        add(desc);add( pol1);add(pol2);add(pol3);add(activate);
        setSize(600,400);
        setLayout(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }
    public void actionPerformed(ActionEvent e){

    }
    public void showPolicyGui() {
        setVisible(true);
    }
}
