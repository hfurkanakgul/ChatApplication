/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author furkan
 */
public class Message implements java.io.Serializable{
    public enum messageType {isim,mesaj,baglanClient, ChatGrupBaglanti,
        roomPrivUpdate, ozelOdaGiris, roomPrivList, ozelOdaOlustur};    //ilgili alanlara göre mesaj tipleri belirlenir
    public messageType tip;                                             //mesaj 
    public String kendi;                                                //sahibi
    public Object icerik;                                               //içerik
    public ArrayList<String> userList;                                  //mesajın ulaşacağı kişilerin listesi
    public String _room;                                                //oda 
    public boolean PrivateRoom = false;                                 //chat grubu
    public HashMap<String, ArrayList<String>> roomList;                 //oda listesi ve kullanıcılar
    public String[] privRoomListe;                                      //özel oda
    public Message(messageType _type){
        tip = _type;
    }
}