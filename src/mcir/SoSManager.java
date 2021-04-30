package mcir;


import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import examples.Jform.*;

import java.util.*;
/*
responsible to manage bridgehead information
- initialize bridgehead with default data
- communicate bridgehead information to agents
- communicate policies to manage agents' activities
-
 */

public class SoSManager extends Agent {

//    private Hashtable coordinates;

    Map<String, List<String>> bhinfo;
    List<String> param;
//    private PatientInfoGui patientGui;
    private SoSManagerMainMenu mainMenu;

    protected void setup() {

        bhinfo = new HashMap<>();
        param = new ArrayList<>();

//        coordinates = new Hashtable();
//        patientGui = new PatientInfoGui(this);
        mainMenu = new SoSManagerMainMenu(this);
        mainMenu.showManagerGui();

//        patientGui.showGui();
        // service registration
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        //        we can list multiple services/roles
        sd.setType("rescuing");
        sd.setType("firefighting");
        sd.setName("Emergency service");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new RequestInterface());
        addBehaviour(new ClaimInterface());
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        mainMenu.dispose();
        System.out.println("Manager Agent " + getAID().getName() + " terminating.");
    }

    public void updatePatientList(final String locname,
                                  final String coordvalue,
                                  final int pnumbers,
                                  final int cpnumners,
                                  final String polCond,
                                  final String polAction) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                param.add(coordvalue);
                param.add("" + pnumbers);
                param.add("" + cpnumners);
                bhinfo.put(locname, param);
//                coordinates.put(locname, coordvalue, new Integer(pnumbers));
                System.out.println(" Bridgehead created. Name = "
                        + locname + "\n Location coordinate : "
                        + coordvalue + "\n Number of patient: "
                        + pnumbers + "\n Critical :" + cpnumners);
            }
        });
    }

    //serve incoming request
    private class RequestInterface extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String locname = msg.getContent();
                ACLMessage reply = msg.createReply();

                List<String> bhdetail = bhinfo.get(locname);
                if (bhdetail != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(bhdetail));
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    //serve incoming claim acceptance
    private class ClaimInterface extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String bhd = msg.getContent();
                ACLMessage reply = msg.createReply();

                List<String> bhdetail = bhinfo.get(bhd);
                int pnum = Integer.parseInt(bhdetail.get(1));
                if (pnum > 0) {
//                    pnum =- 1;
                    bhdetail.set(1, "" + (--pnum));
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(String.valueOf(bhdetail));
                    System.out.println("One patient transferred. Bridgehead status: " + msg.getSender().getName());

                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
