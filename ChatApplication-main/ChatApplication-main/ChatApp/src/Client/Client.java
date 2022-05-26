/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import GroupChat.mesajEkran;
import Message.Message;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import java.io.File;

/**
 *
 * @author mesut
 */
public class Client {

    public Socket socket;   //Soket oluşturulur
    public ObjectInputStream sInput;    //Veri yazma objesi oluşturulur
    public ObjectOutputStream sOutput;  //Gelen veriyi okuma objesi oluşturulur
    public Listen listen;   //Cientin sunucudan gelen mesajları dinlemesi için thread oluşturulur
    public mainScreen mf;    //Programın ana frame'i
    public ArrayList<mesajEkran> chatFrameList;  // Diğer kullanıcılar ile konuştuğu framelerin listesi
    public String userName; //clientin adı

    public void Start(String ip, int port, firstScreen e, String _userName) {
        e.setVisible(false);    //kullanıcı adı girilen ekran kapatılır
        mf = new mainScreen(this);   //ana frame oluşturulur
        mf.setVisible(true);    //görünmesi açılır
        chatFrameList = new ArrayList<mesajEkran>(); //chatframe listesi başlatılır
        try {
            socket = new Socket(ip, port);  //ilgili bilgilerle port açılır ve sunucuya bağlanılır
            sInput = new ObjectInputStream(socket.getInputStream());    //gelen veriyi okuma objesş oluşturulur
            sOutput = new ObjectOutputStream(socket.getOutputStream()); //veri yazma objesi oluşturulur
            listen = new Listen(this);  //sunucuyu dinlemek için thread olşturulur
            listen.start(); //dinleme başlar
            userName = _userName;   //verilen kullanıcı adı cliente atanır
            Message name = new Message(Message.messageType.Name);   //alınan kullanıcı adı mesaja yazılır
            name.content = userName;
            Send(name); //sunucuya gönderilir

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Stop() {    //clienti durdurma fonksiyonu
        if (socket != null) {
            try {
                sOutput.flush();
                sOutput.close();
                sInput.close();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void Send(Message msg) { //clientden mesaj yazma fonksiyonu
        try {
            sOutput.writeObject(msg);   //gelen mesaj porta yazılır
            sOutput.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

class Listen extends Thread {   //sunucuyu dinleme threadi

    Client client;  //client objelerine erişmek için client değişkeni tutulur
    DefaultListModel<String> modelRoomList = new DefaultListModel<String>(); //listeye yazmak için gerekli defaultmodel

    Listen(Client _client) {
        client = _client;   //constructerdan gelen client ilgili değişkene atılır
    }

    @Override
    public void run() {
        while (client.socket.isConnected()) {   //client bağlı olduğu sürece dinlemde bekler
            try {
                Message received = (Message) (client.sInput.readObject());  //mesaj geelince okuduğu bilgiyi mesaj tipine çevirir
                switch (received.type) {    //mesajın tipine göre ilgili alana gider
                    case ConnectedClients:  // Yeni bağlanan kulanıcıya kullanıcı listesini göndermek için kullannılır
                        ArrayList<String> users = (ArrayList<String>) (received.content);   //gelen bilgi arrayliste aktarılır
                        DefaultListModel<String> model = new DefaultListModel<String>();
                        for (int i = 0; i < users.size(); i++) {
                            model.add(i, users.get(i)); //arraylist den delisteye bastırılır
                        }
                        client.mf.userList.setModel(model);
                        client.mf.user.setText(client.userName);    //labelda clientin adı  görünür

                        // Set Private room list
                        int roomIndex = 0;

                        for (Map.Entry<String, ArrayList<String>> room : received.roomList.entrySet()) {
                            modelRoomList.add(roomIndex++, room.getKey());  //burada da özel chat odaları ilgili listeye yazılır
                        }
                        client.mf.roomList.setModel(modelRoomList);

                        break;
                    case ChatGroupConnection: //grup konuşmaları için kullanılır
                        if (received.hasFile != null) { //gelen şey bir dosyaise buraya girer
                            String home = System.getProperty("user.home");  //kullanıcı adı dizini alınır
                            File file= new File(home+"/Downloads/"+client.userName+"_"+received.hasFile); //kaydedilecek path ayarlanır
                           OutputStream os = new FileOutputStream(file); //kaydetmek için outputstream oluşturulur
                           byte[] fileContent = (byte[])received.content;   //gelen bilgi uzunluğunda byte array oluşturulur
                           os.write(fileContent);   //ilgili bilgi kaydedilir
                           os.close();
                           
                        } else {    //gelen şey dosya değil ise buraya girer
                            boolean append = true;  //daha önce bu kullanıcıdan mesaj alınmış mı bunu anlamak için flag tutulur
                            for (int i = 0; i < client.chatFrameList.size(); i++) { //clientin framelistesi kadar dönülür
                                mesajEkran cf = client.chatFrameList.get(i); //her frame sıra ile alınır
                                if (received.userList.contains(cf.roomName) && !received.isPrivateRoom) { //bu chatframe kullanıcılar arasında var mı? ve iki kişilik bir sohbet mi?
                                    String text = "";
                                    text = cf.chatField.getText();
                                    String newMsg = received.owner + " : " + received.content.toString() + "\n"; //gelen mesajı formatla
                                    cf.chatField.setText(text + newMsg);   //chatfrme ekranına bas
                                    append = false; //diğer alana girmesini engellemek için flag değiş
                                } else if (received.isPrivateRoom && cf.roomName.equals(received.owner)) { //grup chati ve frameler arasında mevcut ise
                                    append = false; //yine diğer tarafa girmesini engelle
                                    String text = "";
                                    text = cf.chatField.getText();
                                    String newMsg = received.content.toString() + "\n"; //mesajı formatla
                                    cf.chatField.setText(text + newMsg);    //gönder
                                }
                            }
                            if (append) {   //flag hala true ise bu kişiye ilk kez mesaj geliyor demektir
                                ArrayList<String> tmpChatUserList = new ArrayList<String>(); //geçici bir user arraylisti oluştur
                                tmpChatUserList.add(client.userName);   //gönderilecek listeye kendini ekle
                                tmpChatUserList.add(received.owner);    //mesajı göndereni de gönderilecekler listesine ekle
                                client.chatFrameList.add(new mesajEkran(client, tmpChatUserList, received.owner)); //yeni bir frame oluştur
                                int newCFrameIndex = client.chatFrameList.size() - 1;   //sondaki elemanın indexini al (çünkü sona ekliyor )
                                String newMsg = received.owner + " : " + received.content.toString() + "\n";    //gelen mesajı formatla
                                client.chatFrameList.get(newCFrameIndex).chatField.setText(newMsg); //mesajı ekrana bas
                                client.chatFrameList.get(newCFrameIndex).users.setVisible(false);   //sağdaki listeyi tek kişi olduğu için kapat
                                client.chatFrameList.get(newCFrameIndex).setVisible(true);  //frami ekrana çıkart
                            }
                            append = true; //flagi geri true yap
                        }
                        break;
                    case PrivateRoomList:   //kurulan yeni oda bilgisi için kullanılır
                        modelRoomList.add(modelRoomList.size(), received.roomName); //gelen oda listesini frameinde gösterir
                        client.mf.roomList.setModel(modelRoomList);
                        break;
                    case PrivateRoomUpdated:    //biri odaya girdiğinde odadaki herkese girdiğine dair mesaj gider
                        for (int i = 0; i < client.chatFrameList.size(); i++) { //ilgili frame for ile aranır
                            if (client.chatFrameList.get(i).roomName.equals(received.roomName)) {   //bulunduğunda
                                client.chatFrameList.get(i).userList = new ArrayList<String>(); //null hatası vermemesi için arrayliste atanır
                                DefaultListModel<String> dm = new DefaultListModel<>(); //ekrana basmak çin listmodel olusturulur
                                for (int j = 0; j < received.roomListforPrivate.length; j++) {  //gelen liste for ile dönülür
                                    dm.add(j, received.roomListforPrivate[j]);  //hepsi default modele eklenir
                                    client.chatFrameList.get(i).userList.add(received.roomListforPrivate[j]);   //clientin userlistine eklenir
                                }
                                client.chatFrameList.get(i).users.setModel(dm); //model ekrana basılır
                                client.chatFrameList.get(i).users.setVisible(true); // liste görünür hale getirilir
                            }
                        }
                        break;
                    case Text:  //genel mesaj geldiğinde çalışır
                        String temp = client.mf.chat.getText(); //clientin güncel hali alınır
                        temp += received.content.toString() + "\n"; //yeni mesaj üzerine eklenir
                        client.mf.chat.setText(temp);   //ekrana basılır
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
