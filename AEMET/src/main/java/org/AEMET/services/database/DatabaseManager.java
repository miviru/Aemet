package org.AEMET.services.database;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
public class DatabaseManager {
    private static DatabaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private boolean databaseInitTables = false; // Deberíamos inicializar las tablas? Fichero de configuración
    private String databaseUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"; // Fichero de configuración se lee en el constructor
    private String databaseInitScript = "init.sql"; // Fichero de configuración se lee en el constructor
    private Connection conn;

    private DatabaseManager() {


        loadProperties();

        try {
            openConnection();
            if (databaseInitTables) {
                initTables();
            }
        } catch (SQLException e) {
            logger.error("Error al abrir la conexión con la base de datos " + e.getMessage());
        }
    }
    /**
     * Método para obtener la instancia de la base de datos
     * Lo ideal e
     *
     * @return
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadProperties() {
        logger.debug("Cargando fichero de configuración de la base de datos");
        try {
            var file = ClassLoader.getSystemResource("database.properties").getFile();
            var props = new Properties();
            props.load(new FileReader(file));
            // Establecemos la url de la base de datos
            databaseUrl = props.getProperty("database.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            databaseInitTables = Boolean.parseBoolean(props.getProperty("database.initTables", "false"));
            databaseInitScript = props.getProperty("database.initScript", "init.sql");
        } catch (IOException e) {
            logger.error("Error al leer el fichero de configuración de la base de datos " + e.getMessage());
        }
    }
    private void openConnection() throws SQLException {
        logger.debug("Abriendo conexión con la base de datos en: " + databaseUrl);
        conn = DriverManager.getConnection(databaseUrl);
    }

    private void closeConnection() throws SQLException {
        logger.debug("Cerrando conexión con la base de datos");
        conn.close();
    }
    private void initTables() {
        try {
            executeScript(databaseInitScript, true);
        } catch (FileNotFoundException e) {
            logger.error("Error al leer el fichero de inicialización de la base de datos " + e.getMessage());
        }
    }
    public void executeScript(String scriptSqlFile, boolean logWriter) throws FileNotFoundException {
        ScriptRunner sr = new ScriptRunner(conn);
        var file = ClassLoader.getSystemResource(scriptSqlFile).getFile();
        logger.debug("Ejecutando script de SQL " + file);
        Reader reader = new BufferedReader(new FileReader(file));
        sr.setLogWriter(logWriter ? new PrintWriter(System.out) : null);
        sr.runScript(reader);
    }


    public Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                openConnection();
            } catch (SQLException e) {
                logger.error("Error al abrir la conexión con la base de datos " + e.getMessage());
                throw e;
            }
        }
        return conn;
    }


    public void close() throws Exception {
        closeConnection();
    }



}
