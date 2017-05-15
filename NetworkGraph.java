/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controllorecsse;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.swing.*;

import org.jgraph.*;
import org.jgraph.graph.*;
import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;
// resolve ambiguity
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author Andrea
 */
public class NetworkGraph extends JApplet{
    
    private static final long serialVersionUID = 3256444702936019250L;
    private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFA");
    private static final Dimension DEFAULT_SIZE = new Dimension(800, 600);
    
    
    
    ListenableUndirectedGraph<String, DefaultEdge> g = new ListenableUndirectedGraph<>(DefaultEdge.class);
    private JGraphModelAdapter<String, DefaultEdge> jgAdapter = new JGraphModelAdapter<>(g);
    JGraph jgraph = new JGraph(jgAdapter);
    DefaultEdge link;
    
    ControlloreCSSE ctrl;
    
    private void adjustDisplaySettings(JGraph jg)
    {
        jg.setPreferredSize(DEFAULT_SIZE);
        Color c = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter("bgcolor");
        } catch (Exception e) {
        }

        if (colorStr != null) {
            c = Color.decode(colorStr);
        }

        jg.setBackground(c);
        try {
            ImageIcon img_icon = new ImageIcon(ImageIO.read(new File("sfondo_leo.jpg")));
                    jg.setBackgroundImage(img_icon);
                    } catch (IOException ex) {
            Logger.getLogger(NetworkGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("unchecked") // FIXME hb 28-nov-05: See FIXME below
    private void positionVertexAt(Object vertex, int x, int y, boolean treat, boolean update, int over_tolerance)
    {   
        DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
        AttributeMap attr = cell.getAttributes();
        //Rectangle2D bounds = GraphConstants.getBounds(attr); //questa non si usa
        Rettangolo.Double newBounds = new Rettangolo.Double(x, y, 50, 50);
        GraphConstants.setBounds(attr, newBounds);
        
        GraphConstants.setBorder(attr, BorderFactory.createLineBorder(Color.black, 0));
        if(treat==false)
        {
            if(over_tolerance>3)
                GraphConstants.setBackground(attr, Color.decode("#0000FF")); //nodo sospetto
            else
                GraphConstants.setBackground(attr, Color.decode("#00FF00")); //nodo normale
        }
        else
            GraphConstants.setBackground(attr, Color.decode("#FF0000")); //nodo malevolo 
        if(update==true)
            GraphConstants.setBorder(attr, BorderFactory.createLineBorder(Color.decode("#FFFF00"),4));
        else
            GraphConstants.setBorder(attr, BorderFactory.createLineBorder(Color.black, 1));
        
        AttributeMap cellAttr;
        cellAttr = new AttributeMap();
        cellAttr.put(cell, attr);
        
        jgraph.getGraphLayoutCache().edit(cellAttr,null,null,null);
    }

    
    void init_graph(){
        adjustDisplaySettings(jgraph);
        getContentPane().add(jgraph);
        resize(DEFAULT_SIZE);
        //EVENTO DI CLICK
        jgraph.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { //VERIFICA DI UN SOLO CLICK E PRESA COSCIENZA DEL NODO
                    int x = e.getX(), y = e.getY();
                    Object cell = jgraph.getFirstCellForLocation(x, y);
                    if (cell != null) { //AZIONE DOPO CLICK
                        String lab = jgraph.convertValueToString(cell);
                        for(int i=1; i<=ctrl.getN(); i++)
                        {
                            if(ctrl.nodo[i].ID==Integer.parseInt(lab.replaceAll("[\\D]", "")))
                            {
                                /*Rimozione avviso di aggiornamento del nodo*/
                                ctrl.nodo[i].update=false;
                                ctrl.update_array[i]=false;
                                refresh_graph(ctrl);
                                /*Fine rimozione avviso di aggiornamento del nodo*/
                                
                                /*Apertura del popup "Eliminare il nodo dalle route?"*/
                                int scelta = JOptionPane.showConfirmDialog(null, "Si: nodo accettabile come Next Hop\nNo: nodo non accettabile come Next Hop");
                                //scelta vale 0 se sì; scelta vale 1 se no; scelta vale 2 se annulla;
                                if(scelta==0)
                                {
                                    ctrl.nodi_scartati[i]=false;
                                }
                                if(scelta==1)
                                {
                                    ctrl.nodi_scartati[i]=true;
                                }
                                /*Fine apertura del popup "Eliminare il nodo dalle route?"*/
                                
                            }
                               
                        }
                        //System.out.println(lab); //print di prova, indicava il nodo con evento 
                    }
                }
            }
        });
        //FINE EVENTO DI CLICK
    }
    
    public static <V,E> void removeAllEdges(Graph<V, E> graph) {
		LinkedList<E> copy = new LinkedList<E>();
		for (E e : graph.edgeSet()) {
			copy.add(e);
		}
		graph.removeAllEdges(copy);
	}
    
    public static <V,E> void removeAllVertices(Graph<V, E> graph) {
		LinkedList<V> copy = new LinkedList<V>();
		for (V v : graph.vertexSet()) {
			copy.add(v);
		}
		graph.removeAllVertices(copy);
	}
    
    void refresh_graph(ControlloreCSSE ctrl_import)
    {
        ctrl = ctrl_import;
        int [][] map = ctrl.getMatriceProx();
        
        NetworkGraph.removeAllEdges(g);
        NetworkGraph.removeAllVertices(g);
        
        
        String [] nodo = new String[ctrl.getN()];
        
        for(int i=0; i<ctrl.getN() ;i++)
        {
            nodo[i] = "Nodo" + (i+1); //Leggi quanti nodi ci sono
        }

        
        for(int i=0; i<ctrl.getN() ;i++)
        {
            g.addVertex(nodo[i]); // add some sample data (graph manipulated via JGraphT)
            
        }
        int r,c;
        for(r=0;r<ctrl.getN();r++)
        {
            for(c=0;c<ctrl.getN();c++)
            {
                if(c==r)
                {
                    break;
                }
                    if(map[r][c]==map[c][r]){
                        if(map[r][c]==1)
                        {
                            link = g.addEdge(nodo[r], nodo[c]);
                            DefaultGraphCell link1 = jgAdapter.getEdgeCell(link);
                            AttributeMap attr = link1.getAttributes();
                            /* Controllo del flusso e modifica della dimensione del link */ 
                            if(((ctrl.Matrice_route[r+1][c+1]+ctrl.Matrice_route[c+1][r+1])-(ctrl.sizeLink[r+1][c+1]+ctrl.sizeLink[c+1][r+1]))>0
                                    & ((ctrl.Matrice_route[r+1][c+1]+ctrl.Matrice_route[c+1][r+1])-(ctrl.sizeLink[r+1][c+1]+ctrl.sizeLink[c+1][r+1]))<6)
                                GraphConstants.setLineWidth(attr, 2);
                            if(((ctrl.Matrice_route[r+1][c+1]+ctrl.Matrice_route[c+1][r+1])-(ctrl.sizeLink[r+1][c+1]+ctrl.sizeLink[c+1][r+1]))>5
                                    & ((ctrl.Matrice_route[r+1][c+1]+ctrl.Matrice_route[c+1][r+1])-(ctrl.sizeLink[r+1][c+1]+ctrl.sizeLink[c+1][r+1]))<11)
                                GraphConstants.setLineWidth(attr, 3);
                            if(((ctrl.Matrice_route[r+1][c+1]+ctrl.Matrice_route[c+1][r+1])-(ctrl.sizeLink[r+1][c+1]+ctrl.sizeLink[c+1][r+1]))>10
                                    & ((ctrl.Matrice_route[r+1][c+1]+ctrl.Matrice_route[c+1][r+1])-(ctrl.sizeLink[r+1][c+1]+ctrl.sizeLink[c+1][r+1]))<21)
                                GraphConstants.setLineWidth(attr, 4);
                            if(((ctrl.Matrice_route[r+1][c+1]+ctrl.Matrice_route[c+1][r+1])-(ctrl.sizeLink[r+1][c+1]+ctrl.sizeLink[c+1][r+1]))>20)
                                GraphConstants.setLineWidth(attr, 5);
                            if(((ctrl.Matrice_route[r+1][c+1]+ctrl.Matrice_route[c+1][r+1])-(ctrl.sizeLink[r+1][c+1]+ctrl.sizeLink[c+1][r+1]))<1)
                                GraphConstants.setLineWidth(attr, 1);
                            AttributeMap cellAttr;
                            cellAttr = new AttributeMap();
                            cellAttr.put(link1, attr);
                            jgraph.getGraphLayoutCache().edit(cellAttr,null,null,null);
                        }
                    }
            }
       
                //positionVertexAt(nodo[r],r*100,b*100, ctrl.nodo[r+1].n_corrupt_route!=0, ctrl.nodo[r+1].update);// POSIZIONI FISSE 
                positionVertexAt(nodo[r], ctrl.nodo[r+1].x_position, ctrl.nodo[r+1].y_position, ctrl.nodo[r+1].n_corrupt_route!=0, ctrl.nodo[r+1].update, ctrl.conta_blu[r+1]); //da stream
            //if(r%4==0)
              //  b++;
            
        }
    }

    
}
