/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Group.mesajEkran;
import Message.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

/**
 *
 * @author furkan
 */
public class Client {

    public Socket socket;               //Öncelikle socket oluşturulur
    public ObjectInputStream Input;     //Veri yazma işlemleri yapabilmek için obje oluşturulur.
    public ObjectOutputStream Output;   //Karşıdan gelen veriyi okuma işlemi yapılabilmesi için obje oluşturulur
    public Listen Listen;               //Cient tarafından gelen mesajları okumak için bir thread oluşturulur   
    public mainScreen ms;               //Ana ekran nesnesi
    public ArrayList<mesajEkran> chatFrameListe;  // Mesajlaşma ekranı 
    public String userName;         //client adı verilir

    public void basla(String ip, int port, firstScreen e, String nameUser) {
        e.setVisible(false);                        //eski ekran kapatılır
        ms = new mainScreen(this);                  //ana ekran açılır
        ms.setVisible(true);                        
        chatFrameListe = new ArrayList<mesajEkran>(); //chatframlerine tutulacağı liste açılır
        try {
            socket = new Socket(ip, port);                                  //gerekli port açılır ve sunucu ile bağlantısı sağlanır
            Input = new ObjectInputStream(socket.getInputStream());         //karşı taraftan gelen veriyi okumak için obje oluşturulur.
            Output = new ObjectOutputStream(socket.getOutputStream());      //gelen veriyi yazmak için obje oluşturulur.
            Listen = new Listen(this);                                  
            Listen.start();                                                 //gelen verileri dinleme işlemleri başlar
            userName = nameUser;                                            //atama işlemleri yapılır
            Message name = new Message(Message.messageType.isim);           //kullanıcı adı mesaja yazılır
            name.icerik = userName;                                         //atama işlemleri yapılır
            gonder(name);                                                   //sunucuya aktarım sağlanır
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void dur() {                                                         //clientları durdurmak için kullanılan fonksiyon
        if (socket != null) {
            try {
                Output.flush();
                Output.close();
                Input.close();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void gonder(Message mesaj) {                                           //clientdan gelen mesajı yazma fonksiyonu
        try {
            Output.writeObject(mesaj);                                            
            Output.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class Listen extends Thread {                                                   //sunucuda dinleme işlemlerini yaptığımız thread

    Client _client;                                                             //client objesi değişkene atanır
    DefaultListModel<String> RoomList = new DefaultListModel<String>();         //DefaultModel oluşturulur

    Listen(Client sclient) {
        _client = sclient;                                                      //clientlar arasında değişkenler atanır
    }

    @Override
    public void run() {
        while (_client.socket.isConnected()) {                                  //Clienta bağlı olduğu vakitlerde işlemler başlar.
            try {
                Message alinan = (Message) (_client.Input.readObject());        //gelen datayı message tipine çevirir
                switch (alinan.tip) {                                           //gelen dataya göre ilgiyi sorguya gider
                    case baglanClient:                                          // Yeni bağlanan kulanıcı için
                        ArrayList<String> users = (ArrayList<String>) (alinan.icerik);   //alınan bilgileri tutar
                        DefaultListModel<String> design = new DefaultListModel<String>();
                        for (int i = 0; i < users.size(); i++) {
                            design.add(i, users.get(i));                        
                        }
                        _client.ms.userList.setModel(design);
                        _client.ms.user.setText(_client.userName);              //labelda kullanıcı adı yazar

                        int ındexRoom = 0;                                      //ilgili atamalar yapılır

                        for (Map.Entry<String, ArrayList<String>> room : alinan.roomList.entrySet()) {
                            RoomList.add(ındexRoom++, room.getKey());           //priv chat odaları listeye eklenir
                        }
                        _client.ms.roomList.setModel(RoomList);
                        break;
                        
                    case ChatGrupBaglanti:                                     
                            boolean cont = true;                                 //öncesinde gelen bir mesaj olduğunu kontrol edilir
                            for (int i = 0; i < _client.chatFrameListe.size(); i++) { 
                                mesajEkran chatFrame = _client.chatFrameListe.get(i); //her ekran sırası ile atanır
                                if (alinan.userList.contains(chatFrame.odaIsmi) && !alinan.PrivateRoom) { //chat ekranının kişilerinin kontrolunu sağlar
                                    String text = "";
                                    text = chatFrame.chatField.getText();
                                    String newMsg = alinan.kendi + " : " + alinan.icerik.toString() + "\n"; //gelen data formatlanır 
                                    chatFrame.chatField.setText(text + newMsg);                             //ekrana yansıtılır
                                    cont = false; 
                                } else if (alinan.PrivateRoom && chatFrame.odaIsmi.equals(alinan.kendi)) { //chat ekranının kişilerinin kontrolunu sağlar
                                    cont = false; 
                                    String msg = "";
                                    msg = chatFrame.chatField.getText();
                                    String newMsg = alinan.icerik.toString() + "\n";                        //gelen data formatlanır 
                                    chatFrame.chatField.setText(msg + newMsg);                              //mesajı gönder
                                }
                            }
                            if (cont) {                                                     //control doğru ise daha önce bu kişiye mesaj gelmemiş demektir
                                ArrayList<String> ChatUserList = new ArrayList<String>();   //temp bir liste oluştur
                                ChatUserList.add(_client.userName);                         
                                ChatUserList.add(alinan.kendi);                         
                                _client.chatFrameListe.add(new mesajEkran(_client, ChatUserList, alinan.kendi)); //yeni bir mesaj ekranı oluşturulur
                                int FrameIndex = _client.chatFrameListe.size() - 1;   //sona eklendiğinden dolayı eleman indexini tutulur
                                String newMesaj = alinan.kendi + " : " + alinan.icerik.toString() + "\n";    //gelen data formatlanır 
                                _client.chatFrameListe.get(FrameIndex).chatField.setText(newMesaj); //mesajı göster
                                _client.chatFrameListe.get(FrameIndex).users.setVisible(false);   //tek kişi olduğundan dolayı yan tarafatdaki listeyi kapalı tut
                                _client.chatFrameListe.get(FrameIndex).setVisible(true);  //ekranı göster
                            }
                            cont = true; 
                        
                        break;
                    case roomPrivList:                                          //yeni özel oda oluşturma sorgusu
                        RoomList.add(RoomList.size(), alinan._room);            
                        _client.ms.roomList.setModel(RoomList);
                        break;
                    case roomPrivUpdate:                                                                    //odaya eklemeler olduğunda çalışan sorgu
                        for (int i = 0; i < _client.chatFrameListe.size(); i++) {                           //ilgili ekranlar aranır
                            if (_client.chatFrameListe.get(i).odaIsmi.equals(alinan._room)) {   
                                _client.chatFrameListe.get(i).userList = new ArrayList<String>();           //hata vermemesi için bir arrayliste tutulur
                                DefaultListModel<String> dm = new DefaultListModel<>();                     //list model oluşturulur
                                for (int j = 0; j < alinan.privRoomListe.length; j++) {
                                    dm.add(j, alinan.privRoomListe[j]);                                     //listeler modele eklenir
                                    _client.chatFrameListe.get(i).userList.add(alinan.privRoomListe[j]);    //clientin listesine eklenir
                                }
                                _client.chatFrameListe.get(i).users.setModel(dm);                           //model ekranda gösterilir
                                _client.chatFrameListe.get(i).users.setVisible(true);                       // listeler gösterilir
                            }
                        }
                        break;
                    case mesaj:                                                                             //odaya mesaj geldiğinde çalışan sorgu
                        String foo = _client.ms.chat.getText();                                             
                        foo += alinan.icerik.toString() + "\n"; 
                        _client.ms.chat.setText(foo);                                                       //ekranda gösterilir
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
