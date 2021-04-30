package mcir2;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.UnreadableException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AmbuAgent extends Agent {

    private AID[] collAgent;
    private Random random = new Random();//random decision to follow or decline a policy
    private int dec = -1;
    private int rng = 7;
    private List replylist;
    private List<String> scenarolist;
    private String policyCons;
    private int evNumber;


    Scenario sc;

    protected void setup() {
        System.out.println("Emergency service agent " + getAID().getName() + " is ready.\n");

        addBehaviour(new TickerBehaviour(this, 4000) {
            @Override
            protected void onTick() {
//                System.out.println("Search for matching service/role");
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("firefighting");
                template.addServices(sd);

                String agentname = null;
                try {
                    // search for published services
                    DFAgentDescription[] result = DFService.search(myAgent, template);

                    collAgent = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        collAgent[i] = result[i].getName();
//                        System.out.println("Found the following matching role:" + collAgent[i].getName());
                        agentname = collAgent[i].getName();
                    }
                    if (agentname != null) {
//                        System.out.print(agentname);
                        myAgent.addBehaviour(new collaborate());
                    } else {
                        System.out.println("No manager in need of the service");
                        myAgent.doDelete();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }

            }
        });
    }

    protected void takeDown() {
        System.out.println("Ambulance agent " + getAID().getName() + " terminating. \n");
    }

    private class collaborate extends Behaviour {
        private AID agentid;
        private int repliesCounter = 0;
        private MessageTemplate mt;
        private int step = 0;
        private String currdecision;

        public void action() {

            switch (step) {
                case 0:
                    //send the cfp to all rescuer
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < collAgent.length; ++i) {
                        cfp.addReceiver(collAgent[i]);
                    }
                    cfp.setContent("Hello");
                    cfp.setConversationId("emergency-service");
                    cfp.setReplyWith("cfp " + System.currentTimeMillis());
                    myAgent.send(cfp);
                    //prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("emergency-service"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    //receive responses from sos manager
                    ACLMessage reply = null;
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            scenarolist = new ArrayList<>();
                            replylist = new ArrayList();

                            agentid = reply.getSender();//
                            try {
                                replylist = ((Scenario) reply.getContentObject()).getloadzone();
                                evNumber = ((Scenario) reply.getContentObject()).getEvNumbers();

//                                System.out.println("Inputs from SoS manager: " + replylist + " Number of ev: " + evNumber);
//                                System.out.println("Dec value : "+ dec);

                            } catch (UnreadableException ure) {
                                System.out.println(ure);
                            }

                        }
                        repliesCounter++;
                        if (repliesCounter >= collAgent.length) {
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    //send claim to sos manager
                    ACLMessage claim = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    claim.addReceiver(agentid);
                    List<String> aclresp = new ArrayList<>();
                    //agent decisions based on behavior pool and policy constraint
//                    System.out.println("Random decision value: " + dec +"\n");
                    String nm = getAID().getName();
                    nm = nm.substring(0, nm.indexOf('@'));
//                    dec = random.nextInt(10 - 0) + 0;
                    if (dec == -1){
                        dec = random.nextInt(evNumber - 0) + 0;
                    }
                    if (dec < (0.89 * evNumber) ) {// 8/9
                        currdecision = "NACK";
                        aclresp.add("NACK");
                        aclresp.add(movePatientToCareCenter(replylist, dec) + "");
                        aclresp.add(nm);
                        claim.setContent(aclresp.toString());
                        claim.setConversationId("emergency-service");
                        claim.setReplyWith("claim" + System.currentTimeMillis());

                    } else {
                        currdecision = "ACK";
                        aclresp.add("ACK");
                        aclresp.add(movePatientToCareCenter(replylist, dec) + "");
                        aclresp.add(nm);
                        claim.setContent(aclresp.toString());
                        claim.setConversationId("emergency-service");
                        claim.setReplyWith("claim" + System.currentTimeMillis());

                    }

                    myAgent.send(claim);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("emergency-service"),
                            MessageTemplate.MatchInReplyTo(claim.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    //receive ack from the bridgehead
                    reply = myAgent.receive(mt);
                    if (reply != null && reply.getContent() != null && reply.getPerformative() == ACLMessage.INFORM ) {
                        List<String> info = new ArrayList<String>(Arrays.asList(reply.getContent().replaceAll("\\[|\\]", "").split(",")));
                        System.out.println(info);
                        if (info.get(1).trim().equals("action"))  {
                            System.out.println("Attempt succeed. ");
//                            System.out.println(getAID().getName() + " transported a patient.");
                            //agent decide to collaborate based on collaboration information obtained from the sos level manager
                            //infl contains number of acks, the idea is when there are higher number of acks, the probability for acks increases
                            int ackcount = Integer.parseInt(info.get(0).trim());

                            double probability = Math.random();
                           //CS behaviour based on collaboration information, this range can be determined mathimatically based on the decision range
                            if (currdecision.equals("NACK")) {
                                if (ackcount < (0.17 * evNumber) ) {//1/6
                                    //flip nack to ack with some probability
                                    if (probability < 0.25){
                                        // dec value will shift to ack range [0.89 * evNum, evNum)
                                        dec = random.nextInt(evNumber - (int)(0.89*evNumber)) + (int)(0.89*evNumber);

                                        System.out.println("\n " + getAID().getName().substring(0, getAID().getName().indexOf('@')) + " \n fliped !  \n" + dec);
                                        step = 2;
                                        break;
                                    }
                                }
                                if (ackcount > (0.17 * evNumber) && ackcount < (0.5 * evNumber)) {
                                    //flip nack to ack with some probability
                                    if (probability < 0.5){
                                        dec = random.nextInt(evNumber - (int)(0.89*evNumber)) + (int)(0.89*evNumber);
                                        System.out.println("\n " + getAID().getName().substring(0, getAID().getName().indexOf('@')) + " \n fliped ! \n" + dec);
                                        step = 2;
                                        break;
                                    }
                                }
                                if (ackcount > (0.67 * evNumber) && ackcount < (0.89 * evNumber)) {
                                    //flip nack to ack with some probability
                                    if (probability < 0.75) {
                                        dec = random.nextInt(evNumber - (int)(0.89*evNumber)) + (int)(0.89*evNumber);
                                        System.out.println("\n " + getAID().getName().substring(0, getAID().getName().indexOf('@')) + " \n fliped ! \n" + dec);
                                        step = 2;
                                        break;
                                    }
                                }
                            }


                        } else {
                            System.out.println("Attempt failed: patient already rescued.");
                            block();
                            //this can also be doSuspend()
//                            myAgent.doDelete();
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

    /*behaviour pool, agent can deliver services based on policy recommendation
      basic two assumptions, (1) if cs agrees to enforce policy, it will follow policy guide to decide the ldz based on number of patients,
      (2) if not, it will decide the ldz based on distance computed between itslef (located at center) to the ldz location */
    public int movePatientToCareCenter(List sc, int pol) {

        int ldzdis = 1000;
        int ldzsize = 0;
        int action = -1;
        int cont = 0;
        if (pol < 0.89 * evNumber) {
            // not enforcing policy
            for (int i = 0; i < sc.size(); i++) {
                String temp = sc.get(i).toString();
                // service based on distance, thus select ldz based on shortest distance
                List<String> tempsc = new ArrayList<String>(Arrays.asList(temp.replaceAll("\\[|\\]", "").split(",")));
                if (ldzdis > Integer.parseInt(tempsc.get(1).trim()) && Integer.parseInt(tempsc.get(2).trim()) > 0) {
                    ldzdis = Integer.parseInt(tempsc.get(1).trim());
                    action = i;
                }
            }

        } else {
            // service based on policy inputs, thus select ldz based on max number of patients
            for (int i = 0; i < sc.size(); i++) {
                String temp = sc.get(i).toString();
                // service based on distance, thus select ldz based on shortest distance
                List<String> tempsc = new ArrayList<String>(Arrays.asList(temp.replaceAll("\\[|\\]", "").split(",")));
                if (ldzsize < Integer.parseInt(tempsc.get(2).trim()) && Integer.parseInt(tempsc.get(2).trim()) > 0) {
                    ldzsize = Integer.parseInt(tempsc.get(2).trim());
                    action = i;
                }
            }


        }

        return action;

    }
}
