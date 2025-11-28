package Controlador;

import static Controlador.Encriptar.encriptarSHA256;
import Modelo.Administrador;
import Modelo.CajeroModelo;
import Modelo.Rol;
import Modelo.Usuario;
import com.mongodb.client.*;
import org.bson.Document;

public class MongoDB {

    private static MongoDatabase database;

    public static MongoDatabase conectar() {
        if (database == null) {
            MongoClient cliente = MongoClients.create("mongodb://localhost:27017");
            database = cliente.getDatabase("FarmaciaSaludTotal");
        }
        return database;
    }

    public static void guardarRol(Rol rol) {

        MongoCollection<Document> coleccion
                = MongoDB.conectar().getCollection("roles");

        Document doc = new Document()
                .append("id", rol.getIdRol())
                .append("nombre", rol.getNombre())
                .append("permisos", rol.getPermisos());

        coleccion.insertOne(doc);

        System.out.println("Rol guardado correctamente con usuario y contraseña.");
    }

    public static void guardarUsuarios(Usuario usuario) {

        MongoCollection<Document> coleccion
                = MongoDB.conectar().getCollection("usuarios");

        Document doc = new Document()
                .append("id", usuario.getIdUsuario())
                .append("usuario", usuario.getUsuario())
                .append("contrasena", encriptarSHA256(usuario.getContrasena()))
                .append("rol", usuario.getRol());

        coleccion.insertOne(doc);

        System.out.println("Rol guardado correctamente con usuario y contraseña.");
    }

    public static Usuario buscarUsuario(String usuario, String contrasena) {

        MongoCollection<Document> coleccion
                = MongoDB.conectar().getCollection("usuarios");

        // Encriptamos la contraseña ingresada
        String passwordHash = encriptarSHA256(contrasena);

        // IMPORTANTE: usar el NOMBRE REAL DEL CAMPO
        Document filtro = new Document()
                .append("usuario", usuario)
                .append("contrasena", passwordHash);

        Document resultado = coleccion.find(filtro).first();

        if (resultado == null) {
            return null;
        }

        String id = resultado.getString("id");
        String rol = resultado.getString("rol");

        switch (rol.toLowerCase()) {

            case "administrador":
                return new Administrador(id, usuario, contrasena, rol);

            case "cajero":
                return new CajeroModelo(id, usuario, contrasena, rol);

            default:
                return null;
        }
    }

    public static void inicializarDatos() {

        MongoCollection<Document> usuarios
                = MongoDB.conectar().getCollection("usuarios");

        // Si ya existe un admin, no volver a crearlo
        Document existeAdmin = usuarios.find(new Document("usuario", "admin")).first();
        if (existeAdmin != null) {
            System.out.println("Datos iniciales ya existen.");
            return;
        }

        // Crear rol administrador
        guardarRol(new Rol("1", "administrador", "todo"));

        // Crear rol cajero
        guardarRol(new Rol("2", "cajero", "ventas"));

        // Crear usuario admin por defecto
        Usuario admin = new Administrador("1", "admin", "1234", "administrador");
        guardarUsuarios(admin);

        // Crear usuario cajero por defecto
        Usuario cajero = new CajeroModelo("2", "cajero", "abcd", "cajero");
        guardarUsuarios(cajero);

        System.out.println("Datos iniciales creados correctamente.");
    }

}
