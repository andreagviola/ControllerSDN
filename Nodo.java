/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controllorecsse;

/**
 *
 * @author Andrea
 */
public class Nodo {
    int ID;
    String IP_Address;
    int MAC_Address;
    int x_position;
    int y_position;
    int [][] route;
    boolean update;
    boolean over_tolerance;
    int n_corrupt_route;
    

    public Nodo(int ID, int[][] route, int x_position, int y_position) {
        this.ID = ID;
        this.route = route;
        this.x_position = x_position;
        this.y_position = y_position;
        this.update = false;
        this.over_tolerance=false;
        this.n_corrupt_route = 0;
        this.IP_Address = "";
        this.MAC_Address = 0;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setRoute(int[][] route) {
        this.route = route;
    }
    
}
