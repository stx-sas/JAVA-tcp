package stx.mc;

public class Main {
    public static void main(String[] args) {

        IRobotControl robotControl = new StxRobotControl();
        robotControl.connect("192.168.56.101");

        //up is +Z. forward is +X. left is +Y
        double[][] positions = {
                { 600.0,    0.0,    500,    0.0,    180.0,  0.0     },       //x,y,z, y,p,r
                { 196.0,    662.0,  572,    0.0,    180.0,  0.0     },
                { 588,      -698,   149,    87,     -24.0,  154.0   },
                { 600.0,    0.0,    500,    0.0,    180.0,  0.0     }
        };

        double[] angles1 = {0,-90,90,0,90,0};   //joints 1 to 6 angles in degrees

        try {
            robotControl.enable();
            robotControl.setAngles(angles1);

            for (double[] position : positions) {
                robotControl.moveEndPoint(new Position(
                        new Vector3d(position[0], position[1], position[2]),
                        new Vector3d(position[3], position[4], position[5])
                ));
            }

            robotControl.disable();
            System.out.println("robot reached goal");

        } catch (StxCommunicationException | StxControllerErrorException e) {
            e.printStackTrace();
        }

        robotControl.disconnect();
    }
}
