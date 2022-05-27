/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Group;

import Client.Client;
import java.util.ArrayList;

/**
 *
 * @author mesut
 */
public class ChatRoom {

    public String room;                     //oda adı
    public ArrayList<String> userListe_;    //kullanıcılar
    public mesajEkran f;                    //ekran
    public Client _client;                    
    public ChatRoom( ArrayList<String> userList, String roomName, Client client)
    {
                                                //verilenler ile bir ekran oluşturulur
        room = roomName;
        userListe_ = userList;
        this._client = client;
        f = new mesajEkran(this._client, userListe_, room);
        f.setVisible(true);                     //ekran gösterilir
        f.users.setVisible(false);              //grup mesajı değilse sağdaki ekran kapatılır
    }
}
