package ChatServer;

import java.io.*;
import java.net.*;
import java.util.HashMap;

import MVC_Login.M_Login;
import UserData.Profile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacServer implements Serializable {
    
    private static final HashMap<String, Profile> users = new HashMap<>();
    public static String IP;
    public static final int PORT = 7895;
    
    public PacServer() {
        
        // Server configuration
        PacServer.IP = M_Login.getComputerIP();
        System.out.println("Obtained server IP which is " + IP);
        System.out.println("Obtained server IP which is " + PacServer.IP);
        
        // Debug
        users.put(
                "Alex",
                new Profile(
                        "12345",
                        "Alex",
                        null,
                        "pass"
                )
        );
        System.out.println("Succesfully create a user");
        
        // Start recieving queries
        getQueries();
        
    }
    
    public static void updateIP() {
        IP = M_Login.getComputerIP();
    }
    
    public static void registerUser(Profile user) {
        PacServer.users.put(user.userName, user);
    }
    
    public static void updateUserIP(Profile user) {
        user.updateIP();
        PacServer.users.put(user.userName, user);
    }    
    
    private void getQueries() {
        
        new Thread() {
            
            ServerSocket server = null;
            Socket socket = null;
            String query;
            String hostIP;
            int hostPort;
            
            @Override
            public void run() {
                
                System.out.println("Inside thread of server");
                
                try {
                    server = new ServerSocket(PacServer.PORT);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                // Server loop
                while (true) {
                    
                    System.out.println("Inside loop of server thread");
                    System.out.println("PORT IS: " + PacServer.PORT);
                    
                    try {
                        
                        // Accept host
                        socket = server.accept();
                        
                        System.out.println("Host accepted");
                        
                        // Read from socket
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        
                        // Get query
                        query = reader.readLine();
                        System.out.println("Query is: " + query);
                        
                        switch (query) {
                            
                            case M_Login.QUERY_LOGIN: {
                                System.out.println("Case query login.");
                                
                                // Get Login IP and PORT
                                hostIP = reader.readLine();
                                hostPort = Integer.parseInt(reader.readLine());
                                
                                System.out.println("Inside LOGIN login ip:   " + hostIP);
                                System.out.println("Inside LOGIN login port: " + hostPort);
                                
                                // Get user name and password
                                String userName = reader.readLine();
                                String password = reader.readLine();
                                
                                System.out.println("Inside LOGIN user name: " + userName);
                                System.out.println("Inside LOGIN password:  " + password);
                                
                                // Create socket with object
                                socket = new Socket(hostIP, hostPort);
                                ObjectOutputStream oop = new ObjectOutputStream(socket.getOutputStream());
                                
                                // Check if user exists
                                if (!PacServer.userExists(userName)) {
                                    System.out.println("User login does not exist");
                                    oop.writeObject(null);
                                    oop.flush();
                                    oop.close();
                                    return;
                                }
                                
                                // Check if passwords are equal
                                if (!password.equals(PacServer.users.get(userName).password)) {
                                    System.out.println("Password aren't equal");
                                    oop.writeObject(null);
                                    oop.flush();
                                    oop.close();
                                    return;
                                }
                                
                                // Send user
                                Profile user = PacServer.users.get(userName);
                                oop.writeObject(user);
                                oop.flush();
                                oop.close();
                                
                                System.out.println("Sent user was:");
                                System.out.println(user);
                                
                                break;
                            }
                            
                            case M_Login.QUERY_USER_EXISTS: {
                                // Get Login IP and PORT
                                hostIP   = reader.readLine();
                                hostPort = Integer.parseInt(reader.readLine());

                                System.out.println("Login IP: " + hostIP);
                                System.out.println("Login PORT: " + hostPort);
                                
                                // Get user name
                                String userName = reader.readLine();
                                socket.close();
                                
                                // Send confirmation to login
                                socket = new Socket(hostIP, hostPort);
                                
                                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                                
                                System.out.println("User " + userName + ((PacServer.userExists(userName))? " exists" : " does not exist"));
                                writer.println(
                                        PacServer.users.containsKey(userName)?
                                                M_Login.ANSWER_YES.toCharArray() :
                                                M_Login.ANSWER_NO.toCharArray()
                                );
                                writer.flush();
                                writer.close();
                                
                                break;
                            }
                            
                            case M_Login.QUERY_REGUSTER_USER: {
                                System.out.println("Register user case.");
                                
                                socket = server.accept();
                                
                                ObjectInputStream oip = new ObjectInputStream(socket.getInputStream());
                                
                                Profile user = (Profile) oip.readObject();
                                
                                PacServer.users.put(user.userName, user);
                                
                                System.out.println("Object wrote!");
                                
                                System.out.println(PacServer.users.get(user.userName));
                                
                                oip.close();
                                
                                break;
                            }
                            
                        }
                        
                        socket.close();
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.start();
        
    }
    
    public static boolean userExists(String userName) {
        return users.containsKey(userName);
    }
    
}
