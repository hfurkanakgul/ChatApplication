/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open  the template in the editor.
 */
package chatapp;

import Server.Server;

/**
 *
 * @author mesut
 */
public class start {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Server s = new Server(5030);
        
    }
    
}
