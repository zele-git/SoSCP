package mcir;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class AmbulanceAgent extends Agent {
    private String bridgeHeadName;
    private AID[] rescuerAgent;

    protected void setup() {
        System.out.println("Emergency service agent " + getAID().getName() + " is ready.");
        // discover bridgeheads
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            bridgeHeadName = (String) args[0];
            System.out.println("Target bridgehead name " + bridgeHeadName);
            addBehaviour(new TickerBehaviour(this, 8000) {
                @Override
                protected void onTick() {
                    System.out.println("Search for matching service/role");
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("rescuing");
                    template.addServices(sd);
                    try {
                        // search for published services
                        DFAgentDescription[] result = DFService.search(myAgent,template);
                            rescuerAgent = new AID[result.length];
                            for (int i = 0; i < result.length; ++i) {
                                rescuerAgent[i] = result[i].getName();
                                System.out.println("Found the following matching role:" + rescuerAgent[i].getName());
                            }
//                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    myAgent.addBehaviour(new RequestPerformer());
                }
            });
        } else {
            System.out.println("Patient not identified");
            doDelete();
        }
    }

    protected void takeDown() {
        System.out.println("Ambulance agent " + getAID().getName() + " terminating");
    }

    private class RequestPerformer extends Behaviour {
        private AID rescuerId;
        //        private int coordvalue;
        private String bhinfo;
        private int repliesCounter = 0;
        private MessageTemplate mt;
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    //send the cfp to all rescuer
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < rescuerAgent.length; ++i) {
                        cfp.addReceiver(rescuerAgent[i]);
                    }
                    cfp.setContent(bridgeHeadName);
                    cfp.setConversationId("emergency-service");
                    cfp.setReplyWith("cfp " + System.currentTimeMillis());
                    myAgent.send(cfp);
                    //prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("emergency-service"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    //receive responses from bridgehead
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            bhinfo = reply.getContent();
                            rescuerId = reply.getSender();
                        }
                        repliesCounter++;
                        if (repliesCounter >= rescuerAgent.length) {
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    //send claim to the bridgehead
                    ACLMessage claim = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    claim.addReceiver(rescuerId);
                    claim.setContent(bridgeHeadName);
                    claim.setConversationId("emergency-service");
                    claim.setReplyWith("claim" + System.currentTimeMillis());
                    myAgent.send(claim);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("emergency-service"),
                            MessageTemplate.MatchInReplyTo(claim.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    //receive ack from the bridgehead
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            transport(reply);
                            System.out.println("One patient successfully rescued from bridgehead: " + bridgeHeadName);
//                            System.out.println(bridgeHeadName + " successfully rescued from agent " + reply.getSender().getName());
                            System.out.println("Bridgehead detail : " + bhinfo);
//                            myAgent.doDelete();
                        } else {
                            System.out.println("Attempt failed: patient already rescued.");
//                            block();
                        }
                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }
        public boolean done() {
            return (step == 4);
        }
    }

    public void transport(ACLMessage reply){
        String bhd = reply.getContent();
        String[] bhdArray = bhd.split(", ");
        List<String> list = Arrays.asList(bhdArray);
        moveToBridgeheadLocation(list.get(0));
        selectPatient(list);
//        System.out.println("Bridgehead coordinates :" + list.get(0));

    }

    public void moveToBridgeheadLocation(String coor){

    }
    public String selectPatient(List<String> idlist){
        Random rand = new Random();
        String randomElement = idlist.get(rand.nextInt(idlist.size()));
        return randomElement;
    }



}



