package stx.mc;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;


public class StxTelnetClient {

    public static long requestIdConuter = 1;

    private TelnetClient _telnetClient = new TelnetClient();

    //connect connection
    public void connect(String ip){
        try {
            _telnetClient.connect(ip, 6001);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StxTelnetClient client = this;
        Thread handlerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                client.handleReceive();
            }
        });
        handlerThread.start();

//        try {
//            int robotIdentfier = getRobotIdentifier("PUMA");
//            System.out.println("robot PUMA is " + robotIdentfier);
//            enableGroup(robotIdentfier);
//
//        } catch (StxCommunicationException | StxControllerErrorException e) {
//            e.printStackTrace();
//        }
    }


    private int getRobotIdentifier(String robotName) throws StxCommunicationException, StxControllerErrorException {
        long requestId = requestIdConuter++;
        send(String.format("getId(%d,\"%s\")", requestId, robotName));
        String[] retVals = waitForImmediateResponse(requestId);
        return Integer.parseInt(retVals[0]);
    }

    /*
    // for puma & scara
    public void moveEndPointToXYZ(Vector3d xyz, double speed) throws StxCommunicationException, StxControllerErrorException {
        int groupId = 65;    //todo: ????????????
        long requestId = requestIdConuter++;
        send(String.format("grMoveAbsolute(%d,%d,%.2f,%.2f,%.2f,%.2f)", requestId, groupId, xyz.x,xyz.y,xyz.z,speed));
        waitForImmediateResponse(requestId);
        waitForDifferedResponse(requestId);
    }
    */

    //in straight motion speed is mm/s of end point
    //in non-straight motion speed is deg/s for fastest joint
    public void moveEndPointToPosition(Vector3d xyz, Vector3d ypr, boolean isStraightMotion, double speed) throws StxCommunicationException, StxControllerErrorException {
        int groupId = 65;    //todo: ????????????
        long requestId = requestIdConuter++;
        String command = null;
        if (isStraightMotion){
            command = String.format("grMoveSPnt6dof(%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f)", requestId, groupId, xyz.x,xyz.y,xyz.z, ypr.x, ypr.y, ypr.z,speed);
        }else{
            command = String.format("grMovePnt6dof(%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f)", requestId, groupId, xyz.x,xyz.y,xyz.z, ypr.x, ypr.y, ypr.z,speed);
        }
        send(command);
        waitForImmediateResponse(requestId);
        waitForDifferedResponse(requestId);
    }


    //in straight motion speed is mm/s of end point
    //in non-straight motion speed is deg/s for fastest joint
    public void setAngles(double[] angles, boolean isStraightMotion, double speed) throws StxCommunicationException, StxControllerErrorException {
        int groupId = 65;    //todo: ????????????
        long requestId = requestIdConuter++;
        String command = null;
        if (angles.length ==6){
            if (isStraightMotion){
                command = String.format("grMoveSJnt6dof(%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f)", requestId, groupId, angles[0], angles[1], angles[2], angles[3], angles[4], angles[5], speed);
            }else{
                command = String.format("grMoveJnt6dof(%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f)", requestId, groupId, angles[0], angles[1], angles[2], angles[3], angles[4], angles[5], speed);
            }

        }else{
            System.err.println("cant handle " + angles.length + " axes command");
        }
        send(command);
        waitForImmediateResponse(requestId);
        waitForDifferedResponse(requestId);
    }


/*
    private void enableGroup(int groupId) throws StxCommunicationException, StxControllerErrorException {
        long requestId = requestIdConuter++;
        send(String.format("grEnable(%d,%d,1)", requestId, groupId));
        waitForImmediateResponse(requestId);
    }
*/

    public void enable() throws StxCommunicationException, StxControllerErrorException {
        long requestId = requestIdConuter++;
        send(String.format("grEnable(%d,%d,1)", requestId, 65));
        waitForImmediateResponse(requestId);
    }

    public void disable() throws StxCommunicationException, StxControllerErrorException {
        long requestId = requestIdConuter++;
        send(String.format("grEnable(%d,%d,0)", requestId, 65));
        waitForImmediateResponse(requestId);
    }

    //===============================================

    private String[] waitForImmediateResponse(long requestId) throws StxCommunicationException, StxControllerErrorException {
        ResponseWaitObject responseWaitObject = new ResponseWaitObject();
        _immediateResponsesMap.put(requestId, responseWaitObject);
        return responseWaitObject.waitForResponse(1000);    //this is blocking
    }

    private String[] waitForDifferedResponse(long requestId) throws StxCommunicationException, StxControllerErrorException {
        ResponseWaitObject responseWaitObject = new ResponseWaitObject();
        _differedResponsesMap.put(requestId, responseWaitObject);
        return responseWaitObject.waitForResponse(30000);    //this is blocking
    }

    private void send(String str){
        OutputStream outputStream = _telnetClient.getOutputStream();
        try {
            System.out.println("Writing: " + str);
            str += "\n";
            outputStream.write(str.getBytes(Charset.forName("UTF-8")));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleReceive(){
        InputStream instr = _telnetClient.getInputStream();

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(instr));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println("Line received : " + line);
                if (line.length() < 2){
                    System.err.println("invalid response from controller ["+ line +"]");
                }else{
                    handleLine(line);
                }

            }
        }
        catch (IOException e)
        {
            System.err.println("Exception while reading socket:" + e.getMessage());
        }

        try
        {
            _telnetClient.disconnect();
        }
        catch (IOException e)
        {
            System.err.println("Exception while closing telnet:" + e.getMessage());
        }
    }

    private void handleLine(String line){
        String[] splitted = line.split(" ");
        if (splitted[0].charAt(0) == '!'){
            //differed
            Long requestId = Long.parseLong(splitted[0].substring(1));
            ResponseWaitObject responseWaitObject = _differedResponsesMap.get(requestId);
            responseWaitObject.resolve(Integer.parseInt(splitted[1]), Arrays.copyOfRange(splitted, 2, splitted.length));

        }else{
            //immediate
            Long requestId = Long.parseLong(splitted[0]);
            ResponseWaitObject responseWaitObject = _immediateResponsesMap.get(requestId);
            responseWaitObject.resolve(Integer.parseInt(splitted[1]), Arrays.copyOfRange(splitted, 2, splitted.length));
        }
    }

    private ConcurrentHashMap<Long, ResponseWaitObject> _immediateResponsesMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, ResponseWaitObject> _differedResponsesMap = new ConcurrentHashMap<>();

    public void disconnect() {
        try {
            _telnetClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
