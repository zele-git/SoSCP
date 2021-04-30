package mcir2;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SoSAgent extends Agent {
    List<String> serviceType = new ArrayList<>();
    HashMap<String, String> decision = new HashMap<String, String>();// <ack/nack, ldz id> then sosagent will subtract one from the total size

    Random random = new Random();//random decision to follow or decline a policy
    String pol;
    WriteToFile wtf = new WriteToFile();
    // scenario information
    Scenario scenario = new Scenario();
    int cont = scenario.getloadzone().size();

    List<String> stat = new ArrayList();
    List<List<String>> rprt = new ArrayList();

    int ackActionCounter = 0, ackCount = 0, nackActionCounter = 0;
    boolean lock = false;
    int lost = 0;

    protected void setup() {
        // register services
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription service;
        // we can list multiple services/roles
        serviceType.add("rescuing");
        serviceType.add("firefighting");

        for (int i = 0; i < serviceType.size(); i++) {
            service = new ServiceDescription();
            service.setName("Emergency service");
            service.setType(serviceType.get(i));
            dfd.addServices(service);
            System.out.println("service type: " + serviceType.get(i));

        }

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // intiailize shared resources
        pol = random.nextInt(4 - 0) + 1 + "";

        System.out.print("Number of patients: " + scenario.getPatientNumber() + "\n");

        addBehaviour(new serveRequest());
        addBehaviour(new serveDecision());
    }

    protected void takeDown() {

        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //mainMenu.dispose();
        System.out.println("Execution report \n");
        System.out.println(rprt + "\n");
        wtf.writeToExcel(rprt);
        System.out.println("SoS Agent " + getAID().getName() + " terminating.\n");
        System.out.println(decision);
    }

    // responding for requests from other agents
    private class serveRequest extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                String cont = msg.getContent();
                ACLMessage reply = msg.createReply();

                if (cont != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    try {
                        reply.setContentObject(scenario);
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not available");
                }
                // communicate scenario to cs
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class serveDecision extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);

            stat.clear();
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                if (reply != null && msg.getContent() != null) {
//                    HashMap<String, String> decision = new HashMap<String, String>();
                    List<String> info = new ArrayList<>();
                    List<String> csresp = new ArrayList<String>(Arrays.asList(msg.getContent().replaceAll("\\[|\\]", "").split(",")));
                    reply.setPerformative(ACLMessage.INFORM);

                    List<String> templ = new ArrayList<>();
                    boolean flg = false;

                    Iterator<Map.Entry<String, String>>
                            iterator = decision.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> entry = iterator.next();
                        if (entry.getKey().equals(csresp.get(2).trim())) {
                            flg = true;
                            if (!entry.getValue().equals(csresp.get(0).trim())) {
                                ackCount++;// why not nackCount++
                                decision.put(csresp.get(2).trim(), csresp.get(0).trim());
                            }
                        }
                    }
                    if(flg == false){
                        if (csresp.get(0).equals("ACK"))ackCount++;
                        decision.put(csresp.get(2).trim(), csresp.get(0).trim());

                    }

                    if (csresp.get(0).trim().equals("ACK")) {
                        // guard critical condition - updating ldz
                        while (lock == true) {
                            try {
                                Thread.sleep(6000);// agent task take 6000 time unit
                            } catch (InterruptedException ie) {
                                System.out.println(ie);
                            }
                        }

                        lock = true;
                        if (!csresp.get(1).trim().equals("-1") && scenario.getPatientNumber(Integer.parseInt(csresp.get(1).trim())) > 0) {
                            ackActionCounter++;
                            scenario.updateldz(Integer.parseInt(csresp.get(1).trim()));
                            Instant ackstrt = Instant.now();
                            //agent task approximation
                            try {
                                Thread.sleep(6000);// agent task take 6000 time unit
                            } catch (InterruptedException ie) {
                                System.out.println(ie);
                            }
                            Instant ackend = Instant.now();
                            Duration tasktime = Duration.between(ackstrt, ackend);

                            stat.add("ACKs: " + ackActionCounter + " ACK time: " + tasktime);
                            //number of death when nacks are greater than acks
                            if (nackActionCounter > 4 && nackActionCounter > 2 * ackActionCounter) {
                                Random random = new Random();
                                int rindex = random.nextInt(scenario.getloadzone().size() - 0) + 0;
                                if (scenario.getLDZpatientNumber(rindex) > 0) {
                                    scenario.updateldz(rindex);
                                    lost++;
                                }
                            }

                            templ.add("ACK");
                            templ.add(ackstrt.getNano() / 1000000 + "");
                            templ.add(ackend.getNano() / 1000000 + "");
                            templ.add(ackActionCounter + "");
                            templ.add(tasktime.getSeconds() + "");
                            templ.add(csresp.get(2));
                            templ.add(lost + "");

                            rprt.add(templ);
                            info.add(ackCount + "");
                            info.add("action");
                            info.add(decision.size()+"");

                            reply.setContent(info.toString());
                            System.out.println("Policy choice: " + pol);
                            System.out.println("Role granted: " + msg.getSender().getName());
                            System.out.println("Policy decision: " + msg.getContent());

                        } else {
                            cont -= 1;
                            if (cont < 0) {
                                reply.setPerformative(ACLMessage.FAILURE);
                                reply.setContent("not available");

                            } else
                                block();
                        }
                        lock = false;
                    } else {
                        while (lock == true) {
                            try {
                                Thread.sleep(3000);// agent task take 6000 time unit
                            } catch (InterruptedException ie) {
                                System.out.println(ie);
                            }
                        }
                        lock = true;

                        if (!csresp.get(1).trim().equals("-1") && scenario.getPatientNumber(Integer.parseInt(csresp.get(1).trim())) > 0) {
                            nackActionCounter++;
                            scenario.updateldz(Integer.parseInt(csresp.get(1).trim()));

                            Instant nackstrt = Instant.now();
                            try {
                                Thread.sleep(8000);// agent task take 6000 time unit
                            } catch (InterruptedException ie) {
                                System.out.println(ie);
                            }
                            Instant nackend = Instant.now();
                            Duration tasktime = Duration.between(nackstrt, nackend);
                            stat.add("NACKs: " + nackActionCounter + " Task time: " + tasktime);
                            //why probability ?
                            if (nackActionCounter > 4 && nackActionCounter > 2 * ackActionCounter) {
                                Random random = new Random();
                                int rindex = random.nextInt(scenario.getloadzone().size() - 0) + 0;
                                double probability = Math.random();
                                if (probability < 0.65) {
                                    if (scenario.getLDZpatientNumber(rindex) > 0) {
                                        scenario.updateldz(rindex);
                                        lost++;
                                    }

                                }
                            }

                            templ.add("NACK");
                            templ.add(nackstrt.getNano() / 1000000 + "");
                            templ.add(nackend.getNano() / 1000000 + "");
                            templ.add(nackActionCounter + "");
                            templ.add(tasktime.getSeconds() + "");
                            templ.add(csresp.get(2));
                            templ.add(lost + "");

                            rprt.add(templ);

                            info.add(ackCount + "");
                            info.add("action");
                            info.add(decision.size()+"");

                            reply.setContent(info.toString());
                            System.out.println("Policy choice: " + pol);
                            System.out.println("Role granted: " + msg.getSender().getName());
                            System.out.println("Policy decision: " + msg.getContent());
                            //

                        } else {
                            cont -= 1;
                            if (cont < 0) {
                                reply.setPerformative(ACLMessage.FAILURE);
                                reply.setContent("not available");
                            } else
                                block();
                        }

                        lock = false;
                    }
                    System.out.println(stat);


                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not available");

                }
                myAgent.send(reply);

            } else {
                if (scenario.getPatientNumber() <= 0)
                    myAgent.doDelete();
                else block();
//                myAgent.doDelete();
            }
            //check if there are still paitents, if there are no delete SoSAgent
            if (scenario.getPatientNumber() <= 0)
                myAgent.doDelete();
        }


    }

    private class WriteToFile {
        public void writeToExcel(List<List<String>> input) {
            try {
                FileOutputStream out = new FileOutputStream(new File("C:\\jade\\rprt.xls"));

                HSSFWorkbook workbook = new HSSFWorkbook();
                HSSFSheet sheet = workbook.createSheet("Emergency statistics");

                Iterator<List<String>> i = input.iterator();
                Row headerrow = sheet.createRow(0);
                Cell headerPol = headerrow.createCell(0);
                headerPol.setCellValue("Policy");
                Cell headerStartTime = headerrow.createCell(1);
                headerStartTime.setCellValue("Task starts at");
                Cell headerEndTime = headerrow.createCell(2);
                headerEndTime.setCellValue("Task ends at");
                Cell polcount = headerrow.createCell(3);
                polcount.setCellValue("Counter(ACK/NACK)");
                Cell ttime = headerrow.createCell(4);
                ttime.setCellValue("Task time");
                Cell headerAgent = headerrow.createCell(5);
                headerAgent.setCellValue("Agent name");
                Cell lost = headerrow.createCell(6);
                lost.setCellValue("Lost life");

                int rownum = 1;
                int cellnum = 0;
                while (i.hasNext()) {
                    List<String> templist = (List<String>) i.next();
                    Iterator<String> tempIterator = templist.iterator();
                    Row row = sheet.createRow(rownum++);
                    cellnum = 0;
                    while (tempIterator.hasNext()) {
                        String temp = (String) tempIterator.next();
                        Cell cell = row.createCell(cellnum++);
                        cell.setCellValue(temp);

                    }

                }
                workbook.write(out);
                out.close();
                workbook.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

}


