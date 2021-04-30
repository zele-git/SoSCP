package mcir2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Scenario implements Serializable {

    private List<String> ldz = new ArrayList<>();
    private List<String> ldz2 = new ArrayList<>();
    private List<String> ldz3 = new ArrayList<>();
    private List<String> ldz4 = new ArrayList<>();
    private List<String> ldz5 = new ArrayList<>();
    private List<String> ldz6 = new ArrayList<>();
    private List<String> ldz7 = new ArrayList<>();
    private List<String> ldz8 = new ArrayList<>();
    private int evNumbers;

    private List scenarioContainer;

    Random random = new Random();//random decision to follow or decline a policy
    String pol = random.nextInt(4 - 0) + 1 + "";


    Scenario() {

        this.ldz.add(pol);// policy constraint
        this.ldz.add("14");//location
        this.ldz.add("15");// patient number
        this.ldz.add("4");// critical patient number
        this.ldz.add("24");// average age

        this.ldz2.add(pol);// policy constraint
        this.ldz2.add("4");//location
        this.ldz2.add("9");// patient number
        this.ldz2.add("5");// critical patient number
        this.ldz2.add("28");// average age

        this.ldz3.add(pol);// policy constraint
        this.ldz3.add("24");//location
        this.ldz3.add("18");// patient number
        this.ldz3.add("12");// critical patient number
        this.ldz3.add("45");// average age

        this.ldz4.add(pol);// policy constraint
        this.ldz4.add("20");//location
        this.ldz4.add("13");// patient number
        this.ldz4.add("12");// critical patient number
        this.ldz4.add("45");// average age

        this.ldz5.add(pol);// policy constraint
        this.ldz5.add("34");//location
        this.ldz5.add("8");// patient number
        this.ldz5.add("12");// critical patient number
        this.ldz5.add("45");// average age

        this.ldz6.add(pol);// policy constraint
        this.ldz6.add("34");//location
        this.ldz6.add("8");// patient number
        this.ldz6.add("2");// critical patient number
        this.ldz6.add("45");// average age

        this.ldz7.add(pol);// policy constraint
        this.ldz7.add("28");//location
        this.ldz7.add("23");// patient number
        this.ldz7.add("12");// critical patient number
        this.ldz7.add("55");// average age

        this.ldz8.add(pol);// policy constraint
        this.ldz8.add("22");//location
        this.ldz8.add("29");// patient number
        this.ldz8.add("14");// critical patient number
        this.ldz8.add("70");// average age

        evNumbers = 18;

        scenarioContainer = new ArrayList();
        scenarioContainer.add(ldz);
        scenarioContainer.add(ldz2);
        scenarioContainer.add(ldz3);
        scenarioContainer.add(ldz4);
        scenarioContainer.add(ldz5);
        scenarioContainer.add(ldz6);
        scenarioContainer.add(ldz7);
        scenarioContainer.add(ldz8);



    }

    public List getloadzone() {
        return scenarioContainer;
    }

    public void updateldz(int index) {

        String temp = scenarioContainer.get(index).toString();
        // service based on distance, thus select ldz based on shortest distance
        List<String> tempsc = new ArrayList<String>(Arrays.asList(temp.replaceAll("\\[|\\]", "").split(",")));
//        System.out.println(scenarioContainer);
        int value = Integer.parseInt(tempsc.get(2).trim());
        if (value > 0)
            value = value - 1;
        tempsc.set(2, value + "");
        scenarioContainer.set(index, tempsc);
//        System.out.println(scenarioContainer);


    }

    public int getRemainingPatients(List scinput) {
        int pnum = 0;
        for (int i = 0; i < scinput.size(); i++) {
            String temp = scinput.get(i).toString();
            List<String> tempsc = new ArrayList<String>(Arrays.asList(temp.replaceAll("\\[|\\]", "").split(",")));
            pnum += Integer.parseInt(tempsc.get(2).trim());
        }
        return pnum;
    }

    public int getPatientNumber() {
        return getRemainingPatients(scenarioContainer);
    }

    public int getPatientNumber(int index) {
        int pnum = 0;
        String temp = scenarioContainer.get(index).toString();
        List<String> tempsc = new ArrayList<String>(Arrays.asList(temp.replaceAll("\\[|\\]", "").split(",")));
        pnum += Integer.parseInt(tempsc.get(2).trim());
        return pnum;
    }

    public int getLDZpatientNumber(int index) {
        String temp = scenarioContainer.get(index).toString();
        List<String> tempsc = new ArrayList<String>(Arrays.asList(temp.replaceAll("\\[|\\]", "").split(",")));
        return Integer.parseInt(tempsc.get(2).trim());
    }

    public int getEvNumbers() {
        return evNumbers;
    }

    public void setEvNumbers(int evNumbers) {
        this.evNumbers = evNumbers;
    }
}
