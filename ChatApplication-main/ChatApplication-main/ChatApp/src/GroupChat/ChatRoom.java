/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GroupChat;

import Client.Client;
import java.util.ArrayList;

/**
 *
 * @author mesut
 */
public class ChatRoom {

    public String roomName; //oda adı
    public ArrayList<String> userList;  //odadaki kullanıcılar
    public mesajEkran frame; //frame objesi
    public Client client;   //client
    public ChatRoom( ArrayList<String> _userList, String _roomName, Client _client)
    {
        //bu bilgiler ile frame oluşturulur
        roomName = _roomName;
        userList = _userList;
        client = _client;
        frame = new mesajEkran(client, userList, roomName);
        frame.setVisible(true);        // frame görünür hale getirilir
        frame.users.setVisible(false);  // tekli mesaj ise sağdaki listenin görünümü engellenir
    }
}
