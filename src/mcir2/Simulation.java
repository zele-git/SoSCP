package mcir2;


import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Simulation {
    public static void main(String[] args) {

//        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");
        ContainerController cc = jade.core.Runtime.instance().createMainContainer(profile);

        AgentController managerController;
        try {
            managerController = cc.createNewAgent("Manager", "mcir2.SoSAgent", null);
            managerController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 18; i++) {
            AgentController amController;
            try {
                amController = cc.createNewAgent("AM" + i, "mcir2.AmbuAgent", null);
                amController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }




    }
}
