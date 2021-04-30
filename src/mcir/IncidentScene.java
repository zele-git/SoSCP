package mcir;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class IncidentScene extends Agent{

    private IncidentSceneGui incidentGui;

    protected void setup(){
        incidentGui = new IncidentSceneGui(this);
        incidentGui.showGui();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Incident Scene");
        sd.setName("Emergency service");
        dfd.addServices(sd);
        try{
            DFService.register(this, dfd);

        }
        catch (FIPAException fe){
            fe.printStackTrace();
        }
    }
    protected void takeDown(){
        try{
            DFService.deregister(this);
        }
        catch (FIPAException fe){
            fe.printStackTrace();
        }
        incidentGui.dispose();
        System.out.println("Incident scene " + getAID().getName() + " terminating.");
    }
}
