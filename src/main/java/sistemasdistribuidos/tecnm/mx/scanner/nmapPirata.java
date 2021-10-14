/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemasdistribuidos.tecnm.mx.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author rgo19
 */

public class nmapPirata {
    
    public static void main(String args[]){
        nmapPirata nmap = new nmapPirata();
        ExecutorService es = Executors.newFixedThreadPool(4);
        int time = 20;
        for(int i = 1; i<=1024; i+=4){
            es.execute(new Thread(new portScan("127.0.0.1",i,time,nmap)));
            es.execute(new Thread(new portScan("127.0.0.1",i+1,time,nmap)));
            es.execute(new Thread(new portScan("127.0.0.1",i+2,time,nmap)));
            es.execute(new Thread(new portScan("127.0.0.1",i+3,time,nmap)));
            try {
                Thread.sleep(time);
            } catch (InterruptedException ex) {
                Logger.getLogger(nmapPirata.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        es.shutdown();
        //nmap.getOpenPorts().forEach(System.out::println);
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("---PRESIONE ENTER PARA CONTINUAR---");
            sc.nextLine();
            System.out.print("\033[H\033[2J");
            System.out.flush();
            nmapPirata.executePowershellScript(nmap.getOpenPorts());
        } catch (IOException ex) {
            Logger.getLogger(nmapPirata.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public static void executePowershellScript(List<Integer> l) throws IOException{
        String listPort = "(",state = "Listen", ip = "127.0.0.1";
        int j = l.size(), aux = 1;
        for(int i : l){
            listPort += String.valueOf(i) + ((aux==j)?")":",");
            aux++;
        }
        listPort = "@" + listPort;
        String command = "powershell.exe /c .\\lectura.ps1 " + String.format("-Array %s -state \"%s\" -Ip \"%s\"", listPort,state,ip);
        //System.out.println(command);
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = bf.readLine()) != null) {
            System.out.println(line);
        }
        bf = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while((line = bf.readLine())!=null){
            System.out.println(line);
        }
    }
    public nmapPirata(){
        open = new ArrayList<>();
    }
    public synchronized void addPort(Integer i){
        try {
            this.wait(10);
            System.out.println("Nuevo puerto encontrado " + String.valueOf(i));
            open.add(i);
            this.notifyAll();
        } catch (InterruptedException ex) {
            Logger.getLogger(nmapPirata.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    public List<Integer> getOpenPorts(){
        return open;
    }
    
    private List<Integer> open;
}
class portScan implements Runnable{
    private nmapPirata s;
    private String ip;
    private int port, timeout;

    portScan(String ip, int port, int timeout, nmapPirata s) {
        this.ip = ip;
        this.port = port;
        this.timeout = timeout;
        this.s = s;
    }
    public boolean portIsOpen() {
        try {
            //System.out.println(port);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }  
    @Override
    public void run() {
        if(portIsOpen()){
            //System.out.println(String.format("Puerto abierto: %d con tiempo de espera %d ms\n",port,timeout));
            s.addPort(port);
        }
    }
    
}
