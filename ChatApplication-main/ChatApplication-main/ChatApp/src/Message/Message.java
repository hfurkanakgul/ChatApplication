/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mesut
 */
public class Message implements java.io.Serializable{
    public enum messageType {Name,Text,ConnectedClients, ChatGroupConnection,
        PrivateRoomUpdated, PrivateRoomJoin, PrivateRoomList, PrivateRoomCreated};  //ilgili alanlara göre mesaj tipleri belirlenir
    public messageType type;    //mesaj tipi
    public String owner;    //sahibi
    public Object content;  //içerik
    public ArrayList<String> userList;  //mesajın gideceği kişilerin listesi
    public String roomName; //oda adı
    public boolean isPrivateRoom = false;   //chat gru chatmi iki kişilik mi
    public HashMap<String, ArrayList<String>> roomList; //oda listesi ve içerideki kullanıcılar
    public String[] roomListforPrivate; //özel oda ise kullanıcı listeleri alınır
    public String hasFile = null;   //dosya mı değil mi
    public Message(messageType _type){
        type = _type;
    }
}
