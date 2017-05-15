package controllorecsse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author Andrea
 */
public class ControlloreCSSE {

    private int [][] MatriceProx; //matrice di connettività
    public int [][] Matrice_route;
    public int [][] sizeLink;
    private int n;  //numero di nodi nella rete
    public Nodo [] nodo;
    public int [] bad_array;
    public boolean [] nodi_scartati;
    public boolean [] update_array;
    public boolean [] tolerance_array;
    public int [] conta_blu;
    
    /* variabili parametrizzate */
    int input_parametri=0;
    String IP_Destination;
    int packet_tolerance;
    int T_update;
    
    
    public int[][] getMatriceProx() {
        return MatriceProx;
    }
    
    public int isNear(int r, int c)
    {
        return MatriceProx[r-1][c-1];
    }

    public void setMatrice(int[][] Matrice) {
        this.MatriceProx = Matrice;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public ControlloreCSSE(int[][] Matrice) {
        this.MatriceProx = Matrice;
        this.nodo = new Nodo[100]; //i 100 sono tutti valori indicativi utilizzati come max dimensione degli array
        this.bad_array = new int [100];
        this.update_array = new boolean [100];
        this.nodi_scartati = new boolean [100];
        this.tolerance_array = new boolean [100];
        this.Matrice_route = new int [100][100];
        this.sizeLink = new int [100][100];
        this.conta_blu = new int [100];
        for(int i=1;i<100;i++)
        {
            this.bad_array[i] = 0;
            this.update_array[i] = false;
            this.nodi_scartati[i] = false;
            this.tolerance_array[i] = false;
            this.conta_blu[i] = 0;
            for(int j=1;j<100;j++)
            {
                this.sizeLink[i][j]=0;
                this.Matrice_route[i][j]=0;
            }
        }
    }
    
    public void setMatrice(ControlloreCSSE ctrl) throws FileNotFoundException{
        /*setto la matrice in base a ciò che leggo dal file - ATTENZIONE - NON E' STATA AGGIORNATA*/
        int [][] map;
        int r=-1,c=0;
        Scanner s;
        try{
        s = new Scanner(new File("matrice_prox.txt"));
        //conta quanti nodi sono presenti nella rete
        do{
            System.out.println(s.nextLine());
            r++;
        }while(s.hasNextLine()==true);
        this.setN(r);
        ctrl.nodo = new Nodo [this.getN()+1];
        map = new int [r][r];
        s.close();
        s = new Scanner(new File("matrice_prox.txt"));
        //caricamento della matrice ricevuta
        s.nextLine();
        for(r=0;s.hasNextLine();r++)
        {
            ctrl.nodo[r+1] = new Nodo(r+1,new int [ctrl.getN()+1][ctrl.getN()+1], 0,0);
            ctrl.nodo[r+1].n_corrupt_route=ctrl.bad_array[r+1];
            ctrl.nodo[r+1].update = ctrl.update_array[r+1];
            ctrl.nodo[r+1].over_tolerance = ctrl.tolerance_array[r+1];
            s.next();
            for(c=0;c<this.getN();c++)
            {
                if(s.next().compareTo("x")==0)
                    map[r][c]=0; //Non sono vicini; contrassegnato come 0
                else
                    map[r][c]=1; //Sono vicini; contrassegnato come 1
            }
            
        }
        s.close();
        this.setMatrice(map);
        }catch(InputMismatchException | FileNotFoundException ex)
        {
            System.out.println(ex);
        }
    }
    
    
    public void ctrlMatrice(){
    
        int r,c,x=0,up_comodo;
        
        for(int i=1;i<=this.getN();i++)
        {
            if(this.nodi_scartati[i]==true)
            {
                for(r=0;r<this.getN();r++)
                {
                    if(r!=i-1)//con questa condizione il nodo malizioso non cancella se stesso
                    {
                            this.MatriceProx[r][i-1]=0; //cancello il nodo malizioso dalle route di tutti i nodi
                            //in questo modo, i nodi non lo useranno più come next hop
                            this.Matrice_route[i][r+1]=0;
                    }
                    //non modifico le route del nodo malizioso, altrimento non saprò mai se questo non è più corrotto.
                }
                
            }
            if(this.nodo[i].over_tolerance==true)
                this.conta_blu[i]++;
            else
                conta_blu[i]=0;
        }
        
        
        for(r=0;r<this.getN();r++)
        {
            up_comodo=nodo[r+1].n_corrupt_route;
            nodo[r+1].n_corrupt_route=0; //inizializzo
            for(c=0;c<this.getN();c++)
            {
                if(this.MatriceProx[r][c]!=this.MatriceProx[c][r])
                {
                    x++;
                    if(this.MatriceProx[r][c]==1){
                        System.out.println("IDENTIFICATO NODO MALEVOLO - NODO: " +(r+1));
                        System.out.println("IL NEXT HOP DI " + (c+1) +" NON E' " + (r+1));
                        nodo[r+1].n_corrupt_route++;
                        //this.isolaNodo(r);
                    }
                }
            }
            if(up_comodo!=nodo[r+1].n_corrupt_route)//UPDATE PER NODO R
            {
                nodo[r+1].update=true;
            }
        }
        System.out.print("SONO PRESENTI "+ x/2 + " ROUTE CORROTTE\n");
        for(int i=1;i<=this.getN();i++){
            this.bad_array[i]=this.nodo[i].n_corrupt_route;
            this.update_array[i]=this.nodo[i].update;
        }
    }
    
    public void isolaNodo(int n_evil){ //QUESTA FUNZIONE CANCELLA I NODI MALIZIOSI DALLA TABELLA! DA NON USARE
        int r, c;
        this.setN(n-1);
        for(r=n_evil;r<this.getN();r++)
        {
            for(c=0;c<this.getN();c++)
            {
                this.MatriceProx[r][c]=this.MatriceProx[r+1][c]; //elimino tutta la riga e shifto le altre verso l'alto
            }
        }
        for(c=n_evil;c<this.getN();c++)
        {
            for(r=0;r<this.getN();r++)
            {
                this.MatriceProx[r][c]=this.MatriceProx[r][c+1]; //elimino tutta la colonna e shifto le altre verso l'alto
            }
        }
    }
    
    public void stampaMatrice()
    {
        //stampa della matrice
        int r,c;
        
        for(r=0;r<this.getN();r++)
        {
            System.out.println("Nodo " + (r+1));
            for(c=0;c<this.getN();c++)
            {
                System.out.print(this.getMatriceProx()[r][c] + " ");
            }
                System.out.print("\n");
        }
    }

    public int [][] duplicate_matrice_adiacenze(ControlloreCSSE ctrl)
    {
        int numberofvertices = ctrl.getN();
        int map [][] = ctrl.getMatriceProx();
        int adjacencymatrix[][] = new int[numberofvertices + 1][numberofvertices + 1];
        for (int sourcenode = 1; sourcenode <= numberofvertices; sourcenode++)
        {
            for (int destinationnode = 1; destinationnode <= numberofvertices; destinationnode++)
            {
                adjacencymatrix[sourcenode][destinationnode] = map[sourcenode-1][destinationnode-1];
 	        if (sourcenode == destinationnode)
                {
                    adjacencymatrix[sourcenode][destinationnode] = 0;
                    continue;
                }
                if (adjacencymatrix[sourcenode][destinationnode] == 0)
                {
                    adjacencymatrix[sourcenode][destinationnode] = 999;
                }
            }
        }
        return adjacencymatrix;
    }
    
    public void send_path_route(ControlloreCSSE ctrl) throws SocketException, IOException
    {
        
        int port =1819;
        int escluso=0;
        byte[] buf = new byte[200];
        String payload ="";
        InetSocketAddress addr = new InetSocketAddress(this.IP_Destination, port);
        DatagramSocket c_socket = new DatagramSocket();//creazione socket
        for(int i=1; i<=ctrl.getN();i++)
        {
            payload = payload.concat(ctrl.nodo[i].IP_Address + " ");
            for(int dest=1; dest<=ctrl.getN();dest++)
            {
                //se esiste una route per quel nodo, allora scrivo IP destinazione ed IP next hop
                if(ctrl.nodo[i].route[dest][1]!=999 & ctrl.nodo[i].route[dest][1]!=dest)
                {
                    escluso=1;
                    payload = payload.concat(ctrl.nodo[dest].IP_Address + " "); //destinazione
                    payload = payload.concat(ctrl.nodo[ctrl.nodo[i].route[dest][1]].IP_Address + " "); //next hop
                }
                
            }
            if(escluso==0) //non ha route se, non per i vicini
            {
                payload ="";
                continue;
            }
            payload = payload.concat("- "); //terminatore stringa di pacchetto intermedio
            //System.out.println("\n"+payload+"\n");
            buf = payload.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, addr);
            c_socket.send(packet);
            payload = ""; //clear payload
            escluso = 0;
        }
        buf = "endstream".getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, addr);
        c_socket.send(packet);
        c_socket.close();
        
        
    }
    
    public void send_malev_array(ControlloreCSSE ctrl) throws SocketException, IOException
    {
        
        int port =1822;
        byte[] buf = new byte[200];
        String payload ="";
        InetSocketAddress addr = new InetSocketAddress(this.IP_Destination, port);
        DatagramSocket c_socket = new DatagramSocket();//creazione socket
        for(int i=1; i<=ctrl.getN();i++)
        {
            if(nodi_scartati[i]==true)
            {
                payload = payload.concat(ctrl.nodo[i].MAC_Address + " ");
            }
        }
        //System.out.println("\n"+payload+"\n");
        buf = payload.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, addr);
        c_socket.send(packet);
        buf = "endstream".getBytes();
        packet = new DatagramPacket(buf, buf.length, addr);
        c_socket.send(packet);
        c_socket.close();
    }
   
    
    public static void main(String[] args) throws FileNotFoundException, SocketException, IOException {
        
        ControlloreCSSE ctrl = new ControlloreCSSE(null);
        int ripassa=0;
        if(ctrl.input_parametri==0)
        {
            Scanner input;
            ctrl.input_parametri=1;
            System.out.println("Inserire indirizzo IP di SVC: "); 
            input = new Scanner(System.in);
            ctrl.IP_Destination = input.nextLine(); //192.168.0.3
            System.out.println("Inserire il valore di packet tolerance: ");
            input = new Scanner(System.in);
            ctrl.packet_tolerance = Integer.parseInt(input.nextLine()); //150
            System.out.println("Inserire il periodo di aggiornamento del controllore (secondi): ");
            input = new Scanner(System.in);
            ctrl.T_update = Integer.parseInt(input.nextLine())*1000; //5
        }
        NetworkGraph applet = new NetworkGraph();
        //inizio codice gestione stream
        Thread t1 = new Thread(new Server_sock(ctrl));
        Thread t2 = new Thread(new Counter_MAC_sock(ctrl));
        
        t1.start();
        t2.start();
        //fine codice gestione stream
        
        JFrame frame = new JFrame();
        applet.init_graph();
        frame.getContentPane().add(applet);
        frame.setSize(600, 700);
        frame.setTitle("Leonardo CSSE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        
        
        for(;;){
        try {
            sleep(ctrl.T_update);
            //ctrl.setMatrice(ctrl); //QUESTA SOLO IN CASO DI INPUT DA FILE
        } catch (InterruptedException ex) {
            Logger.getLogger(ControlloreCSSE.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        //ctrl.stampaMatrice();
        ctrl.ctrlMatrice();
        applet.refresh_graph(ctrl);
        if(ripassa==1)
        {
            for(int i=1;i<ctrl.getN();i++){
                for(int j=1;j<ctrl.getN();j++)
                    ctrl.sizeLink[i][j]=ctrl.Matrice_route[i][j];
            }
            ripassa=0;
        }
        else
            ripassa++;
        /*frame.invalidate();
        frame.revalidate();
        frame.repaint();*/
    
        /*------------------------------------------------------------------------------------*/
        //COSTRUZIONE TABELLE DI ROUTING-> NEXTHOP PER OGNI NODO DELLA RETE
        BellmanFord bellmanford = new BellmanFord(ctrl.getN());
        for(int i=1; i<=ctrl.getN(); i++)
            ctrl.nodo[i].route=bellmanford.BellmanFordEvaluation(i, ctrl.duplicate_matrice_adiacenze(ctrl), ctrl.nodo[i].route);
        //FINE COSTRUZIONE TABELLE DI ROUTING PER OGNI NODO DELLA RETE
        /*------------------------------------------------------------------------------------*/
        ctrl.send_path_route(ctrl);
        ctrl.send_malev_array(ctrl);
    }
  }
    
}
