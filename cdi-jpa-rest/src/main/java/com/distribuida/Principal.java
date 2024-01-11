package com.distribuida;

import com.distribuida.db.Persona;
import com.distribuida.servicios.ServicioPersona;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.List;

import static spark.Spark.*;

public class Principal {
    static SeContainer container;

    static List<Persona> listarPersonas(Request req, Response res) {
        var servicio = container.select(ServicioPersona.class)
                .get();
        res.type("application/json");

        return servicio.findAll();
    }

    static Persona buscarPersona(Request req, Response res) {
        var servicio = container.select(ServicioPersona.class)
                .get();
        res.type("application/json");

        String _id = req.params(":id");

        var persona = servicio.findById(Integer.valueOf(_id));

        if (persona == null) {
            // 404
            halt(404, "Persona no encontrada");
        }

        return persona;
    }

    public static Object eliminarPersona(Request req, Response res) {
        res.type("application/json");

        // Obtener el ID de la persona de los parámetros de la solicitud
        String idStr = req.params(":id");
        Integer id = Integer.valueOf(idStr); // Convertir el ID a Integer

        var servicio = container.select(ServicioPersona.class).get();

        // Llamar al método del servicio para eliminar la persona
        boolean eliminado = servicio.borrar(id);

        if (eliminado) {
            // Si la persona fue eliminada exitosamente
            return new HashMap<String, String>() {{
                put("message", "Persona eliminada exitosamente");
            }};
        } else {
            // Si no se pudo eliminar la persona (por ejemplo, si la persona no existe)
            res.status(404); // Establecer el código de estado a 404 (Not Found)
            return new HashMap<String, String>() {{
                put("message", "Persona no encontrada o no pudo ser eliminada");
            }};
        }
    }

    static Object crearPersona(Request req, Response res) {
        res.type("application/json");

        // Convertir el cuerpo de la solicitud a un objeto Persona (asumiendo que estás enviando datos JSON en la solicitud)
        Gson gson = new Gson();
        Persona nuevaPersona = gson.fromJson(req.body(), Persona.class);

        var servicio = container.select(ServicioPersona.class).get();
        servicio.insert(nuevaPersona);

        // Retornar un mensaje de éxito o cualquier otro objeto que desees convertir a JSON
        return new HashMap<String, String>() {{
            put("message", "Persona creada exitosamente");
        }};
    }

    static void configureCors() {
        before((request, response) -> {
            // Configuración CORS
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
            response.header("Access-Control-Allow-Credentials", "true");
        });

        options("/*", (request, response) -> {
            response.status(200);
            return "OK";
        });
    }

    public static void main(String[] args) {
        container = SeContainerInitializer
                .newInstance()
                .initialize();

        ServicioPersona servicio = container.select(ServicioPersona.class)
                .get();

        port(8080);

        configureCors();

        Gson gson = new Gson();
        get("/personas", Principal::listarPersonas, gson::toJson);
        get("/personas/:id", Principal::buscarPersona, gson::toJson);
        post("/personas", Principal::crearPersona, gson::toJson);
        delete("/personas/:id", Principal::eliminarPersona, gson::toJson);
    }
}
