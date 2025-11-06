package org.example;

import java.sql.*;
import javax.sql.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    /*
    Atributos para la conexion a la base de datos
     */
    private final String URL = "jdbc:postgresql://localhost:5432/MinisterioGuerra";
    private final String USER = "postgres"; //Para ver el usuario SELECT current_user;
    private final String PASSWORD = "admin";
    private Connection conexion = null; //para realizar la conexion


    void main() {
        //Mostrar drivers y realizar conexion con BBDD
        DriverManager.drivers().forEach(driver -> System.out.println(driver.toString()));
        realizarConexion();
        //insertarEnemigos();
        sqlInjection();
        //seleccionarDatosEnemigos();
        cerrarConexion();

    }//Fin main

    public void realizarConexion(){
        try {
            /*
            Podemos realizar la consulta de dos formas, poniendo todo el codigo de una:

            conexion = DriverManager.getConnection("jdbc:postgresql://localhost:5432/MinisterioGuerra?user=postgres&password=admin");

            O podemos poner la consulta con los atributos separados:
             */
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexion realizada");

        } catch (SQLException e) {
            System.out.println("No se conecto con la base de datos, error "+e.getMessage());
        }
    }//Fin realizar conexion

    public void seleccionarDatosEnemigos(){
        try{
            Statement st = conexion.createStatement(); //statement para las consultas
            ResultSet resultado = st.executeQuery("SELECT * FROM enemigos");  //result para almacenar la respuesta

            //Vamos a ir recorriendo todos los resultados de la consulta
            while(resultado.next()){
                String nombre= resultado.getString("nombre");
                String genero = resultado.getString("genero");
                String pais = resultado.getString("pais_origen");
                System.out.println("ENEMIGO "+nombre+" de "+pais);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally { //Para cerrar la conexion
            cerrarConexion();
        }
    }//Fin seleccionar

    public void cerrarConexion(){
        if (conexion != null) {
            try{
                conexion.close();
                System.out.println("Conexion cerrada");
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }//Fin cerrar conexion

    public void insertarEnemigos(){
        String sql = "INSERT INTO enemigos (nombre, genero, pais_origen) VALUES ('Napoleon', 'helicopteroApache', 'francia')";
        try{
            PreparedStatement consultaInsert = conexion.prepareStatement(sql); //es distinto al statement normal
            int filasAfectadas = consultaInsert.executeUpdate();
            System.out.println("Se han insertado "+filasAfectadas+" filas");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }//Fin insertarEnemigo

    /*
    Siempre usa PreparedStatement cuando tengas valores que vienen del usuario o de variables,
    dado que statement normal es proclive al SQL Injection
     */
    public void sqlInjection(){
        System.out.println("--- SQL INJECTION DEMO ---");

        // Entrada maliciosa del usuario
        String entradaMaliciosa = "' OR '1'='1";

        // VULNERABLE: Statement con concatenación
        System.out.println("\n⚠️ VULNERABLE - Statement:");
        String sqlVulnerable = "SELECT * FROM enemigos WHERE nombre = '" + entradaMaliciosa + "'";
        System.out.println("SQL generado: " + sqlVulnerable);
        System.out.println("Esto devolvería TODOS los enemigos (¡ataque exitoso!)");

        try (Statement stmt = conexion.createStatement();
             ResultSet resultado = stmt.executeQuery(sqlVulnerable)) {

            int count = 0;
            while (resultado.next()) {
                String nombre= resultado.getString("nombre");
                String genero = resultado.getString("genero");
                String pais = resultado.getString("pais_origen");
                System.out.println("ENEMIGO "+nombre+" de "+pais);
                count++;
            }
            System.out.println("Registros devueltos: " + count);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // SEGURO: PreparedStatement
        System.out.println("\n✅ SEGURO - PreparedStatement:");
        String sqlSeguro = "SELECT * FROM enemigos WHERE nombre = ?";
        System.out.println("SQL: " + sqlSeguro);

        try (PreparedStatement pstmt = conexion.prepareStatement(sqlSeguro)) {
            pstmt.setString(1, entradaMaliciosa);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                System.out.println("Registros devueltos: " + count);
                System.out.println("El PreparedStatement escapa automáticamente los caracteres especiales");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println();
    }

}//Fin clase main
