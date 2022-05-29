/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Message.Message;
import Message.Message.messageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author furkan
 */
public class Sclient {

    Server _server;                                                             //serverdaki objeler için değişken atanır,cliet id verilir,soket objesi 
    int _id = 0; 
    Socket soket;  
    ObjectOutputStream sOutput;                                                  //Porta yazmak ve okumak için gerekli değişkenler oluşturulur
    ObjectInputStream sInput;   
    Listen _listen;                                                             // Clienti dinlemek için gerekli olan thread 
    String Client_name = "";

    public Sclient(Socket _socket, int _id, Server _server) {
        this._id = _id;                                                         //id, server ve soket bilgisi alınır
        this._server = _server;   
        soket = _socket;   
        try {
            sOutput = new ObjectOutputStream(soket.getOutputStream());          //dinleme ve yazma olması için gerekli objeler oluşturulur
            sInput = new ObjectInputStream(soket.getInputStream());             
        } catch (IOException ex) {
            Logger.getLogger(Sclient.class.getName()).log(Level.SEVERE, null, ex);
        }
        _listen = new Listen(this, this._server);                               //clientin dinleme threadı
    }

    public void Send(Message message) {                                         //mesja göndermek için
        try {
            sOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Sclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

class Listen extends Thread {                                                   //dinleme thread oluşturulur ve server, client objeleri oluşturulur.

    Sclient Client;    
    Server server;
    
    Listen(Sclient Client, Server _server) {
        this.Client = Client;                                                   //ilgili client ve server atanır
        server = _server;   
    }

    @Override
    public void run() {
        while (Client.soket.isConnected()) {                                    //client cağlı olduğu sürece dinlenir
            try {
                try {
                    Message received = (Message) (Client.sInput.readObject());  //gelen mesaj dönüştürülür
                    switch (received.tip) {                                     
                        case isim:                                              //ilk mesaj isim se ilk gönderen clientin ismidir ve client'a isim verilir
                            Client.Client_name = received.icerik.toString();    
                            Message clients = new Message(messageType.baglanClient);    // kullanıcıya geri mesaj gönderilir
                            ArrayList<String> users = new ArrayList<String>();  // Diğer kullanıcıların olduğu liste oluşur ve 
                            for (int i = 0; i < server.clients.size(); i++) {   // Sunucudaki client listesinden kişiler bu listeye aktarılır
                                users.add(server.clients.get(i).Client_name);  
                            }
                            clients.icerik = users;    
                            clients.roomList = server.odaListe; 
                            
                            server.Send(clients);                                                       //mesaj cliente gönder
                            
                            break;
                        case ChatGrupBaglanti:
                            server.Send(received);                                                      // mesaj geldiği gibi iat olduğu client veya clientlere iletilir
                            break;
                        case ozelOdaOlustur:                                                            // yeni oda oluşturulduğundaki mesaj tipi 
                            server.odaListe.put(received._room, new ArrayList<String>());               //serverda hashmape oluşturulan oda adı ve kullanıcıları koyulur
                            Message msg = new Message(Message.messageType.roomPrivList); 
                            msg._room = received._room;                                                 //oda isimlerine oda isimleri gider
                              server.Send(msg);                                                         //ait clientlere gönderilir
                            break;
                        case ozelOdaGiris:                                                              //özel odaya biri bağlandığı zaman
                                ArrayList<String> tmp = server.odaListe.get(received._room);     
                                tmp.add(received.kendi);                                                //değişkene gerçek kişiyi ekle
                                server.odaListe.put(received._room, tmp);                               //gerçek listeye bu kişiyi ekle
                                String[] liste = new String[server.odaListe.get(received._room).size()]; //güncellenmişleri odalarda bulunan clientlere gönder 
                                for (int i = 0; i < liste.length; i++) {
                                liste[i] = server.odaListe.get(received._room).get(i);                  //listeye kişileri ekle
                            }
                            Message odaBilgi = new Message(Message.messageType.roomPrivUpdate);         //yeni listenin gideceği mesaj
                            odaBilgi._room = received._room;                                            //oda eşitlenmesi
                            odaBilgi.roomList = server.odaListe;    
                            odaBilgi.userList = server.odaListe.get(received._room);
                            odaBilgi.privRoomListe = liste;    
                            server.Send(odaBilgi);  
                            break;
                        case mesaj:                                                                     //genel odaya atılan mesaj
                            received.icerik = Client.Client_name + " : " + received.icerik.toString();   //gelen data formatlanır 
                            server.Send(received);                                                      //mesajı  gönder
                            break;
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}